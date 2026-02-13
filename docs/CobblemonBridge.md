# CobblemonBridge - Arclight 混合端插件 (Kotlin实现)

## 功能说明

该插件专为 Arclight 混合端设计，可同时支持 Bukkit 插件和 Fabric 模组。
主要功能是注册 Cobblemon 的 POKEMON_CAPTURED 事件监听器，
当玩家成功捕获精灵时，提取并打印精灵的关键属性信息到玩家聊天框。

## Arclight 环境特点

1. Arclight 是 Bukkit + Forge/Fabric 的混合端
2. Cobblemon 作为 Fabric 模组运行在 Arclight 上
3. 本插件通过反射访问 Cobblemon 的 Fabric API
4. 需要在 plugin.yml 中将 Cobblemon 设为 softdepend

## 注意事项

- Cobblemon 返回的玩家对象是 Minecraft 原生的 ServerPlayer (net.minecraft.class_3222)
- 需要通过反射调用 getGameProfile().getName() 或使用 Bukkit 转换获取玩家名
- 控制台输出捕获记录默认为 debug 模式，默认值为 false

## Debug 模式说明

### 功能
- **默认状态**: 关闭 (false)
- **开启效果**: 在控制台显示精灵捕获事件的详细记录
- **关闭效果**: 仅在玩家聊天框显示捕获信息，不在控制台记录

### 影响范围
- 仅影响服务器控制台的捕获事件记录
- 不影响玩家聊天框的消息显示
- 不影响其他插件功能

### 配置方法

插件使用 `config.yml` 文件来控制 Debug 模式：

```yaml
# CobblemonBridge 插件配置文件

# Debug 模式设置
# 开启后，会在控制台显示精灵捕获事件的详细记录
# 默认值: false
debug-mode: false
```

**修改步骤**:
1. 找到插件目录中的 `config.yml` 文件
2. 将 `debug-mode` 的值修改为 `true` 开启 Debug 模式
3. 保存文件并重启服务器或重新加载插件

**注意**:
- 插件启动时会自动创建默认配置文件
- 配置修改后需要重启服务器或重载插件才能生效

## 核心功能

### 插件启用

- 检测 Arclight 环境和 Cobblemon 模组是否存在
- 注册事件监听器
- 打印插件信息和作者详情

### 精灵捕获事件处理

当玩家成功捕获精灵时，插件会：

1. 提取精灵的关键属性信息
2. 发送格式化消息到玩家聊天框
3. 记录捕获事件到服务器控制台

### 聊天框消息格式

```
[Cobblemon] 你成功捕获了一只精灵！
精灵: [精灵名称]
性格: [性格名称]
等级: [等级]
闪光: [是/否]
```

## 技术实现

### 环境检测

- 检测服务器是否为 Arclight 混合端
- 检查 Cobblemon 模组是否可用
- 加载必要的类和依赖

### 事件注册

- 使用反射方式动态加载 Cobblemon 事件
- 避免硬依赖，提高兼容性
- 注册 POKEMON_CAPTURED 事件监听器

### 玩家信息处理

- 处理 Minecraft 原生玩家对象转换
- 获取玩家名称的多种方法
- 支持不同环境下的玩家对象格式

### 精灵信息提取

- 从事件对象中提取精灵信息
- 获取精灵物种、等级、性格和闪光状态
- 处理不同格式的返回值

## 插件信息

- **作者**: KyotoAnimation
- **版本**: 1.0.0
- **描述**: A Bukkit plugin that bridges Cobblemon events for extended functionality

## 配置要求

- Arclight 混合端服务器
- Cobblemon 模组
- Bukkit API 兼容性

## 安装说明

1. 将插件放入服务器的 plugins 目录
2. 确保服务器已安装 Cobblemon 模组
3. 重启服务器
4. 插件将自动检测环境并注册事件监听器

## 运行效果

- 玩家捕获精灵时，会在聊天框收到格式化的精灵信息
- 服务器控制台会记录捕获事件
- 插件启动时会显示环境检测结果和作者信息

## 故障排除

- 确保服务器为 Arclight 混合端
- 确保已安装 Cobblemon 模组
- 检查服务器日志中的错误信息
- 验证 plugin.yml 配置正确

## 总结

CobblemonBridge 插件为 Arclight 混合端服务器提供了便捷的 Cobblemon 精灵捕获事件处理功能，通过反射技术实现了 Bukkit 插件与 Fabric 模组的无缝集成，为玩家提供了更好的游戏体验。