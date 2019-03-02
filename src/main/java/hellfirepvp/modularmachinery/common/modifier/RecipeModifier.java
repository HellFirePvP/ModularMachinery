/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.modifier;

import com.google.gson.*;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeModifier
 * Created by HellFirePvP
 * Date: 30.03.2018 / 10:48
 */
public class RecipeModifier {

    public static final String TARGET_ITEM = "item";
    public static final String TARGET_FLUID = "fluid";
    public static final String TARGET_ENERGY = "energy";
    public static final String TARGET_GAS = "gas";
    public static final String TARGET_DURATION = "duration";

    public static final String IO_INPUT = "input";
    public static final String IO_OUTPUT = "output";

    public static final int OPERATION_ADD = 0;
    public static final int OPERATION_MULTIPLY = 1;

    protected final String target;
    protected final MachineComponent.IOType ioTarget;
    protected final float modifier;
    protected final int operation;
    protected final boolean chance;

    public RecipeModifier(String target, MachineComponent.IOType ioTarget, float modifier, int operation, boolean affectsChance) {
        this.target = target;
        this.ioTarget = ioTarget;
        this.modifier = modifier;
        this.operation = operation;
        this.chance = affectsChance;
    }

    public String getTarget() {
        return target;
    }

    public MachineComponent.IOType getIOTarget() {
        return ioTarget;
    }

    public float getModifier() {
        return modifier;
    }

    public boolean affectsChance() {
        return chance;
    }

    public int getOperation() {
        return operation;
    }

    public static float applyModifiers(RecipeCraftingContext context, ComponentRequirement<?> in, float value, boolean isChance) {
        String target = in.getRequiredComponentType().getRegistryName();
        return applyModifiers(context.getModifiers(target), target, in.getActionType(), value, isChance);
    }

    public static float applyModifiers(Collection<RecipeModifier> modifiers, ComponentRequirement<?> in, float value, boolean isChance) {
        return applyModifiers(modifiers, in.getRequiredComponentType().getRegistryName(), in.getActionType(), value, isChance);
    }

    public static float applyModifiers(Collection<RecipeModifier> modifiers, String target, MachineComponent.IOType ioType, float value, boolean isChance) {
        List<RecipeModifier> applicable = modifiers
                .stream()
                .filter(mod -> mod.getTarget().equals(target) &&
                        (ioType == null || mod.getIOTarget() == ioType) &&
                        mod.affectsChance() == isChance)
                .collect(Collectors.toList());
        float add = 0F;
        float mul = 1F;
        for (RecipeModifier mod : applicable) {
            if(mod.getOperation() == 0) {
                add += mod.getModifier();
            } else if(mod.getOperation() == 1) {
                mul *= mod.getModifier();
            } else {
                throw new RuntimeException("Unknown modifier operation: " + mod.getOperation());
            }
        }
        return (value + add) * mul;
    }

    public static class Deserializer implements JsonDeserializer<RecipeModifier> {

        @Override
        public RecipeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject part = json.getAsJsonObject();
            if(!part.has("io") || !part.get("io").isJsonPrimitive() || !part.getAsJsonPrimitive("io").isString()) {
                throw new JsonParseException("'io' string-tag not found when deserializing recipemodifier!");
            }
            String ioTarget = part.getAsJsonPrimitive("io").getAsString();
            MachineComponent.IOType ioType = MachineComponent.IOType.getByString(ioTarget);
            if(ioType == null) {
                throw new JsonParseException("Unknown machine iotype: " + ioTarget);
            }
            if(!part.has("target") || !part.get("target").isJsonPrimitive() || !part.getAsJsonPrimitive("target").isString()) {
                throw new JsonParseException("'target' string-tag not found when deserializing recipemodifier!");
            }
            String target = part.getAsJsonPrimitive("target").getAsString();
            if(!target.equalsIgnoreCase("duration")) {
                ComponentType<?> type = ComponentType.Registry.getComponent(target);
                if(type == null) {
                    throw new JsonParseException("'target' has to be a recipe-component! Unknown component: " + target);
                }
            }
            if(!part.has("multiplier") || !part.get("multiplier").isJsonPrimitive() || !part.getAsJsonPrimitive("multiplier").isNumber()) {
                throw new JsonParseException("'multiplier' float-tag not found when deserializing recipemodifier!");
            }
            float multiplier = part.getAsJsonPrimitive("multiplier").getAsFloat();
            if(!part.has("operation") || !part.get("operation").isJsonPrimitive() || !part.getAsJsonPrimitive("operation").isNumber()) {
                throw new JsonParseException("'operation' int-tag not found when deserializing recipemodifier!");
            }
            int operation = part.getAsJsonPrimitive("operation").getAsInt();
            if(operation < 0 || operation > 1) {
                throw new JsonParseException("There are currently only operation 0 and 1 available (add and multiply operations)! Found: " + operation);
            }
            boolean affectsChance = false;
            if(part.has("affectChance")) {
                if(!part.get("affectChance").isJsonPrimitive() || !part.getAsJsonPrimitive("affectChance").isBoolean()) {
                    throw new JsonParseException("'affectChance', if defined, needs to be either true or false!");
                }
                affectsChance = part.getAsJsonPrimitive("affectChance").getAsBoolean();
            }
            return new RecipeModifier(target, ioType, multiplier, operation, affectsChance);
        }
    }

}
