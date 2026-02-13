# CobblemonBridge

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 一个用于 Arclight 混合端的 Bukkit 插件，监听 Cobblemon 精灵捕获事件并将详细信息发送到玩家聊天框。

## 功能特性

- 监听 Cobblemon 的 `POKEMON_CAPTURED` 事件
- 在玩家聊天框显示捕获的精灵详细信息
- 支持 Debug 模式，可选择在控制台记录捕获事件
- 通过配置文件灵活控制功能
- 专为 Arclight 混合端（Bukkit + Fabric）优化

## 环境要求

- **服务器**: Arclight 混合端（支持 Bukkit + Fabric）
- **Minecraft 版本**: 1.21+
- **依赖模组**: [Cobblemon](https://cobblemon.com/)
- **Java 版本**: 21+

## 安装方法

### 1. 下载插件

从 [Releases](../../releases) 页面下载最新版本的 `cobblemonbridge.jar` 文件。

### 2. 安装插件

1. 将下载的 `cobblemonbridge.jar` 文件放入服务器的 `plugins/` 目录
2. 确保服务器已安装 Cobblemon 模组
3. 启动或重启服务器
4. 插件将自动创建默认配置文件

### 3. 配置文件

插件启动后会在 `plugins/CobblemonBridge/` 目录下创建 `config.yml` 文件：

```yaml
# CobblemonBridge 插件配置文件

# Debug 模式设置
# 开启后，会在控制台显示精灵捕获事件的详细记录
# 默认值: false
debug-mode: false
```

## 使用方法

### 玩家体验

当玩家成功捕获精灵时，聊天框会显示：

```
[Cobblemon] 你成功捕获了一只精灵！
精灵: [精灵名称]
性格: [性格名称]
等级: [等级]
闪光: [是/否]
```

### 管理员功能

#### 开启 Debug 模式

1. 打开 `plugins/CobblemonBridge/config.yml`
2. 将 `debug-mode` 设置为 `true`
3. 保存文件
4. 重启服务器或执行 `/reload` 命令

开启后，服务器控制台将显示详细的捕获事件记录。

## 编译指南

### 环境准备

- JDK 21 或更高版本
- Gradle 8.10 或更高版本（或使用项目自带的 Gradle Wrapper）

### 编译步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/yourusername/CobblemonBridge.git
   cd CobblemonBridge
   ```

2. **编译项目**
   ```bash
   # Windows
   ./gradlew.bat build
   
   # Linux/Mac
   ./gradlew build
   ```

3. **获取插件**
   
   编译完成后，插件文件位于 `build/libs/cobblemod-1.0.0.jar`

### 清理构建

```bash
./gradlew clean
```

### 重新构建

```bash
./gradlew clean build
```

## 项目结构

```
CobblemonBridge/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── com/kyotoanimation/cobblemod/
│       │       └── CobblemonBridge.kt    # 主插件代码
│       └── resources/
│           ├── config.yml                 # 默认配置文件
│           └── plugin.yml                 # 插件元数据
├── docs/
│   └── CobblemonBridge.md                 # 详细文档
├── build.gradle                           # Gradle 构建脚本
├── gradle/
│   └── wrapper/                           # Gradle Wrapper
├── README.md                              # 本文件
├── LICENSE                                # 许可证
└── .gitignore                             # Git 忽略配置
```

## 技术实现

### 核心功能

- **事件监听**: 使用反射方式动态加载 Cobblemon 事件，避免硬依赖
- **跨平台兼容**: 支持 Arclight 混合端环境（Bukkit + Fabric）
- **玩家对象处理**: 处理 Minecraft 原生玩家对象与 Bukkit 玩家的转换
- **反射调用**: 通过反射访问 Cobblemon 的 Fabric API

### 事件处理流程

1. 插件启动时检测 Arclight 环境和 Cobblemon 模组
2. 注册 `POKEMON_CAPTURED` 事件监听器
3. 捕获事件触发时，提取精灵信息（物种、性格、等级、闪光状态）
4. 将格式化信息发送到玩家聊天框
5. 根据 Debug 模式设置，可选择记录到控制台

## 常见问题

### Q: 插件无法加载？

**A**: 请确保：
- 服务器为 Arclight 混合端
- 已安装 Cobblemon 模组
- Java 版本为 21 或更高

### Q: 捕获事件没有触发？

**A**: 检查服务器日志，确认 Cobblemon 模组已正确加载，且插件成功注册了事件监听器。

### Q: 如何修改聊天消息格式？

**A**: 目前消息格式是固定的。如需自定义，请修改源代码中的 `handlePokemonCapture` 方法并重新编译。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 许可证

本项目采用 [MIT 许可证](LICENSE) 开源。

## 作者

- **KyotoAnimation** - 初始开发和维护

## 致谢

- [Cobblemon](https://cobblemon.com/) - 优秀的 Minecraft 精灵模组
- [Arclight](https://github.com/IzzelAliz/Arclight) - 混合端服务器软件
- [PaperMC](https://papermc.io/) - 高性能 Minecraft 服务器

## 更新日志

### v1.0.0 (2026-02-12)

- 初始版本发布
- 实现 POKEMON_CAPTURED 事件监听
- 支持 Debug 模式配置
- 添加详细的玩家聊天消息
- 支持 Arclight 混合端环境

---

**注意**: 本项目与 Minecraft、Cobblemon 或 Arclight 官方无关，仅为社区开发的第三方插件。