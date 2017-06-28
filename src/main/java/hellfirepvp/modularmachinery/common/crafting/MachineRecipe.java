/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Lists;
import com.google.gson.*;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRecipe
 * Created by HellFirePvP
 * Date: 27.06.2017 / 00:24
 */
public class MachineRecipe {

    private final String recipeFilePath;
    private final ResourceLocation owningMachine;
    private final int tickTime;
    private final List<ComponentRequirement> recipeRequirements = Lists.newArrayList();

    public MachineRecipe(String path, ResourceLocation owningMachine, int tickTime) {
        this.recipeFilePath = path;
        this.owningMachine = owningMachine;
        this.tickTime = tickTime;
    }

    public String getRecipeFilePath() {
        return recipeFilePath;
    }

    public ResourceLocation getOwningMachineIdentifier() {
        return owningMachine;
    }

    public List<ComponentRequirement> getCraftingRequirements() {
        return Collections.unmodifiableList(recipeRequirements);
    }

    public int getRecipeTotalTickTime() {
        return tickTime;
    }

    @Nullable
    public DynamicMachine getOwningMachine() {
        return MachineRegistry.getRegistry().getMachine(getOwningMachineIdentifier());
    }

    public static class Deserializer implements JsonDeserializer<MachineRecipe> {

        @Override
        public MachineRecipe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();
            if(!root.has("machine")) {
                throw new JsonParseException("No 'machine'-entry specified!");
            }
            if(!root.has("recipeTime")) {
                throw new JsonParseException("No 'recipeTime'-entry specified!");
            }
            JsonElement elementMachine = root.get("machine");
            if(!elementMachine.isJsonPrimitive() || !elementMachine.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'machine' has to have as value only a String that defines its owning machine!");
            }
            JsonElement elementTime = root.get("recipeTime");
            if(!elementTime.isJsonPrimitive() || !elementTime.getAsJsonPrimitive().isNumber()) {
                throw new JsonParseException("'recipeTime' has to be a number!");
            }
            String name = elementMachine.getAsJsonPrimitive().getAsString();
            int recipeTime = elementTime.getAsJsonPrimitive().getAsInt();
            MachineRecipe recipe = new MachineRecipe(RecipeLoader.currentlyReadingPath,
                    new ResourceLocation(ModularMachinery.MODID, name), recipeTime);

            if(!root.has("requirements")) {
                throw new JsonParseException("No 'requirements'-entry specified!");
            }
            JsonElement elementRequirements = root.get("requirements");
            if(!elementRequirements.isJsonArray()) {
                throw new JsonParseException("'requirements' should be an array of recipe requirements!");
            }
            JsonArray requirementsArray = elementRequirements.getAsJsonArray();
            for (int i = 0; i < requirementsArray.size(); i++) {
                JsonElement elementRequirement = requirementsArray.get(i);
                if(!elementRequirement.isJsonObject()) {
                    throw new JsonParseException("Each element in the 'requirements' array needs to be a fully defined requirement-object!");
                }
                recipe.recipeRequirements.add(deserializeRequirement(elementRequirement.getAsJsonObject()));
            }
            if(recipe.recipeRequirements.isEmpty()) {
                throw new JsonParseException("A recipe needs to have at least 1 requirement!");
            }
            return recipe;
        }

        @Nonnull
        private ComponentRequirement deserializeRequirement(JsonObject requirement) throws JsonParseException {
            if(!requirement.has("type") || !requirement.get("type").isJsonPrimitive() ||
                    !requirement.get("type").getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'type' of a requirement is missing or isn't a string!");
            }
            String type = requirement.getAsJsonPrimitive("type").getAsString();
            if(!requirement.has("io-type") || !requirement.get("io-type").isJsonPrimitive() ||
                    !requirement.get("io-type").getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'io-type' of a requirement is missing or isn't a string!");
            }
            String ioType = requirement.getAsJsonPrimitive("io-type").getAsString();

            MachineComponent.ComponentType machineComponentType = MachineComponent.ComponentType.getByString(type);
            if(machineComponentType == null) {
                throw new JsonParseException("'type' is not a valid ComponentType!");
            }
            MachineComponent.IOType machineIoType = MachineComponent.IOType.getByString(ioType);
            if(machineIoType == null) {
                throw new JsonParseException("'io-type' is not a valid IOType!");
            }
            ComponentRequirement req;
            switch (machineComponentType) {
                case ITEM:
                    if(!requirement.has("item") || !requirement.get("item").isJsonPrimitive() ||
                            !requirement.get("item").getAsJsonPrimitive().isString()) {
                        throw new JsonParseException("The ComponentType 'item' expects an 'item'-entry that defines the item!");
                    }
                    String itemDefinition = requirement.getAsJsonPrimitive("item").getAsString();
                    int meta = 0;
                    int indexMeta = itemDefinition.indexOf('@');
                    if(indexMeta != -1 && indexMeta != itemDefinition.length() - 1) {
                        try {
                            meta = Integer.parseInt(itemDefinition.substring(indexMeta + 1));
                        } catch (NumberFormatException exc) {
                            throw new JsonParseException("Expected a metadata number, got " + itemDefinition.substring(indexMeta + 1), exc);
                        }
                        itemDefinition = itemDefinition.substring(0, indexMeta);
                    }
                    ResourceLocation res = new ResourceLocation(itemDefinition);
                    Item item = ForgeRegistries.ITEMS.getValue(res);
                    if(item == null || item == Items.AIR) {
                        throw new JsonParseException("Couldn't find item with registryName '" + res.toString() + "' !");
                    }
                    ItemStack result;
                    if(meta > 0) {
                        result = new ItemStack(item, 1, meta);
                    } else {
                        result = new ItemStack(item);
                    }
                    req =
                            new ComponentRequirement.RequirementItem(machineComponentType, machineIoType, result);
                    if(requirement.has("chance")) {
                        if(!requirement.get("chance").isJsonPrimitive() || !requirement.getAsJsonPrimitive("chance").isNumber()) {
                            throw new JsonParseException("'chance', if defined, needs to be a chance-number between 0 and 1!");
                        }
                        float chance = requirement.getAsJsonPrimitive("chance").getAsFloat();
                        if(chance >= 0 && chance <= 1) {
                            ((ComponentRequirement.RequirementItem) req).setChance(chance);
                        }
                    }
                    return req;
                case FLUID:
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
                    req = new ComponentRequirement.RequirementFluid(machineComponentType, machineIoType, fluidStack);

                    if(requirement.has("chance")) {
                        if(!requirement.get("chance").isJsonPrimitive() || !requirement.getAsJsonPrimitive("chance").isNumber()) {
                            throw new JsonParseException("'chance', if defined, needs to be a chance-number between 0 and 1!");
                        }
                        float chance = requirement.getAsJsonPrimitive("chance").getAsFloat();
                        if(chance >= 0 && chance <= 1) {
                            ((ComponentRequirement.RequirementFluid) req).setChance(chance);
                        }
                    }
                    return req;
                case ENERGY:
                    if(!requirement.has("energyPerTick") || !requirement.get("energyPerTick").isJsonPrimitive() ||
                            !requirement.get("energyPerTick").getAsJsonPrimitive().isNumber()) {
                        throw new JsonParseException("The ComponentType 'energy' expects an 'energyPerTick'-entry that defines the amount of energy per tick!");
                    }
                    int energyPerTick = requirement.getAsJsonPrimitive("energyPerTick").getAsInt();
                    req = new ComponentRequirement.RequirementEnergy(machineComponentType, machineIoType, energyPerTick);
                    return req;
            }
            throw new JsonParseException("Unknown machine component type: " + type);
        }

    }

}
