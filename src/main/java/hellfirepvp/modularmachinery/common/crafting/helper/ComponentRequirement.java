/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidGas;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.*;
import hellfirepvp.modularmachinery.common.util.nbt.NBTMatchingHelper;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
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

        public final HybridFluid required;
        public float chance = 1F;

        private HybridFluid requirementCheck;
        private boolean doesntConsumeInput;

        private NBTTagCompound tagMatch = null, tagDisplay = null;

        public RequirementFluid(MachineComponent.IOType ioType, FluidStack fluid) {
            super(MachineComponent.ComponentType.FLUID, ioType);
            this.required = new HybridFluid(fluid);
            this.requirementCheck = this.required.copy();
        }

        private RequirementFluid(MachineComponent.IOType ioType, HybridFluid required) {
            super(MachineComponent.ComponentType.FLUID, ioType);
            this.required = required.copy();
            this.requirementCheck = this.required.copy();
        }

        @Optional.Method(modid = "mekanism")
        public static RequirementFluid createMekanismGasRequirement(MachineComponent.IOType ioType, GasStack gasStack) {
            return new RequirementFluid(ioType, new HybridFluidGas(gasStack));
        }

        @Override
        public ComponentRequirement deepCopy() {
            RequirementFluid fluid = new RequirementFluid(this.getActionType(), this.required);
            fluid.chance = this.chance;
            fluid.tagMatch = this.tagMatch;
            fluid.tagDisplay = this.tagDisplay;
            return fluid;
        }

        public void setMatchNBTTag(@Nullable NBTTagCompound tag) {
            this.tagMatch = tag;
        }

        @Nullable
        public NBTTagCompound getTagMatch() {
            if(tagMatch == null) {
                return null;
            }
            return tagMatch.copy();
        }

        public void setDisplayNBTTag(@Nullable NBTTagCompound tag) {
            this.tagDisplay = tag;
        }

        @Nullable
        public NBTTagCompound getTagDisplay() {
            if(tagDisplay == null) {
                return null;
            }
            return tagDisplay.copy();
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
            HybridTank handler = context.getFluidHandler(component);

            if(ModularMachinery.isMekanismLoaded) {
                return checkStartCraftingWithMekanism(component, context, handler, restrictions);
            }

            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    FluidStack drained = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), false);
                    if(drained == null) {
                        return false;
                    }
                    if(!NBTMatchingHelper.matchNBTCompound(this.tagMatch, drained.tag)) {
                        return false;
                    }
                    this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drained.amount, 0));
                    return this.requirementCheck.getAmount() <= 0;
                case OUTPUT:
                    handler = CopyHandlerHelper.copyTank(handler);

                    for (ComponentOutputRestrictor restrictor : restrictions) {
                        if(restrictor instanceof ComponentOutputRestrictor.RestrictionTank) {
                            ComponentOutputRestrictor.RestrictionTank tank = (ComponentOutputRestrictor.RestrictionTank) restrictor;

                            if(tank.exactComponent.equals(component)) {
                                handler.fillInternal(tank.inserted == null ? null : tank.inserted.copy().asFluidStack(), true);
                            }
                        }
                    }
                    int filled = handler.fillInternal(this.required.copy().asFluidStack(), false); //True or false doesn't really matter tbh
                    boolean didFill = filled >= this.required.getAmount();
                    if(didFill) {
                        context.addRestriction(new ComponentOutputRestrictor.RestrictionTank(this.required.copy(), component));
                    }
                    return didFill;
            }
            return false;
        }

        @Optional.Method(modid = "mekanism")
        private boolean checkStartCraftingWithMekanism(MachineComponent component, RecipeCraftingContext context,
                                                       HybridTank handler, List<ComponentOutputRestrictor> restrictions) {
            if(handler instanceof HybridGasTank) {
                HybridGasTank gasTank = (HybridGasTank) handler;
                switch (getActionType()) {
                    case INPUT:
                        if(this.requirementCheck instanceof HybridFluidGas) {
                            GasStack drained = gasTank.drawGas(EnumFacing.UP, this.requirementCheck.getAmount(), false);
                            if(drained == null) {
                                return false;
                            }
                            if(drained.getGas() != ((HybridFluidGas) this.requirementCheck).asGasStack().getGas()) {
                                return false;
                            }
                            this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drained.amount, 0));
                            return this.requirementCheck.getAmount() <= 0;
                        }
                        break;
                    case OUTPUT:
                        if(this.required instanceof HybridFluidGas) {
                            gasTank = (HybridGasTank) CopyHandlerHelper.copyTank(gasTank);

                            for (ComponentOutputRestrictor restrictor : restrictions) {
                                if(restrictor instanceof ComponentOutputRestrictor.RestrictionTank) {
                                    ComponentOutputRestrictor.RestrictionTank tank = (ComponentOutputRestrictor.RestrictionTank) restrictor;

                                    if(tank.exactComponent.equals(component) && tank.inserted instanceof HybridFluidGas) {
                                        gasTank.receiveGas(EnumFacing.UP, ((HybridFluidGas) this.required).asGasStack(), true);
                                    }
                                }
                            }
                            int gasFilled = gasTank.receiveGas(EnumFacing.UP, ((HybridFluidGas) this.required).asGasStack(), false);
                            boolean didFill = gasFilled >= this.required.getAmount();
                            if(didFill) {
                                context.addRestriction(new ComponentOutputRestrictor.RestrictionTank(this.required.copy(), component));
                            }
                            return didFill;
                        }
                }
            }
            switch (getActionType()) {
                case INPUT:
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    FluidStack drained = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), false);
                    if(drained == null) {
                        return false;
                    }
                    this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drained.amount, 0));
                    return this.requirementCheck.getAmount() <= 0;
                case OUTPUT:
                    handler = CopyHandlerHelper.copyTank(handler);

                    for (ComponentOutputRestrictor restrictor : restrictions) {
                        if(restrictor instanceof ComponentOutputRestrictor.RestrictionTank) {
                            ComponentOutputRestrictor.RestrictionTank tank = (ComponentOutputRestrictor.RestrictionTank) restrictor;

                            if(tank.exactComponent.equals(component) && !(tank.inserted instanceof HybridFluidGas)) {
                                handler.fillInternal(tank.inserted == null ? null : tank.inserted.copy().asFluidStack(), true);
                            }
                        }
                    }
                    int filled = handler.fillInternal(this.required.copy().asFluidStack(), false); //True or false doesn't really matter tbh
                    boolean didFill = filled >= this.required.getAmount();
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
            HybridTank handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case INPUT:
                    if(ModularMachinery.isMekanismLoaded) {
                        return startCraftingWithMekanismHandling(handler, chance);
                    }

                    //If it doesn't consume the item, we only need to see if it's actually there.
                    FluidStack drainedSimulated = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), false);
                    if(drainedSimulated == null) {
                        return false;
                    }
                    if(!NBTMatchingHelper.matchNBTCompound(this.tagMatch, drainedSimulated.tag)) {
                        return false;
                    }
                    if(this.doesntConsumeInput) {
                        this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drainedSimulated.amount, 0));
                        return this.requirementCheck.getAmount() <= 0;
                    }
                    FluidStack actualDrained = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), true);
                    if(actualDrained == null) {
                        return false;
                    }
                    if(!NBTMatchingHelper.matchNBTCompound(this.tagMatch, actualDrained.tag)) {
                        return false;
                    }
                    this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - actualDrained.amount, 0));
                    return this.requirementCheck.getAmount() <= 0;
            }
            return false;
        }

        @Optional.Method(modid = "mekanism")
        private boolean startCraftingWithMekanismHandling(HybridTank handler, ResultChance chance) {
            if(this.requirementCheck instanceof HybridFluidGas && handler instanceof HybridGasTank) {
                HybridGasTank gasHandler = (HybridGasTank) handler;

                GasStack drainSimulated = gasHandler.drawGas(EnumFacing.UP, this.requirementCheck.getAmount(), false);
                if(drainSimulated == null) {
                    return false;
                }
                if(drainSimulated.getGas() != ((HybridFluidGas) this.requirementCheck).asGasStack().getGas()) {
                    return false;
                }
                if(this.doesntConsumeInput) {
                    this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drainSimulated.amount, 0));
                    return this.requirementCheck.getAmount() <= 0;
                }
                GasStack actualDrain = gasHandler.drawGas(EnumFacing.UP, this.requirementCheck.getAmount(), true);
                if(actualDrain == null) {
                    return false;
                }
                if(actualDrain.getGas() != ((HybridFluidGas) this.requirementCheck).asGasStack().getGas()) {
                    return false;
                }
                this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - actualDrain.amount, 0));
                return this.requirementCheck.getAmount() <= 0;

            } else {
                FluidStack drainedSimulated = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), false);
                if(drainedSimulated == null) {
                    return false;
                }
                if(!NBTMatchingHelper.matchNBTCompound(this.tagMatch, drainedSimulated.tag)) {
                    return false;
                }
                if(this.doesntConsumeInput) {
                    this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drainedSimulated.amount, 0));
                    return this.requirementCheck.getAmount() <= 0;
                }
                FluidStack actualDrained = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), true);
                if(actualDrained == null) {
                    return false;
                }
                if(!NBTMatchingHelper.matchNBTCompound(this.tagMatch, actualDrained.tag)) {
                    return false;
                }
                this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - actualDrained.amount, 0));
                return this.requirementCheck.getAmount() <= 0;
            }
        }

        @Override
        public boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
            if(component.getComponentType() != MachineComponent.ComponentType.FLUID ||
                    !(component instanceof MachineComponent.FluidHatch) ||
                    component.getIOType() != getActionType()) return false;
            HybridTank handler = context.getFluidHandler(component);
            switch (getActionType()) {
                case OUTPUT:
                    if(ModularMachinery.isMekanismLoaded) {
                        return finishWithMekanismHandling(handler, chance);
                    } else {
                        FluidStack outStack = this.required.asFluidStack();
                        if(outStack != null) {
                            int fillableAmount = handler.fillInternal(outStack.copy(), false);
                            if(chance.canProduce(this.chance)) {
                                return fillableAmount >= outStack.amount;
                            }
                            FluidStack copyOut = outStack.copy();
                            if(this.tagDisplay != null ){
                                copyOut.tag = this.tagDisplay.copy();
                            }
                            return fillableAmount >= outStack.amount && handler.fillInternal(copyOut.copy(), true) >= copyOut.amount;
                        }
                    }
            }
            return false;
        }

        @Optional.Method(modid = "mekanism")
        private boolean finishWithMekanismHandling(HybridTank handler, ResultChance chance) {
            if(this.required instanceof HybridFluidGas && handler instanceof HybridGasTank) {
                GasStack gasOut = ((HybridFluidGas) this.required).asGasStack();
                HybridGasTank gasTankHandler = (HybridGasTank) handler;
                int fillableGas = gasTankHandler.receiveGas(EnumFacing.UP, gasOut, false);
                if(chance.canProduce(this.chance)) {
                    return fillableGas >= gasOut.amount;
                }
                return fillableGas >= gasOut.amount && gasTankHandler.receiveGas(EnumFacing.UP, gasOut, true) >= gasOut.amount;
            } else {
                FluidStack outStack = this.required.asFluidStack();
                if(outStack != null) {
                    int fillableAmount = handler.fillInternal(outStack.copy(), false);
                    if(chance.canProduce(this.chance)) {
                        return fillableAmount >= outStack.amount;
                    }
                    FluidStack copyOut = outStack.copy();
                    if(this.tagDisplay != null ){
                        copyOut.tag = this.tagDisplay.copy();
                    }
                    return fillableAmount >= outStack.amount && handler.fillInternal(copyOut.copy(), true) >= copyOut.amount;
                }
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

        public int countIOBuffer = 0;

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
        public void startRequirementCheck(ResultChance contextChance) {
            switch (this.requirementType) {
                case ITEMSTACKS:
                    this.countIOBuffer = this.required.getCount();
                    break;
                case OREDICT:
                    this.countIOBuffer = this.oreDictItemAmount;
                    break;
                case FUEL:
                    this.countIOBuffer = this.fuelBurntime;
                    break;
            }
        }

        @Override
        public void endRequirementCheck() {
            this.countIOBuffer = 0;
        }

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
                    ItemStack stack = ItemUtils.copyStackWithSize(required, this.countIOBuffer);
                    if(tag != null) {
                        stack.setTagCompound(tag.copy());
                    }
                    int inserted = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, true);
                    if(inserted > 0) {
                        context.addRestriction(new ComponentOutputRestrictor.RestrictionInventory(ItemUtils.copyStackWithSize(stack, inserted), component));
                    }
                    this.countIOBuffer -= inserted;
                    return this.countIOBuffer <= 0;
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
                    ItemStack stack = ItemUtils.copyStackWithSize(required, this.countIOBuffer);
                    if(tag != null) {
                        stack.setTagCompound(tag);
                    }
                    //If we don't produce the item, we only need to see if there would be space for it at all.
                    int inserted = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, true);
                    if(chance.canProduce(this.chance)) {
                        return inserted > 0;
                    }
                    if(inserted > 0) {
                        int actual = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, false);
                        this.countIOBuffer -= actual;
                        return this.countIOBuffer <= 0;
                    }
                    return false;
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
