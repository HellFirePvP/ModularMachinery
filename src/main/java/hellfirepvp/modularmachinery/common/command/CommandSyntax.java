/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.command;

import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandSyntax
 * Created by HellFirePvP
 * Date: 16.08.2017 / 15:00
 */
public class CommandSyntax extends CommandBase {

    @Override
    public String getName() {
        return "mm-syntax";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.modularmachinery.syntax";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        player.sendMessage(new TextComponentString("Testing Machines:"));
        MachineRegistry.getRegistry().loadMachines(player);
        player.sendMessage(new TextComponentString(""));
        player.sendMessage(new TextComponentString("Testing Recipes:"));
        RecipeRegistry.getRegistry().loadRecipes(player);
    }

}
