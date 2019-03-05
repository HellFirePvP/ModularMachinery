/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapterAccessor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.data.DataLoadProfiler;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.ProgressManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import java.io.File;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeRegistry
 * Created by HellFirePvP
 * Date: 27.06.2017 / 23:21
 */
public class RecipeRegistry {

    private static RecipeRegistry INSTANCE = new RecipeRegistry();
    private static Map<ResourceLocation, TreeMap<Integer, TreeSet<MachineRecipe>>> REGISTRY_RECIPE_BY_MACHINE;
    private static Map<ResourceLocation, MachineRecipe> RECIPE_REGISTRY;

    private List<PreparedRecipe> earlyRecipes = new LinkedList<>();

    private RecipeRegistry() {}

    public static RecipeRegistry getRegistry() {
        return INSTANCE;
    }

    @Nonnull
    public Iterable<MachineRecipe> getRecipesFor(DynamicMachine machine) {
        TreeMap<Integer, TreeSet<MachineRecipe>> recipes = REGISTRY_RECIPE_BY_MACHINE.get(machine.getRegistryName());
        if(recipes == null) {
            return Lists.newArrayList();
        }
        return Iterables.concat(recipes.values());
    }

    @Nullable
    public MachineRecipe getRecipe(ResourceLocation key) {
        return RECIPE_REGISTRY.get(key);
    }

    public void buildRegistry() {
        REGISTRY_RECIPE_BY_MACHINE = new HashMap<>();
        RECIPE_REGISTRY = new HashMap<>();
    }

    public void loadRecipeRegistry(@Nullable EntityPlayer player, boolean doRegister) {
        Map<ResourceLocation, MachineRecipe> sharedLoadRegistry = new HashMap<>();

        Map<DynamicMachine, List<MachineRecipe>> recipes = loadRecipes(player, sharedLoadRegistry);
        if (doRegister) {
            registerRecipes(recipes);
        }
        recipes = loadAdapters(player, sharedLoadRegistry);
        if (doRegister) {
            registerRecipes(recipes);
        }
    }

    private Map<DynamicMachine, List<MachineRecipe>> loadRecipes(@Nullable EntityPlayer player, Map<ResourceLocation, MachineRecipe> sharedLoadRegistry) {
        ProgressManager.ProgressBar barRecipes = ProgressManager.push("RecipeRegistry - Recipes", 3);
        barRecipes.step("Discovering Files");
        DataLoadProfiler profiler = new DataLoadProfiler();

        Map<RecipeLoader.FileType, List<File>> potentialRecipes = RecipeLoader.discoverDirectory(CommonProxy.dataHolder.getRecipeDirectory());
        barRecipes.step("Loading Recipes");

        List<MachineRecipe> recipes = RecipeLoader.loadRecipes(potentialRecipes.getOrDefault(RecipeLoader.FileType.RECIPE, Lists.newArrayList()), earlyRecipes);
        DataLoadProfiler.StatusLine sl = profiler.createLine("Load-Phase: ");
        DataLoadProfiler.Status success = sl.appendStatus("%s recipes loaded");
        DataLoadProfiler.Status failed = sl.appendStatus("%s recipes failed");

        success.setCounter(recipes.size());

        Map<String, Exception> failures = RecipeLoader.captureFailedAttempts();
        failed.setCounter(failures.size());
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading recipes!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load recipe from file " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }

        barRecipes.step("Validation");

        Map<DynamicMachine, List<MachineRecipe>> validRecipes = loadAndValidateRecipes(recipes, profiler, sharedLoadRegistry);

        profiler.printLines(player);
        ProgressManager.pop(barRecipes);
        return validRecipes;
    }

    private Map<DynamicMachine, List<MachineRecipe>> loadAdapters(@Nullable EntityPlayer player, Map<ResourceLocation, MachineRecipe> sharedLoadRegistry) {
        ProgressManager.ProgressBar barRecipes = ProgressManager.push("RecipeRegistry - Adapters", 3);
        barRecipes.step("Discovering Adapter-Files");
        DataLoadProfiler profiler = new DataLoadProfiler();

        Map<RecipeLoader.FileType, List<File>> potentialRecipes = RecipeLoader.discoverDirectory(CommonProxy.dataHolder.getRecipeDirectory());
        barRecipes.step("Loading Adapters");

        List<MachineRecipe> recipes = RecipeLoader.loadAdapterRecipes(potentialRecipes.getOrDefault(RecipeLoader.FileType.ADAPTER, Lists.newArrayList()));
        DataLoadProfiler.StatusLine sl = profiler.createLine("Load-Phase: ");
        DataLoadProfiler.Status success = sl.appendStatus("%s adapter-recipes loaded");
        DataLoadProfiler.Status failed = sl.appendStatus("%s adapter-recipes failed");

        success.setCounter(recipes.size());

        Map<String, Exception> failures = RecipeLoader.captureFailedAttempts();
        failed.setCounter(failures.size());
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading adapters!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load recipe from file " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }

        barRecipes.step("Validation");

        Map<DynamicMachine, List<MachineRecipe>> validRecipes = loadAndValidateRecipes(recipes, profiler, sharedLoadRegistry);

        profiler.printLines(player);
        ProgressManager.pop(barRecipes);
        return validRecipes;
    }

    private Map<DynamicMachine, List<MachineRecipe>> loadAndValidateRecipes(List<MachineRecipe> recipes,
                                                                            DataLoadProfiler profiler,
                                                                            Map<ResourceLocation, MachineRecipe> sharedLoadRegistry) {
        DataLoadProfiler.StatusLine unknown = profiler.createLine("");
        DataLoadProfiler.Status unknownCounter = unknown.appendStatus("Unknown Machinery: %s");

        Map<DynamicMachine, Tuple<DataLoadProfiler.Status, DataLoadProfiler.Status>> statusMap = new HashMap<>();
        Map<DynamicMachine, List<MachineRecipe>> out = new HashMap<>();

        for (MachineRecipe mr : recipes) {
            DynamicMachine m = mr.getOwningMachine();
            if(m == null) {
                unknownCounter.incrementCounter();
                ModularMachinery.log.warn("MachineRecipe loaded for unknown machine: " + mr.getOwningMachineIdentifier() + " - responsible file: " + mr.getRecipeFilePath());
                continue;
            }
            Tuple<DataLoadProfiler.Status, DataLoadProfiler.Status> status = statusMap.get(m);
            if(status == null) {
                DataLoadProfiler.StatusLine line = profiler.createLine(m.getRegistryName() + " (Recipes): ");
                status = new Tuple<>(line.appendStatus("%s loaded"), line.appendStatus("%s failed"));
                statusMap.put(m, status);
            }

            DataLoadProfiler.Status loaded = status.getFirst();
            DataLoadProfiler.Status fail = status.getSecond();

            if(sharedLoadRegistry.containsKey(mr.getRegistryName())) {
                MachineRecipe other = sharedLoadRegistry.get(mr.getRegistryName());
                if(other != null) {
                    ModularMachinery.log.warn("MachineRecipe with registryName " + mr.getRegistryName() + " already exists!");
                    ModularMachinery.log.warn("Offending files: '" + mr.getRecipeFilePath() + "' and '" + other.getRecipeFilePath() + "' !");
                    fail.incrementCounter();
                    continue;
                }
            }
            loaded.incrementCounter();
            sharedLoadRegistry.put(mr.getRegistryName(), mr);
            List<MachineRecipe> recipeList = out.computeIfAbsent(mr.getOwningMachine(), r -> Lists.newArrayList());
            recipeList.add(mr);
        }
        return out;
    }

    public void registerRecipes(Map<DynamicMachine, List<MachineRecipe>> map) {
        for (DynamicMachine machine : map.keySet()) {
            List<MachineRecipe> recipes = map.get(machine);
            for (MachineRecipe recipe : recipes) {
                RECIPE_REGISTRY.put(recipe.getRegistryName(), recipe);
                Map<Integer, TreeSet<MachineRecipe>> recipeList = REGISTRY_RECIPE_BY_MACHINE.computeIfAbsent(machine.getRegistryName(), k -> new TreeMap<>());
                TreeSet<MachineRecipe> recipeSet = recipeList.computeIfAbsent(recipe.getConfiguredPriority(), inte -> new TreeSet<>());
                recipeSet.add(recipe);
            }
        }
    }

    public void registerRecipeEarly(PreparedRecipe recipe) {
        this.earlyRecipes.add(recipe);
    }

    public void reloadAdapters() {
        for (RecipeAdapterAccessor accessor : RecipeLoader.recipeAdapters) {
            Map<Integer, TreeSet<MachineRecipe>> machineRecipeList = REGISTRY_RECIPE_BY_MACHINE.get(accessor.getOwningMachine());
            for (MachineRecipe cached : accessor.getCachedRecipes()) {
                RECIPE_REGISTRY.remove(cached.getRegistryName());
                if(machineRecipeList != null) {
                    TreeSet<MachineRecipe> recipeTreeSet = machineRecipeList.get(cached.getConfiguredPriority());
                    if(recipeTreeSet != null) {
                        recipeTreeSet.remove(cached);
                    }
                }
            }
        }

        for (RecipeAdapterAccessor accessor : RecipeLoader.recipeAdapters) {
            for (MachineRecipe recipe : accessor.loadRecipesForAdapter()) {
                RECIPE_REGISTRY.put(recipe.getRegistryName(), recipe);
                Map<Integer, TreeSet<MachineRecipe>> recipeList = REGISTRY_RECIPE_BY_MACHINE.computeIfAbsent(accessor.getOwningMachine(), k -> new TreeMap<>());
                TreeSet<MachineRecipe> recipeSet = recipeList.computeIfAbsent(recipe.getConfiguredPriority(), inte -> new TreeSet<>());
                recipeSet.add(recipe);
            }
        }
    }

    public void clearLingeringRecipes() {
        this.earlyRecipes.clear();
    }

}
