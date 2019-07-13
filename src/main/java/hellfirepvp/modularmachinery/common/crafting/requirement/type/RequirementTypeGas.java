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
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementTypeGas
 * Created by HellFirePvP
 * Date: 13.07.2019 / 11:11
 */
public class RequirementTypeGas extends RequirementTypeFluid {

    public RequirementTypeGas() {
        super(ComponentTypesMM.COMPONENT_GAS);
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "mekanism";
    }

    @Override
    public RequirementFluid createRequirement(IOType type, JsonObject requirement) {
        return this.provideMekGasComponent(type, requirement);
    }

    @Nonnull
    @Optional.Method(modid = "mekanism")
    private RequirementFluid provideMekGasComponent(IOType machineIoType, JsonObject requirement) {
        if(!requirement.has("gas") || !requirement.get("gas").isJsonPrimitive() ||
                !requirement.get("gas").getAsJsonPrimitive().isString()) {
            throw new JsonParseException("The ComponentType 'gas' expects an 'gas'-entry that defines the type of gas!");
        }
        if(!requirement.has("amount") || !requirement.get("amount").isJsonPrimitive() ||
                !requirement.get("amount").getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("The ComponentType 'gas' expects an 'amount'-entry that defines the type of gas!");
        }
        String gasName = requirement.getAsJsonPrimitive("gas").getAsString();
        int mbAmount = requirement.getAsJsonPrimitive("amount").getAsInt();
        Gas gas = GasRegistry.getGas(gasName);
        if(gas == null) {
            throw new JsonParseException("The gas specified in the 'gas'-entry (" + gasName + ") doesn't exist!");
        }
        mbAmount = Math.max(0, mbAmount);
        GasStack gasStack = new GasStack(gas, mbAmount);
        RequirementFluid req = RequirementFluid.createMekanismGasRequirement(RequirementTypesMM.REQUIREMENT_GAS, machineIoType, gasStack);

        if(requirement.has("chance")) {
            if(!requirement.get("chance").isJsonPrimitive() || !requirement.getAsJsonPrimitive("chance").isNumber()) {
                throw new JsonParseException("'chance', if defined, needs to be a chance-number between 0 and 1!");
            }
            float chance = requirement.getAsJsonPrimitive("chance").getAsFloat();
            if(chance >= 0 && chance <= 1) {
                req.setChance(chance);
            }
        }
        return req;
    }
}
