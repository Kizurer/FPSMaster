package top.fpsmaster.features.impl.utility;

import top.fpsmaster.features.manager.Category;
import top.fpsmaster.features.manager.Module;

public class Translator extends Module {
    private static boolean using = false;

    public Translator() {
        super("Translator", Category.Utility);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        using = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        using = false;
    }

    public static boolean isUsing() {
        return using;
    }

    public static void setUsing(boolean using) {
        Translator.using = using;
    }
}
