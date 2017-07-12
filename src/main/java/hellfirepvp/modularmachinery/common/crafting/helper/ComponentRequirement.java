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
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

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

    //True, if the requirement could be fulfilled by the given component
    public abstract boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance);

    //True, if the requirement could be fulfilled by the given component
    public abstract boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance);

    public abstract boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context);

    public static class RequirementEnergy extends ComponentRequirement {

        private int requirementPerTick;
        private int activeIO;

        public RequirementEnergy(MachineComponent.IOType ioType, int requirementPerTick) {
            super(MachineComponent.ComponentType.ENERGY, ioType);
            this.requirementPerTick = requirementPerTick;
            this.activeIO = this.requirementPerTick;
        }

        public int getRequiredEnergyPerTick() {
            return requirementPerTick;
        }

        @Override
        public boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context) {
            if(component.getComponentType() != MachineComponent.ComponentType.ENERGY ||
                    !(component instanceof MachineComponent.EnergyHatch) ||
                    component.getIOType() != getActionType()) return false;
            IEnergyHandler handler = context.getEnergyHandler(component);
            switch (getActionType()) {
                case INPUT:
                    return handler.getCurrentEnergy() >= this.requirementPerTick;
                case OUTPUT:
                    return true;
            }
            return false;
        }

        @Override
        public boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            return canStartCrafting(component, context);
        }

        @Override
        public boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            return true;
        }

        public void resetEnergyIO() {
            this.activeIO = this.requirementPerTick;
        }

        //returns remaining energy needing to be consumed/distributed
        public int handleEnergyIO(MachineComponent component, RecipeCraftingContext context) {
            if(component.getComponentType() != MachineComponent.ComponentType.ENERGY ||
                    !(component instanceof MachineComponent.EnergyHatch) ||
                    component.getIOType() != getActionType()) return this.activeIO;
            IEnergyHandler handler = context.getEnergyHandler(component);
            switch (getActionType()) {
                case INPUT:
                    if(handler.getCurrentEnergy() >= this.activeIO) {
                        handler.setCurrentEnergy(handler.getCurrentEnergy() - this.activeIO);
                        this.activeIO = 0;
                        return activeIO;
                    }
                    return this.activeIO;
                case OUTPUT:
                    int remaining = handler.getRemainingCapacity();
                    if(remaining - this.activeIO < 0) {
                        handler.setCurrentEnergy(handler.getMaxEnergy());
                        this.activeIO -= remaining;
                        return this.activeIO;
                    }
                    handler.setCurrentEnergy(Math.min(handler.getCurrentEnergy() + this.activeIO, handler.getMaxEnergy()));
                    this.activeIO = 0;
                    return activeIO;
            }
            return this.activeIO;
        }

    }

    public static class RequirementFluid extends ComponentRequirement {

        public final FluidStack required;
        public float chance = 1F;

        public RequirementFluid(MachineComponent.IOType ioType, FluidStack fluid) {
            super(MachineComponent.ComponentType.FLUID, ioType);
            this.required = fluid;
        }

        public void setChance(float chance) {
            this.chance = chance;
        }

        @Override
        public boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context) {
            if(component.getComponentType() != MachineComponent.ComponentType.FLUID ||
                    !(component instanceof MachineComponent.FluidHatch) ||
                    component.getIOType() != getActionType()) return false;
            FluidTank handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    return handler.drainInternal(this.required.copy(), false) != null;
                case OUTPUT:
                    return handler.fillInternal(this.required.copy(), false) >= this.required.amount;
            }
            return false;
        }

        @Override
        public boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.FLUID ||
                    !(component instanceof MachineComponent.FluidHatch) ||
                    component.getIOType() != getActionType()) return false;
            FluidTank handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    FluidStack drainedSimulated = handler.drainInternal(this.required.copy(), false);
                    if(chance.canProduce(this.chance)) {
                        return drainedSimulated != null;
                    }
                    return drainedSimulated != null && handler.drainInternal(this.required.copy(), true) != null;
            }
            return false;
        }

        @Override
        public boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.FLUID ||
                    !(component instanceof MachineComponent.FluidHatch) ||
                    component.getIOType() != getActionType()) return false;
            FluidTank handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case OUTPUT:
                    int fillableAmount = handler.fillInternal(this.required.copy(), false);
                    if(chance.canProduce(this.chance)) {
                        return fillableAmount >= this.required.amount;
                    }
                    return fillableAmount >= this.required.amount && handler.fillInternal(this.required.copy(), true) >= this.required.amount;
            }
            return false;
        }

    }

    public static class RequirementItem extends ComponentRequirement {

        public final ItemStack required;
        public final String oreDictName;
        public final int oreDictItemAmount;

        public float chance = 1F;

        public RequirementItem(MachineComponent.IOType ioType, ItemStack item) {
            super(MachineComponent.ComponentType.ITEM, ioType);
            this.required = item.copy();
            this.oreDictName = null;
            this.oreDictItemAmount = 0;
        }

        public RequirementItem(MachineComponent.IOType ioType, String oreDictName, int oreDictAmount) {
            super(MachineComponent.ComponentType.ITEM, ioType);
            this.oreDictName = oreDictName;
            this.oreDictItemAmount = oreDictAmount;
            this.required = ItemStack.EMPTY;
        }

        public void setChance(float chance) {
            this.chance = chance;
        }

        @Override
        public boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context) {
            if(component.getComponentType() != MachineComponent.ComponentType.ITEM ||
                    !(component instanceof MachineComponent.ItemBus) ||
                    component.getIOType() != getActionType()) return false;
            IItemHandlerModifiable handler = context.getItemHandler(component);
            switch (getActionType()) {
                case INPUT:
                    if(oreDictName != null) {
                        return ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, this.oreDictItemAmount, true);
                    } else {
                        return ItemUtils.consumeFromInventory(handler, this.required.copy(), true);
                    }
                case OUTPUT:
                    return ItemUtils.tryPlaceItemInInventory(this.required.copy(), handler, true);
            }
            return false;
        }

        @Override
        public boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.ITEM ||
                    !(component instanceof MachineComponent.ItemBus) ||
                    component.getIOType() != getActionType()) return false;
            IItemHandlerModifiable handler = context.getItemHandler(component);
            switch (getActionType()) {
                case INPUT:
                    if(oreDictName != null) {
                        //If it doesn't consume the item, we only need to see if it's actually there.
                        boolean can = ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, this.oreDictItemAmount, true);
                        if(chance.canProduce(this.chance)) {
                            return can;
                        }
                        return can && ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, this.oreDictItemAmount, false);
                    } else {
                        boolean can = ItemUtils.consumeFromInventory(handler, this.required.copy(), true);
                        if(chance.canProduce(this.chance)) {
                            return can;
                        }
                        return can && ItemUtils.consumeFromInventory(handler, this.required.copy(), false);
                    }
            }
            return false;
        }

        @Override
        public boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.ITEM ||
                    !(component instanceof MachineComponent.ItemBus) ||
                    component.getIOType() != getActionType()) return false;

            if(oreDictName != null && required.isEmpty()) {
                throw new IllegalStateException("Can't output item by oredict!");
            }
            IItemHandlerModifiable handler = context.getItemHandler(component);
            switch (getActionType()) {
                case OUTPUT:
                    //If we don't produce the item, we only need to see if there would be space for it at all.
                    boolean hasSpace = ItemUtils.tryPlaceItemInInventory(this.required.copy(), handler, true);
                    if(chance.canProduce(this.chance)) {
                        return hasSpace;
                    }
                    return hasSpace && ItemUtils.tryPlaceItemInInventory(this.required.copy(), handler, false);
            }
            return false;
        }

    }

}
