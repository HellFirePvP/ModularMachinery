/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementTypeEnergy
 * Created by HellFirePvP
 * Date: 13.07.2019 / 10:47
 */
public class RequirementTypeEnergy extends RequirementType<Long, RequirementEnergy> {

    public RequirementTypeEnergy() {
        this(ComponentTypesMM.COMPONENT_ENERGY);
    }

    protected RequirementTypeEnergy(@Nullable ComponentType requiredType) {
        super(requiredType);
    }

    @Override
    public RequirementEnergy createRequirement(IOType type, JsonObject requirement) {
        if(!requirement.has("energyPerTick") || !requirement.get("energyPerTick").isJsonPrimitive() ||
                !requirement.get("energyPerTick").getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("The ComponentType 'energy' expects an 'energyPerTick'-entry that defines the amount of energy per tick!");
        }
        long energyPerTick = requirement.getAsJsonPrimitive("energyPerTick").getAsLong();
        return new RequirementEnergy(type, energyPerTick);
    }
}
