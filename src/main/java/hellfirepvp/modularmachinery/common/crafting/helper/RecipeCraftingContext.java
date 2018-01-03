/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.HybridTank;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeCraftingContext
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:23
 */
public class RecipeCraftingContext {

    private static final Random RAND = new Random();

    private final MachineRecipe recipe;
    private int currentCraftingTick = 0;
    private Map<MachineComponent, IOInventory> itemComponents = new HashMap<>();
    private Map<MachineComponent, HybridTank> fluidComponents = new HashMap<>();
    private Map<MachineComponent, IEnergyHandler> energyComponents = new HashMap<>();

    private List<ComponentOutputRestrictor> currentRestrictions = Lists.newArrayList();

    public RecipeCraftingContext(MachineRecipe recipe) {
        this.recipe = recipe;
    }

    public MachineRecipe getParentRecipe() {
        return recipe;
    }

    public void setCurrentCraftingTick(int currentCraftingTick) {
        this.currentCraftingTick = currentCraftingTick;
    }

    public int getCurrentCraftingTick() {
        return currentCraftingTick;
    }

    public void addRestriction(ComponentOutputRestrictor restrictor) {
        this.currentRestrictions.add(restrictor);
    }

    public Collection<MachineComponent> getComponentsFor(MachineComponent.ComponentType type) {
        switch (type) {
            case ITEM:
                return Collections.unmodifiableCollection(this.itemComponents.keySet());
            case FLUID:
                return Collections.unmodifiableCollection(this.fluidComponents.keySet());
            case ENERGY:
                return Collections.unmodifiableCollection(this.energyComponents.keySet());
        }
        throw new IllegalArgumentException("Tried to get components for illegal ComponentType: " + type);
    }

    public boolean energyTick() {
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getRequiredComponentType() != MachineComponent.ComponentType.ENERGY ||
                    requirement.getActionType() == MachineComponent.IOType.OUTPUT) continue;
            ComponentRequirement.RequirementEnergy energyRequirement = (ComponentRequirement.RequirementEnergy) requirement;

            energyRequirement.resetEnergyIO();
            boolean enough = false;
            for (MachineComponent component : getComponentsFor(MachineComponent.ComponentType.ENERGY)) {
                if(energyRequirement.handleEnergyIO(component, this) <= 0) {
                    enough = true;
                    break;
                }
            }
            energyRequirement.resetEnergyIO();
            if(!enough) {
                return false;
            }
        }
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getRequiredComponentType() != MachineComponent.ComponentType.ENERGY ||
                    requirement.getActionType() == MachineComponent.IOType.INPUT) continue;
            ComponentRequirement.RequirementEnergy energyRequirement = (ComponentRequirement.RequirementEnergy) requirement;

            energyRequirement.resetEnergyIO();
            for (MachineComponent component : getComponentsFor(MachineComponent.ComponentType.ENERGY)) {
                energyRequirement.handleEnergyIO(component, this);
            }
            energyRequirement.resetEnergyIO();
        }
        return true;
    }

    public void startCrafting() {
        startCrafting(RAND.nextLong());
    }

    public void startCrafting(long seed) {
        ResultChance chance = new ResultChance(seed);
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getActionType() == MachineComponent.IOType.OUTPUT) continue;

            requirement.startRequirementCheck(chance);
            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                if(requirement.startCrafting(component, this, chance)) {
                    requirement.endRequirementCheck();
                    break;
                }
            }
            requirement.endRequirementCheck();
        }
    }

    public void finishCrafting() {
        finishCrafting(RAND.nextLong());
    }

    public void finishCrafting(long seed) {
        ResultChance chance = new ResultChance(seed);
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getActionType() == MachineComponent.IOType.INPUT) continue;

            requirement.startRequirementCheck(chance);
            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                if(requirement.finishCrafting(component, this, chance)) {
                    requirement.endRequirementCheck();
                    break;
                }
            }
            requirement.endRequirementCheck();
        }
    }

    public ComponentRequirement.CraftCheck canStartCrafting() {
        currentRestrictions.clear();

        lblRequirements:
        for (ComponentRequirement requirement : recipe.getCraftingRequirements()) {
            if(requirement.getRequiredComponentType() == MachineComponent.ComponentType.ENERGY &&
                    requirement.getActionType() == MachineComponent.IOType.OUTPUT) {

                for (MachineComponent component : getComponentsFor(MachineComponent.ComponentType.ENERGY)) {
                    if(component.getIOType() == MachineComponent.IOType.OUTPUT) {
                        continue lblRequirements; //Check if it has at least 1 energy output.
                    }
                }
                return ComponentRequirement.CraftCheck.FAILURE_MISSING_INPUT;
            }

            requirement.startRequirementCheck(ResultChance.GUARANTEED);

            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                ComponentRequirement.CraftCheck check = requirement.canStartCrafting(component, this, this.currentRestrictions);
                if(check == ComponentRequirement.CraftCheck.SUCCESS) {
                    requirement.endRequirementCheck();
                    continue lblRequirements;
                }
            }

            requirement.endRequirementCheck();
            currentRestrictions.clear();
            return requirement.getRequiredComponentType() == MachineComponent.ComponentType.ENERGY ?
                    ComponentRequirement.CraftCheck.FAILURE_MISSING_ENERGY :
                    ComponentRequirement.CraftCheck.FAILURE_MISSING_INPUT;
        }
        currentRestrictions.clear();
        return ComponentRequirement.CraftCheck.SUCCESS;
    }

    public void addComponent(MachineComponent component) {
        switch (component.getComponentType()) {
            case ITEM:
                itemComponents.put(component, ((MachineComponent.ItemBus) component).getInventory());
                return;
            case FLUID:
                fluidComponents.put(component, ((MachineComponent.FluidHatch) component).getTank());
                return;
            case ENERGY:
                energyComponents.put(component, ((MachineComponent.EnergyHatch) component).getEnergyBuffer());
                return;
        }
        throw new IllegalArgumentException("Tried to add component for illegal ComponentType: " + component.getComponentType());
    }

    public IOInventory getItemHandler(MachineComponent component) {
        return itemComponents.get(component);
    }

    public HybridTank getFluidHandler(MachineComponent component) {
        return fluidComponents.get(component);
    }

    public IEnergyHandler getEnergyHandler(MachineComponent component) {
        return energyComponents.get(component);
    }

}
