package top.fpsmaster.features.manager;

import net.minecraft.client.Minecraft;
import top.fpsmaster.FPSMaster;
import top.fpsmaster.event.EventDispatcher;
import top.fpsmaster.features.settings.Setting;
import top.fpsmaster.features.settings.impl.*;
import top.fpsmaster.interfaces.ProviderManager;
import top.fpsmaster.ui.notification.NotificationManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Module {
    protected String name;
    private String description = "";
    protected Category category;
    protected List<Setting<?>> settings = new LinkedList<>();
    protected boolean isEnabled = false;
    protected int key = 0;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public void addSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            if (setting instanceof BooleanSetting) {
                this.settings.add(setting);
            }
        }
        for (Setting<?> setting : settings) {
            if (setting instanceof BindSetting) {
                this.settings.add(setting);
            }
        }
        for (Setting<?> setting : settings) {
            if (setting instanceof ModeSetting) {
                this.settings.add(setting);
            }
        }
        for (Setting<?> setting : settings) {
            if (setting instanceof NumberSetting) {
                this.settings.add(setting);
            }
        }
        for (Setting<?> setting : settings) {
            if (setting instanceof TextSetting) {
                this.settings.add(setting);
            }
        }
        for (Setting<?> setting : settings) {
            if (setting instanceof ColorSetting) {
                this.settings.add(setting);
            }
        }
    }

    public void setState(boolean state) {
        this.isEnabled = state;
    }

    public void toggle() {
        set(!isEnabled);
    }

    public void set(boolean state) {
        this.isEnabled = state;
        try {
            if (state) {
                onEnable();
                if (ProviderManager.mcProvider.getPlayer() != null) {
                    NotificationManager.INSTANCE.addNotification(
                            FPSMaster.i18n.get("notification.module.enable"),
                            String.format(
                                    FPSMaster.i18n.get("notification.module.enable.desc"),
                                    FPSMaster.i18n.get(this.name.toLowerCase(Locale.getDefault()))
                            ),
                            1f
                    );
                }
            } else {
                onDisable();
                if (ProviderManager.mcProvider.getPlayer() != null) {
                    NotificationManager.INSTANCE.addNotification(
                            FPSMaster.i18n.get("notification.module.disable"),
                            String.format(
                                    FPSMaster.i18n.get("notification.module.disable.desc"),
                                    FPSMaster.i18n.get(this.name.toLowerCase(Locale.getDefault()))
                            ),
                            1f
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEnable() {
        EventDispatcher.registerListener(this);
    }

    public void onDisable() {
        EventDispatcher.unregisterListener(this);
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    protected Minecraft mc = Minecraft.getMinecraft();
}
