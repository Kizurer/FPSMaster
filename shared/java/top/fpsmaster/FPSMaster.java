package top.fpsmaster;

import top.fpsmaster.features.GlobalSubmitter;
import top.fpsmaster.features.command.CommandManager;
import top.fpsmaster.features.manager.ModuleManager;
import top.fpsmaster.font.FontManager;
import top.fpsmaster.modules.account.AccountManager;
import top.fpsmaster.modules.client.AsyncTask;
import top.fpsmaster.modules.client.PlayerManager;
import top.fpsmaster.modules.config.ConfigManager;
import top.fpsmaster.modules.logger.Logger;
import top.fpsmaster.modules.music.MusicPlayer;
import top.fpsmaster.modules.music.netease.NeteaseApi;
import top.fpsmaster.modules.plugin.PluginManager;
import top.fpsmaster.ui.click.music.MusicPanel;
import top.fpsmaster.ui.click.themes.DarkTheme;
import top.fpsmaster.ui.click.themes.LightTheme;
import top.fpsmaster.ui.click.themes.Theme;
import top.fpsmaster.ui.custom.ComponentsManager;
import top.fpsmaster.ui.screens.oobe.OOBEScreen;
import top.fpsmaster.utils.GitInfo;
import top.fpsmaster.utils.os.FileUtils;
import top.fpsmaster.modules.i18n.Language;
import top.fpsmaster.utils.thirdparty.github.UpdateChecker;
import top.fpsmaster.websocket.client.WsClient;
import top.fpsmaster.wrapper.Constants;

public class FPSMaster {

    public boolean hasOptifine;
    public boolean loggedIn;
    public WsClient wsClient;

    private void initializeFonts() {
        Logger.info("Initializing Fonts...");
        fontManager.load();
    }

    private void initializeLang() {
        Logger.info("Initializing I18N...");
        i18n.read("zh_cn");
    }

    private void initializeConfigures() {
        Logger.info("Initializing Config...");
        configManager.loadConfig("default");
        if ("dark".equals(themeSlot)) {
            theme = new DarkTheme();
        } else {
            theme = new LightTheme();
        }
        MusicPlayer.INSTANCE.setVolume(Float.parseFloat(configManager.configure.getOrCreate("volume", "1")));
        NeteaseApi.cookies = FileUtils.INSTANCE.readTempValue("cookies");
        MusicPanel.INSTANCE.setNickname(FileUtils.INSTANCE.readTempValue("nickname"));
        accountManager.autoLogin();
    }

    private void initializeMusic() {
        Logger.info("Checking music cache...");
        long dirSize = FileUtils.INSTANCE.getDirSize(FileUtils.INSTANCE.getArtists());
        if (dirSize > 1024) {
            FileUtils.INSTANCE.getArtists().delete();
            Logger.info("Cleared img cache");
        }
        Logger.info("Found image: " + dirSize + "mb");
        long dirSize1 = FileUtils.INSTANCE.getDirSize(FileUtils.INSTANCE.getMusic());
        if (dirSize1 > 2048) {
            FileUtils.INSTANCE.getMusic().delete();
            Logger.warn("Cleared music cache");
        }
        Logger.info("Found music: " + dirSize1 + "mb");
    }

    private void initializeComponents() {
        Logger.info("Initializing component...");
        componentsManager.init();
    }

    private void initializeCommands() {
        Logger.info("Initializing commands");
        commandManager.init();
    }

    private void initializePlugins() {
        Logger.info("Start loading plugins");
        plugins.init();
        Logger.info("Loaded " + PluginManager.Companion.getPlugins().size() + " plugins!");
        Logger.info("Initialized");
    }

    private void initializeModules() {
        moduleManager.init();
        submitter.init();
    }

    private void checkOptifine() {
        try {
            Class.forName("optifine.Patcher");
            hasOptifine = true;
        } catch (ClassNotFoundException e) {
        }
    }

    private void checkUpdate() {
        AsyncTask asyncTask = new AsyncTask(100);
        asyncTask.runnable(() -> {
            String s = UpdateChecker.INSTANCE.getLatestVersion();
            if (s == null) {
                isLatest = false;
                updateFailed = true;
                return;
            }
            if (!s.isEmpty()) {
                latest = s;
                isLatest = CLIENT_VERSION.equals(s);
            }
        });
    }

    public void initialize() {
        initializeFonts();
        initializeLang();
        initializeMusic();
        initializeModules();
        initializeComponents();
        initializeConfigures();
        initializeCommands();
        initializePlugins();

        checkUpdate();
        checkOptifine();
    }

    public void shutdown() {
        configManager.saveConfig("default");
    }

    public static final String SERVICE_API = "https://service.fpsmaster.top";
    public static final String FILE_API = "https://files.fpsmaster.top";

    public static final String EDITION = Constants.EDITION;
    public static final String COPYRIGHT = "Copyright ©2020-2024  FPSMaster Team  All Rights Reserved.";

    public static FPSMaster INSTANCE = new FPSMaster();

    public static String CLIENT_NAME = "FPSMaster";
    public static String CLIENT_VERSION = "v4";

    public static Theme theme = new DarkTheme();
    public static String themeSlot = "dark";

    public static ModuleManager moduleManager = new ModuleManager();
    public static FontManager fontManager = new FontManager();
    public static ConfigManager configManager = new ConfigManager();
    public static PlayerManager playerManager = new PlayerManager();
    public static OOBEScreen oobeScreen = new OOBEScreen();
    public static AccountManager accountManager = new AccountManager();
    public static GlobalSubmitter submitter = new GlobalSubmitter();
    public static PluginManager plugins = new PluginManager();
    public static CommandManager commandManager = new CommandManager();
    public static ComponentsManager componentsManager = new ComponentsManager();
    public static Language i18n = new Language();
    public static AsyncTask async = new AsyncTask(100);
    public static boolean development = false;
    public static boolean debug = false;
    public static boolean isLatest = false;
    public static boolean updateFailed = false;
    public static String latest = "";

    private static void checkDevelopment() {
        try {
            Class.forName("net.fabricmc.devlaunchinjector.Main");
            development = true;
        } catch (Throwable e) {
        }
    }

    public static String getClientTitle() {
        checkDevelopment();
        return CLIENT_NAME + " " + CLIENT_VERSION + " " + Constants.VERSION + " (" + Constants.EDITION + ") (" + GitInfo.INSTANCE.getBranch() + " - " + GitInfo.INSTANCE.getCommitIdAbbrev() + ")" + (development? " - Developer Mode" : "");
    }
}