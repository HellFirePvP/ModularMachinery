/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementItem;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: AdapterMinecraftFurnace
 * Created by HellFirePvP
 * Date: 23.07.2017 / 14:48
 */
public class AdapterMinecraftFurnace extends RecipeAdapter {

    public AdapterMinecraftFurnace() {
        super(new ResourceLocation("minecraft", "furnace"));
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName, List<RecipeModifier> modifiers) {
        Map<ItemStack, ItemStack> inputOutputMap = FurnaceRecipes.instance().getSmeltingList();
        List<MachineRecipe> smeltingRecipes = new ArrayList<>(inputOutputMap.size());
        int incId = 0;
        for (Map.Entry<ItemStack, ItemStack> smelting : inputOutputMap.entrySet()) {
            int tickTime = Math.round(Math.max(1, RecipeModifier.applyModifiers(modifiers, RecipeModifier.TARGET_DURATION, null, 120, false)));

            MachineRecipe recipe = createRecipeShell(
                    new ResourceLocation("minecraft", "smelting_recipe_" + incId),
                    owningMachineName,
                    tickTime, 0);

            int inAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RecipeModifier.TARGET_ITEM, MachineComponent.IOType.INPUT, smelting.getKey().getCount(), false));
            if (inAmount > 0) {
                recipe.addRequirement(new RequirementItem(MachineComponent.IOType.INPUT, ItemUtils.copyStackWithSize(smelting.getKey(), inAmount)));
            }

            int outAmount = Math.round(RecipeModifier.applyModifiers(modifiers, RecipeModifier.TARGET_ITEM, MachineComponent.IOType.OUTPUT, smelting.getKey().getCount(), false));
            if (outAmount > 0) {
                recipe.addRequirement(new RequirementItem(MachineComponent.IOType.OUTPUT, ItemUtils.copyStackWithSize(smelting.getValue(), outAmount)));
            }

            int inEnergy = Math.round(RecipeModifier.applyModifiers(modifiers, RecipeModifier.TARGET_ENERGY, MachineComponent.IOType.INPUT, 20, false));
            if (inEnergy > 0) {
                recipe.addRequirement(new RequirementEnergy(MachineComponent.IOType.INPUT, inEnergy));
            }
            smeltingRecipes.add(recipe);
            incId++;
        }
        return smeltingRecipes;
    }
}
