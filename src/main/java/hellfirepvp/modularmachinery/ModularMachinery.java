/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery;

import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModularMachinery
 * Created by HellFirePvP
 * Date: 26.06.2017 / 20:26
 */
@Mod(modid = ModularMachinery.MODID, name = ModularMachinery.NAME, version = ModularMachinery.VERSION,
        acceptedMinecraftVersions="[1.12]", dependencies = "required-after:forge@[14.21.0.2371,)")
public class ModularMachinery {

    public static final String MODID = "modularmachinery";
    public static final String NAME = "Modular Machinery";
    public static final String VERSION = "0.1";
    public static final String CLIENT_PROXY = "hellfirepvp.modularmachinery.client.ClientProxy";
    public static final String COMMON_PROXY = "hellfirepvp.modularmachinery.common.CommonProxy";

    private static boolean devEnvChache = false;

    @Mod.Instance(MODID)
    public static ModularMachinery instance;

    public static Logger log = LogManager.getLogger(NAME);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = VERSION;
        devEnvChache = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

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
    }

    public static boolean isRunningInDevEnvironment() {
        return devEnvChache;
    }

    static {
        FluidRegistry.enableUniversalBucket();
    }

}
