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
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentFluid
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:10
 */
public class ComponentFluid extends ComponentType<RequirementFluid> {

    @Nonnull
    @Override
    public String getRegistryName() {
        return "fluid";
    }

    @Nullable
    @Override
    public String requiresModid() {
        return null;
    }

    @Nonnull
    @Override
    public RequirementFluid provideComponent(MachineComponent.IOType machineIoType, JsonObject requirement) {
        RequirementFluid req;

        if(!requirement.has("fluid") || !requirement.get("fluid").isJsonPrimitive() ||
                !requirement.get("fluid").getAsJsonPrimitive().isString()) {
            throw new JsonParseException("The ComponentType 'fluid' expects an 'fluid'-entry that defines the type of fluid!");
        }
        if(!requirement.has("amount") || !requirement.get("amount").isJsonPrimitive() ||
                !requirement.get("amount").getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("The ComponentType 'fluid' expects an 'amount'-entry that defines the type of fluid!");
        }
        String fluidName = requirement.getAsJsonPrimitive("fluid").getAsString();
        int mbAmount = requirement.getAsJsonPrimitive("amount").getAsInt();
        Fluid f = FluidRegistry.getFluid(fluidName);
        if(f == null) {
            throw new JsonParseException("The fluid specified in the 'fluid'-entry (" + fluidName + ") doesn't exist!");
        }
        mbAmount = Math.max(0, mbAmount);
        FluidStack fluidStack = new FluidStack(f, mbAmount);
        req = new RequirementFluid(ComponentType.Registry.getComponent("fluid"), machineIoType, fluidStack);

        if(requirement.has("chance")) {
            if(!requirement.get("chance").isJsonPrimitive() || !requirement.getAsJsonPrimitive("chance").isNumber()) {
                throw new JsonParseException("'chance', if defined, needs to be a chance-number between 0 and 1!");
            }
            float chance = requirement.getAsJsonPrimitive("chance").getAsFloat();
            if(chance >= 0 && chance <= 1) {
                req.setChance(chance);
            }
        }
        if (requirement.has("nbt")) {
            if (!requirement.has("nbt") || !requirement.get("nbt").isJsonObject()) {
                throw new JsonParseException("The ComponentType 'nbt' expects a json compound that defines the NBT tag!");
            }
            String nbtString = requirement.getAsJsonObject("nbt").toString();
            try {
                req.setMatchNBTTag(NBTJsonDeserializer.deserialize(nbtString));
            } catch (NBTException exc) {
                throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
            }
            if (requirement.has("nbt-display")) {
                if(!requirement.has("nbt-display") || !requirement.get("nbt-display").isJsonObject()) {
                    throw new JsonParseException("The ComponentType 'nbt-display' expects a json compound that defines the NBT tag meant to be used for displaying!");
                }
                String nbtDisplayString = requirement.getAsJsonObject("nbt-display").toString();
                try {
                    req.setDisplayNBTTag(NBTJsonDeserializer.deserialize(nbtDisplayString));
                } catch (NBTException exc) {
                    throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                }
            } else {
                req.setDisplayNBTTag(req.getTagMatch());
            }
        }
        return req;
    }

}
