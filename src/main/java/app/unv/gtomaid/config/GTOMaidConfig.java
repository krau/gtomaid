package app.unv.gtomaid.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class GTOMaidConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_TIME_TWIST;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MAID_MAGIC_IMMUNE;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MAGIC_DAMAGE_TYPES;

    public static final ForgeConfigSpec.IntValue OWNER_DISTANCE;

    public static final ForgeConfigSpec.IntValue SCAN_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue MOVE_SPEED;
    public static final ForgeConfigSpec.IntValue VERTICAL_SEARCH_RANGE;
    public static final ForgeConfigSpec.DoubleValue WANDER_SPEED;
    public static final ForgeConfigSpec.IntValue WANDER_RADIUS;
    public static final ForgeConfigSpec.IntValue WANDER_RETRY_INTERVAL;
    public static final ForgeConfigSpec.IntValue ASSIGNMENT_TIMEOUT_TICKS;
    public static final ForgeConfigSpec.IntValue STAND_RADIUS;
    public static final ForgeConfigSpec.IntValue STAND_VERTICAL_RADIUS;

    public static final ForgeConfigSpec.DoubleValue ARRIVE_HORIZONTAL;
    public static final ForgeConfigSpec.DoubleValue ARRIVE_VERTICAL;
    public static final ForgeConfigSpec.IntValue MAX_TICKS_AT_STAND;
    public static final ForgeConfigSpec.IntValue SWING_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue MAX_CLICKS_PER_MACHINE;

    private GTOMaidConfig() {
    }

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("features");
        ENABLE_TIME_TWIST = b
                .comment("Enable Time Twist maid task")
                .define("enableTimeTwist", true);
        ENABLE_MAID_MAGIC_IMMUNE = b
                .comment("Maids holding a mana item are immune to magic/player damage")
                .define("enableMaidMagicImmune", true);
        b.pop();

        b.push("magicImmune");
        MAGIC_DAMAGE_TYPES = b
                .comment("Damage type IDs treated as magic damage")
                .defineListAllowEmpty("magicDamageTypes",
                        List.of(
                                "minecraft:magic",
                                "botania:key_explosion",
                                "extrabotany:excalibur"),
                        o -> o instanceof String s && s.contains(":"));
        b.pop();

        b.push("timeTwist");
        OWNER_DISTANCE = b
                .comment("Max distance from a machine to the owner (non-home mode)")
                .defineInRange("ownerDistance", 8, 1, 256);

        b.push("move");
        SCAN_INTERVAL = b
                .comment("Scan interval in ticks")
                .defineInRange("scanInterval", 60, 1, 1200);
        MOVE_SPEED = b
                .comment("Movement speed multiplier")
                .defineInRange("moveSpeed", 0.6D, 0.1D, 2.0D);
        VERTICAL_SEARCH_RANGE = b
                .comment("Vertical search range in blocks")
                .defineInRange("verticalSearchRange", 3, 0, 32);
        WANDER_SPEED = b
                .comment("Wander speed when no target is found")
                .defineInRange("wanderSpeed", 0.3D, 0.0D, 2.0D);
        WANDER_RADIUS = b
                .comment("Wander radius")
                .defineInRange("wanderRadius", 5, 0, 32);
        WANDER_RETRY_INTERVAL = b
                .comment("Wander retry cooldown in ticks")
                .defineInRange("wanderRetryInterval", 120, 1, 6000);
        ASSIGNMENT_TIMEOUT_TICKS = b
                .comment("Ticks before giving up on an assigned machine")
                .defineInRange("assignmentTimeoutTicks", 200, 20, 12000);
        STAND_RADIUS = b
                .comment("Horizontal radius for searching a stand position around the controller")
                .defineInRange("standRadius", 4, 1, 16);
        STAND_VERTICAL_RADIUS = b
                .comment("Vertical radius for searching a stand position around the controller")
                .defineInRange("standVerticalRadius", 2, 0, 8);
        b.pop();

        b.push("action");
        ARRIVE_HORIZONTAL = b
                .comment("Horizontal distance threshold for arrival")
                .defineInRange("arriveHorizontal", 1.5D, 0.1D, 8.0D);
        ARRIVE_VERTICAL = b
                .comment("Vertical distance threshold for arrival")
                .defineInRange("arriveVertical", 1.5D, 0.1D, 8.0D);
        MAX_TICKS_AT_STAND = b
                .comment("Max ticks spent at the stand position")
                .defineInRange("maxTicksAtStand", 80, 1, 6000);
        SWING_COOLDOWN_TICKS = b
                .comment("Cooldown ticks between right-clicks")
                .defineInRange("swingCooldownTicks", 10, 1, 200);
        MAX_CLICKS_PER_MACHINE = b
                .comment("Max right-clicks per machine")
                .defineInRange("maxClicksPerMachine", 3, 1, 64);
        b.pop();
        b.pop();

        SPEC = b.build();
    }
}
