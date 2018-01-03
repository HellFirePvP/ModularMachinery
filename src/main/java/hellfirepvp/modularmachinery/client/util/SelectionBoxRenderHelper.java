/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.item.ItemConstructTool;
import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        ClientProxy.renderHelper.renderTranslucentBlocks();
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent event) {
        if(event.getEntityPlayer().equals(Minecraft.getMinecraft().player) &&
                (event instanceof PlayerInteractEvent.RightClickBlock ||
                event instanceof PlayerInteractEvent.RightClickEmpty ||
                event instanceof PlayerInteractEvent.RightClickItem)) {
            if(ClientProxy.renderHelper.placePreview()) {
                event.setCancellationResult(EnumActionResult.FAIL);
                if(event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void purgeDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PlayerStructureSelectionHelper.clientSelection = null;
        ClientProxy.renderHelper.unloadWorld();
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload unload) {
        if(unload.getWorld().isRemote) {
            ClientProxy.renderHelper.unloadWorld();
        }
    }

}
