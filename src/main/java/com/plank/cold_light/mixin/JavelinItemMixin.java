package com.plank.cold_light.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.dries007.tfc.common.items.JavelinItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JavelinItem.class)
public abstract class JavelinItemMixin {

    @Unique
    private static final int RIPTIDE_USE_DURATION = 10;

    @Inject(method = "m_5551_", at = @At("HEAD"), cancellable = true, remap = false)
    private void handleRiptideEnchantment(ItemStack stack, Level level, LivingEntity entity, int ticksLeft, CallbackInfo ci) {
        if (entity instanceof Player player) {
            int useDuration = ((JavelinItem)(Object)this).getUseDuration(stack);
            int timeUsed = useDuration - ticksLeft;

            if (EnchantmentHelper.getRiptide(stack) !=0 &&(timeUsed >= RIPTIDE_USE_DURATION)) {
                int riptideLevel = EnchantmentHelper.getRiptide(stack);

                if (entity.isInWaterOrRain() || entity.isInFluidType()) {
                    // 激流冲刺逻辑
                    float yRot = player.getYRot();
                    float xRot = player.getXRot();
                    float xComp = -Mth.sin(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
                    float yComp = -Mth.sin(xRot * ((float)Math.PI / 180F));
                    float zComp = Mth.cos(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));

                    float magnitude = Mth.sqrt(xComp * xComp + yComp * yComp + zComp * zComp);
                    float power = 3.0F * ((1.0F + (float)riptideLevel) / 4.0F);

                    xComp *= power / magnitude;
                    yComp *= power / magnitude;
                    zComp *= power / magnitude;

                    player.push(xComp, yComp, zComp);
                    player.startAutoSpinAttack(20);

                    if (player.onGround()) {
                        player.move(MoverType.SELF, new Vec3(0.0D, 1.2D, 0.0D));
                    }

                    // 播放激流音效
                    SoundEvent soundEvent;
                    if (riptideLevel >= 3) {
                        soundEvent = SoundEvents.TRIDENT_RIPTIDE_3;
                    } else if (riptideLevel == 2) {
                        soundEvent = SoundEvents.TRIDENT_RIPTIDE_2;
                    } else {
                        soundEvent = SoundEvents.TRIDENT_RIPTIDE_1;
                    }

                    level.playSound(null, player, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // 消耗耐久
                    if (!player.getAbilities().instabuild) {
                        stack.hurtAndBreak(riptideLevel, player,
                                p -> p.broadcastBreakEvent(entity.getUsedItemHand()));
                    }

                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    ci.cancel(); // 取消原版投掷逻辑
                }
            }
        }
    }

    @Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true, remap = false)
    private void handleRiptideUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查激流附魔使用条件
        if(EnchantmentHelper.getRiptide(stack)!=0){
            int riptideLevel = EnchantmentHelper.getRiptide(stack);
            if (!player.isInWaterOrRain() && !player.isInFluidType()) {
                cir.setReturnValue(InteractionResultHolder.fail(stack));
            }
        }
    }
}