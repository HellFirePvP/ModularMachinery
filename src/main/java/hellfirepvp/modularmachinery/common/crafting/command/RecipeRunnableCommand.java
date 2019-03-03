/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.command;

import com.google.gson.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.lang.reflect.Type;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeRunnableCommand
 * Created by HellFirePvP
 * Date: 03.03.2019 / 21:46
 */
public class RecipeRunnableCommand {

    private String command;
    private int interval;

    public RecipeRunnableCommand(String command) {
        this(command, -1);
    }

    public RecipeRunnableCommand(String command, int interval) {
        this.command = command;
        this.interval = MathHelper.clamp(interval, -1, Integer.MAX_VALUE);
    }

    public void run(ICommandSender sender, int tick) {
        World world = sender.getEntityWorld();
        if (!world.isRemote && (!hasInterval() || (tick % getInterval()) == 0)) {
            MinecraftServer server = sender.getServer();
            if (server != null) {
                server.getCommandManager().executeCommand(sender, this.command);
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public int getInterval() {
        return interval;
    }

    public boolean hasInterval() {
        return interval != -1;
    }

    public static class Deserializer implements JsonDeserializer<RecipeRunnableCommand> {

        @Override
        public RecipeRunnableCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new RecipeRunnableCommand(json.getAsString());
            } else if (json.isJsonObject()) {
                JsonObject objCommand = json.getAsJsonObject();
                if (!objCommand.has("command")) {
                    throw new JsonParseException("Tried to deserialize recipe command, found no 'command' string in command object!");
                }
                JsonElement elementCommandString = objCommand.get("command");
                if (!elementCommandString.isJsonPrimitive()) {
                    throw new JsonParseException("Command specified in 'command' of recipe command object is not a string!");
                }
                int interval = -1;
                String command = elementCommandString.getAsString();
                if (objCommand.has("interval")) {
                    JsonElement elementInterval = objCommand.get("interval");
                    if (!elementInterval.isJsonPrimitive() ||
                            !elementInterval.getAsJsonPrimitive().isNumber()) {
                        throw new JsonParseException("Interval specified in 'interval' of recipe command object is not an int!");
                    }
                    interval = elementInterval.getAsInt();
                }
                return new RecipeRunnableCommand(command, interval);
            }
            throw new JsonParseException("Tried to deserialize recipe command, found neither command nor object describing command and optional interval!");
        }
    }

}
