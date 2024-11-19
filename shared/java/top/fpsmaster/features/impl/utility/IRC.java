package top.fpsmaster.features.impl.utility;

import top.fpsmaster.FPSMaster;
import top.fpsmaster.event.Subscribe;
import top.fpsmaster.event.events.EventTick;
import top.fpsmaster.features.manager.Category;
import top.fpsmaster.features.manager.Module;
import top.fpsmaster.features.settings.impl.BooleanSetting;
import top.fpsmaster.utils.Utility;
import top.fpsmaster.utils.math.MathTimer;
import top.fpsmaster.websocket.client.WsClient;
import top.fpsmaster.interfaces.ProviderManager;

import java.net.URISyntaxException;

public class IRC extends Module {
    private static boolean using = false;
    private static final BooleanSetting showMates = new BooleanSetting("showMates", true);
    private final MathTimer timer = new MathTimer();

    public IRC() {
        super("IRC", Category.Utility);
        addSettings(showMates);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        using = true;
    }

    @Subscribe
    public void onTick(EventTick e) throws URISyntaxException {
        if (!timer.delay(5000)) {
            return;
        }
        if (ProviderManager.mcProvider.getWorld() == null) {
            return;
        }
        if (FPSMaster.INSTANCE.wsClient == null) {
            FPSMaster.INSTANCE.wsClient = (WsClient.start("wss://service.fpsmaster.top/"));
            if (FPSMaster.debug) {
                Utility.sendClientMessage("尝试连接");
            }
        } else if (FPSMaster.INSTANCE.wsClient.isClosed() && !FPSMaster.INSTANCE.wsClient.isOpen()) {
            FPSMaster.INSTANCE.wsClient.connect();
            if (FPSMaster.debug) {
                Utility.sendClientMessage("尝试连接");
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (FPSMaster.INSTANCE.wsClient != null && FPSMaster.INSTANCE.wsClient.isOpen()) {
            FPSMaster.INSTANCE.wsClient.close();
        }
        using = false;
    }

    public static boolean isUsing() {
        return using;
    }

    public static void setUsing(boolean using) {
        IRC.using = using;
    }

    public static BooleanSetting getShowMates() {
        return showMates;
    }
}
