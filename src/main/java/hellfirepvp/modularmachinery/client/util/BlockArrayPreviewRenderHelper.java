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
import net.minecraft.world.World;
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
    private Vec3i renderHelperOffset = null;
    private DynamicMachine machine = null;

    private BlockPos attachedPosition = null;

    private int renderedLayer = -1;

    private static int hash = -1;
    private static int batchDList = -1;

    public boolean startPreview(DynamicMachineRenderContext currentContext) {
        if(currentContext.getShiftSnap() != -1) {
            this.renderHelper = currentContext.getRender();
            this.renderHelper.sampleSnap = currentContext.getShiftSnap(); //Just for good measure
            this.renderHelperOffset = currentContext.getMoveOffset();
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
            if(lookBlock != null && lookBlock.typeOfHit == RayTraceResult.Type.BLOCK) {
                attachedPosition = lookBlock.getBlockPos().offset(lookBlock.sideHit);
                updateLayers();
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

            if(Minecraft.getMinecraft().world != null && renderHelper != null) {
                if (hasLowerLayer() && !doesPlacedLayerMatch(this.renderedLayer - 1)) {
                    updateLayers();
                } else if (doesPlacedLayerMatch(this.renderedLayer)) {
                    if (!renderHelper.getBlocks().matches(Minecraft.getMinecraft().world, this.attachedPosition, true, this.machine.getModifiersAsMatchingReplacements())) {
                        updateLayers();
                    } else {
                        clearSelection();
                    }
                }
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
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        Blending.ALPHA.applyStateManager();
        GlStateManager.callList(batchDList);
        Blending.DEFAULT.applyStateManager();
        GlStateManager.enableDepth();

        GlStateManager.popMatrix();

        //Color desync on block rendering - prevent that, resync
        GlStateManager.color(1F, 1F, 1F, 1F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private int hashBlocks() {
        int hash = 80238287;
        if(this.renderHelper != null && Minecraft.getMinecraft().player != null) {
            Vec3i move = getRenderOffset();
            if(move != null) {
                BlockArray render = new BlockArray(renderHelper.getBlocks(), move);
                for (Map.Entry<BlockPos, BlockArray.BlockInformation> entry : render.getPattern().entrySet()) {
                    if(Minecraft.getMinecraft().world != null && entry.getValue().matches(Minecraft.getMinecraft().world, entry.getKey(), false)) {
                        continue;
                    }
                    int layer = entry.getKey().subtract(move).getY();
                    if (this.attachedPosition != null && this.renderedLayer != layer) {
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
        Vec3i move = getRenderOffset();
        if(move == null || this.renderHelper == null) {
            batchDList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(batchDList, GL11.GL_COMPILE);
            GlStateManager.glEndList();
            return;
        }
        batchDList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(batchDList, GL11.GL_COMPILE);
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();

        BlockArrayRenderHelper.WorldBlockArrayRenderAccess access = renderHelper.getRenderAccess().move(renderHelper, move);
        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        VertexFormat blockFormat = DefaultVertexFormats.BLOCK;

        for (Map.Entry<BlockPos, BlockArrayRenderHelper.BakedBlockData> data : access.blockRenderData.entrySet()) {
            BlockPos offset = data.getKey();
            int layer = offset.subtract(move).getY();
            if (this.attachedPosition != null && this.renderedLayer != layer) {
                continue;
            }

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
                GlStateManager.pushMatrix();
                GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());
                GlStateManager.translate(0.125, 0.125, 0.125);
                GlStateManager.scale(0.75, 0.75, 0.75);
                vb.begin(GL11.GL_QUADS, blockFormat);
                brd.renderBlock(actRenderState, BlockPos.ORIGIN, access, vb);
                tes.draw();
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.glEndList();
    }

    private Vec3i getRenderOffset() {
        Vec3i move = this.attachedPosition;
        if(move == null) {
            RayTraceResult res = getLookBlock(Minecraft.getMinecraft().player, false, true, 20);
            if(res != null && res.typeOfHit == RayTraceResult.Type.BLOCK) {
                move = res.getBlockPos().offset(res.sideHit);
            }
        }
        return move;
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

    private boolean doesPlacedLayerMatch(int slice) {
        if (this.attachedPosition != null) {
            BlockArray matchingArray = this.renderHelper.getBlocks();
            World world = Minecraft.getMinecraft().world;
            if (world != null) {
                DynamicMachine.ModifierReplacementMap replacements = this.machine.getModifiersAsMatchingReplacements();
                Map<BlockPos, BlockArray.BlockInformation> patternSlice = matchingArray.getPatternSlice(slice);
                lblMatching:
                for (Map.Entry<BlockPos, BlockArray.BlockInformation> data : patternSlice.entrySet()) {
                    BlockPos offset = data.getKey();
                    BlockPos actualPosition = offset.add(this.attachedPosition);
                    BlockArray.BlockInformation info = matchingArray.getPattern().get(offset);
                    if (info.matches(world, actualPosition, false)) {
                        continue;
                    }
                    if(replacements.containsKey(offset)) {
                        for (BlockArray.BlockInformation bi : replacements.get(offset)) {
                            if (bi.matches(world, actualPosition, false)) {
                                continue lblMatching;
                            }
                        }
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean hasLowerLayer() {
        if (this.attachedPosition != null) {
            return (this.renderHelper.getBlocks().getMin().getY() + this.renderHelperOffset.getY()) <= this.renderedLayer - 1;
        }
        return false;
    }

    private void updateLayers() {
        this.renderedLayer = -1;
        if (this.attachedPosition != null) {
            BlockArray matchingArray = this.renderHelper.getBlocks();
            int lowestSlice = matchingArray.getMin().getY() + this.renderHelperOffset.getY();
            int maxSlice = matchingArray.getMax().getY() + this.renderHelperOffset.getY();
            for (int y = lowestSlice; y <= maxSlice; y++) {
                if (!doesPlacedLayerMatch(y)) {
                    this.renderedLayer = y;
                    return;
                }
            }
        }
    }

    private void clearSelection() {
        this.renderHelper = null;
        this.renderHelperOffset = null;
        this.attachedPosition = null;
        this.machine = null;
    }

    public void unloadWorld() {
        clearSelection();
    }

}
