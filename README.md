## GTOMaid

为整合包 [GregTech-Odyssey](https://gtodyssey.com/) 设计的 [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid) 增强 Mod。

GTO 专供, 其他包大概率不能用. 当前适配的 GTO 版本为 0.5.5beta, 车万女仆版本为 1.5.3-forge, 其他版本没测.

- Minecraft 1.20.1 / Forge 47.4.10
- 依赖: Touhou Little Maid、GTOCore、GTCEu、Curios、Botania、ExtraBotany

## 功能

所有功能可以配置开关, 默认全开

### 时间扭曲任务

让女仆使用 GTO 的「时间扭曲者」自动加速附近的格雷机器.

直接模拟玩家使用, 因此需要主人在线.

> 寻路和朝向什么的太难写了, 可能小问题有点多, 欢迎pr

### 植物魔法兼容

- 女仆使用植魔魔力工具和装备时，从背包/饰品栏的魔力容器扣除魔力代替耐久
- 女仆可攻击盖亚守护者, 视作主人的攻击

### 魔法伤害免疫

~~私货~~

女仆持有任何植魔魔力容器物品(如魔力之戒)时, 免疫魔法伤害并避免被玩家造成伤害.

## 配置

### `config/gtomaid.toml`

| 节 | 项 | 默认 | 说明 |
| --- | --- | --- | --- |
| `features` | `enableTimeTwist` | true | 时间扭曲任务总开关 |
| `features` | `enableMaidMagicImmune` | true | 魔法免疫总开关 |
| `magicImmune` | `magicDamageTypes` | 见上 | 视为魔法伤害的 ID 列表 |
| `timeTwist` | `ownerDistance` | 8 | 非Home模式下机器到主人的最大距离 |
| `timeTwist.move` | `scanInterval` | 60 | 扫描间隔 (tick) |
| `timeTwist.move` | `moveSpeed` | 0.6 | 移动速度倍率 |
| `timeTwist.move` | `verticalSearchRange` | 3 | 垂直搜索范围 |
| `timeTwist.move` | `wanderSpeed` | 0.3 | 找不到目标时游荡速度 |
| `timeTwist.move` | `wanderRadius` | 5 | 游荡半径 |
| `timeTwist.move` | `wanderRetryInterval` | 120 | 游荡冷却 (tick) |
| `timeTwist.move` | `assignmentTimeoutTicks` | 200 | 分配后超时 (tick) |
| `timeTwist.move` | `standRadius` | 4 | 站位搜索水平半径 |
| `timeTwist.move` | `standVerticalRadius` | 2 | 站位搜索垂直半径 |
| `timeTwist.action` | `arriveHorizontal` | 1.5 | 抵达水平阈值 |
| `timeTwist.action` | `arriveVertical` | 1.5 | 抵达垂直阈值 |
| `timeTwist.action` | `maxTicksAtStand` | 80 | 站位上的最大 tick 数 |
| `timeTwist.action` | `swingCooldownTicks` | 10 | 两次加速之间的冷却 |
| `timeTwist.action` | `maxClicksPerMachine` | 3 | 单次寻路中每台机器最多加速次数 |

### `config/gtomaid-mixin.properties`

| 键 | 默认 | 说明 |
| --- | --- | --- |
| `enableBotaniaToolDurability` | true | 女仆使用植物魔法工具时以魔力代替耐久 |
| `enableBotaniaGaiaAttack` | true | 允许女仆攻击原版植魔盖亚守护者(盖亚一和二) |
| `enableExtrabotanyGaiaAttack` | true | 允许女仆攻击盖亚三 |

## 安装

在 [releases](https://github.com/krau/gtomaid/releases/) 页面下载 jar , 服务端和客户端均需要安装

## 构建

```bash
./gradlew build
```