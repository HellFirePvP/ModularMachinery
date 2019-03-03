/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.command;

import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeCommandContainer
 * Created by HellFirePvP
 * Date: 03.03.2019 / 22:03
 */
public class RecipeCommandContainer {

    private final List<RecipeRunnableCommand> startCommands = new ArrayList<>();
    private final List<RecipeRunnableCommand> processingCommands = new ArrayList<>();
    private final List<RecipeRunnableCommand> finishCommands = new ArrayList<>();

    public void addStartCommand(RecipeRunnableCommand command) {
        this.startCommands.add(command);
    }

    public void addProcessingCommand(RecipeRunnableCommand command) {
        this.processingCommands.add(command);
    }

    public void addFinishCommand(RecipeRunnableCommand command) {
        this.finishCommands.add(command);
    }

    public void runStartCommands(ICommandSender sender) {
        this.startCommands.forEach(cmd -> cmd.run(sender, 0));
    }

    public void runTickCommands(ICommandSender sender, int craftTick) {
        this.processingCommands.forEach(cmd -> cmd.run(sender, craftTick));
    }

    public void runFinishCommands(ICommandSender sender) {
        this.finishCommands.forEach(cmd -> cmd.run(sender, 0));
    }

}
