/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import hellfirepvp.modularmachinery.common.util.handlers.CopyableFluidHandler;
import hellfirepvp.modularmachinery.common.util.handlers.CopyableItemHandler;
import hellfirepvp.modularmachinery.common.util.handlers.IEnergyHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentRequirement
 * Created by HellFirePvP
 * Date: 28.06.2017 / 10:34
 */
public abstract class ComponentRequirement {

    private final MachineComponent.ComponentType componentType;
    private final MachineComponent.IOType actionType;

    public ComponentRequirement(MachineComponent.ComponentType componentType, MachineComponent.IOType actionType) {
        this.componentType = componentType;
        this.actionType = actionType;
    }

    public final MachineComponent.ComponentType getRequiredComponentType() {
        return componentType;
    }

    public final MachineComponent.IOType getActionType() {
        return actionType;
    }

    //- Used to complete crafting for a specific component in the current context with a given chance. (result can be ignored)
    //- Used to see if a component fulfills its specific requirement (true if yes with the given chance, false if not)
    public abstract boolean doComplete(MachineComponent component, RecipeCraftingContext context, ResultChance chance);

    public static class RequirementEnergy extends ComponentRequirement {

        private int requirementPerTick;

        public RequirementEnergy(MachineComponent.ComponentType emptyRequirement, MachineComponent.IOType ioType, int requirementPerTick) {
            super(emptyRequirement, ioType);
            this.requirementPerTick = requirementPerTick;
        }

        @Override
        public boolean doComplete(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.ENERGY ||
                    !(component instanceof MachineComponent.EnergyHatch) ||
                    component.getIOType() != getActionType()) return false;
            IEnergyHandler handler = context.getEnergyHandler(component);
            int time = context.getParentRecipe().getRecipeTotalTickTime();
            time -= context.getCurrentCraftingTick();
            switch (getActionType()) {
                case INPUT:
                    return handler.getCurrentEnergy() >= (time * this.requirementPerTick);
                case OUTPUT:
                    return handler.getRemainingCapacity() >= (time * this.requirementPerTick);
            }
            return false;
        }
    }

    public static class RequirementFluid extends ComponentRequirement {

        private final FluidStack required;
        private float chance = 1F;

        public RequirementFluid(MachineComponent.ComponentType emptyRequirement, MachineComponent.IOType ioType, FluidStack fluid) {
            super(emptyRequirement, ioType);
            this.required = fluid;
        }

        public void setChance(float chance) {
            this.chance = chance;
        }

        @Override
        public boolean doComplete(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.FLUID ||
                    !(component instanceof MachineComponent.FluidHatch) ||
                    component.getIOType() != getActionType()) return false;
            CopyableFluidHandler handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    FluidStack drainedSimulated = handler.drain(this.required.copy(), false);
                    if(!chance.canProduce(this.chance)) {
                        return drainedSimulated != null;
                    }
                    return drainedSimulated != null && handler.drain(this.required.copy(), true) != null;
                case OUTPUT:
                    boolean doFill = chance.canProduce(this.chance);

                    int fillableAmount = handler.fill(this.required.copy(), false);
                    if (fillableAmount >= this.required.amount) {
                        handler.fill(this.required.copy(), doFill);
                        return true;
                    }
                    return false;
            }
            return false;
        }

    }

    public static class RequirementItem extends ComponentRequirement {

        private final ItemStack required;
        private float chance = 1F;

        public RequirementItem(MachineComponent.ComponentType emptyRequirement, MachineComponent.IOType ioType, ItemStack item) {
            super(emptyRequirement, ioType);
            this.required = item.copy();
        }

        public void setChance(float chance) {
            this.chance = chance;
        }

        @Override
        public boolean doComplete(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.ITEM ||
                    !(component instanceof MachineComponent.ItemBus) ||
                    component.getIOType() != getActionType()) return false;
            CopyableItemHandler handler = context.getItemHandler(component);
            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    boolean can = ItemUtils.consumeFromInventory(handler, this.required.copy(), true);
                    if(!chance.canProduce(this.chance)) {
                        return can;
                    }
                    return can && ItemUtils.consumeFromInventory(handler, this.required.copy(), false);
                case OUTPUT:
                    //If we don't produce the item, we only need to see if there would be space for it at all.
                    boolean hasSpace = ItemUtils.tryPlaceItemInInventory(this.required.copy(), handler, true);
                    if(!chance.canProduce(this.chance)) {
                        return hasSpace;
                    }
                    return hasSpace && ItemUtils.tryPlaceItemInInventory(this.required.copy(), handler, false);
            }
            return false;
        }

    }

}
