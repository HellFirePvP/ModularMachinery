/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Iterables;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

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

    public ActiveMachineRecipe(NBTTagCompound serialized) {
        this.recipe = RecipeRegistry.getRegistry().getRecipe(new ResourceLocation(serialized.getString("recipeName")));
        this.tick = serialized.getInteger("tick");
    }

    public void reset() {
        this.tick = 0;
    }

    public MachineRecipe getRecipe() {
        return recipe;
    }

    @Nonnull
    public TileMachineController.CraftingStatus tick(RecipeCraftingContext context) {
        RecipeCraftingContext.CraftingCheckResult check;
        if(!(check = context.ioTick()).isFailure()) {
            this.tick++;
            return TileMachineController.CraftingStatus.working();
        } else {
            this.tick = 0;
            return TileMachineController.CraftingStatus.failure(
                    Iterables.getFirst(check.getUnlocalizedErrorMessages(), ""));
        }
    }

    public int getTick() {
        return tick;
    }

    public boolean isCompleted(TileMachineController controller, RecipeCraftingContext context) {
        int time = this.recipe.getRecipeTotalTickTime();
        //Not sure which a user will use... let's try both.
        time = Math.round(RecipeModifier.applyModifiers(context.getModifiers(RecipeModifier.TARGET_DURATION), RecipeModifier.TARGET_DURATION, null, time, false));
        return this.tick >= time;
    }

    public void complete(RecipeCraftingContext completionContext) {
        completionContext.finishCrafting();
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("tick", this.tick);
        tag.setString("recipeName", this.recipe.getRegistryName().toString());
        return tag;
    }

}
