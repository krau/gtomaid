package app.unv.gtomaid.mixin.extrabotany;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;

@Mixin(value = Gaia.class, remap = false)
public abstract class ExtrabotanyGaiaMixin {

    @Inject(method = "m_6469_", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtomaid$allowMaidAttack(DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof EntityMaid maid) {
            LivingEntity owner = maid.getOwner();
            if (owner instanceof Player player) {
                DamageSource proxySource = player.damageSources().playerAttack(player);
                if (proxySource != null) {
                    cir.setReturnValue(((LivingEntity) (Object) this).hurt(proxySource, amount));
                }
            }
        }
    }
}
