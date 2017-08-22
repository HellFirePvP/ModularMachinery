/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.*;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRecipe
 * Created by HellFirePvP
 * Date: 27.06.2017 / 00:24
 */
public class MachineRecipe implements Comparable<MachineRecipe> {

    private static int counter = 0;
    private static boolean frozen = false;
    private static final int PRIORITY_WEIGHT_ENERGY = 50_000_000;
    private static final int PRIORITY_WEIGHT_FLUID  = 1_000_000;
    private static final int PRIORITY_WEIGHT_ITEM   = 5_000_000;

    private final int sortId;
    private final String recipeFilePath;
    private final ResourceLocation owningMachine, registryName;
    private final int tickTime;
    private final List<ComponentRequirement> recipeRequirements = Lists.newArrayList();
    private final int configuredPriority;

    public MachineRecipe(String path, ResourceLocation registryName, ResourceLocation owningMachine, int tickTime, int configuredPriority) {
        this.sortId = counter;
        counter++;
        this.recipeFilePath = path;
        this.registryName = registryName;
        this.owningMachine = owningMachine;
        this.tickTime = tickTime;
        this.configuredPriority = configuredPriority;
    }

    public String getRecipeFilePath() {
        return recipeFilePath;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public ResourceLocation getOwningMachineIdentifier() {
        return owningMachine;
    }

    public List<ComponentRequirement> getCraftingRequirements() {
        return Collections.unmodifiableList(recipeRequirements);
    }

    public void addRequirement(ComponentRequirement requirement) {
        if(frozen) {
            throw new IllegalStateException("Tried to add Requirement after recipes have been registered!");
        }
        this.recipeRequirements.add(requirement);
    }

    public int getRecipeTotalTickTime() {
        return tickTime;
    }

    public int getConfiguredPriority() {
        return configuredPriority;
    }

    @Nullable
    public DynamicMachine getOwningMachine() {
        return MachineRegistry.getRegistry().getMachine(getOwningMachineIdentifier());
    }

    static boolean isFrozen() {
        return frozen;
    }

    static void freezeChanges() {
        frozen = true;
    }

    static void unfreeze() {
        frozen = false;
    }

    @Override
    public int compareTo(MachineRecipe o) {
        return Integer.compare(buildWeight(), o.buildWeight());
    }

    private int buildWeight() {
        int weightOut = sortId;
        for (ComponentRequirement req : this.recipeRequirements) {
            if(req.getActionType() == MachineComponent.IOType.OUTPUT) continue;
            switch (req.getRequiredComponentType()) {
                case ITEM:
                    weightOut -= PRIORITY_WEIGHT_ITEM;
                    break;
                case FLUID:
                    weightOut -= PRIORITY_WEIGHT_FLUID;
                    break;
                case ENERGY:
                    weightOut -= PRIORITY_WEIGHT_ENERGY;
                    break;
            }
        }
        return weightOut;
    }

    public static class MachineRecipeContainer {

        private final MachineRecipe parent;
        private List<ResourceLocation> recipeOwnerList = Lists.newLinkedList();

        private MachineRecipeContainer(MachineRecipe copyParent) {
            this.parent = copyParent;
        }

        public List<MachineRecipe> getRecipes() {
            List<MachineRecipe> out = Lists.newArrayListWithCapacity(recipeOwnerList.size());
            for (int i = 0; i < recipeOwnerList.size(); i++) {
                ResourceLocation location = recipeOwnerList.get(i);
                MachineRecipe rec = new MachineRecipe(parent.recipeFilePath + "_sub_" + i,
                        new ResourceLocation(parent.registryName.getResourceDomain(), parent.registryName.getResourcePath() + "_sub_" + i),
                        location, parent.tickTime, parent.configuredPriority);
                for (ComponentRequirement req : parent.recipeRequirements) {
                    rec.recipeRequirements.add(req.deepCopy());
                }
                out.add(rec);
            }
            return out;
        }

    }

    public static class Deserializer implements JsonDeserializer<MachineRecipeContainer> {

        @Override
        public MachineRecipeContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();
            if(!root.has("machine")) {
                throw new JsonParseException("No 'machine'-entry specified!");
            }
            if(!root.has("registryName")) {
                throw new JsonParseException("No 'registryName'-entry specified!");
            }
            if(!root.has("recipeTime")) {
                throw new JsonParseException("No 'recipeTime'-entry specified!");
            }
            JsonElement elementMachine = root.get("machine");
            List<ResourceLocation> qualifiedMachineNames = Lists.newLinkedList();
            if(elementMachine.isJsonArray()) {
                if(elementMachine.getAsJsonArray().size() <= 0) {
                    throw new JsonParseException("'machine' is an array, but it's empty! Provide at least 1 owning-machine name!");
                }
                JsonArray jar = elementMachine.getAsJsonArray();
                for (JsonElement je : jar) {
                    if(je.isJsonPrimitive() && je.getAsJsonPrimitive().isString()) {
                        qualifiedMachineNames.add(new ResourceLocation(ModularMachinery.MODID, je.getAsJsonPrimitive().getAsString()));
                        continue;
                    }
                    throw new JsonParseException("Found an element in the array specified in 'machine' that is not a string! " + je.toString());
                }
                if(qualifiedMachineNames.isEmpty()) {
                    //We capture this before already, but just to be safe...
                    throw new JsonParseException("'machine' is an array, but it's empty! Provide at least 1 owning-machine name!");
                }
            } else if(elementMachine.isJsonPrimitive() && elementMachine.getAsJsonPrimitive().isString()) {
                qualifiedMachineNames.add(new ResourceLocation(ModularMachinery.MODID, elementMachine.getAsJsonPrimitive().getAsString()));
            } else {
                throw new JsonParseException("'machine' has to be either an array of strings or just a string! - Found " + elementMachine.toString() + " instead!");
            }
            JsonElement elementRegistryName = root.get("registryName");
            if(!elementRegistryName.isJsonPrimitive() || !elementRegistryName.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'registryName' has to have as value only a String that defines its unique registry name!");
            }
            JsonElement elementTime = root.get("recipeTime");
            if(!elementTime.isJsonPrimitive() || !elementTime.getAsJsonPrimitive().isNumber()) {
                throw new JsonParseException("'recipeTime' has to be a number!");
            }
            int priority = 0;
            if(root.has("priority")) {
                JsonElement elementPriority = root.get("priority");
                if(!elementPriority.isJsonPrimitive() || !elementPriority.getAsJsonPrimitive().isNumber()) {
                    throw new JsonParseException("'priority' has to be a number! (if specified)");
                }
                priority = elementPriority.getAsInt();
            }

            ResourceLocation parentName = Iterables.getFirst(qualifiedMachineNames, null);
            if(parentName == null) {
                //This actually never happens. Never. But just to be sure and prevent weird issues down the line.
                throw new IllegalStateException("Couldn't find machine name from qualified-names list: " + Arrays.toString(qualifiedMachineNames.toArray()));
            }

            String registryName = elementRegistryName.getAsJsonPrimitive().getAsString();
            int recipeTime = elementTime.getAsJsonPrimitive().getAsInt();
            MachineRecipe recipe = new MachineRecipe(RecipeLoader.currentlyReadingPath,
                    new ResourceLocation(ModularMachinery.MODID, registryName),
                    parentName, recipeTime, priority);

            MachineRecipeContainer outContainer = new MachineRecipeContainer(recipe);
            outContainer.recipeOwnerList.addAll(qualifiedMachineNames);

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
            return outContainer;
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
                    int amount = 1;
                    if(requirement.has("amount")) {
                        if(!requirement.get("amount").isJsonPrimitive() || !requirement.getAsJsonPrimitive("amount").isNumber()) {
                            throw new JsonParseException("'amount', if defined, needs to be a amount-number!");
                        }
                        amount = MathHelper.clamp(requirement.getAsJsonPrimitive("amount").getAsInt(), 1, 64);
                    }
                    ResourceLocation res = new ResourceLocation(itemDefinition);
                    if(res.getResourceDomain().equalsIgnoreCase("ore")) {
                        if(machineIoType == MachineComponent.IOType.OUTPUT) {
                            throw new JsonParseException("You cannot define an oredict as item output! Offending oredict entry: " + res.toString());
                        }
                        req = new ComponentRequirement.RequirementItem(machineIoType, itemDefinition.substring(4), amount);
                    } else {
                        Item item = ForgeRegistries.ITEMS.getValue(res);
                        if(item == null || item == Items.AIR) {
                            throw new JsonParseException("Couldn't find item with registryName '" + res.toString() + "' !");
                        }
                        ItemStack result;
                        if(meta > 0) {
                            result = new ItemStack(item, amount, meta);
                        } else {
                            result = new ItemStack(item, amount);
                        }
                        req = new ComponentRequirement.RequirementItem(machineIoType, result);
                    }
                    if(requirement.has("chance")) {
                        if(!requirement.get("chance").isJsonPrimitive() || !requirement.getAsJsonPrimitive("chance").isNumber()) {
                            throw new JsonParseException("'chance', if defined, needs to be a chance-number between 0 and 1!");
                        }
                        float chance = requirement.getAsJsonPrimitive("chance").getAsFloat();
                        if(chance >= 0 && chance <= 1) {
                            ((ComponentRequirement.RequirementItem) req).setChance(chance);
                        }
                    }
                    if (requirement.has("nbt")) {
                        if(!requirement.has("nbt") || !requirement.get("nbt").isJsonObject()) {
                            throw new JsonParseException("The ComponentType 'nbt' expects a json compound that defines the NBT tag!");
                        }
                        String nbtString = requirement.getAsJsonObject("nbt").toString();
                        try {
                            ((ComponentRequirement.RequirementItem) req).tag = NBTJsonDeserializer.deserialize(nbtString);
                        } catch (NBTException exc) {
                            throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                        }

                        if (requirement.has("nbt-display")) {
                            if(!requirement.has("nbt-display") || !requirement.get("nbt-display").isJsonObject()) {
                                throw new JsonParseException("The ComponentType 'nbt-display' expects a json compound that defines the NBT tag meant to be used for displaying!");
                            }
                            String nbtDisplayString = requirement.getAsJsonObject("nbt-display").toString();
                            try {
                                ((ComponentRequirement.RequirementItem) req).previewDisplayTag = NBTJsonDeserializer.deserialize(nbtDisplayString);
                            } catch (NBTException exc) {
                                throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                            }
                        } else {
                            ((ComponentRequirement.RequirementItem) req).previewDisplayTag = ((ComponentRequirement.RequirementItem) req).tag.copy();
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
                    req = new ComponentRequirement.RequirementFluid(machineIoType, fluidStack);

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
                    req = new ComponentRequirement.RequirementEnergy(machineIoType, energyPerTick);
                    return req;
            }
            throw new JsonParseException("Unknown machine component type: " + type);
        }

    }

}
