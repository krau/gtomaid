package app.unv.gtomaid.compat;

import app.unv.gtomaid.GTOMaid;
import app.unv.gtomaid.config.GTOMaidConfig;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GTOMaid.MOD_ID)
public final class MaidMagicImmune {

    private static volatile Set<ResourceKey<DamageType>> cachedTypes;
    private static volatile List<? extends String> cachedSource;

    private MaidMagicImmune() {
    }

    @SuppressWarnings({ "null" })
    private static Set<ResourceKey<DamageType>> getMagicDamageTypes() {
        List<? extends String> source = GTOMaidConfig.MAGIC_DAMAGE_TYPES.get();
        Set<ResourceKey<DamageType>> cached = cachedTypes;
        if (cached != null && source == cachedSource) {
            return cached;
        }
        Set<ResourceKey<DamageType>> set = new HashSet<>();
        for (String id : source) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl != null) {
                set.add(ResourceKey.create(Registries.DAMAGE_TYPE, rl));
            }
        }
        cachedTypes = set;
        cachedSource = source;
        return set;
    }

    private static Boolean loaded;

    private static boolean isLoaded() {
        if (loaded == null) {
            try {
                Class.forName("vazkii.botania.api.mana.ManaItem");
                Class.forName("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid");
                loaded = true;
            } catch (ClassNotFoundException e) {
                loaded = false;
            }
        }
        return loaded;
    }

    private static boolean isImmuneMaid(LivingEntity entity) {
        if (!GTOMaidConfig.ENABLE_MAID_MAGIC_IMMUNE.get()) {
            return false;
        }
        if (!isLoaded()) {
            return false;
        }
        return entity instanceof EntityMaid maid && BotaniaCompat.entityHasManaContainer(maid);
    }

    private static boolean isPlayerDamage(DamageSource source) {
        return source.getEntity() instanceof net.minecraft.world.entity.player.Player;
    }

    private static boolean isMagicDamage(DamageSource source) {
        for (ResourceKey<DamageType> key : getMagicDamageTypes()) {
            if (key != null && source.is(key)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!isImmuneMaid(event.getEntity())) {
            return;
        }
        DamageSource source = event.getSource();
        if (isPlayerDamage(source) || isMagicDamage(source)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!isImmuneMaid(event.getEntity())) {
            return;
        }
        if (isPlayerDamage(event.getSource()) || isMagicDamage(event.getSource())) {
            event.setAmount(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!isImmuneMaid(event.getEntity())) {
            return;
        }
        if (isPlayerDamage(event.getSource()) || isMagicDamage(event.getSource())) {
            event.setAmount(0);
        }
    }
}
