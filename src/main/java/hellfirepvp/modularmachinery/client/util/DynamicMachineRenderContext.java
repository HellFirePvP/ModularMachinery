/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicMachineRenderContext
 * Created by HellFirePvP
 * Date: 09.07.2017 / 21:18
 */
public class DynamicMachineRenderContext {

    private final DynamicMachine machine;
    private final BlockArrayRenderHelper render;

    private boolean render3D = true;
    private int renderSlice = 0;
    private float scale = 1F;

    private long shiftSnap = -1;

    private DynamicMachineRenderContext(DynamicMachine machine) {
        this.machine = machine;
        BlockArray copy = new BlockArray(machine.getPattern());
        copy.getPattern().put(BlockPos.ORIGIN,
                new BlockArray.BlockInformation(
                        Lists.newArrayList(
                                new BlockArray.IBlockStateDescriptor(
                                        BlocksMM.blockController.getDefaultState()))));
        this.render = new BlockArrayRenderHelper(copy);
    }

    BlockArrayRenderHelper getRender() {
        return render;
    }

    public long getShiftSnap() {
        return shiftSnap;
    }

    public void snapSamples() {
        this.shiftSnap = ClientScheduler.getClientTick();
    }

    public void releaseSamples() {
        this.shiftSnap = -1;
    }

    public void resetRender() {
        setTo2D();
        setTo3D();
    }

    public void setTo2D() {
        if(!render3D) return;
        render3D = false;
        renderSlice = render.getBlocks().getMin().getY();
        render.resetRotation2D();
        scale = 1F;
    }

    public void setTo3D() {
        if(render3D) return;
        render3D = true;
        renderSlice = 0;
        render.resetRotation();
        scale = 1F;
    }

    public float getScale() {
        return scale;
    }

    public Vec3d getCurrentMachineTranslate() {
        if(render3D) {
            return new Vec3d(0, 0, 0);
        }
        return this.render.getCurrentTranslation();
    }

    public Vec2f getCurrentRenderOffset(float x, float z) {
        Minecraft mc = Minecraft.getMinecraft();
        double sc = new ScaledResolution(mc).getScaleFactor();
        double oX = x + 16D / sc;
        double oZ = z + 16D / sc;
        Vec3d tr = getCurrentMachineTranslate();
        return new Vec2f((float) (oX + tr.x), (float) (oZ + tr.z));
    }

    public void zoomOut() {
        scale *= 0.85F;
    }

    public void zoomIn() {
        scale *= 1.15F;
    }

    public boolean doesRenderIn3D() {
        return render3D;
    }

    public int getRenderSlice() {
        return renderSlice;
    }

    public boolean hasSliceDown() {
        return render.getBlocks().getMin().getY() <= renderSlice - 1;
    }

    public boolean hasSliceUp() {
        return render.getBlocks().getMax().getY() >= renderSlice + 1;
    }

    public void sliceUp() {
        if(hasSliceUp()) {
            renderSlice++;
        }
    }

    public void sliceDown() {
        if(hasSliceDown()) {
            renderSlice--;
        }
    }

    public DynamicMachine getDisplayedMachine() {
        return machine;
    }

    @SideOnly(Side.CLIENT)
    public List<ItemStack> getDescriptiveStacks() {
        return this.getDisplayedMachine().getPattern().getAsDescriptiveStacks(shiftSnap == -1 ? Optional.empty() : Optional.of(shiftSnap));
    }

    public static DynamicMachineRenderContext createContext(DynamicMachine machine) {
        return new DynamicMachineRenderContext(machine);
    }

    public void renderAt(int x, int z) {
        renderAt(x, z, 1F);
    }

    public void renderAt(int x, int z, float partialTicks) {
        render.sampleSnap = shiftSnap;
        if(render3D) {
            render.render3DGUI(x, z, scale, partialTicks);
        } else {
            render.render3DGUI(x, z, scale, partialTicks, Optional.of(renderSlice));
        }
    }

    public void rotateRender(double x, double y, double z) {
        this.render.rotate(x, y, z);
    }

    public void moveRender(double x, double y, double z) {
        this.render.translate(x, y, z);
    }

}
