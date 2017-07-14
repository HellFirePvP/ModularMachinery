/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.integration.preview.CategoryStructurePreview;
import hellfirepvp.modularmachinery.common.integration.preview.StructurePreviewWrapper;
import hellfirepvp.modularmachinery.common.integration.recipe.CategoryDynamicRecipe;
import hellfirepvp.modularmachinery.common.integration.recipe.DynamicRecipeWrapper;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutHelper;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModIntegrationJEI
 * Created by HellFirePvP
 * Date: 10.07.2017 / 19:22
 */
@JEIPlugin
public class ModIntegrationJEI implements IModPlugin {

    public static final String CATEGORY_PREVIEW = "modularmachinery.preview";
    private static Map<DynamicMachine, CategoryDynamicRecipe> recipeCategories = new HashMap<>();

    public static IStackHelper stackHelper;
    public static IJeiHelpers jeiHelpers;
    public static IRecipeRegistry recipeRegistry;

    public static String getCategoryStringFor(DynamicMachine machine) {
        return "modularmachinery.recipes." + machine.getRegistryName().getResourcePath();
    }

    public static CategoryDynamicRecipe getCategory(DynamicMachine machine) {
        return recipeCategories.get(machine);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
        RecipeLayoutHelper.init();

        registry.addRecipeCategories(new CategoryStructurePreview());

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            CategoryDynamicRecipe recipe = new CategoryDynamicRecipe(machine);
            recipeCategories.put(machine, recipe);
            registry.addRecipeCategories(recipe);
        }
    }

    @Override
    public void register(IModRegistry registry) {
        jeiHelpers = registry.getJeiHelpers();
        RecipeLayoutHelper.init();

        registry.addRecipeCatalyst(new ItemStack(BlocksMM.blockController), CATEGORY_PREVIEW);
        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            ItemStack stack = new ItemStack(ItemsMM.blueprint);
            ItemBlueprint.setAssociatedMachine(stack, machine);
            registry.addRecipeCatalyst(stack, getCategoryStringFor(machine));
        }

        List<StructurePreviewWrapper> previews = Lists.newArrayList();
        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            previews.add(new StructurePreviewWrapper(machine));
        }
        registry.addRecipes(previews, CATEGORY_PREVIEW);

        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            List<MachineRecipe> recipes = RecipeRegistry.getRegistry().getRecipesFor(machine);
            List<DynamicRecipeWrapper> recipeWrappers = new ArrayList<>(recipes.size());
            for (MachineRecipe recipe : recipes) {
                recipeWrappers.add(new DynamicRecipeWrapper(recipe));
            }
            registry.addRecipes(recipeWrappers, getCategoryStringFor(machine));
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        recipeRegistry = jeiRuntime.getRecipeRegistry();
    }

}
