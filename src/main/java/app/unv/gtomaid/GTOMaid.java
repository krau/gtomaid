package app.unv.gtomaid;

import app.unv.gtomaid.config.GTOMaidConfig;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(GTOMaid.MOD_ID)
public final class GTOMaid {
    public static final String MOD_ID = "gtomaid";

    @SuppressWarnings("removal")
    public GTOMaid() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GTOMaidConfig.SPEC, MOD_ID + ".toml");
    }
}
