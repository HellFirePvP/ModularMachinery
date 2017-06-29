/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveMachineRecipe
 * Created by HellFirePvP
 * Date: 29.06.2017 / 15:50
 */
public class ActiveMachineRecipe {

    private final MachineRecipe recipe;
    private int tick = 0;

    public ActiveMachineRecipe(MachineRecipe recipe) {
        this.recipe = recipe;
    }

    public MachineRecipe getRecipe() {
        return recipe;
    }

    public void tick(RecipeCraftingContext context) {
        if(context.isValid()) {
            this.tick++;
        }
    }

    public boolean isCompleted(TileMachineController controller) {
        return this.tick >= this.recipe.getRecipeTotalTickTime();
    }

    public void complete(RecipeCraftingContext completionContext) {
        completionContext.craft();
    }
}
