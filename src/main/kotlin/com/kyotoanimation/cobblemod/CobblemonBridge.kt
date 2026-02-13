package com.kyotoanimation.cobblemod

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.Bukkit

/**
 * CobblemonBridge - Arclight 混合端 Bukkit 插件 (Kotlin实现)
 *
 * 功能说明：
 * 该插件专为 Arclight 混合端设计，可同时支持 Bukkit 插件和 Fabric 模组。
 * 主要功能是注册 Cobblemon 的 POKEMON_CAPTURED 事件监听器，
 * 当玩家成功捕获精灵时，提取并打印精灵的关键属性信息到玩家聊天框。
 *
 * Arclight 环境特点：
 * 1. Arclight 是 Bukkit + Forge/Fabric 的混合端
 * 2. Cobblemon 作为 Fabric 模组运行在 Arclight 上
 * 3. 本插件通过反射访问 Cobblemon 的 Fabric API
 * 4. 需要在 plugin.yml 中将 Cobblemon 设为 softdepend
 *
 * 注意事项：
 * - Cobblemon 返回的玩家对象是 Minecraft 原生的 ServerPlayer (net.minecraft.class_3222)
 * - 需要通过反射调用 getGameProfile().getName() 或使用 Bukkit 转换获取玩家名
 */
class CobblemonBridge : JavaPlugin(), Listener {

    companion object {
        lateinit var instance: CobblemonBridge
            private set
        
        // Cobblemon 类名常量
        private const val COBBLEMON_EVENTS_CLASS = "com.cobblemon.mod.common.api.events.CobblemonEvents"
        private const val COBBLEMON_PRIORITY_CLASS = "com.cobblemon.mod.common.api.Priority"
        private const val COBBLEMON_CAPTURE_EVENT_CLASS = "com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent"
    }

    private var cobblemonAvailable = false
    private var debugMode = false // debug模式，默认为false

    /**
     * 插件启用时调用
     * 检测 Arclight 环境和 Cobblemon 模组是否存在
     */
    override fun onEnable() {
        instance = this
        logger.info("[CobblemonBridge] 插件启用中...")
        
        // 加载配置文件
        saveDefaultConfig()
        reloadConfig()
        debugMode = config.getBoolean("debug-mode", false)
        
        // 检测运行环境
        detectEnvironment()
        
        // 注册 Bukkit 事件监听器
        server.pluginManager.registerEvents(this, this)

        // 检查 Cobblemon 是否存在
        if (checkCobblemonAvailable()) {
            logger.info("[CobblemonBridge] 成功检测到 Cobblemon 模组")
            cobblemonAvailable = true
            registerCobblemonEvents()
        } else {
            logger.warning("[CobblemonBridge] 未检测到 Cobblemon 模组，精灵捕获监听功能将不可用")
            logger.warning("[CobblemonBridge] 请确保服务器为 Arclight 混合端且已安装 Cobblemon")
        }

        logger.info("[CobblemonBridge] 插件启用完成")
        logger.info("[CobblemonBridge] Debug 模式: ${if (debugMode) "开启" else "关闭"}")
    }

    /**
     * 插件禁用时调用
     */
    override fun onDisable() {
        logger.info("[CobblemonBridge] 插件禁用中...")
        logger.info("[CobblemonBridge] 插件已禁用")
    }

    /**
     * 检测服务器运行环境
     * 移除服务器信息输出，添加作者信息
     */
    private fun detectEnvironment() {
        // 检测是否为 Arclight
        val serverVersion = server.version
        val isArclight = serverVersion.contains("Arclight", ignoreCase = true) ||
                         Bukkit.getPluginManager().plugins.any { 
                             it.name.contains("Arclight", ignoreCase = true) 
                         }
        
        if (isArclight) {
            logger.info("[CobblemonBridge] 检测到 Arclight 混合端环境")
        } else {
            logger.warning("[CobblemonBridge] 未检测到 Arclight 环境，插件可能无法正常工作")
        }
        
        // 添加作者信息
        logger.info("[CobblemonBridge] 作者: KyotoAnimation")
        logger.info("[CobblemonBridge] 版本: ${description.version}")
        logger.info("[CobblemonBridge] 描述: ${description.description}")
    }

    /**
     * 检查 Cobblemon 模组是否可用
     * 通过反射检查 Cobblemon 的核心类是否存在
     */
    private fun checkCobblemonAvailable(): Boolean {
        return try {
            // 尝试加载 Cobblemon 的核心类
            Class.forName(COBBLEMON_EVENTS_CLASS)
            Class.forName(COBBLEMON_PRIORITY_CLASS)
            Class.forName(COBBLEMON_CAPTURE_EVENT_CLASS)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * 注册 Cobblemon 事件监听
     * 使用反射方式动态加载 Cobblemon 事件，避免硬依赖
     * 
     * Arclight 环境下，Fabric 模组和 Bukkit 插件共享同一个 JVM，
     * 因此可以通过反射直接访问 Cobblemon 的 Fabric API
     */
    private fun registerCobblemonEvents() {
        try {
            // 通过反射获取 CobblemonEvents 类
            val cobblemonEventsClass = Class.forName(COBBLEMON_EVENTS_CLASS)
            val priorityClass = Class.forName(COBBLEMON_PRIORITY_CLASS)

            // 获取 POKEMON_CAPTURED 事件对象
            val pokemonCapturedField = cobblemonEventsClass.getDeclaredField("POKEMON_CAPTURED")
            val pokemonCapturedEvent = pokemonCapturedField.get(null)

            // 获取 NORMAL 优先级
            val normalPriority = priorityClass.getDeclaredField("NORMAL").get(null)

            // 创建事件处理器 (Consumer)
            val eventHandler = java.util.function.Consumer<Any> { event ->
                handlePokemonCapture(event)
            }

            // 调用 subscribe 方法注册监听器
            val subscribeMethod = pokemonCapturedEvent.javaClass.getMethod(
                "subscribe",
                priorityClass,
                java.util.function.Consumer::class.java
            )
            subscribeMethod.invoke(pokemonCapturedEvent, normalPriority, eventHandler)

            logger.info("[CobblemonBridge] POKEMON_CAPTURED 事件监听器注册成功")
        } catch (e: Exception) {
            logger.severe("[CobblemonBridge] 注册 Cobblemon 事件监听器失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 处理精灵捕获事件
     * 通过反射从事件对象中提取精灵信息，并发送到玩家聊天框
     * 
     * @param event PokemonCapturedEvent 对象 (通过反射访问)
     */
    private fun handlePokemonCapture(event: Any) {
        try {
            // 获取 pokemon 属性
            val pokemonMethod = event.javaClass.getMethod("getPokemon")
            val pokemon = pokemonMethod.invoke(event) ?: run {
                logger.warning("[CobblemonBridge] 捕获事件中精灵实体为 null")
                return
            }

            // 获取玩家 (Cobblemon 返回的是 Minecraft 原生的 ServerPlayer)
            val playerMethod = event.javaClass.getMethod("getPlayer")
            val player = playerMethod.invoke(event)
            val playerName = getPlayerName(player)
            val bukkitPlayer = getBukkitPlayer(player)

            // 获取精灵信息
            val speciesMethod = pokemon.javaClass.getMethod("getSpecies")
            val species = speciesMethod.invoke(pokemon)
            val speciesName = species?.let {
                val nameMethod = it.javaClass.getMethod("getName")
                nameMethod.invoke(it) as? String ?: "未知物种"
            } ?: "未知物种"

            // 获取等级
            val levelMethod = pokemon.javaClass.getMethod("getLevel")
            val level = levelMethod.invoke(pokemon) as? Int ?: 0

            // 获取闪光状态
            val shinyMethod = pokemon.javaClass.getMethod("getShiny")
            val isShiny = shinyMethod.invoke(pokemon) as? Boolean ?: false

            // 获取性格
            val natureName = getNatureName(pokemon)

            // 发送消息到玩家聊天框
            if (bukkitPlayer != null) {
                bukkitPlayer.sendMessage("§6[Cobblemon] §a你成功捕获了一只精灵！")
                bukkitPlayer.sendMessage("§6精灵: §f$speciesName")
                bukkitPlayer.sendMessage("§6性格: §f$natureName")
                bukkitPlayer.sendMessage("§6等级: §f$level")
                bukkitPlayer.sendMessage("§6闪光: §f${if (isShiny) "是" else "否"}")
            }

            // 输出到控制台（仅debug模式）
            if (debugMode) {
                logger.info("[CobblemonBridge] 精灵捕获事件 - 玩家: $playerName, 精灵: $speciesName, 等级: $level, 闪光: ${if (isShiny) "是" else "否"}")
            }

        } catch (e: Exception) {
            logger.severe("[CobblemonBridge] 处理精灵捕获事件时发生错误: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 获取玩家名称
     * 处理 Arclight 环境下 Cobblemon 返回的 Minecraft 原生玩家对象
     * 
     * @param playerObj Minecraft 原生玩家对象 (ServerPlayer)
     * @return 玩家名称
     */
    private fun getPlayerName(playerObj: Any?): String {
        if (playerObj == null) return "未知玩家"

        val playerClass = playerObj.javaClass

        // 方法1: 尝试调用 getGameProfile().getName()
        try {
            val getGameProfileMethod = playerClass.getMethod("getGameProfile")
            val gameProfile = getGameProfileMethod.invoke(playerObj)
            if (gameProfile != null) {
                val getNameMethod = gameProfile.javaClass.getMethod("getName")
                val name = getNameMethod.invoke(gameProfile) as? String
                if (!name.isNullOrEmpty()) return name
            }
        } catch (e: Exception) {
            // 忽略异常，尝试下一个方法
        }

        // 方法2: 尝试通过 Bukkit 获取玩家名
        try {
            val bukkitPlayer = getBukkitPlayer(playerObj)
            if (bukkitPlayer != null) {
                return bukkitPlayer.name
            }
        } catch (e: Exception) {
            // 忽略异常，尝试下一个方法
        }

        // 方法3: 尝试调用 toString() 并解析
        try {
            val toString = playerObj.toString()
            if (toString.contains("name=")) {
                val nameMatch = Regex("name=([^,}]+)").find(toString)
                if (nameMatch != null) {
                    return nameMatch.groupValues[1].trim()
                }
            }
        } catch (e: Exception) {
            // 忽略异常
        }

        // 如果所有方法都失败，返回类名作为标识
        return "未知玩家(${playerClass.simpleName})"
    }

    /**
     * 获取 Bukkit 玩家对象
     * 从 Minecraft 原生玩家对象转换为 Bukkit 玩家
     * 
     * @param playerObj Minecraft 原生玩家对象 (ServerPlayer)
     * @return Bukkit 玩家对象
     */
    private fun getBukkitPlayer(playerObj: Any?): org.bukkit.entity.Player? {
        if (playerObj == null) return null
        
        try {
            // 尝试获取玩家 UUID
            val getUUIDMethod = playerObj.javaClass.getMethod("getUUID")
            val uuid = getUUIDMethod.invoke(playerObj) as? java.util.UUID
            if (uuid != null) {
                return Bukkit.getPlayer(uuid)
            }
        } catch (e: Exception) {
            // 忽略异常
        }
        
        return null
    }

    /**
     * 获取性格名称
     * 处理 Arclight 环境下性格对象的显示名称获取
     *
     * @param pokemon Pokemon 对象
     * @return 性格名称
     */
    private fun getNatureName(pokemon: Any): String {
        return try {
            val natureMethod = pokemon.javaClass.getMethod("getNature")
            val nature = natureMethod.invoke(pokemon) ?: return "未知"

            val displayNameMethod = nature.javaClass.getMethod("getDisplayName")
            val displayName = displayNameMethod.invoke(nature) ?: return "未知"

            // 根据返回类型处理
            when (displayName) {
                is String -> displayName
                else -> {
                    // 尝试调用 getString() 方法
                    try {
                        val getStringMethod = displayName.javaClass.getMethod("getString")
                        getStringMethod.invoke(displayName) as? String ?: "未知"
                    } catch (e: NoSuchMethodException) {
                        // 如果没有 getString() 方法，尝试 toString()
                        displayName.toString()
                    }
                }
            }
        } catch (e: Exception) {
            logger.warning("[CobblemonBridge] 获取性格名称失败: ${e.message}")
            "未知"
        }
    }
}
