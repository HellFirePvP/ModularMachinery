/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.crafting.requirement.jei.JEIComponentEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeEnergy;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementEnergy
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:26
 */
public class RequirementEnergy extends ComponentRequirement.PerTick<Long, RequirementTypeEnergy> {

    public final long requirementPerTick;
    private long activeIO;

    public RequirementEnergy(IOType ioType, long requirementPerTick) {
        super(RequirementTypesMM.REQUIREMENT_ENERGY, ioType);
        this.requirementPerTick = requirementPerTick;
        this.activeIO = this.requirementPerTick;
    }

    @Override
    public int getSortingWeight() {
        return PRIORITY_WEIGHT_ENERGY;
    }

    @Override
    public ComponentRequirement<Long, RequirementTypeEnergy> deepCopy() {
        RequirementEnergy energy = new RequirementEnergy(this.getActionType(), this.requirementPerTick);
        energy.activeIO = this.activeIO;
        return energy;
    }

    @Override
    public ComponentRequirement<Long, RequirementTypeEnergy> deepCopyModified(List<RecipeModifier> modifiers) {
        int requirement = Math.round(RecipeModifier.applyModifiers(modifiers, this, this.requirementPerTick, false));
        RequirementEnergy energy = new RequirementEnergy(this.getActionType(), requirement);
        energy.activeIO = this.activeIO;
        return energy;
    }

    @Override
    public void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context) {}

    @Override
    public void endRequirementCheck() {}

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        ResourceLocation compKey = this.getRequirementType().getRegistryName();
        return String.format("component.missing.%s.%s.%s",
                compKey.getResourceDomain(), compKey.getResourcePath(), ioType.name().toLowerCase());
    }

    public long getRequiredEnergyPerTick() {
        return requirementPerTick;
    }

    @Override
    public JEIComponent<Long> provideJEIComponent() {
        return new JEIComponentEnergy(this);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType().equals(this.getRequirementType().getComponentType()) &&
                cmp instanceof MachineComponent.EnergyHatch &&
                cmp.getIOType() == this.getActionType();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        IEnergyHandler handler = (IEnergyHandler) component.getProvidedComponent();
        switch (getActionType()) {
            case INPUT:
                if(handler.getCurrentEnergy() >= RecipeModifier.applyModifiers(context, this, this.requirementPerTick, false)) {
                    return CraftCheck.success();
                }
                break;
            case OUTPUT:
                return CraftCheck.success();
        }
        return CraftCheck.failure("craftcheck.failure.energy.input");
    }

    @Override
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        return canStartCrafting(component, context, Lists.newArrayList()).isSuccess();
    }

    @Override
    public boolean finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        return true;
    }

    @Override
    public void startIOTick(RecipeCraftingContext context, float durationMultiplier) {
        this.activeIO = Math.round(((double) RecipeModifier.applyModifiers(context, this, this.activeIO, false)) * durationMultiplier);
    }

    @Nonnull
    @Override
    public CraftCheck resetIOTick(RecipeCraftingContext context) {
        boolean enough = this.activeIO <= 0;
        this.activeIO = this.requirementPerTick;
        return enough ? CraftCheck.success() : CraftCheck.failure("craftcheck.failure.energy.input");
    }

    @Nonnull
    @Override
    public CraftCheck doIOTick(ProcessingComponent<?> component, RecipeCraftingContext context) {
        IEnergyHandler handler = (IEnergyHandler) component.getProvidedComponent();
        switch (getActionType()) {
            case INPUT:
                if(handler.getCurrentEnergy() >= this.activeIO) {
                    handler.setCurrentEnergy(handler.getCurrentEnergy() - this.activeIO);
                    this.activeIO = 0;
                    return CraftCheck.success();
                } else {
                    this.activeIO -= handler.getCurrentEnergy();
                    handler.setCurrentEnergy(0);
                    return CraftCheck.partialSuccess();
                }
            case OUTPUT:
                long remaining = handler.getRemainingCapacity();
                if(remaining - this.activeIO < 0) {
                    handler.setCurrentEnergy(handler.getMaxEnergy());
                    this.activeIO -= remaining;
                    return CraftCheck.partialSuccess();
                }
                handler.setCurrentEnergy(Math.min(handler.getCurrentEnergy() + this.activeIO, handler.getMaxEnergy()));
                this.activeIO = 0;
                return CraftCheck.success();
        }
        //This is neither input nor output? when do we actually end up in this case down here?
        return CraftCheck.skipComponent();
    }
}
