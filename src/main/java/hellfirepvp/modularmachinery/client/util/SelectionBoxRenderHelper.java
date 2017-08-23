/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import hellfirepvp.modularmachinery.common.item.ItemConstructTool;
import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: SelectionBoxRenderHelper
 * Created by HellFirePvP
 * Date: 22.08.2017 / 23:05
 */
public class SelectionBoxRenderHelper {

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if(Minecraft.getMinecraft().player == null) return;

        ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
        if(held.isEmpty()) {
            held = Minecraft.getMinecraft().player.getHeldItemOffhand();
        }
        if(!held.isEmpty() && held.getItem() instanceof ItemConstructTool) {
            PlayerStructureSelectionHelper.StructureSelection sel = PlayerStructureSelectionHelper.clientSelection;
            if(sel != null) {
                List<BlockPos> toRender = sel.getSelectedPositions().stream()
                        .filter((pos) -> pos.distanceSq(Minecraft.getMinecraft().player.getPosition()) <= 1024)
                        .collect(Collectors.toList());
                RenderingUtils.drawWhiteOutlineCubes(toRender, event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent
    public void purgeDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PlayerStructureSelectionHelper.clientSelection = null;
    }

}
