/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

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

    public abstract boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions);

    public abstract ComponentRequirement deepCopy();

    public abstract void startRequirementCheck(ResultChance contextChance);

    public abstract void endRequirementCheck();

    public static class RequirementEnergy extends ComponentRequirement {

        private int requirementPerTick;
        private int activeIO;

        public RequirementEnergy(MachineComponent.IOType ioType, int requirementPerTick) {
            super(MachineComponent.ComponentType.ENERGY, ioType);
            this.requirementPerTick = requirementPerTick;
            this.activeIO = this.requirementPerTick;
        }

        @Override
        public ComponentRequirement deepCopy() {
            RequirementEnergy energy = new RequirementEnergy(this.getActionType(), this.requirementPerTick);
            energy.activeIO = this.activeIO;
            return energy;
        }

        @Override
        public void startRequirementCheck(ResultChance contextChance) {}

        @Override
        public void endRequirementCheck() {}

        public int getRequiredEnergyPerTick() {
            return requirementPerTick;
        }

        @Override
        public boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
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
            return canStartCrafting(component, context, Lists.newArrayList());
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

        private FluidStack requirementCheck;
        private boolean doesntConsumeInput;

        public RequirementFluid(MachineComponent.IOType ioType, FluidStack fluid) {
            super(MachineComponent.ComponentType.FLUID, ioType);
            this.required = fluid;
            this.requirementCheck = this.required.copy();
        }

        @Override
        public ComponentRequirement deepCopy() {
            RequirementFluid fluid = new RequirementFluid(this.getActionType(), this.required);
            fluid.chance = this.chance;
            return fluid;
        }

        public void setChance(float chance) {
            this.chance = chance;
        }

        @Override
        public void startRequirementCheck(ResultChance contextChance) {
            this.requirementCheck = this.required.copy();
            this.doesntConsumeInput = contextChance.canProduce(this.chance);
        }

        @Override
        public void endRequirementCheck() {
            this.requirementCheck = this.required.copy();
            this.doesntConsumeInput = true;
        }

        @Override
        public boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
            if(component.getComponentType() != MachineComponent.ComponentType.FLUID ||
                    !(component instanceof MachineComponent.FluidHatch) ||
                    component.getIOType() != getActionType()) return false;
            FluidTank handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    FluidStack drained = handler.drainInternal(this.requirementCheck.copy(), false);
                    if(drained == null) {
                        return false;
                    }
                    this.requirementCheck.amount = Math.max(this.requirementCheck.amount - drained.amount, 0);
                    return this.requirementCheck.amount <= 0;
                case OUTPUT:
                    handler = CopyHandlerHelper.copyTank(handler);

                    for (ComponentOutputRestrictor restrictor : restrictions) {
                        if(restrictor instanceof ComponentOutputRestrictor.RestrictionTank) {
                            ComponentOutputRestrictor.RestrictionTank tank = (ComponentOutputRestrictor.RestrictionTank) restrictor;

                            if(tank.exactComponent.equals(component)) {
                                handler.fillInternal(tank.inserted.copy(), true);
                            }
                        }
                    }
                    int filled = handler.fillInternal(this.required.copy(), false); //True or false doesn't really matter tbh
                    boolean didFill = filled >= this.required.amount;
                    if(didFill) {
                        context.addRestriction(new ComponentOutputRestrictor.RestrictionTank(this.required.copy(), component));
                    }
                    return didFill;
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
                    FluidStack drainedSimulated = handler.drainInternal(this.requirementCheck.copy(), false);
                    if(drainedSimulated == null) {
                        return false;
                    }
                    if(this.doesntConsumeInput) {
                        this.requirementCheck.amount = Math.max(this.requirementCheck.amount - drainedSimulated.amount, 0);
                        return this.requirementCheck.amount <= 0;
                    }
                    FluidStack actualDrained = handler.drainInternal(this.requirementCheck.copy(), true);
                    if(actualDrained == null) {
                        return false;
                    }
                    this.requirementCheck.amount = Math.max(this.requirementCheck.amount - actualDrained.amount, 0);
                    return this.requirementCheck.amount <= 0;
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

        public final ItemRequirementType requirementType;

        public final ItemStack required;

        public final String oreDictName;
        public final int oreDictItemAmount;

        public final int fuelBurntime;

        public NBTTagCompound tag = null;
        public NBTTagCompound previewDisplayTag = null;

        public float chance = 1F;

        public RequirementItem(MachineComponent.IOType ioType, ItemStack item) {
            super(MachineComponent.ComponentType.ITEM, ioType);
            this.requirementType = ItemRequirementType.ITEMSTACKS;
            this.required = item.copy();
            this.oreDictName = null;
            this.oreDictItemAmount = 0;
            this.fuelBurntime = 0;
        }

        public RequirementItem(MachineComponent.IOType ioType, String oreDictName, int oreDictAmount) {
            super(MachineComponent.ComponentType.ITEM, ioType);
            this.requirementType = ItemRequirementType.OREDICT;
            this.oreDictName = oreDictName;
            this.oreDictItemAmount = oreDictAmount;
            this.required = ItemStack.EMPTY;
            this.fuelBurntime = 0;
        }

        public RequirementItem(MachineComponent.IOType actionType, int fuelBurntime) {
            super(MachineComponent.ComponentType.ITEM, actionType);
            this.requirementType = ItemRequirementType.FUEL;
            this.fuelBurntime = fuelBurntime;
            this.oreDictName = null;
            this.oreDictItemAmount = 0;
            this.required = ItemStack.EMPTY;
        }

        @Override
        public ComponentRequirement deepCopy() {
            RequirementItem item;
            switch (this.requirementType) {
                case OREDICT:
                    item = new RequirementItem(this.getActionType(), this.oreDictName, this.oreDictItemAmount);
                    break;

                case FUEL:
                    item = new RequirementItem(this.getActionType(), this.fuelBurntime);
                    break;

                default:
                case ITEMSTACKS:
                    item = new RequirementItem(this.getActionType(), this.required.copy());
                    break;
            }
            item.chance = this.chance;
            if(this.tag != null) {
                item.tag = this.tag.copy();
            }
            if(this.previewDisplayTag != null) {
                item.previewDisplayTag = this.previewDisplayTag.copy();
            }
            return item;
        }

        @Override
        public void startRequirementCheck(ResultChance contextChance) {}

        @Override
        public void endRequirementCheck() {}

        public void setChance(float chance) {
            this.chance = chance;
        }

        @Override
        public boolean canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
            if(component.getComponentType() != MachineComponent.ComponentType.ITEM ||
                    !(component instanceof MachineComponent.ItemBus) ||
                    component.getIOType() != getActionType()) return false;
            IOInventory handler = context.getItemHandler(component);
            switch (getActionType()) {
                case INPUT:
                    switch (this.requirementType) {
                        case ITEMSTACKS:
                            return ItemUtils.consumeFromInventory(handler, this.required.copy(), true, this.tag);
                        case OREDICT:
                            return ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, this.oreDictItemAmount, true, this.tag);
                        case FUEL:
                            return ItemUtils.consumeFromInventoryFuel(handler, this.fuelBurntime, true, this.tag) <= 0;
                    }
                case OUTPUT:
                    handler = CopyHandlerHelper.copyInventory(handler);

                    for (ComponentOutputRestrictor restrictor : restrictions) {
                        if(restrictor instanceof ComponentOutputRestrictor.RestrictionInventory) {
                            ComponentOutputRestrictor.RestrictionInventory inv = (ComponentOutputRestrictor.RestrictionInventory) restrictor;

                            if(inv.exactComponent.equals(component)) {
                                ItemUtils.tryPlaceItemInInventory(inv.inserted.copy(), handler, false);
                            }
                        }
                    }
                    ItemStack stack = ItemUtils.copyStackWithSize(required, required.getCount());
                    if(tag != null) {
                        stack.setTagCompound(tag.copy());
                    }
                    boolean inserted = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, true);
                    if(inserted) {
                        context.addRestriction(new ComponentOutputRestrictor.RestrictionInventory(stack.copy(), component));
                    }
                    return inserted;
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
                    switch (this.requirementType) {
                        //If it doesn't consume the item, we only need to see if it's actually there.
                        case ITEMSTACKS:
                            boolean can = ItemUtils.consumeFromInventory(handler, this.required.copy(), true, this.tag);
                            if(chance.canProduce(this.chance)) {
                                return can;
                            }
                            return can && ItemUtils.consumeFromInventory(handler, this.required.copy(), false, this.tag);
                        case OREDICT:
                            can = ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, this.oreDictItemAmount, true, this.tag);
                            if(chance.canProduce(this.chance)) {
                                return can;
                            }
                            return can && ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, this.oreDictItemAmount, false, this.tag);
                        case FUEL:
                            can = ItemUtils.consumeFromInventoryFuel(handler, this.fuelBurntime, true, this.tag) <= 0;
                            if(chance.canProduce(this.chance)) {
                                return can;
                            }
                            return can && ItemUtils.consumeFromInventoryFuel(handler, this.fuelBurntime, false, this.tag) <= 0;
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
                    ItemStack stack = ItemUtils.copyStackWithSize(required, required.getCount());
                    if(tag != null) {
                        stack.setTagCompound(tag);
                    }
                    //If we don't produce the item, we only need to see if there would be space for it at all.
                    boolean hasSpace = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, true);
                    if(chance.canProduce(this.chance)) {
                        return hasSpace;
                    }
                    return hasSpace && ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, false);
            }
            return false;
        }

    }

    public static enum ItemRequirementType {

        ITEMSTACKS,
        OREDICT,
        FUEL

    }

}
