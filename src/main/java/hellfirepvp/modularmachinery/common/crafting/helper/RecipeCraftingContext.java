/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
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
    private Map<MachineComponent, IItemHandlerModifiable> itemComponents = new HashMap<>();
    private Map<MachineComponent, IFluidHandler> fluidComponents = new HashMap<>();
    private Map<MachineComponent, IEnergyHandler> energyComponents = new HashMap<>();

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

            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                if(requirement.startCrafting(component, this, chance)) {
                    break;
                }
            }
        }
    }

    public void finishCrafting() {
        finishCrafting(RAND.nextLong());
    }

    public void finishCrafting(long seed) {
        ResultChance chance = new ResultChance(seed);
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getActionType() == MachineComponent.IOType.INPUT) continue;

            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                if(requirement.finishCrafting(component, this, chance)) {
                    break;
                }
            }
        }
    }

    public boolean canStartCrafting() {
        lblRequirements:
        for (ComponentRequirement requirement : recipe.getCraftingRequirements()) {
            if(requirement.getRequiredComponentType() == MachineComponent.ComponentType.ENERGY &&
                    requirement.getActionType() == MachineComponent.IOType.OUTPUT) continue;

            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                if(requirement.canStartCrafting(component, this)) {
                    continue lblRequirements;
                }
            }
            return false;
        }
        return true;
    }

    public void addComponent(MachineComponent component) {
        switch (component.getComponentType()) {
            case ITEM:
                itemComponents.put(component, ((MachineComponent.ItemBus) component).getInventory());
                break;
            case FLUID:
                fluidComponents.put(component, ((MachineComponent.FluidHatch) component).getTank());
                break;
            case ENERGY:
                energyComponents.put(component, ((MachineComponent.EnergyHatch) component).getEnergyBuffer());
                break;
        }
        throw new IllegalArgumentException("Tried to add component for illegal ComponentType: " + component.getComponentType());
    }

    public IItemHandlerModifiable getItemHandler(MachineComponent component) {
        return itemComponents.get(component);
    }

    public IFluidHandler getFluidHandler(MachineComponent component) {
        return fluidComponents.get(component);
    }

    public IEnergyHandler getEnergyHandler(MachineComponent component) {
        return energyComponents.get(component);
    }

}
