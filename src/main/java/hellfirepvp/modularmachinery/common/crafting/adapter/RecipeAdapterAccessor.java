/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.adapter;

import com.google.gson.*;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeAdapterAccessor
 * Created by HellFirePvP
 * Date: 23.07.2017 / 14:27
 */
public class RecipeAdapterAccessor {

    private final ResourceLocation owningMachine, adapterKey;

    private RecipeAdapterAccessor(ResourceLocation owningMachine, ResourceLocation adapterKey) {
        this.owningMachine = owningMachine;
        this.adapterKey = adapterKey;
    }

    public ResourceLocation getOwningMachine() {
        return owningMachine;
    }

    public ResourceLocation getAdapterKey() {
        return adapterKey;
    }

    public static class Deserializer implements JsonDeserializer<RecipeAdapterAccessor> {

        @Override
        public RecipeAdapterAccessor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();
            if(!root.has("machine")) {
                throw new JsonParseException("No 'machine'-entry specified!");
            }
            if(!root.has("adapter")) {
                throw new JsonParseException("No 'adapter'-entry specified!");
            }
            JsonElement elementMachine = root.get("machine");
            if(!elementMachine.isJsonPrimitive() || !elementMachine.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'machine' has to have as value only a String that defines its owning machine!");
            }
            JsonElement elementAdapter = root.get("adapter");
            if(!elementAdapter.isJsonPrimitive() || !elementAdapter.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'adapter' has to have as value only a String that defines the name of the adapter!");
            }
            ResourceLocation owningMachineKey = new ResourceLocation(ModularMachinery.MODID, elementMachine.getAsJsonPrimitive().getAsString());
            ResourceLocation adapterKey = new ResourceLocation(elementAdapter.getAsJsonPrimitive().getAsString());
            return new RecipeAdapterAccessor(owningMachineKey, adapterKey);
        }
    }

}
