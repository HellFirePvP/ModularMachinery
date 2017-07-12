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
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;

import javax.annotation.Nonnull;
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
    private static Map<ResourceLocation, List<MachineRecipe>> REGISTRY_RECIPE_BY_MACHINE;
    private static Map<ResourceLocation, MachineRecipe> RECIPE_REGISTRY;

    private RecipeRegistry() {}

    public static RecipeRegistry getRegistry() {
        return INSTANCE;
    }

    @Nonnull
    public List<MachineRecipe> getRecipesFor(DynamicMachine machine) {
        List<MachineRecipe> recipes = REGISTRY_RECIPE_BY_MACHINE.get(machine.getRegistryName());
        if(recipes == null) {
            recipes = Lists.newArrayList();
            REGISTRY_RECIPE_BY_MACHINE.put(machine.getRegistryName(), recipes);
        }
        return recipes;
    }

    @Nullable
    public MachineRecipe getRecipe(ResourceLocation key) {
        return RECIPE_REGISTRY.get(key);
    }

    public void buildRegistry() {
        REGISTRY_RECIPE_BY_MACHINE = new HashMap<>();
        RECIPE_REGISTRY = new HashMap<>();
    }

    public void initializeAndLoad() {
        ProgressManager.ProgressBar barRecipes = ProgressManager.push("RecipeRegistry", 3);
        barRecipes.step("Discovering Files");

        List<File> potentialRecipes = RecipeLoader.discoverDirectory(CommonProxy.dataHolder.getRecipeDirectory());
        barRecipes.step("Loading Recipes");
        List<MachineRecipe> recipes = RecipeLoader.loadRecipes(potentialRecipes);

        Map<String, Exception> failures = RecipeLoader.captureFailedAttempts();
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading recipe!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load recipe from file " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }

        barRecipes.step("Validation and Registration");
        for (MachineRecipe mr : recipes) {
            DynamicMachine m = mr.getOwningMachine();
            if(m == null) {
                ModularMachinery.log.warn("MachineRecipe loaded for unknown machine: " + mr.getOwningMachineIdentifier() + " - responsible file: " + mr.getRecipeFilePath());
                continue;
            }
            if(RECIPE_REGISTRY.containsKey(mr.getRegistryName())) {
                MachineRecipe other = RECIPE_REGISTRY.get(mr.getRegistryName());
                if(other != null) {
                    ModularMachinery.log.warn("MachineRecipe with registryName " + mr.getRegistryName() + " already exists!");
                    ModularMachinery.log.warn("Offending files: '" + mr.getRecipeFilePath() + "' and '" + other.getRecipeFilePath() + "' !");
                    continue;
                }
            }
            RECIPE_REGISTRY.put(mr.getRegistryName(), mr);
            List<MachineRecipe> recipeList = REGISTRY_RECIPE_BY_MACHINE.get(mr.getOwningMachineIdentifier());
            if(recipeList == null) {
                recipeList = Lists.newArrayList();
            }
            recipeList.add(mr);
            REGISTRY_RECIPE_BY_MACHINE.put(mr.getOwningMachineIdentifier(), recipeList);
        }
        ProgressManager.pop(barRecipes);
    }

}
