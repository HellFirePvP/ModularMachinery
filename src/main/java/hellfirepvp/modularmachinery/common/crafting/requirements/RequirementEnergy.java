/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirements;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentOutputRestrictor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirements.jei.JEIComponentEnergy;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import hellfirepvp.modularmachinery.common.util.ResultChance;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementEnergy
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:26
 */
public class RequirementEnergy extends ComponentRequirement.PerTick<Long> {

    public final long requirementPerTick;
    private long activeIO;

    public RequirementEnergy(MachineComponent.IOType ioType, long requirementPerTick) {
        super(ComponentType.Registry.getComponent("energy"), ioType);
        this.requirementPerTick = requirementPerTick;
        this.activeIO = this.requirementPerTick;
    }

    @Override
    public ComponentRequirement<Long> deepCopy() {
        RequirementEnergy energy = new RequirementEnergy(this.getActionType(), this.requirementPerTick);
        energy.activeIO = this.activeIO;
        return energy;
    }

    @Override
    public void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context) {}

    @Override
    public void endRequirementCheck() {}

    public long getRequiredEnergyPerTick() {
        return requirementPerTick;
    }

    @Override
    public JEIComponent<Long> provideJEIComponent() {
        return new JEIComponentEnergy(this);
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        if(!component.getComponentType().equals(this.getRequiredComponentType()) ||
                !(component instanceof MachineComponent.EnergyHatch) ||
                component.getIOType() != getActionType()) return CraftCheck.skipComponent();

        IEnergyHandler handler = (IEnergyHandler) context.getProvidedCraftingComponent(component);
        switch (getActionType()) {
            case INPUT:
                if(handler.getCurrentEnergy() >= context.applyModifiers(this, getActionType(), this.requirementPerTick, false)) {
                    return CraftCheck.success();
                }
                break;
            case OUTPUT:
                return CraftCheck.success();
        }
        return CraftCheck.failure("craftcheck.failure.energy.input");
    }

    @Override
    public boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
        return canStartCrafting(component, context, Lists.newArrayList()).isSuccess();
    }

    @Override
    public boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
        return true;
    }

    @Override
    public void startIOTick(RecipeCraftingContext context, float durationMultiplier) {
        this.activeIO = Math.round(((double) context.applyModifiers(this, getActionType(), this.activeIO, false)) * durationMultiplier);
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
    public CraftCheck doIOTick(MachineComponent component, RecipeCraftingContext context) {
        if(!component.getComponentType().equals(this.getRequiredComponentType()) ||
                !(component instanceof MachineComponent.EnergyHatch) ||
                component.getIOType() != getActionType()) return CraftCheck.skipComponent();
        IEnergyHandler handler = (IEnergyHandler) context.getProvidedCraftingComponent(component);
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
