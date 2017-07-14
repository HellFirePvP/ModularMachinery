/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

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

    public void zoomOut() {
        scale *= 0.95F;
    }

    public void zoomIn() {
        scale *= 1.05F;
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

    public List<ItemStack> getDescriptiveStacks() {
        return this.getDisplayedMachine().getPattern().getAsDescriptiveStacks();
    }

    public static DynamicMachineRenderContext createContext(DynamicMachine machine) {
        return new DynamicMachineRenderContext(machine);
    }

    public void renderAt(int x, int z) {
        renderAt(x, z, 1F);
    }

    public void renderAt(int x, int z, float partialTicks) {
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
