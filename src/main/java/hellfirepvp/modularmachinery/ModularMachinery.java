/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.command.CommandHand;
import hellfirepvp.modularmachinery.common.command.CommandSyntax;
import hellfirepvp.modularmachinery.common.network.PktCopyToClipboard;
import hellfirepvp.modularmachinery.common.network.PktSyncSelection;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModularMachinery
 * Created by HellFirePvP
 * Date: 26.06.2017 / 20:26
 */
@Mod(modid = ModularMachinery.MODID, name = ModularMachinery.NAME, version = ModularMachinery.VERSION,
acceptedMinecraftVersions = "[1.12, 1.13)", dependencies = "required-after:forge@[14.21.0.2371,);after:crafttweaker@[4.0.4,)")
public class ModularMachinery {

    public static final String MODID = "modularmachinery";
    public static final String NAME = "Modular Machinery";
    public static final String VERSION = "1.6.5";
    public static final String CLIENT_PROXY = "hellfirepvp.modularmachinery.client.ClientProxy";
    public static final String COMMON_PROXY = "hellfirepvp.modularmachinery.common.CommonProxy";

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    private static boolean devEnvChache = false;

    @Mod.Instance(MODID)
    public static ModularMachinery instance;

    public static Logger log;
    public static boolean isMekanismLoaded = false;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = VERSION;
        log = event.getModLog();
        devEnvChache = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        isMekanismLoaded = Loader.isModLoaded("mekanism");

        NET_CHANNEL.registerMessage(PktCopyToClipboard.class, PktCopyToClipboard.class, 0, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktSyncSelection.class, PktSyncSelection.class, 1, Side.CLIENT);

        proxy.loadModData(event.getModConfigurationDirectory());

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        //Cmd registration
        event.registerServerCommand(new CommandSyntax());
        event.registerServerCommand(new CommandHand());
    }

    public static boolean isRunningInDevEnvironment() {
        return devEnvChache;
    }

    static {
        FluidRegistry.enableUniversalBucket();
    }

}
