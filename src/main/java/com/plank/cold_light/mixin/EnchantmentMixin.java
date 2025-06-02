package com.plank.cold_light.mixin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.dries007.tfc.common.items.JavelinItem;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void allowJavelinEnchantments(ItemStack p_44689_, CallbackInfoReturnable<Boolean> cir) {
        Item item = p_44689_.getItem();
        if (item instanceof JavelinItem) {
            Enchantment self = (Enchantment)(Object)this;

            // 允许三叉戟附魔应用于标枪
            if (self.category == EnchantmentCategory.TRIDENT) {
                cir.setReturnValue(true);
            }
        }
    }
}