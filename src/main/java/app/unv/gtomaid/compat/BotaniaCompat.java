package app.unv.gtomaid.compat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.mana.ManaItem;

import java.util.ArrayList;
import java.util.List;

public final class BotaniaCompat {
    private BotaniaCompat() {
    }

    private static Boolean tlmLoaded;

    private static boolean isTlmLoaded() {
        if (tlmLoaded == null) {
            try {
                Class.forName("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid");
                tlmLoaded = true;
            } catch (ClassNotFoundException e) {
                tlmLoaded = false;
            }
        }
        return tlmLoaded;
    }

    public static List<IItemHandler> getAllItemHandlers(LivingEntity entity) {
        List<IItemHandler> handlers = new ArrayList<>();

        var capHandler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
        if (capHandler != null) {
            handlers.add(capHandler);
        }

        try {
            var curiosHelper = CuriosApi.getCuriosInventory(entity).resolve().orElse(null);
            if (curiosHelper != null) {
                handlers.add(curiosHelper.getEquippedCurios());
            }
        } catch (NoClassDefFoundError ignored) {
        }

        if (isTlmLoaded()
                && entity instanceof com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid maid) {
            handlers.add(maid.getMaidInv());
            handlers.add(maid.getMaidBauble());
        }

        return handlers;
    }

    public static boolean entityHasManaContainer(LivingEntity entity) {
        for (IItemHandler handler : getAllItemHandlers(entity)) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty() && findManaItem(stack) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean requestManaExactForEntity(ItemStack stack, LivingEntity entity, int manaToGet,
            boolean remove) {
        if (manaToGet <= 0)
            return true;
        if (entity instanceof Player)
            return false;
        return entityHasManaContainer(entity);
    }

    @Nullable
    public static ManaItem findManaItem(ItemStack stack) {
        try {
            return stack.getCapability(vazkii.botania.api.BotaniaForgeCapabilities.MANA_ITEM).resolve().orElse(null);
        } catch (NoClassDefFoundError ignored) {
            return null;
        }
    }
}
