package top.fpsmaster.ui.screens.account

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import top.fpsmaster.FPSMaster
import top.fpsmaster.utils.thirdparty.microsoft.MicrosoftLogin.login
import top.fpsmaster.utils.render.Render2DUtils
import java.awt.Color
import java.io.IOException

class GuiWaiting : GuiScreen() {
    override fun initGui() {
        super.initGui()
        login()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        val sr = ScaledResolution(Minecraft.getMinecraft())
        Render2DUtils.drawRect(
            0f,
            0f,
            sr.scaledWidth.toFloat(),
            sr.scaledHeight.toFloat(),
            Color(255, 255, 255),
        )
        FPSMaster.fontManager.s24.drawCenteredString(
            FPSMaster.i18n["microsoft.login.desc"],
            sr.scaledWidth / 2f,
            sr.scaledHeight / 2f - 30,
            FPSMaster.theme.textColorDescription.rgb
        )
        FPSMaster.fontManager.s40.drawCenteredString(
            FPSMaster.i18n["microsoft.login.title"],
            sr.scaledWidth / 2f,
            sr.scaledHeight / 2f + 10,
            FPSMaster.theme.primary.rgb
        )
        if (logged) {
            logged = false
            Minecraft.getMinecraft().displayGuiScreen(GuiMainMenu())
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        if (keyCode == 1) {
            Minecraft.getMinecraft().displayGuiScreen(GuiMainMenu())
        }
    }

    companion object {
        var logged = false
    }
}
