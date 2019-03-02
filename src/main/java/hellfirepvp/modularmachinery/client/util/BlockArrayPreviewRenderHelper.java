/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArrayPreviewRenderHelper
 * Created by HellFirePvP
 * Date: 30.12.2017 / 14:38
 */
public class BlockArrayPreviewRenderHelper {

    private BlockArrayRenderHelper renderHelper = null;
    private BlockPos attachedPosition = null;
    private DynamicMachine machine = null;

    private static int hash = -1;
    private static int batchDList = -1;

    public boolean startPreview(DynamicMachineRenderContext currentContext) {
        if(currentContext.getShiftSnap() != -1) {
            this.renderHelper = currentContext.getRender();
            this.renderHelper.sampleSnap = currentContext.getShiftSnap(); //Just for good measure
            this.machine = currentContext.getDisplayedMachine();
            this.attachedPosition = null;
            if(Minecraft.getMinecraft().player != null) {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("gui.blueprint.popout.place"));
            }
            return true;
        }
        return false;
    }

    public boolean placePreview() {
        EntityPlayer player = Minecraft.getMinecraft().player;

        if(player != null && this.renderHelper != null && this.attachedPosition == null) {
            RayTraceResult lookBlock = getLookBlock(player, false, true, 20);
            if(lookBlock != null) {
                attachedPosition = lookBlock.getBlockPos();
                return true;
            }
        }
        return false;
    }

    public void tick() {
        if(attachedPosition != null) {
            if(Minecraft.getMinecraft().player != null &&
                    Minecraft.getMinecraft().player.getDistanceSqToCenter(attachedPosition) >= 1024) {
                clearSelection();
            }
            if(Minecraft.getMinecraft().world != null &&
                    renderHelper != null &&
                    renderHelper.getBlocks().matches(Minecraft.getMinecraft().world, this.attachedPosition, true, this.machine.getModifiersAsMatchingReplacements())) {
                clearSelection();
            }
        }
    }

    void renderTranslucentBlocks() {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();

        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        Entity rView = Minecraft.getMinecraft().getRenderViewEntity();
        if(rView == null) rView = Minecraft.getMinecraft().player;
        Entity entity = rView;
        double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * partialTicks);
        double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * partialTicks);
        double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * partialTicks);
        GlStateManager.translate(-tx, -ty, -tz);

        GlStateManager.color(1F, 1F, 1F, 1F);

        if(batchDList == -1) {
            batchBlocks();
            hash = hashBlocks();
        } else {
            int currentHash = hashBlocks();
            if(hash != currentHash) {
                GLAllocation.deleteDisplayLists(batchDList);
                batchBlocks();
                hash = currentHash;
            }
        }
        GlStateManager.enableBlend();
        Blending.ALPHA.applyStateManager();
        GlStateManager.callList(batchDList);
        Blending.DEFAULT.applyStateManager();
        GlStateManager.popMatrix();

        //Color desync on block rendering - prevent that, resync
        GlStateManager.color(1F, 1F, 1F, 1F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private int hashBlocks() {
        int hash = 80238287;
        if(this.renderHelper != null && Minecraft.getMinecraft().player != null) {
            Vec3i move = this.attachedPosition;
            if(move == null) {
                RayTraceResult res = getLookBlock(Minecraft.getMinecraft().player, false, true, 20);
                if(res != null) {
                    move = res.getBlockPos();
                }
            }
            if(move != null) {
                BlockArray render = new BlockArray(renderHelper.getBlocks(), move);
                for (Map.Entry<BlockPos, BlockArray.BlockInformation> entry : render.getPattern().entrySet()) {
                    if(Minecraft.getMinecraft().world != null &&
                            entry.getValue().matches(Minecraft.getMinecraft().world, entry.getKey(), false)) {
                        continue;
                    }
                    hash = (hash << 4) ^ (hash >> 28) ^ (entry.getKey().getX() * 5449 % 130651);
                    hash = (hash << 4) ^ (hash >> 28) ^ (entry.getKey().getY() * 5449 % 130651);
                    hash = (hash << 4) ^ (hash >> 28) ^ (entry.getKey().getZ() * 5449 % 130651);
                    hash = (hash << 4) ^ (hash >> 28) ^ (entry.getValue().getSampleState(Optional.of(renderHelper.sampleSnap)).hashCode() * 5449 % 130651);
                }
            }
        }
        return hash % 75327403;
    }

    private void batchBlocks() {
        Vec3i move = this.attachedPosition;
        if(move == null) {
            RayTraceResult res = getLookBlock(Minecraft.getMinecraft().player, false, true, 20);
            if(res != null) {
                move = res.getBlockPos();
            }
        }
        if(move == null || this.renderHelper == null) {
            batchDList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(batchDList, GL11.GL_COMPILE);
            GlStateManager.glEndList();
            return;
        }
        batchDList = GLAllocation.generateDisplayLists(1);
        GlStateManager.enableBlend();
        Blending.DEFAULT.applyStateManager();
        GlStateManager.glNewList(batchDList, GL11.GL_COMPILE);
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();

        BlockArrayRenderHelper.WorldBlockArrayRenderAccess access = renderHelper.getRenderAccess().move(renderHelper, move);
        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        VertexFormat blockFormat = DefaultVertexFormats.BLOCK;

        vb.begin(GL11.GL_QUADS, blockFormat);
        for (Map.Entry<BlockPos, BlockArrayRenderHelper.BakedBlockData> data : access.blockRenderData.entrySet()) {
            BlockPos offset = data.getKey();
            BlockArrayRenderHelper.BakedBlockData renderData = data.getValue();
            BlockArrayRenderHelper.SampleRenderState state = renderData.getSampleState();

            if(Minecraft.getMinecraft().world != null &&
                    renderHelper.getBlocks().getPattern().get(offset.subtract(move)).matches(Minecraft.getMinecraft().world, offset, false)) {
                continue;
            }
            if(state.state.getBlock() != Blocks.AIR) {
                BlockArrayRenderHelper.TileEntityRenderData terd = state.renderData;
                if(terd != null && terd.tileEntity != null) {
                    terd.tileEntity.setWorld(Minecraft.getMinecraft().world);
                    terd.tileEntity.setPos(offset);
                }
                IBlockState actRenderState = state.state;
                actRenderState = actRenderState.getBlock().getActualState(actRenderState, access, offset);
                brd.renderBlock(actRenderState, offset, access, vb);
            }
        }
        vb.sortVertexData(
                (float) TileEntityRendererDispatcher.staticPlayerX,
                (float) TileEntityRendererDispatcher.staticPlayerY,
                (float) TileEntityRendererDispatcher.staticPlayerZ);
        tes.draw();
        GlStateManager.glEndList();
        Blending.DEFAULT.applyStateManager();
    }

    @Nullable
    private RayTraceResult getLookBlock(Entity e, boolean stopTraceOnLiquids, boolean ignoreBlockWithoutBoundingBox, double range) {
        float pitch = e.rotationPitch;
        float yaw = e.rotationYaw;
        Vec3d entityVec = new Vec3d(e.posX, e.posY + e.getEyeHeight(), e.posZ);
        float f2 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-pitch * 0.017453292F);
        float f5 = MathHelper.sin(-pitch * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3d vec3d1 = entityVec.addVector((double) f6 * range, (double) f5 * range, (double) f7 * range);
        RayTraceResult rtr = e.getEntityWorld().rayTraceBlocks(entityVec, vec3d1, stopTraceOnLiquids, ignoreBlockWithoutBoundingBox, false);
        if (rtr == null || rtr.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }
        return rtr;
    }

    private void clearSelection() {
        this.renderHelper = null;
        this.attachedPosition = null;
        this.machine = null;
    }

    public void unloadWorld() {
        clearSelection();
    }

}
