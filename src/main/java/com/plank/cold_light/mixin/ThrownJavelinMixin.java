package com.plank.cold_light.mixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.dries007.tfc.common.entities.misc.ThrownJavelin;

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
        int i = this.cold_light$loyaltyLevel;
        if (i > 0 && (this.cold_light$dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!this.cold_light$isAcceptibleReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = entity.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * (double)i, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }
                double d0 = 0.05D * (double)i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3.normalize().scale(d0)));
                if (this.cold_light$clientSideReturnTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                ++this.cold_light$clientSideReturnTickCount;
            }
        }
        super.tick();
    }
    @Unique
    private boolean cold_light$isAcceptibleReturnOwner() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        } else {
            return false;
        }
    }
    @Inject(method = "m_5790_", at = @At("HEAD"), remap = false)
    private void handleChannelingEnchantment(EntityHitResult result, CallbackInfo ci) {
        if (!this.level().isClientSide && this.cold_light$hasChanneling) {
            Entity target = result.getEntity();
            BlockPos pos = target.blockPosition();

            if (this.level().canSeeSky(pos) && this.level().isThundering()) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
                if (lightning != null) {
                    lightning.moveTo(Vec3.atBottomCenterOf(pos));

                    if (this.getOwner() instanceof ServerPlayer player) {
                        lightning.setCause(player);
                    }

                    this.level().addFreshEntity(lightning);
                }
            }
        }
        this.cold_light$dealtDamage = true;
    }

    @Inject(method = "m_7378_", at = @At("TAIL"), remap = false)
    private void loadEnchantmentData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("TFCJavelinLoyalty", CompoundTag.TAG_BYTE)) {
            this.cold_light$loyaltyLevel = tag.getByte("TFCJavelinLoyalty");
        }
        if (tag.contains("TFCJavelinChanneling", CompoundTag.TAG_BYTE)) {
            this.cold_light$hasChanneling = tag.getBoolean("TFCJavelinChanneling");
        }
        if (tag.contains("TFCJavelinDealtDamage", CompoundTag.TAG_BYTE)) {
            this.cold_light$dealtDamage = tag.getBoolean("TFCJavelinDealtDamage");
        }
    }

    @Inject(method = "m_7380_", at = @At("TAIL"), remap = false)
    private void saveEnchantmentData(CompoundTag tag, CallbackInfo ci) {
        tag.putByte("TFCJavelinLoyalty", this.cold_light$loyaltyLevel);
        tag.putBoolean("TFCJavelinChanneling", this.cold_light$hasChanneling);
        tag.putBoolean("TFCJavelinDealtDamage", this.cold_light$dealtDamage);
    }
}