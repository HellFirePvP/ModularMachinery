/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
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
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementEnergy;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
    private static final int PRIORITY_WEIGHT_FLUID  = 100;
    private static final int PRIORITY_WEIGHT_ITEM   = 50_000;

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
        if(requirement instanceof RequirementEnergy) {
            for (ComponentRequirement req : this.recipeRequirements) {
                if(req instanceof RequirementEnergy && req.getActionType() == requirement.getActionType()) {
                    throw new IllegalStateException("Tried to add multiple energy requirements for the same ioType! Please only add one for each ioType!");
                }
            }
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

    public MachineRecipe deepCopy(Function<ResourceLocation, ResourceLocation> registryNameChange,
                                  ResourceLocation newOwningMachineIdentifier) {
        boolean freeze = false;
        if (isFrozen()) {
            freeze = true;
            unfreeze();
        }
        MachineRecipe copy = new MachineRecipe(this.getRecipeFilePath(),
                registryNameChange.apply(this.getRegistryName()),
                newOwningMachineIdentifier,
                this.getRecipeTotalTickTime(),
                this.getConfiguredPriority());

        for (ComponentRequirement<?> requirement : this.getCraftingRequirements()) {
            copy.addRequirement(requirement.deepCopy());
        }

        if (freeze) {
            freezeChanges();
        }
        return copy;
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
            switch (req.getRequiredComponentType().getRegistryName().toLowerCase()) {
                case "item":
                    weightOut -= PRIORITY_WEIGHT_ITEM;
                    break;
                case "fluid":
                    weightOut -= PRIORITY_WEIGHT_FLUID;
                    break;
                case "energy":
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
            if(!root.has("registryName") && !root.has("registryname")) {
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
            if(elementRegistryName == null) {
                elementRegistryName = root.get("registryname");
            }
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

            ComponentType<?> componentType = ComponentType.Registry.getComponent(type);
            if(componentType == null) {
                throw new JsonParseException("'" + type + "' is not a valid ComponentType!");
            }
            MachineComponent.IOType machineIoType = MachineComponent.IOType.getByString(ioType);
            if(machineIoType == null) {
                throw new JsonParseException("'" + ioType + "' is not a valid IOType!");
            }
            return componentType.provideComponent(machineIoType, requirement);
        }

    }

}
