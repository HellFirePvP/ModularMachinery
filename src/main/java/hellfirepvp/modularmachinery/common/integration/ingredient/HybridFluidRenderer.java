/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.ingredient;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.gas.GasStackRenderer;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.plugins.vanilla.ingredients.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridFluidRenderer
 * Created by HellFirePvP
 * Date: 27.08.2017 / 10:10
 */
public class HybridFluidRenderer<T extends HybridFluid> implements IIngredientRenderer<T> {

    private FluidStackRenderer fluidStackRenderer = null;
    private IIngredientRenderer gasRenderer = null;

    public HybridFluidRenderer<T> copyPrepareFluidRender(int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
        HybridFluidRenderer<T> copy = new HybridFluidRenderer<>();
        copy.fluidStackRenderer = new FluidStackRenderer(capacityMb, showCapacity, width, height, overlay);
        copy.gasRenderer = this.gasRenderer;
        return copy;
    }

    @Optional.Method(modid = "mekanism")
    public HybridFluidRenderer<T> copyPrepareGasRender(int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
        HybridFluidRenderer<T> copy = new HybridFluidRenderer<>();
        copy.gasRenderer = new GasStackRenderer(capacityMb, showCapacity, width, height, overlay);
        copy.fluidStackRenderer = this.fluidStackRenderer;
        return copy;
    }

    @Override
    public void render(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nullable T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            if(attemptRenderGas(minecraft, xPosition, yPosition, ingredient)) {
                return;
            }
        }
        IIngredientRenderer<FluidStack> fluidRenderer = fluidStackRenderer;
        if(fluidRenderer == null) {
            fluidRenderer = ModIntegrationJEI.ingredientRegistry.getIngredientRenderer(FluidStack.class);
        }
        fluidRenderer.render(minecraft, xPosition, yPosition, ingredient == null ? null : ingredient.asFluidStack());
    }

    @Optional.Method(modid = "mekanism")
    private boolean attemptRenderGas(Minecraft minecraft, int xPosition, int yPosition, @Nullable T ingredient) {
        if(ingredient == null) {
            return false;
        }
        if(ingredient instanceof HybridFluidGas) {
            redirectRenderGasStack(minecraft, xPosition, yPosition, (HybridFluidGas) ingredient);
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "mekanism")
    private void redirectRenderGasStack(Minecraft minecraft, int x, int y, @Nonnull HybridFluidGas ingredient) {
        IIngredientRenderer<GasStack> gasRenderer = this.gasRenderer;
        if(gasRenderer == null) {
            gasRenderer = ModIntegrationJEI.ingredientRegistry.getIngredientRenderer(GasStack.class);
        }
        gasRenderer.render(minecraft, x, y, ingredient.asGasStack());
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, T ingredient, ITooltipFlag tooltipFlag) {
        if(ModularMachinery.isMekanismLoaded) {
            List<String> tooltip = attemptGetTooltip(minecraft, ingredient, tooltipFlag);
            if(tooltip != null) {
                return tooltip;
            }
        }
        FluidStack f = ingredient.asFluidStack();
        if(f == null) {
            return Lists.newArrayList();
        }
        IIngredientRenderer<FluidStack> fluidStackRenderer = this.fluidStackRenderer;
        if(fluidStackRenderer == null) {
            fluidStackRenderer = ModIntegrationJEI.ingredientRegistry.getIngredientRenderer(FluidStack.class);
        }
        return fluidStackRenderer.getTooltip(minecraft, f, tooltipFlag);
    }

    @Optional.Method(modid = "mekanism")
    private List<String> attemptGetTooltip(Minecraft minecraft, T ingredient, ITooltipFlag tooltipFlag) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientRenderer<GasStack> gasStackRenderer = this.gasRenderer;
            if(gasStackRenderer == null) {
                gasStackRenderer = ModIntegrationJEI.ingredientRegistry.getIngredientRenderer(GasStack.class);
            }
            return gasStackRenderer.getTooltip(minecraft, ((HybridFluidGas) ingredient).asGasStack(), tooltipFlag);
        }
        return null;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, T ingredient) {
        return minecraft.fontRenderer;
    }

}
