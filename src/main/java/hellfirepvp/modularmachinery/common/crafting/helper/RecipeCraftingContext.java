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
import hellfirepvp.modularmachinery.common.util.handlers.CopyableFluidHandler;
import hellfirepvp.modularmachinery.common.util.handlers.CopyableItemHandler;
import hellfirepvp.modularmachinery.common.util.handlers.IEnergyHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeCraftingContext
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:23
 */
public class RecipeCraftingContext {

    private final MachineRecipe recipe;
    private int currentCraftingTick = 0;
    private Map<MachineComponent, CopyableItemHandler> itemComponents = new HashMap<>();
    private Map<MachineComponent, CopyableFluidHandler> fluidComponents = new HashMap<>();
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

    public boolean isValid() {
        RecipeCraftingContext copy = copy();
        lblRequirements:
        for (ComponentRequirement requirement : copy.recipe.getCraftingRequirements()) {
            for (MachineComponent component : getComponentsFor(requirement.getRequiredComponentType())) {
                if(requirement.doComplete(component, this, ResultChance.GUARANTEED)) {
                    continue lblRequirements;
                }
            }
            return false;
        }
        return true;
    }

    public RecipeCraftingContext copy() {
        RecipeCraftingContext newContext = new RecipeCraftingContext(this.recipe);
        newContext.currentCraftingTick = this.currentCraftingTick;
        for (Map.Entry<MachineComponent, CopyableItemHandler> component : this.itemComponents.entrySet()) {
            newContext.itemComponents.put(component.getKey(), component.getValue().copy());
        }
        for (Map.Entry<MachineComponent, CopyableFluidHandler> component : this.fluidComponents.entrySet()) {
            newContext.fluidComponents.put(component.getKey(), component.getValue().copy());
        }
        for (Map.Entry<MachineComponent, IEnergyHandler> component : this.energyComponents.entrySet()) {
            newContext.energyComponents.put(component.getKey(), component.getValue().copy());
        }
        return newContext;
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

    public CopyableItemHandler getItemHandler(MachineComponent component) {
        return itemComponents.get(component);
    }

    public CopyableFluidHandler getFluidHandler(MachineComponent component) {
        return fluidComponents.get(component);
    }

    public IEnergyHandler getEnergyHandler(MachineComponent component) {
        return energyComponents.get(component);
    }

}
