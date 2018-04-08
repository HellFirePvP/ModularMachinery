/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.plugins.vanilla.ingredients.ItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.awt.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeLayoutPart
 * Created by HellFirePvP
 * Date: 12.07.2017 / 10:59
 */
public abstract class RecipeLayoutPart<T> {

    private final Point offset;

    protected RecipeLayoutPart(Point offset) {
        this.offset = offset;
    }

    public abstract int getComponentWidth();

    public abstract int getComponentHeight();

    public final Point getOffset() {
        return offset;
    }

    public abstract Class<T> getLayoutTypeClass();

    public abstract IIngredientRenderer<T> provideIngredientRenderer();

    //Defines how many of them can be placed next to each other horizontally, before
    //a new 'line' is used for more.
    public abstract int getMaxHorizontalCount();

    public abstract int getComponentHorizontalGap();

    public abstract int getComponentVerticalGap();

    //The higher number, the more left (for inputs) and the more right (for outputs) the component is gonna appear.
    //Should be unique/final depending on component type and NOT vary between different recipe instances or components!!
    public abstract int getComponentHorizontalSortingOrder();

    @Deprecated
    public abstract boolean canBeScaled();

    public abstract void drawBackground(Minecraft mc);

    public abstract void drawForeground(Minecraft mc, T obj);

    public static class Tank extends RecipeLayoutPart<HybridFluid> {

        public Tank(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentHeight() {
            return 63;
        }

        @Override
        public int getComponentWidth() {
            return 22;
        }

        @Override
        public Class<HybridFluid> getLayoutTypeClass() {
            return HybridFluid.class;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 4;
        }

        @Override
        public int getComponentVerticalGap() {
            return 4;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 2;
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 100;
        }

        @Override
        public boolean canBeScaled() {
            return true;
        }

        @Override
        public IIngredientRenderer<HybridFluid> provideIngredientRenderer() {
            HybridFluidRenderer<HybridFluid> copy = new HybridFluidRenderer<>().
                    copyPrepareFluidRender(
                            getComponentWidth(),
                            getComponentHeight(),
                            1000,
                            false,
                            RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            if(Loader.isModLoaded("mekanism")) {
                copy = addGasRenderer(copy);
            }
            return copy;
        }

        @Optional.Method(modid = "mekanism")
        private HybridFluidRenderer<HybridFluid> addGasRenderer(HybridFluidRenderer<HybridFluid> copy) {
            return copy.copyPrepareGasRender(
                    getComponentWidth(),
                    getComponentHeight(),
                    1000,
                    false,
                    RecipeLayoutHelper.PART_TANK_SHELL.drawable);
        }

        @Override
        public void drawBackground(Minecraft mc) {}

        //JEI draws fluids and gases for us
        @Override
        public void drawForeground(Minecraft mc, HybridFluid obj) {}

    }

    public static class Energy extends RecipeLayoutPart<Long> {

        public Energy(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentWidth() {
            return 22;
        }

        @Override
        public int getComponentHeight() {
            return 63;
        }

        @Override
        public Class<Long> getLayoutTypeClass() {
            return Long.class;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 1;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 0;
        }

        @Override
        public int getComponentVerticalGap() {
            return 4;
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 1000;
        }

        @Override
        public boolean canBeScaled() {
            return true;
        }

        @Override
        public IIngredientRenderer<Long> provideIngredientRenderer() {
            throw new UnsupportedOperationException("Cannot provide Energy ingredientrenderer as this is no ingredient!");
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_ENERGY_BACKGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
        }

        @Override
        public void drawForeground(Minecraft mc, Long obj) {
            if(obj > 0) {
                RecipeLayoutHelper.PART_ENERGY_FOREGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
            }
        }

    }

    public static class Item extends RecipeLayoutPart<ItemStack> {

        public Item(Point offset) {
            super(offset);
        }

        @Override
        public int getComponentHeight() {
            return 18;
        }

        @Override
        public int getComponentWidth() {
            return 18;
        }

        @Override
        public Class<ItemStack> getLayoutTypeClass() {
            return ItemStack.class;
        }

        @Override
        public int getMaxHorizontalCount() {
            return 3;
        }

        @Override
        public int getComponentVerticalGap() {
            return 0;
        }

        @Override
        public int getComponentHorizontalGap() {
            return 0;
        }

        @Override
        public int getComponentHorizontalSortingOrder() {
            return 10;
        }

        @Override
        public boolean canBeScaled() {
            return false;
        }

        @Override
        public IIngredientRenderer<ItemStack> provideIngredientRenderer() {
            return new ItemStackRenderer();
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_INVENTORY_CELL.drawable.draw(mc, getOffset().x, getOffset().y);
        }

        //JEI draws itemstacks as inputs for us
        @Override
        public void drawForeground(Minecraft mc, ItemStack obj) {}
    }

}
