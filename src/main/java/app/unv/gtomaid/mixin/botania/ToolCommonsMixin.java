package app.unv.gtomaid.mixin.botania;

import app.unv.gtomaid.compat.BotaniaCompat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.item.equipment.tool.ToolCommons;

@Mixin(ToolCommons.class)
public abstract class ToolCommonsMixin {

    @Inject(method = "damageItemIfPossible",
            at = @At(value = "RETURN", ordinal = 0),
            cancellable = true,
            remap = false)
    @SuppressWarnings("null")
    private static void onDamageItemReturn(ItemStack stack, int amount, LivingEntity entity, int manaPerDamage,
            CallbackInfoReturnable<Integer> cir) {
        if (amount > 0 && !(entity instanceof Player)) {
            int unbreaking = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, stack);
            while (amount > 0) {
                if (BotaniaCompat.requestManaExactForEntity(stack, entity, manaPerDamage, false)) {
                    if (entity.level().getRandom().nextInt(unbreaking + 1) == 0) {
                        BotaniaCompat.requestManaExactForEntity(stack, entity, manaPerDamage, true);
                    }
                    amount--;
                } else {
                    break;
                }
            }
            cir.setReturnValue(amount);
        }
    }
}
