/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementDuration
 * Created by HellFirePvP
 * Date: 13.07.2019 / 12:32
 */
//This is actually only a helper type for recipe modifiers
public class RequirementDuration extends RequirementType<Object, ComponentRequirement<Object, RequirementDuration>> {

    @Override
    public ComponentRequirement<Object, ? extends RequirementType<Object, ComponentRequirement<Object, RequirementDuration>>> createRequirement(IOType type, JsonObject jsonObject) {
        throw new UnsupportedOperationException("Cannot instantiate requirement for duration!");
    }
}
