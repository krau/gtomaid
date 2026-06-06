# GTOMaid - 为 GregTech-Odyssey 提供的 Touhou Little Maid 增强 Mod

## 项目简介

GregTech-Odyssey (GTO) 是一个以格雷科技为核心的 Minecraft 整合包，包含了大量的模组和内容。Touhou Little Maid (女仆) 是一个以东方 Project 为主题的模组，提供了可爱的女仆角色和相关的功能。

本项目 (GTOMaid) 旨在增强女仆在 GTO 中的表现和功能，当前核心功能包括：

- **时间扭曲任务 (Time Twist)**：让女仆使用 GTOCore 的「时间扭曲者」物品，自动寻路至格雷科技机器并加速其运转
- **魔法免疫**：持有魔力物品的女仆免疫魔法伤害（Botania / ExtraBotany 伤害类型）
- **Botania 兼容**：让女仆能正确使用魔力物品（Mana Item）修复工具耐久
- **盖亚守护者交互**：允许女仆代替主人攻击盖亚守护者（Botania & ExtraBotany）

## 技术栈

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Java | 21 |
| Mixin | 0.8.5 (SpongePowered) |
| GregTech CEu | 1.8.0+ |
| GTOCore | (本地 JAR) |
| Touhou Little Maid | 1.4.0+ |
| Botania | 1.20.1-443+ (可选) |
| ExtraBotany | 1.9+ (可选) |
| Curios | 5.7.2+ |

## 项目结构

```
gtomaid/
├── build.gradle                  # Gradle 构建脚本
├── gradle.properties             # 版本号、依赖版本等属性
├── settings.gradle               # Gradle 设置
├── libs/                         # 编译期依赖的本地 JAR
├── pkg/                          # 第三方模组源码参考（不提交到 Git）
│   ├── Botania/
│   ├── Extrabotany/
│   ├── GTOCore/
│   └── TouhouLittleMaid/
└── src/
    └── main/
        ├── java/app/unv/gtomaid/
        │   ├── GTOMaid.java               # Mod 主类，@Mod 入口
        │   ├── compat/                     # 模组兼容层
        │   │   ├── BotaniaCompat.java      # Botania API 桥接（魔力物品查询、耐久修复）
        │   │   └── MaidMagicImmune.java    # 女仆魔法免疫事件处理器
        │   ├── config/                     # 配置
        │   │   ├── GTOMaidConfig.java      # Forge TOML 配置：功能开关 + TimeTwist 参数 + 伤害类型
        │   │   └── MixinConfig.java        # Mixin 早期开关 (properties)，在 ModConfig 之前加载
        │   ├── mixin/                      # Mixin 注入
        │   │   ├── GTOMaidMixinPlugin.java # Mixin 插件，按模组加载状态条件加载
        │   │   ├── botania/                # Botania 相关 Mixin
        │   │   │   ├── ToolCommonsMixin.java       # 非玩家实体魔力耐久修复
        │   │   │   └── GaiaGuardianMixin.java      # 允许女仆攻击盖亚守护者
        │   │   └── extrabotany/            # ExtraBotany 相关 Mixin
        │   │       └── ExtrabotanyGaiaMixin.java   # 允许女仆攻击 ExtraBotany 盖亚
        │   └── twist/                      # 时间扭曲任务（核心功能）
        │       ├── GTTimeTwistLogic.java   # 时间扭曲核心逻辑（物品/机器识别）
        │       ├── GTOMaidExtension.java   # TLM API 扩展注册点
        │       ├── TaskTimeTwist.java      # IMaidTask 实现，注册行为任务
        │       ├── TwistScheduleState.java # 寻路调度状态（去重与跳过）
        │       └── task/                   # 行为任务实现
        │           ├── MaidTimeTwistMoveTask.java   # 寻路到可加速机器
        │           └── MaidTimeTwistActionTask.java # 对机器使用时间扭曲者
        └── resources/
            ├── META-INF/mods.toml          # Forge Mod 描述文件
            ├── gtomaid.mixins.json         # Mixin 配置
            ├── pack.mcmeta                 # 资源包元数据
            └── assets/gtomaid/
                └── lang/                   # 国际化语言文件
                    ├── en_us.json
                    └── zh_cn.json
```

## 模块说明

### `twist/` - 时间扭曲任务

女仆的 AI 行为任务，让女仆自动寻找格雷科技机器并使用「时间扭曲者」加速它们。

- **TaskTimeTwist**: 实现 `IMaidTask` 接口，注册为女仆可用任务，图标为时间扭曲者物品
- **GTTimeTwistLogic**: 核心逻辑 — 识别时间扭曲者物品、判断机器是否可加速（`RecipeLogic` 正在工作且 `inputEUt > 0` (跳过发电机))
- **MaidTimeTwistMoveTask**: 继承 `MaidMoveToBlockTask`，负责寻路到附近可加速的 GT 机器
- **MaidTimeTwistActionTask**: 继承 `Behavior<EntityMaid>`，到达机器后模拟玩家右键使用时间扭曲者
- **TwistScheduleState**: 跟踪已访问的机器位置，避免女仆重复访问同一机器

### `compat/` - 模组兼容

- **BotaniaCompat**: 桥接 Botania API，查询实体的魔力物品、请求魔力消耗。兼容 Curios 饰品栏和女仆背包
- **MaidMagicImmune**: Forge 事件处理器，当女仆持有魔力容器时免疫魔法和玩家伤害

### `mixin/` - Mixin 注入

- **GTOMaidMixinPlugin**: 条件加载 Mixin，按 `MixinConfig` 开关 + 目标模组存在与否决定是否应用
- **botania/**: 修复 Botania 工具对非玩家实体的兼容问题，允许女仆参与盖亚战
- **extrabotany/**: 允许女仆攻击 ExtraBotany 的盖亚 Boss

### `config/` - 配置

- **GTOMaidConfig**: Forge TOML 配置（`config/gtomaid.toml`），含功能开关 (`features`)、魔法伤害类型列表 (`magicImmune`)、时间扭曲参数 (`timeTwist.move` / `timeTwist.action`)，运行时可热更新
- **MixinConfig**: 独立 properties 文件 (`config/gtomaid-mixin.properties`)，承载 Mixin 类的开关。Mixin 在 Forge ModConfig 就绪前加载，故不能复用 TOML；改动需重启游戏

### 命名约定

- **包名**: `app.unv.gtomaid.<模块>`，模块按功能划分 (`compat`, `config`, `mixin`, `twist`)
- **类名**: PascalCase，功能描述性命名（如 `MaidTimeTwistActionTask`）
- **方法名**: camelCase
- **常量**: `UPPER_SNAKE_CASE`，声明为 `private static final`
- **Mixin 方法**: 使用 `gtomaid$` 前缀避免命名冲突（如 `gtomaid$allowMaidAttack`）
- **资源路径**: 使用 `gtomaid` 作为命名空间（如 `task.gtomaid.time_twist`）

### 编码风格

- 工具类/无状态类使用 `final` 修饰 + 私有构造函数，禁止实例化
- 优先使用不可变集合（`ImmutableMap`, `Set.of()`）
- 模组间兼容检查采用懒加载 `Boolean` + `Class.forName()` 模式，避免类加载时崩溃
- Mixin 注入使用 `remap = false` 标注非官方映射方法
- 事件处理器使用 `@Mod.EventBusSubscriber` 自动注册，优先级设为 `HIGH`
- 条件分支中早期返回（guard clause），减少嵌套

### Mixin 规范

- 所有 Mixin 类放在 `app.unv.gtomaid.mixin.<模组名>` 包下，按目标模组分组
- 必须在 `gtomaid.mixins.json` 中注册
- `GTOMaidMixinPlugin` 负责按模组加载状态动态决定是否应用
- 新增模组相关 Mixin 时，需在 `GTOMaidMixinPlugin.shouldApplyMixin()` 中添加对应前缀判断

### 兼容层规范

- 所有对可选依赖模组的 API 调用必须通过 `compat/` 包中的桥接类
- 桥接类内部需处理 `NoClassDefFoundError` / `ClassNotFoundException`
- 不在 `compat/` 之外的代码中直接引用可选依赖的类

### 资源文件

- 语言文件必须同时提供 `en_us.json` 和 `zh_cn.json`
- 翻译键格式：`<类型>.gtomaid.<名称>`（如 `task.gtomaid.time_twist`）
- `mods.toml` 中的可选依赖标记为 `mandatory=false`

## 开发约定

### 构建与运行

```bash
# 编译
./gradlew build
```

### 依赖管理

- 核心依赖（`touhou_little_maid`, `gtocore`, `gtceu`）放在 `libs/` 下以 `compileOnly files(...)` 引入
- 可选依赖（`botania`, `extrabotany`）同样以 `compileOnly` 引入，运行时由整合包提供
- Maven 仓库依赖（如 Curios）通过标准坐标引入

### 新增功能指引

1. **新增女仆任务**: 在 `twist/` 下创建 `IMaidTask` 实现，在 `GTOMaidExtension.addMaidTask()` 中注册，包含 Move + Action 行为任务
2. **新增模组兼容**: 在 `compat/` 下创建桥接类，处理类加载安全；如需 Mixin 则在 `mixin/<模组名>/` 下添加，并更新 MixinPlugin 和 mixins.json
3. **新增事件处理**: 创建类并添加 `@Mod.EventBusSubscriber(modid = GTOMaid.MOD_ID)` 注解
4. **新增语言键**: 在 `en_us.json` 和 `zh_cn.json` 中同步添加
5. **新增配置项**: 事件类/任务参数加到 `GTOMaidConfig`；Mixin 开关加到 `MixinConfig` 并在 `GTOMaidMixinPlugin.shouldApplyMixin()` 中分支判断