/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeAdapterRegistry
 * Created by HellFirePvP
 * Date: 23.07.2017 / 14:15
 */
public class RecipeAdapterRegistry {

    private static IForgeRegistry<RecipeAdapter> ADAPTER_REGISTRY = null;

    @Nullable
    public static Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachine,
                                                             ResourceLocation adapterKey,
                                                             List<RecipeModifier> modifiers,
                                                             List<ComponentRequirement<?>> additionalRequirements) {
        RecipeAdapter adapter = ADAPTER_REGISTRY.getValue(adapterKey);
        if(adapter == null) {
            return null;
        }
        return adapter.createRecipesFor(owningMachine, modifiers, additionalRequirements);
    }

    public static void createRegistry() {
        if (ADAPTER_REGISTRY != null) {
            return;
        }

        ADAPTER_REGISTRY = new RegistryBuilder<RecipeAdapter>()
                .setName(new ResourceLocation(ModularMachinery.MODID, "recipeadapters"))
                .setType(RecipeAdapter.class)
                .disableSaving()
                .setMaxID(Short.MAX_VALUE)
                .create();
    }

    public static void registerAdapter(RecipeAdapter adapter) {
        ADAPTER_REGISTRY.register(adapter);
    }

    public static void initDefaultAdapters() {
        registerAdapter(new AdapterMinecraftFurnace());
    }

    public static void registerMachineAdapters() {
        for (DynamicMachine machine : MachineRegistry.getRegistry()) {
            registerAdapter(new DynamicMachineRecipeAdapter(machine.getRegistryName(), machine));
        }
    }

}
