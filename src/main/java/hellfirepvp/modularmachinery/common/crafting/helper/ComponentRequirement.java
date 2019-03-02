/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentRequirement
 * Created by HellFirePvP
 * Date: 28.06.2017 / 10:34
 */
public abstract class ComponentRequirement<T> {

    private final ComponentType componentType;
    private final MachineComponent.IOType actionType;

    public ComponentRequirement(ComponentType componentType, MachineComponent.IOType actionType) {
        this.componentType = componentType;
        this.actionType = actionType;
    }

    public final ComponentType getRequiredComponentType() {
        return componentType;
    }

    public final MachineComponent.IOType getActionType() {
        return actionType;
    }

    //True, if the requirement could be fulfilled by the given component
    public abstract boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance);

    //True, if the requirement could be fulfilled by the given component
    public abstract boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance);

    @Nonnull
    public abstract CraftCheck canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions);

    //Creates an exact copy of the current requirement
    public abstract ComponentRequirement<T> deepCopy();

    //Creates a copy of the current requirement and applies all modifiers to the requirement.
    //Supplying an empty list should behave identical to deepCopy
    public abstract ComponentRequirement<T> deepCopyModified(List<RecipeModifier> modifiers);

    public abstract void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context);

    public abstract void endRequirementCheck();

    //Be sure, that if you specify a new object here as type that you register that along with a helper and renderer
    //in the JEI Integration! Otherwise JEI will complain about not having proper handling for this
    //Also, be sure that this generic T is the *only one* with that type otherwise internally stuff might break...
    public abstract JEIComponent<T> provideJEIComponent();

    public static abstract class JEIComponent<T> {

        public abstract Class<T> getJEIRequirementClass();

        public abstract List<T> getJEIIORequirements();

        @SideOnly(Side.CLIENT)
        public abstract RecipeLayoutPart<T> getLayoutPart(Point offset);

        @SideOnly(Side.CLIENT)
        public abstract void onJEIHoverTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip);

    }

    public static abstract class PerTick<T> extends ComponentRequirement<T> {

        public PerTick(ComponentType componentType, MachineComponent.IOType actionType) {
            super(componentType, actionType);
        }

        //Multiplier is passed into this to adjust 'production' or 'consumption' accordingly if the recipe has a longer or shorter duration
        public abstract void startIOTick(RecipeCraftingContext context, float durationMultiplier);

        // Returns the actual result of the IOTick-check after a sufficient amount of components have been checked for the requirement
        // Supply a failure message if invalid!
        @Nonnull
        public abstract CraftCheck resetIOTick(RecipeCraftingContext context);

        // Returns either success, partial success or skip component
        // Return value indicates whether the IO tick requirement was already successful
        // or if more components need to be checked.
        @Nonnull
        public abstract CraftCheck doIOTick(MachineComponent component, RecipeCraftingContext context);

    }

    public static interface ChancedRequirement {

        public void setChance(float chance);

    }

}
