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
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicMachineRecipeAdapter
 * Created by HellFirePvP
 * Date: 02.03.2019 / 15:02
 */
public class DynamicMachineRecipeAdapter extends RecipeAdapter {

    private final DynamicMachine originalMachine;

    public DynamicMachineRecipeAdapter(@Nonnull ResourceLocation registryName, DynamicMachine originalMachine) {
        super(registryName);
        this.originalMachine = originalMachine;
    }

    @Nonnull
    @Override
    public Collection<MachineRecipe> createRecipesFor(ResourceLocation owningMachineName) {
        String newIdentifier = owningMachineName.getResourceDomain() + "." + owningMachineName.getResourcePath();

        List<MachineRecipe> recipesNew = new ArrayList<>();
        for (MachineRecipe recipe : RecipeRegistry.getRegistry().getRecipesFor(this.originalMachine)) {
            recipesNew.add(recipe.deepCopy(
                    (res) -> new ResourceLocation(ModularMachinery.MODID, res.getResourcePath() + ".copy." + newIdentifier),
                    owningMachineName));
        }
        return recipesNew;
    }

}
