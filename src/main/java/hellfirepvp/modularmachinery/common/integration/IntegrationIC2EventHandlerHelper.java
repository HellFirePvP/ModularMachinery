/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationIC2EventHandlerHelper
 * Created by HellFirePvP
 * Date: 17.08.2017 / 00:22
 */
public class IntegrationIC2EventHandlerHelper {

    public static void fireLoadEvent(World world, IEnergyTile tileEnergyInputHatch) {
        MinecraftServer ms = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(ms != null) {
            ms.addScheduledTask(() -> {
                if(!world.isRemote) {
                    MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(tileEnergyInputHatch));
                }
            });
        }
    }

}
