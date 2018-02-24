/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import net.minecraft.client.Minecraft;

import java.awt.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeLayoutPart
 * Created by HellFirePvP
 * Date: 12.07.2017 / 10:59
 */
public abstract class RecipeLayoutPart {

    private final Point offset;

    protected RecipeLayoutPart(Point offset) {
        this.offset = offset;
    }

    public abstract int getComponentWidth();

    public abstract int getComponentHeight();

    public final Point getOffset() {
        return offset;
    }

    public abstract boolean canBeScaled();

    public abstract void drawBackground(Minecraft mc);

    public static class Tank extends RecipeLayoutPart {

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
        public boolean canBeScaled() {
            return true;
        }

        @Override
        public void drawBackground(Minecraft mc) {}

    }

    public static class Energy extends RecipeLayoutPart {

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
        public boolean canBeScaled() {
            return true;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_ENERGY_BACKGROUND.drawable.draw(mc, getOffset().x, getOffset().y);
        }

    }

    public static class Item extends RecipeLayoutPart {

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
        public boolean canBeScaled() {
            return false;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_INVENTORY_CELL.drawable.draw(mc, getOffset().x, getOffset().y);
        }

    }

}
