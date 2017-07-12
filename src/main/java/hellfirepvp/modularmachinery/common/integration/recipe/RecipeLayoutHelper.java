/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeLayoutHelper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 15:09
 */
public class RecipeLayoutHelper {

    static final ResourceLocation LOCATION_JEI_ICONS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/jeirecipeicons.png");

    static RecipePart PART_TANK_SHELL;
    static RecipePart PART_ENERGY_BACKGROUND;
    static RecipePart PART_ENERGY_FOREGROUND;
    static RecipePart PART_INVENTORY_CELL;
    static RecipePart PART_PROCESS_ARROW;
    static RecipePart PART_PROCESS_ARROW_ACTIVE;

    public static void init() {
        if(PART_TANK_SHELL != null) return;

        PART_TANK_SHELL               = new RecipePart(LOCATION_JEI_ICONS, 0,  0, 22, 63);
        PART_ENERGY_FOREGROUND        = new RecipePart(LOCATION_JEI_ICONS, 22, 0, 22, 63);
        PART_ENERGY_BACKGROUND        = new RecipePart(LOCATION_JEI_ICONS, 44, 0, 22, 63);
        PART_INVENTORY_CELL           = new RecipePart(LOCATION_JEI_ICONS, 66, 0, 18, 18);
        PART_PROCESS_ARROW            = new RecipePart(LOCATION_JEI_ICONS, 84, 0, 22, 15);
        PART_PROCESS_ARROW_ACTIVE     = new RecipePart(LOCATION_JEI_ICONS, 84, 15, 22, 15);
    }

    public static class RecipePart {

        public final IDrawable drawable;
        public final int xSize, zSize;

        public RecipePart(ResourceLocation location, int textureX, int textureZ, int xSize, int zSize) {
            this.drawable = ModIntegrationJEI.jeiHelpers.getGuiHelper().createDrawable(location, textureX, textureZ, xSize, zSize);
            this.xSize = xSize;
            this.zSize = zSize;
        }

    }

}