/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.crafting.adapter.AdapterMinecraftFurnace;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;

import static hellfirepvp.modularmachinery.common.lib.RecipeAdaptersMM.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRecipeAdapters
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:05
 */
public class RegistryRecipeAdapters {

    private RegistryRecipeAdapters() {}

    public static void initialize() {
        MINECRAFT_FURNACE = register(new AdapterMinecraftFurnace());
    }

    private static <T extends RecipeAdapter> T register(T adapter) {
        CommonProxy.registryPrimer.register(adapter);
        return adapter;
    }

}
