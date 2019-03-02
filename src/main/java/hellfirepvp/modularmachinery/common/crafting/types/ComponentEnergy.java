/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementEnergy;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentEnergy
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:10
 */
public class ComponentEnergy extends ComponentType<RequirementEnergy> {

    @Nonnull
    @Override
    public String getRegistryName() {
        return "energy";
    }

    @Nullable
    @Override
    public String requiresModid() {
        return null;
    }

    @Nonnull
    @Override
    public RequirementEnergy provideComponent(MachineComponent.IOType machineIoType, JsonObject requirement) {
        if(!requirement.has("energyPerTick") || !requirement.get("energyPerTick").isJsonPrimitive() ||
                !requirement.get("energyPerTick").getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("The ComponentType 'energy' expects an 'energyPerTick'-entry that defines the amount of energy per tick!");
        }
        long energyPerTick = requirement.getAsJsonPrimitive("energyPerTick").getAsLong();
        return new RequirementEnergy(machineIoType, energyPerTick);
    }

}
