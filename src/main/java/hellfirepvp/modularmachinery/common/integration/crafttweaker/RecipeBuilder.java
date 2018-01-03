/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeBuilder
 * Created by HellFirePvP
 * Date: 02.01.2018 / 18:16
 */
@ZenRegister
@ZenClass("mods.modularmachinery.RecipeBuilder")
public class RecipeBuilder {

    @ZenMethod
    public static RecipePrimer newBuilder(String recipeRegistryName, String associatedMachineRegistryName, int processingTickTime) {
        return newBuilder(recipeRegistryName, associatedMachineRegistryName, processingTickTime, 0);
    }

    @ZenMethod
    public static RecipePrimer newBuilder(String recipeRegistryName, String associatedMachineRegistryName, int processingTickTime, int sortingPriority) {
        ResourceLocation recipeLoc = new ResourceLocation(recipeRegistryName);
        if(recipeLoc.getResourceDomain().equals("minecraft")) {
            recipeLoc = new ResourceLocation(ModularMachinery.MODID, recipeLoc.getResourcePath());
        }
        ResourceLocation machineLoc = new ResourceLocation(associatedMachineRegistryName);
        if(machineLoc.getResourceDomain().equals("minecraft")) {
            machineLoc = new ResourceLocation(ModularMachinery.MODID, machineLoc.getResourcePath());
        }
        if(processingTickTime <= 0) {
            CraftTweakerAPI.logError("Recipe processing tick time has to be at least 1 tick!");
            return null;
        }

        return new RecipePrimer(recipeLoc, machineLoc, processingTickTime, sortingPriority);
    }

}
