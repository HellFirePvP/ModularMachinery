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
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementFluid;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentGas
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:10
 */
public class ComponentGas extends ComponentType {

    @Nonnull
    @Override
    public String getRegistryName() {
        return "gas";
    }

    @Nullable
    @Override
    public String requiresModid() {
        return "mekanism";
    }

    @Nonnull
    @Override
    public ComponentRequirement provideComponent(MachineComponent.IOType machineIoType, JsonObject jsonObject) {
        return provideMekGasComponent(machineIoType, jsonObject);
    }

    @Nonnull
    @Optional.Method(modid = "mekanism")
    private ComponentRequirement provideMekGasComponent(MachineComponent.IOType machineIoType, JsonObject requirement) {
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
        RequirementFluid req = RequirementFluid.createMekanismGasRequirement(ComponentType.Registry.getComponent("gas"), machineIoType, gasStack);

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
