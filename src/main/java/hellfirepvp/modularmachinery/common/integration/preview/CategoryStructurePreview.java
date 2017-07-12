/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.preview;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryStructurePreview
 * Created by HellFirePvP
 * Date: 11.07.2017 / 12:36
 */
public class CategoryStructurePreview implements IRecipeCategory<StructurePreviewWrapper> {

    private final IDrawable background;
    private final String trTitle;

    public CategoryStructurePreview() {
        ResourceLocation location = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiblueprint.png");
        this.background = ModIntegrationJEI.jeiHelpers.getGuiHelper().createDrawable(location, 4, 4, 168, 136);
        this.trTitle = I18n.format("jei.category.preview");
    }

    @Override
    public String getUid() {
        return ModIntegrationJEI.CATEGORY_PREVIEW;
    }

    @Override
    public String getTitle() {
        return trTitle;
    }

    @Override
    public String getModName() {
        return ModularMachinery.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, StructurePreviewWrapper recipeWrapper, IIngredients ingredients) {}

}
