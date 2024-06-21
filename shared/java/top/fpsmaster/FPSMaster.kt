package top.fpsmaster

import net.minecraft.crash.CrashReport
import top.fpsmaster.features.GlobalSubmitter
import top.fpsmaster.features.command.CommandManager
import top.fpsmaster.features.manager.ModuleManager
import top.fpsmaster.font.FontManager
import top.fpsmaster.interfaces.ProviderManager
import top.fpsmaster.modules.account.AccountManager
import top.fpsmaster.modules.client.AsyncTask
import top.fpsmaster.modules.client.PlayerManager
import top.fpsmaster.modules.logger.Logger
import top.fpsmaster.modules.music.MusicPlayer
import top.fpsmaster.modules.music.netease.NeteaseApi
import top.fpsmaster.modules.plugin.PluginManager
import top.fpsmaster.ui.click.music.MusicPanel
import top.fpsmaster.ui.click.themes.DarkTheme
import top.fpsmaster.ui.click.themes.LightTheme
import top.fpsmaster.ui.click.themes.Theme
import top.fpsmaster.ui.custom.ComponentsManager
import top.fpsmaster.ui.screens.oobe.OOBEScreen
import top.fpsmaster.utils.GitInfo
import top.fpsmaster.utils.os.FileUtils
import top.fpsmaster.utils.os.HttpRequest
import top.fpsmaster.utils.system.config.ConfigManager
import top.fpsmaster.utils.system.i18n.Language
import top.fpsmaster.websocket.client.WsClient
import top.fpsmaster.wrapper.Constants
import java.awt.Desktop
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import javax.swing.JOptionPane


class FPSMaster {
    var hasOptifine: Boolean = false

    @JvmField
    var loggedIn = false

    var wsClient: WsClient? = null


    private fun initializeFonts() {
        Logger.info("Initializing Fonts...")
        fontManager.load()
    }


    private fun initializeLang() {
        Logger.info("Initializing I18N...")
        i18n.read("zh_cn")
    }

    private fun initializeConfigures() {
        Logger.info("Initializing Config...")
        configManager.loadConfig("default")
        theme = if (themeSlot == "dark") {
            DarkTheme()
        } else {
            LightTheme()
        }
        MusicPlayer.setVolume(configManager.configure.getOrCreate("volume", "1").toFloat())
        NeteaseApi.cookies = FileUtils.readTempValue("cookies")
        MusicPanel.nickname = FileUtils.readTempValue("nickname")
        accountManager.autoLogin()
    }

    private fun initializeMusic() {
        Logger.info("Checking music cache...")
        val dirSize = FileUtils.getDirSize(FileUtils.artists)
        if (dirSize > 1024) {
            FileUtils.artists.delete()
            Logger.info("Cleared img cache")
        }
        Logger.info("Found image: " + dirSize + "mb")
        val dirSize1 = FileUtils.getDirSize(FileUtils.music)
        if (dirSize1 > 2048) {
            FileUtils.music.delete()
            Logger.warn("Cleared music cache")
        }
        Logger.info("Found music: " + dirSize1 + "mb")
    }

    private fun initializeComponents() {
        Logger.info("Initializing component...")
        componentsManager = ComponentsManager()
    }

    private fun initializeCommands() {
        Logger.info("Initializing commands")
        commandManager = CommandManager()
    }

    private fun initializePlugins() {
        Logger.info("Start loading plugins")
        plugins.init()
        Logger.info("Loaded ${PluginManager.plugins.size} plugins!")
        Logger.info("Initialized")
    }

    private fun checkOptifine() {
        try {
            Class.forName("optifine.Patcher")
            hasOptifine = true
        } catch (_: ClassNotFoundException) {
        }
    }

    private fun checkUpdate() {
        async.runnable {
            isLatest = true
            val s = HttpRequest["https://fpsmaster.top/version"]
            val version = CLIENT_VERSION.replace("v".toRegex(), "").replace("\\.".toRegex(), "").toInt()
            val newVersion = s.replace("v".toRegex(), "").replace("\\.".toRegex(), "").toInt()
            if (s.isNotEmpty()) {
                latest = s
                isLatest = version >= newVersion
            }
        }
    }

    fun initialize() {
        initializeFonts()
        initializeLang()
        initializeMusic()
        initializeComponents()
        initializeConfigures()
        initializeCommands()
        initializePlugins()

        checkUpdate()
        checkOptifine()
    }


    fun shutdown() {
        configManager.saveConfig("default")
    }

    companion object {
        const val SERVICE_API = "https://service.fpsmaster.top"
        const val FILE_API = "https://files.fpsmaster.top"

        const val EDITION = Constants.EDITION
        const val COPYRIGHT = "Copyright ©2020-2024  FPSMaster Team  All Rights Reserved."

        @JvmField
        var INSTANCE = FPSMaster()

        @JvmField
        var CLIENT_NAME = "FPSMaster"

        @JvmField
        var CLIENT_VERSION = "v3.2.9"

        @JvmField
        var theme: Theme = DarkTheme()

        @JvmField
        var themeSlot = "dark"

        @JvmField
        var moduleManager: ModuleManager = ModuleManager()

        @JvmField
        var fontManager: FontManager = FontManager()

        @JvmField
        var configManager: ConfigManager = ConfigManager()

        @JvmField
        var playerManager: PlayerManager = PlayerManager()

        @JvmField
        var oobeScreen: OOBEScreen = OOBEScreen()

        @JvmField
        var accountManager: AccountManager = AccountManager()

        @JvmField
        var submitter: GlobalSubmitter = GlobalSubmitter()

        @JvmField
        var plugins: PluginManager = PluginManager()

        @JvmField
        var commandManager: CommandManager = CommandManager()

        @JvmField
        var componentsManager: ComponentsManager = ComponentsManager()

        @JvmField
        var i18n: Language = Language()

        @JvmField
        var async = AsyncTask(100)

        @JvmField
        var isLatest = false

        @JvmField
        var debug = false

        @JvmField
        var latest = CLIENT_VERSION


        @JvmStatic
        fun getClientTitle(): String {
            return "$CLIENT_NAME $CLIENT_VERSION ${Constants.VERSION} (${Constants.EDITION}) (${GitInfo.branch} - ${GitInfo.commitIdAbbrev})"
        }


        @JvmStatic
        fun crashed(crashReport: CrashReport) {
            Logger.fatal("Game crashed")
            val debugFolder = File(ProviderManager.mcProvider.getGameDir(), "debugs")
            val file2 = File(debugFolder, "crash.txt")
            val filelog = File(ProviderManager.mcProvider.getGameDir(), "logs")
            val log = File(filelog, "latest.log")

            // write crash logs
            try {
                debugFolder.mkdir()
                file2.createNewFile()
                val printwriter = PrintWriter(FileWriter(file2))
                printwriter.println(crashReport.completeReport)
                printwriter.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // copy client config to crash folder
            val dir = File(ProviderManager.mcProvider.getGameDir(), CLIENT_NAME + EDITION)
            if (dir.exists()) {
                try {
                    val config = File(dir, "default.json")
                    val conponent = File(dir, "components.json")
                    val crashConfig = File(debugFolder, "default.json")
                    val crashConponent = File(debugFolder, "components.json")
                    val logs = File(debugFolder, "latest.log")
                    org.apache.commons.io.FileUtils.copyFile(log, logs)
                    if (config.exists()) {
                        org.apache.commons.io.FileUtils.copyFile(config, crashConfig)
                    }
                    if (conponent.exists()) {
                        org.apache.commons.io.FileUtils.copyFile(conponent, crashConponent)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            // open crash folder
            try {
                Desktop.getDesktop().open(debugFolder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // show crash dialog
            JOptionPane.showConfirmDialog(
                null,
                "游戏已崩溃，崩溃文件已保存到: " + file2.absolutePath + "，将本文件夹发送给作者获取帮助。",
                "Game Crash",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}
