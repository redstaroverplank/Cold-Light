package com.plank.cold_light.mixin;

import com.plank.cold_light.Sound;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrownJavelin.class)
public abstract class ThrownJavelinMixin extends AbstractArrow {
    @Unique
    private byte cold_light$loyaltyLevel;
    @Unique
    private boolean cold_light$hasChanneling;
    @Unique
    private boolean cold_light$dealtDamage;
    @Unique
    private int cold_light$clientSideReturnTickCount;
    public ThrownJavelinMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"), remap = false)
    private void initEnchantments(Level level, LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        this.cold_light$loyaltyLevel = (byte) EnchantmentHelper.getLoyalty(stack);
        this.cold_light$hasChanneling = EnchantmentHelper.hasChanneling(stack);
        this.cold_light$dealtDamage = false;
    }
    @Inject(method = "m_8119_", at = @At("HEAD"), remap = false)
    private void handleLoyaltyEnchantment(CallbackInfo ci) {
        if (this.inGroundTime > 4) {
            this.cold_light$dealtDamage = true;
        }
        Entity entity = this.getOwner();
        int level = this.cold_light$loyaltyLevel;
        if (level > 0 && (this.cold_light$dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!this.cold_light$canReturnToOwner()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = entity.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * level, this.getZ());
                if (this.level().isClientSide) this.yOld = this.getY();
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D)
                        .add(vec3.normalize().scale(0.05D * level)));
                if (this.cold_light$clientSideReturnTickCount == 0) {
                    this.playSound(Sound.JAVELIN_RETURN.get(), 10.0F, 1.0F);
                }
                ++this.cold_light$clientSideReturnTickCount;
            }
        }
        super.tick();
    }
    @Unique
    private boolean cold_light$canReturnToOwner() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        else return false;
    }
    @Inject(method = "m_5790_", at = @At("HEAD"), remap = false)
    private void handleChannelingEnchantment(EntityHitResult result, CallbackInfo ci) {
        if (this.level() instanceof ServerLevel && this.level().isThundering() && cold_light$hasChanneling) {
            Entity target = result.getEntity();
            BlockPos blockpos = target.blockPosition();
            if (this.level().canSeeSky(blockpos)) {
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(this.level());
                if (lightningbolt != null) {
                    lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
                    lightningbolt.setCause(this.getOwner() instanceof ServerPlayer ? (ServerPlayer) this.getOwner() : null);
                    this.level().addFreshEntity(lightningbolt);
                    this.playSound(Sound.JAVELIN_THUNDER.get(), 5.0f, 1.0F);
                }
            }
        }
        this.cold_light$dealtDamage = true;

    }
    @Inject(method = "m_7378_", at = @At("TAIL"), remap = false)
    private void loadEnchantmentData(@NotNull CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("loyalty", CompoundTag.TAG_BYTE)) {
            this.cold_light$loyaltyLevel = tag.getByte("loyalty");
        }
        if (tag.contains("channeling", CompoundTag.TAG_BYTE)) {
            this.cold_light$hasChanneling = tag.getBoolean("channeling");
        }
        if (tag.contains("dealtDamage", CompoundTag.TAG_BYTE)) {
            this.cold_light$dealtDamage = tag.getBoolean("dealtDamage");
        }
    }
    @Inject(method = "m_7380_", at = @At("TAIL"), remap = false)
    private void saveEnchantmentData(@NotNull CompoundTag tag, CallbackInfo ci) {
        tag.putByte("loyalty", this.cold_light$loyaltyLevel);
        tag.putBoolean("channeling", this.cold_light$hasChanneling);
        tag.putBoolean("dealtDamage", this.cold_light$dealtDamage);
    }
}