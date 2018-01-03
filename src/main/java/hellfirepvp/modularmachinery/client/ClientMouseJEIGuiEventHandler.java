/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.preview.StructurePreviewWrapper;
import mezz.jei.api.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientMouseJEIGuiEventHandler
 * Created by HellFirePvP
 * Date: 14.07.2017 / 22:47
 */
@SideOnly(Side.CLIENT)
public class ClientMouseJEIGuiEventHandler {

    private static final Field glMouseEventDWheelField;
    public static int eventDWheelState;

    @SubscribeEvent
    public void onMouseEventPre(GuiScreenEvent.MouseInputEvent.Pre preMouse) {
        if(glMouseEventDWheelField == null) return;

        GuiScreen cScreen = preMouse.getGui();
        if(cScreen != null && cScreen instanceof IRecipesGui && System.currentTimeMillis() - StructurePreviewWrapper.lastRenderMs <= 200) {
            try {
                eventDWheelState = Mouse.getEventDWheel();
                glMouseEventDWheelField.set(null, 0);
            } catch (Exception e) {}
        }
    }

    @SubscribeEvent
    public void onMouseEventPost(GuiScreenEvent.MouseInputEvent.Post postMouse) {
        if(glMouseEventDWheelField == null) return;

        if(eventDWheelState != 0) {
            try {
                glMouseEventDWheelField.set(null, eventDWheelState);
            } catch (Exception e) {}
        }
    }

    static {
        Field f = null;
        try {
            f = Mouse.class.getDeclaredField("event_dwheel");
            f.setAccessible(true);
        } catch (Exception exc) {
            ModularMachinery.log.error("Couldn't find mouseWheelEvent field in current GL context! Scrolling/Zooming in JEI might be problematic!");
            exc.printStackTrace();
        }
        glMouseEventDWheelField = f;
    }

}
