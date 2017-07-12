/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArrayRenderHelper
 * Created by HellFirePvP
 * Date: 09.07.2017 / 20:16
 */
public class BlockArrayRenderHelper {

    private BlockArray blocks;
    private WorldBlockArrayRenderAccess renderAccess;
    private double rotX, rotY, rotZ;
    private double sliceTrX, sliceTrY, sliceTrZ;

    BlockArrayRenderHelper(BlockArray blocks) {
        this.blocks = blocks;
        this.renderAccess = new WorldBlockArrayRenderAccess(blocks);
        resetRotation();
    }

    void resetRotation() {
        this.rotX = -30;
        this.rotY = 45;
        this.rotZ = 0;
        this.sliceTrX = 0;
        this.sliceTrY = 0;
        this.sliceTrZ = 0;
    }

    void resetRotation2D() {
        this.rotX = -90;
        this.rotY = 0;
        this.rotZ = 0;
        this.sliceTrX = 0;
        this.sliceTrY = 0;
        this.sliceTrZ = 0;
    }

    BlockArray getBlocks() {
        return blocks;
    }

    public void translate(double x, double y, double z) {
        this.sliceTrX += x;
        this.sliceTrY += y;
        this.sliceTrZ += z;
    }

    public void rotate(double x, double y, double z) {
        this.rotX += x;
        this.rotY += y;
        this.rotZ += z;
    }

    public void render3DGUI(double x, double y, float scaleMultiplier, float pTicks) {
        render3DGUI(x, y, scaleMultiplier, pTicks, Optional.empty());
    }

    public void render3DGUI(double x, double y, float scaleMultiplier, float pTicks, Optional<Integer> slice) {
        GuiScreen scr = Minecraft.getMinecraft().currentScreen;
        if(scr == null) return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        double sc = new ScaledResolution(mc).getScaleFactor();
        GL11.glTranslated(x + 16D / sc, y + 16D / sc, 512);

        double mul = scaleMultiplier * 7;

        double size = 2;
        double minSize = 0.5;

        Vec3i max = blocks.getMax();
        Vec3i min = blocks.getMin();

        double maxLength = 0;
        double pointDst = max.getX() - min.getX();
        if(pointDst > maxLength) maxLength = pointDst;
        pointDst = max.getY() - min.getY();
        if(pointDst > maxLength) maxLength = pointDst;
        pointDst = max.getZ() - min.getZ();
        if(pointDst > maxLength) maxLength = pointDst;
        maxLength -= 5;

        if(maxLength > 0) {
            size = (size - minSize) * (1D - (maxLength / 20D));
        }

        double dr = -5.75*size;
        GL11.glTranslated(dr, dr, dr);
        GL11.glRotated(rotX, 1, 0, 0);
        GL11.glRotated(rotY, 0, 1, 0);
        GL11.glRotated(rotZ, 0, 0, 1);
        GL11.glTranslated(-dr, -dr, -dr);

        GL11.glTranslated(sliceTrX, sliceTrY, sliceTrZ);

        GL11.glScaled(-size*mul, -size*mul, -size*mul);

        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        VertexFormat blockFormat = DefaultVertexFormats.BLOCK;

        renderAccess.respectRenderSlice = slice.isPresent();
        renderAccess.currentRenderSlice = slice.orElse(0);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();

        vb.begin(GL11.GL_QUADS, blockFormat);
        for (Map.Entry<BlockPos, BakedBlockData> data : renderAccess.blockRenderData.entrySet()) {
            BlockPos offset = data.getKey();
            if(slice.isPresent()) {
                if(slice.get() != offset.getY()) {
                    continue;
                }
            }
            SampleRenderState state = data.getValue().getSampleState();
            if(state.state.getBlock() != Blocks.AIR) {
                brd.renderBlock(state.state, offset, renderAccess, vb);
            }
        }
        tes.draw();

        for (Map.Entry<BlockPos, BakedBlockData> data : renderAccess.blockRenderData.entrySet()) {
            BlockPos offset = data.getKey();
            if(slice.isPresent()) {
                if(slice.get() != offset.getY()) {
                    continue;
                }
            }
            SampleRenderState state = data.getValue().getSampleState();
            TileEntityRenderData terd = state.renderData;
            if(terd != null && terd.tileEntity != null && terd.renderer != null) {
                terd.tileEntity.setWorld(Minecraft.getMinecraft().world);
                terd.renderer.render(terd.tileEntity, offset.getX(), offset.getY(), offset.getZ(), pTicks, 0, 1F);
            }
        }

        renderAccess.respectRenderSlice = false;
        renderAccess.currentRenderSlice = 0;

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static class BakedBlockData {

        private List<SampleRenderState> renderStates = Lists.newArrayList();

        private BakedBlockData(List<BlockArray.IBlockStateDescriptor> states) {
            for (BlockArray.IBlockStateDescriptor desc : states) {
                for (IBlockState state : desc.applicable) {
                    renderStates.add(new SampleRenderState(state));
                }
            }
        }

        private SampleRenderState getSampleState() {
            int tickSpeed = BlockArray.BlockInformation.CYCLE_TICK_SPEED;
            if(renderStates.size() > 10) {
                tickSpeed *= 0.6;
            }
            if(renderStates.size() > 20) {
                tickSpeed *= 0.6;
            }
            int p = (int) (ClientScheduler.getClientTick() / tickSpeed);
            int part = p % renderStates.size();
            return renderStates.get(part);
        }

    }

    private static class SampleRenderState {

        private IBlockState state;
        private TileEntityRenderData renderData;

        private SampleRenderState(IBlockState state) {
            this.state = state;
            if(state.getBlock().hasTileEntity(state)) {
                TileEntity te = state.getBlock().createTileEntity(Minecraft.getMinecraft().world, state);
                renderData = new TileEntityRenderData(te);
            } else {
                renderData = null;
            }
        }
    }

    private static class TileEntityRenderData {

        private TileEntity tileEntity;
        private TileEntitySpecialRenderer<TileEntity> renderer;

        private TileEntityRenderData(TileEntity tileEntity) {
            this.tileEntity = tileEntity;
            this.renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
        }
    }

    public static class WorldBlockArrayRenderAccess implements IBlockAccess {

        private Map<BlockPos, BakedBlockData> blockRenderData = new HashMap<>();
        private int currentRenderSlice = 0;
        private boolean respectRenderSlice = false;

        private WorldBlockArrayRenderAccess(BlockArray array) {
            for (Map.Entry<BlockPos, BlockArray.BlockInformation> entry : array.getPattern().entrySet()) {
                BlockPos offset = entry.getKey();
                BlockArray.BlockInformation info = entry.getValue();
                blockRenderData.put(offset, new BakedBlockData(info.matchingStates));
            }
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return blockRenderData.containsKey(pos) ? blockRenderData.get(pos).getSampleState().renderData.tileEntity : null;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return 0;
        }

        @Nonnull
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return isInBounds(pos) ? blockRenderData.get(pos).getSampleState().state : Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return !isInBounds(pos) || blockRenderData.get(pos).getSampleState().state.getBlock() == Blocks.AIR;
        }

        @Nonnull
        @Override
        @SideOnly(Side.CLIENT)
        public Biome getBiome(BlockPos pos) {
            return Biomes.PLAINS;
        }

        private boolean isInBounds(BlockPos pos) {
            if(respectRenderSlice) {
                if(pos.getY() != currentRenderSlice) {
                    return false;
                }
            }
            return blockRenderData.containsKey(pos);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        @Nonnull
        @Override
        @SideOnly(Side.CLIENT)
        public WorldType getWorldType() {
            return Minecraft.getMinecraft().world.getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return isInBounds(pos) ? getBlockState(pos).isSideSolid(this, pos, side) : _default;
        }

    }

}
