package top.fpsmaster.features.impl.utility;

import net.minecraft.client.Minecraft;
import top.fpsmaster.event.Subscribe;
import top.fpsmaster.event.events.EventUpdate;
import top.fpsmaster.features.manager.Category;
import top.fpsmaster.features.manager.Module;
import top.fpsmaster.interfaces.ProviderManager;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", Category.Utility);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        ProviderManager.gameSettings.setKeyPress(Minecraft.getMinecraft().gameSettings.keyBindSprint, true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ProviderManager.gameSettings.setKeyPress(Minecraft.getMinecraft().gameSettings.keyBindSprint, false);
    }
}
