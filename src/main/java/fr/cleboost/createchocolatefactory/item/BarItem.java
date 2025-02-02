package fr.cleboost.createchocolatefactory.item;

import fr.cleboost.createchocolatefactory.CreateChocolateFactory;
import fr.cleboost.createchocolatefactory.utils.Chocolate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BarItem extends Item {
    public BarItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pLivingEntity) {
        if (!(pLivingEntity instanceof Player))
            return pStack;
        int eatProgress = getEatProgress(pStack);
        Player player = (Player) (pLivingEntity);
        Chocolate ch = new Chocolate(pStack.getTag());
        //EAT :
        player.getFoodData().eat(ch.getNutrition(), ch.getSaturationModifier());
        player.awardStat(Stats.ITEM_USED.get(pStack.getItem()));
        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
        player.gameEvent(GameEvent.EAT);
        if (eatProgress++ > 1) {
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, pStack);
            }
            if (player.isCreative()) {
                setEatProgress(pStack, 0);
            } else {
                pStack.shrink(1);
            }
        } else {
            setEatProgress(pStack, eatProgress);
            ch.addIngredients("milk", 10);
            ch.saveTag(pStack);
        }
        return pStack;
    }

    public static int getEatProgress(@NotNull ItemStack pStack) {
        if (pStack.hasTag()) {
            CompoundTag tag = pStack.getTag();
            if (tag != null && tag.contains(CreateChocolateFactory.MOD_ID)) {
                if (!tag.getCompound(CreateChocolateFactory.MOD_ID).contains("eatProgress")) return 0;
                return tag.getCompound(CreateChocolateFactory.MOD_ID).getInt("eatProgress");
            }
        }
        return 0;
    }

    private static void setEatProgress(@NotNull ItemStack bar, int eatProgress) {
        CompoundTag tag = bar.getOrCreateTag();
        if (tag.contains(CreateChocolateFactory.MOD_ID)) {
            tag.getCompound(CreateChocolateFactory.MOD_ID).putInt("eatProgress", eatProgress);
            return;
        }
        var progressTag = new CompoundTag();
        progressTag.putInt("eatProgress", eatProgress);
        tag.put(CreateChocolateFactory.MOD_ID, progressTag);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        int eatProgress = getEatProgress(pStack);
        //Chocolate ch = new Chocolate(pStack.getTag());
        pTooltipComponents.add(Component.translatable("tooltip.createchocolatefactory.bar" + eatProgress));
        /*pTooltipComponents.add(
                Component.translatable("tooltip.createchocolatefactory.strength",
                Component.literal(ch.getStreght() + "%"),
                Component.literal(ch.getMilk() + "%"),
                Component.literal(ch.getSugar() + "%"),
                Component.literal(ch.getCocoaButter() + "%")
        ));*/
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide()) return;
        if (Chocolate.hasChocolateProperties(pStack.getTag())) return;
        Chocolate ch;
        if (((Player) (pEntity)).isCreative()) ch = new Chocolate(true);
        else ch = new Chocolate(pStack.getTag());
        ch.saveTag(pStack);
    }
}
