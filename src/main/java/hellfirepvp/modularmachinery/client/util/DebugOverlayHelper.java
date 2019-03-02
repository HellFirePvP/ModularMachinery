/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DebugOverlayHelper
 * Created by HellFirePvP
 * Date: 15.08.2017 / 15:20
 */
public class DebugOverlayHelper {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTextOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.gameSettings.showDebugInfo && !event.getRight().isEmpty()) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && mc.objectMouseOver.getBlockPos() != null) {
                BlockPos pos = mc.objectMouseOver.getBlockPos();
                IBlockState state = mc.world.getBlockState(pos);
                try {
                    int meta = state.getBlock().getMetaFromState(state);
                    event.getRight().add("");
                    event.getRight().add("serialized as metadata: " + meta);
                } catch (Exception ignored) {}
            }
        }
    }

}
