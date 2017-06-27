/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common;

import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.data.ModDataHolder;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.registry.RegistrationBus;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 26.06.2017 / 21:00
 */
public class CommonProxy {

    public static ModDataHolder dataHolder = new ModDataHolder();

    public void loadModData(File configDir) {
        dataHolder.setup(configDir);
    }

    public void preInit() {
        MachineRegistry.getRegistry().buildRegistry();
        RecipeRegistry.getRegistry().buildRegistry();
        MinecraftForge.EVENT_BUS.register(new RegistrationBus());
    }

    public void init() {
        MachineRegistry.getRegistry().initializeAndLoad();
        RecipeRegistry.getRegistry().initializeAndLoad();

    }

    public void postInit() {

    }

}
