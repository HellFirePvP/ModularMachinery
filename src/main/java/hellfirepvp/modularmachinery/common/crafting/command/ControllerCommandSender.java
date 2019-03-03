/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.command;

import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ControllerCommandSender
 * Created by HellFirePvP
 * Date: 03.03.2019 / 21:42
 */
public class ControllerCommandSender implements ICommandSender {

    private final TileMachineController controller;

    public ControllerCommandSender(TileMachineController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "@[MM-Controller]";
    }

    @Override
    public BlockPos getPosition() {
        return controller.getPos();
    }

    @Override
    public Vec3d getPositionVector() {
        return new Vec3d(controller.getPos());
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    @Override
    public World getEntityWorld() {
        return controller.getWorld();
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return controller.getWorld().getMinecraftServer();
    }
}
