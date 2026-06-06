package app.unv.gtomaid.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Early mixin toggles, loaded before Forge ModConfig is ready. */
public final class MixinConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("gtomaid/mixin-config");
    private static final Path CONFIG_PATH = Paths.get("config", "gtomaid-mixin.properties");

    private static final String K_BOTANIA_TOOL = "enableBotaniaToolDurability";
    private static final String K_BOTANIA_GAIA = "enableBotaniaGaiaAttack";
    private static final String K_EXTRABOTANY_GAIA = "enableExtrabotanyGaiaAttack";

    public static boolean enableBotaniaToolDurability = true;
    public static boolean enableBotaniaGaiaAttack = true;
    public static boolean enableExtrabotanyGaiaAttack = true;

    private static boolean loaded;

    private MixinConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                props.load(in);
            } catch (IOException e) {
                LOGGER.error("Failed to read {}, using defaults", CONFIG_PATH, e);
            }
            enableBotaniaToolDurability = parseBool(props, K_BOTANIA_TOOL, true);
            enableBotaniaGaiaAttack = parseBool(props, K_BOTANIA_GAIA, true);
            enableExtrabotanyGaiaAttack = parseBool(props, K_EXTRABOTANY_GAIA, true);
        } else {
            writeDefault();
        }
    }

    private static boolean parseBool(Properties props, String key, boolean defaultValue) {
        String v = props.getProperty(key);
        if (v == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(v.trim());
    }

    private static void writeDefault() {
        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                String content = "# GTOMaid mixin toggles (all enabled by default)\n"
                        + "# Loaded before Forge config; restart required after editing.\n"
                        + "\n"
                        + K_BOTANIA_TOOL + "=true\n"
                        + K_BOTANIA_GAIA + "=true\n"
                        + K_EXTRABOTANY_GAIA + "=true\n";
                out.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write default config {}", CONFIG_PATH, e);
        }
    }
}
