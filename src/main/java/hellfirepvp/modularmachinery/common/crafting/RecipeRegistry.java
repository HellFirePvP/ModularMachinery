/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeRegistry
 * Created by HellFirePvP
 * Date: 27.06.2017 / 23:21
 */
public class RecipeRegistry {

    private static RecipeRegistry INSTANCE = new RecipeRegistry();
    private static Map<ResourceLocation, List<MachineRecipe>> REGISTRY_RECIPE;

    private RecipeRegistry() {}

    public static RecipeRegistry getRegistry() {
        return INSTANCE;
    }

    @Nullable
    public List<MachineRecipe> getRecipesFor(DynamicMachine machine) {
        return REGISTRY_RECIPE.get(machine.getRegistryName());
    }

    public void buildRegistry() {
        REGISTRY_RECIPE = new HashMap<>();
    }

    public void initializeAndLoad() {
        List<File> potentialRecipes = RecipeLoader.discoverDirectory(CommonProxy.dataHolder.getRecipeDirectory());
        List<MachineRecipe> recipes = RecipeLoader.loadRecipes(potentialRecipes);

        Map<String, Exception> failures = RecipeLoader.captureFailedAttempts();
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading recipes!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load recipe from file " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }

        for (MachineRecipe mr : recipes) {
            DynamicMachine m = mr.getOwningMachine();
            if(m == null) {
                ModularMachinery.log.warn("MachineRecipe loaded for unknown machine: " + mr.getOwningMachineIdentifier() + " - responsible file: " + mr.getRecipeFilePath());
                continue;
            }
            List<MachineRecipe> recipeList = REGISTRY_RECIPE.get(mr.getOwningMachineIdentifier());
            if(recipeList == null) {
                recipeList = Lists.newArrayList();
                REGISTRY_RECIPE.put(mr.getOwningMachineIdentifier(), recipeList);
            }
            recipeList.add(mr);
        }
    }

}
