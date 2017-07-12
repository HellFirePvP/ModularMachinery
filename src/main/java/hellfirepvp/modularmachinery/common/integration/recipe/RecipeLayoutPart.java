/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
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

    public abstract Rectangle getSize();

    public abstract void drawBackground(Minecraft mc);

    public static class Tank extends RecipeLayoutPart {

        private final Rectangle size;

        public Tank(Point offset) {
            this.size = new Rectangle(offset.x, offset.y, 22, 63);
        }

        @Override
        public Rectangle getSize() {
            return size;
        }

        @Override
        public void drawBackground(Minecraft mc) {}

    }

    public static class Energy extends RecipeLayoutPart {

        private final Rectangle size;

        public Energy(Point offset) {
            this.size = new Rectangle(offset.x, offset.y, 22, 63);
        }

        @Override
        public Rectangle getSize() {
            return size;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_ENERGY_BACKGROUND.drawable.draw(mc, size.x, size.y);
        }

    }

    public static class Item extends RecipeLayoutPart {

        private final Rectangle size;

        public Item(Point offset) {
            this.size = new Rectangle(offset.x, offset.y, 18, 18);
        }

        @Override
        public Rectangle getSize() {
            return size;
        }

        @Override
        public void drawBackground(Minecraft mc) {
            RecipeLayoutHelper.PART_INVENTORY_CELL.drawable.draw(mc, size.x, size.y);
        }

    }

}
