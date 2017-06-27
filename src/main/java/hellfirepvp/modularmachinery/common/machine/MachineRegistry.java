/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraftforge.registries.*;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRegistry
 * Created by HellFirePvP
 * Date: 27.06.2017 / 11:53
 */
public class MachineRegistry {

    private static MachineRegistry INSTANCE = new MachineRegistry();
    private static Map<ResourceLocation, DynamicMachine> REGISTRY_MACHINERY;

    private MachineRegistry() {}

    public static MachineRegistry getRegistry() {
        return INSTANCE;
    }

    @Nullable
    public DynamicMachine getMachine(ResourceLocation name) {
        return REGISTRY_MACHINERY.get(name);
    }

    public void buildRegistry() {
        REGISTRY_MACHINERY = new HashMap<>();
    }

    public void initializeAndLoad() {
        Map<MachineLoader.FileType, List<File>> candidates = MachineLoader.discoverDirectory(CommonProxy.dataHolder.getMachineryDirectory());
        MachineLoader.prepareContext(candidates.get(MachineLoader.FileType.VARIABLES));
        Map<String, Exception> failures = MachineLoader.captureFailedAttempts();
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading variables!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load variables of " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        List<DynamicMachine> found = MachineLoader.loadMachines(candidates.get(MachineLoader.FileType.MACHINE));
        failures = MachineLoader.captureFailedAttempts();
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading machinery!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load machinery " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        for (DynamicMachine m : found) {
            REGISTRY_MACHINERY.put(m.getRegistryName(), m);
        }
    }

}
