package app.unv.gtomaid.mixin;

import app.unv.gtomaid.config.MixinConfig;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class GTOMaidMixinPlugin implements IMixinConfigPlugin {
    private static boolean botaniaLoaded;
    private static boolean extrabotanyLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        MixinConfig.load();
        botaniaLoaded = net.minecraftforge.fml.loading.FMLLoader.getLoadingModList().getModFileById("botania") != null;
        extrabotanyLoaded = net.minecraftforge.fml.loading.FMLLoader.getLoadingModList()
                .getModFileById("extrabotany") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".ToolCommonsMixin")) {
            return botaniaLoaded && MixinConfig.enableBotaniaToolDurability;
        }
        if (mixinClassName.endsWith(".GaiaGuardianMixin")) {
            return botaniaLoaded && MixinConfig.enableBotaniaGaiaAttack;
        }
        if (mixinClassName.endsWith(".ExtrabotanyGaiaMixin")) {
            return extrabotanyLoaded && MixinConfig.enableExtrabotanyGaiaAttack;
        }

        if (mixinClassName.startsWith("app.unv.gtomaid.mixin.botania.")) {
            return botaniaLoaded;
        }
        if (mixinClassName.startsWith("app.unv.gtomaid.mixin.extrabotany.")) {
            return extrabotanyLoaded;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
