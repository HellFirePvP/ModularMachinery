/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

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

    public MachineRecipe(String path, ResourceLocation owningMachine) {
        this.recipeFilePath = path;
        this.owningMachine = owningMachine;
    }

    public String getRecipeFilePath() {
        return recipeFilePath;
    }

    public ResourceLocation getOwningMachineIdentifier() {
        return owningMachine;
    }

    @Nullable
    public DynamicMachine getOwningMachine() {
        return MachineRegistry.getRegistry().getMachine(getOwningMachineIdentifier());
    }

    public static class Deserializer implements JsonDeserializer<MachineRecipe> {

        @Override
        public MachineRecipe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return null;
        }

    }

}
