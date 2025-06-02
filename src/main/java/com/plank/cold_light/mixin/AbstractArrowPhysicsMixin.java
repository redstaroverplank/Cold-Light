package com.plank.cold_light.mixin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowPhysicsMixin {

    @Inject(method = "getWaterInertia", at = @At("HEAD"), cancellable = true)
    private void getWaterInertia(CallbackInfoReturnable<Float> cir) {
        // 如果是标枪实体，保持 TFC 的水下惯性
        if ((Object)this instanceof net.dries007.tfc.common.entities.misc.ThrownJavelin) {
            cir.setReturnValue(0.99F);
        }
    }
}