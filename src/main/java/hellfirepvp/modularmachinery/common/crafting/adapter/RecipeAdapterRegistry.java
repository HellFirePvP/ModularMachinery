/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    public static Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachine, ResourceLocation adapterKey) {
        RecipeAdapter adapter = ADAPTER_REGISTRY.getValue(adapterKey);
        if(adapter == null) {
            return null;
        }
        return adapter.createRecipesFor(owningMachine);
    }

    public static void initDefaultAdapters() {
        ADAPTER_REGISTRY = new RegistryBuilder<RecipeAdapter>()
                .setName(new ResourceLocation(ModularMachinery.MODID, "recipeadapters"))
                .setType(RecipeAdapter.class)
                .disableSaving()
                .setMaxID(Short.MAX_VALUE)
        .create();

        ADAPTER_REGISTRY.register(new AdapterMinecraftFurnace());

        RegistryEvent<RecipeAdapter> adapterRegistryEvent = new RegistryEvent.Register<>(
                new ResourceLocation(ModularMachinery.MODID, "recipeadapters"), ADAPTER_REGISTRY);
        MinecraftForge.EVENT_BUS.post(adapterRegistryEvent);
    }

}
