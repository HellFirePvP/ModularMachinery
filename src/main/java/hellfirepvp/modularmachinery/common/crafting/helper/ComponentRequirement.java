/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    public abstract CraftCheck canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions);

    public abstract ComponentRequirement deepCopy();

    public abstract void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context);

    public abstract void endRequirementCheck();

    //Be sure, that if you specify a new object here as type that you register that along with a helper and renderer
    //in the JEI Integration! Otherwise JEI will complain about not having proper handling for this
    //Also, be sure that this generic T is the *only one* with that type otherwise internally stuff might break...
    public abstract JEIComponent<T> provideJEIComponent();

    public abstract RecipeLayoutPart provideRenderableLayoutPart(Point componentOffset);

    public static abstract class JEIComponent<T> {

        public abstract Class<T> getJEIRequirementClass();

        public abstract List<T> getJEIIORequirements();

        @SideOnly(Side.CLIENT)
        public abstract RecipeLayoutPart<T> getLayoutPart(Point offset);

        @SideOnly(Side.CLIENT)
        public abstract void onJEIHoverTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip);

    }

    public static enum CraftCheck {

        //requirement check succeeded.
        SUCCESS,

        //requirement check failed
        FAILURE_MISSING_INPUT,

        //requirement check for energy failed
        FAILURE_MISSING_ENERGY,

        //component is not suitable to be checked for given requirement-check (i.e. component type != requirement type)
        INVALID_SKIP

    }

    public static interface ChancedRequirement {

        public void setChance(float chance);

    }

}
