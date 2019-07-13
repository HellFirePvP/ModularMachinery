/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentHybridFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidGas;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.CopyHandlerHelper;
import hellfirepvp.modularmachinery.common.util.HybridGasTank;
import hellfirepvp.modularmachinery.common.util.HybridTank;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import hellfirepvp.modularmachinery.common.util.nbt.NBTMatchingHelper;
import mekanism.api.gas.GasStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementFluid
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:28
 */
public class RequirementFluid extends ComponentRequirement<HybridFluid, RequirementTypeFluid> implements ComponentRequirement.ChancedRequirement {

    public final HybridFluid required;
    public float chance = 1F;

    private HybridFluid requirementCheck;
    private boolean doesntConsumeInput;

    private NBTTagCompound tagMatch = null, tagDisplay = null;

    public RequirementFluid(IOType ioType, FluidStack fluid) {
        this(RequirementTypesMM.REQUIREMENT_FLUID, ioType, new HybridFluid(fluid));
    }

    private RequirementFluid(RequirementTypeFluid type, IOType ioType, HybridFluid required) {
        super(type, ioType);
        this.required = required.copy();
        this.requirementCheck = this.required.copy();
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "mekanism")
    public static RequirementFluid createMekanismGasRequirement(RequirementTypeFluid type, IOType ioType, GasStack gasStack) {
        return new RequirementFluid(type, ioType, new HybridFluidGas(gasStack));
    }

    @Override
    public int getSortingWeight() {
        return PRIORITY_WEIGHT_FLUID;
    }

    @Override
    public ComponentRequirement<HybridFluid, RequirementTypeFluid> deepCopy() {
        RequirementFluid fluid = new RequirementFluid(this.getRequirementType(), this.getActionType(), this.required.copy());
        fluid.chance = this.chance;
        fluid.tagMatch = getTagMatch();
        fluid.tagDisplay = getTagDisplay();
        return fluid;
    }

    @Override
    public ComponentRequirement<HybridFluid, RequirementTypeFluid> deepCopyModified(List<RecipeModifier> modifiers) {
        HybridFluid hybrid = this.required.copy();
        hybrid.setAmount(Math.round(RecipeModifier.applyModifiers(modifiers, this, hybrid.getAmount(), false)));
        RequirementFluid fluid = new RequirementFluid(this.getRequirementType(), this.getActionType(), hybrid);

        fluid.chance = RecipeModifier.applyModifiers(modifiers, this, this.chance, true);
        fluid.tagMatch = getTagMatch();
        fluid.tagDisplay = getTagDisplay();
        return fluid;
    }

    @Override
    public JEIComponent<HybridFluid> provideJEIComponent() {
        return new JEIComponentHybridFluid(this);
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

    @Override
    public void setChance(float chance) {
        this.chance = chance;
    }

    @Override
    public void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context) {
        this.requirementCheck = this.required.copy();
        this.requirementCheck.setAmount(Math.round(RecipeModifier.applyModifiers(context, this, this.requirementCheck.getAmount(), false)));
        this.doesntConsumeInput = contextChance.canProduce(RecipeModifier.applyModifiers(context, this, this.chance, true));
    }

    @Override
    public void endRequirementCheck() {
        this.requirementCheck = this.required.copy();
        this.doesntConsumeInput = true;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        ResourceLocation compKey = this.getRequirementType().getRegistryName();
        return String.format("component.missing.%s.%s.%s",
                compKey.getResourceDomain(), compKey.getResourcePath(), ioType.name().toLowerCase());
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType().equals(this.getRequirementType().getComponentType()) &&
                cmp instanceof MachineComponent.FluidHatch &&
                cmp.getIOType() == this.getActionType();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        HybridTank handler = (HybridTank) component.getProvidedComponent();

        if(Mods.MEKANISM.isPresent()) {
            java.util.Optional<CraftCheck> check = checkStartCraftingWithMekanism(component, context, handler, restrictions);
            if (check.isPresent()) {
                return check.get();
            }
        }

        switch (getActionType()) {
            case INPUT:
                //If it doesn't consume the item, we only need to see if it's actually there.
                FluidStack drained = handler.drainInternal(this.requirementCheck.copy().asFluidStack(), false);
                if(drained == null) {
                    return CraftCheck.failure("craftcheck.failure.fluid.input");
                }
                if(!NBTMatchingHelper.matchNBTCompound(this.tagMatch, drained.tag)) {
                    return CraftCheck.failure("craftcheck.failure.fluid.input");
                }
                this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drained.amount, 0));
                if(this.requirementCheck.getAmount() <= 0) {
                    return CraftCheck.success();
                }
                return CraftCheck.failure("craftcheck.failure.fluid.input");
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
                int filled = handler.fillInternal(this.requirementCheck.copy().asFluidStack(), false); //True or false doesn't really matter tbh
                boolean didFill = filled >= this.requirementCheck.getAmount();
                if(didFill) {
                    context.addRestriction(new ComponentOutputRestrictor.RestrictionTank(this.requirementCheck.copy(), component));
                }
                if(didFill) {
                    return CraftCheck.success();
                }
                return CraftCheck.failure("craftcheck.failure.fluid.output.space");
        }
        return CraftCheck.skipComponent();
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "mekanism")
    private Optional<CraftCheck> checkStartCraftingWithMekanism(ProcessingComponent<?> component,
                                                                RecipeCraftingContext context,
                                                                HybridTank handler,
                                                                List<ComponentOutputRestrictor> restrictions) {
        if(handler instanceof HybridGasTank) {
            HybridGasTank gasTank = (HybridGasTank) handler;
            switch (getActionType()) {
                case INPUT:
                    if(this.requirementCheck instanceof HybridFluidGas) {
                        GasStack drained = gasTank.drawGas(EnumFacing.UP, this.requirementCheck.getAmount(), false);
                        if(drained == null) {
                            return Optional.of(CraftCheck.failure("craftcheck.failure.gas.input"));
                        }
                        if(drained.getGas() != ((HybridFluidGas) this.requirementCheck).asGasStack().getGas()) {
                            return Optional.of(CraftCheck.failure("craftcheck.failure.gas.input"));
                        }
                        this.requirementCheck.setAmount(Math.max(this.requirementCheck.getAmount() - drained.amount, 0));
                        if(this.requirementCheck.getAmount() <= 0) {
                            return Optional.of(CraftCheck.success());
                        }
                        return Optional.of(CraftCheck.failure("craftcheck.failure.gas.input"));
                    }
                    break;
                case OUTPUT:
                    if(this.requirementCheck instanceof HybridFluidGas) {
                        gasTank = (HybridGasTank) CopyHandlerHelper.copyTank(gasTank);

                        for (ComponentOutputRestrictor restrictor : restrictions) {
                            if(restrictor instanceof ComponentOutputRestrictor.RestrictionTank) {
                                ComponentOutputRestrictor.RestrictionTank tank = (ComponentOutputRestrictor.RestrictionTank) restrictor;

                                if(tank.exactComponent.equals(component) && tank.inserted instanceof HybridFluidGas) {
                                    gasTank.receiveGas(EnumFacing.UP, ((HybridFluidGas) this.requirementCheck).asGasStack(), true);
                                }
                            }
                        }

                        int gasFilled = gasTank.receiveGas(EnumFacing.UP, ((HybridFluidGas) this.requirementCheck).asGasStack(), false);
                        boolean didFill = gasFilled >= this.requirementCheck.getAmount();
                        if(didFill) {
                            context.addRestriction(new ComponentOutputRestrictor.RestrictionTank(this.requirementCheck.copy(), component));
                        }
                        if(didFill) {
                            return Optional.of(CraftCheck.success());
                        }
                        return Optional.of(CraftCheck.failure("craftcheck.failure.gas.output.space"));
                    }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        HybridTank handler = (HybridTank) component.getProvidedComponent();
        switch (getActionType()) {
            case INPUT:
                if(Mods.MEKANISM.isPresent()) {
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

    @net.minecraftforge.fml.common.Optional.Method(modid = "mekanism")
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
    public boolean finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        HybridTank handler = (HybridTank) component.getProvidedComponent();
        switch (getActionType()) {
            case OUTPUT:
                if(Mods.MEKANISM.isPresent()) {
                    return finishWithMekanismHandling(handler, context, chance);
                } else {
                    FluidStack outStack = this.requirementCheck.asFluidStack();
                    if(outStack != null) {
                        int fillableAmount = handler.fillInternal(outStack.copy(), false);
                        if(chance.canProduce(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
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

    @net.minecraftforge.fml.common.Optional.Method(modid = "mekanism")
    private boolean finishWithMekanismHandling(HybridTank handler, RecipeCraftingContext context, ResultChance chance) {
        if(this.requirementCheck instanceof HybridFluidGas && handler instanceof HybridGasTank) {
            GasStack gasOut = ((HybridFluidGas) this.requirementCheck).asGasStack();
            HybridGasTank gasTankHandler = (HybridGasTank) handler;
            int fillableGas = gasTankHandler.receiveGas(EnumFacing.UP, gasOut, false);
            if(chance.canProduce(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
                return fillableGas >= gasOut.amount;
            }
            return fillableGas >= gasOut.amount && gasTankHandler.receiveGas(EnumFacing.UP, gasOut, true) >= gasOut.amount;
        } else {
            FluidStack outStack = this.requirementCheck.asFluidStack();
            if(outStack != null) {
                int fillableAmount = handler.fillInternal(outStack.copy(), false);
                if(chance.canProduce(RecipeModifier.applyModifiers(context, this, this.chance, true))) {
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
