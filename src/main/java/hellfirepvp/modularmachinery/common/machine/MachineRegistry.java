/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import com.google.common.collect.ImmutableList;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.data.DataLoadProfiler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.registries.*;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRegistry
 * Created by HellFirePvP
 * Date: 27.06.2017 / 11:53
 */
public class MachineRegistry implements Iterable<DynamicMachine> {

    private static MachineRegistry INSTANCE = new MachineRegistry();
    private static Map<ResourceLocation, DynamicMachine> REGISTRY_MACHINERY;

    private MachineRegistry() {}

    public static MachineRegistry getRegistry() {
        return INSTANCE;
    }

    @Override
    public Iterator<DynamicMachine> iterator() {
        return REGISTRY_MACHINERY.values().iterator();
    }

    @Nullable
    public DynamicMachine getMachine(@Nullable ResourceLocation name) {
        if(name == null) return null;
        return REGISTRY_MACHINERY.get(name);
    }

    public void buildRegistry() {
        REGISTRY_MACHINERY = new HashMap<>();
    }

    public Collection<DynamicMachine> loadMachines(@Nullable EntityPlayer player) {
        ProgressManager.ProgressBar barMachinery = ProgressManager.push("MachineRegistry", 3);
        barMachinery.step("Discovering Files");
        DataLoadProfiler profiler = new DataLoadProfiler();

        Map<MachineLoader.FileType, List<File>> candidates = MachineLoader.discoverDirectory(CommonProxy.dataHolder.getMachineryDirectory());
        barMachinery.step("Loading Variables");
        MachineLoader.prepareContext(candidates.get(MachineLoader.FileType.VARIABLES));

        DataLoadProfiler.StatusLine variables = profiler.createLine("Variables: ");
        DataLoadProfiler.Status success = variables.appendStatus("%s loaded");
        DataLoadProfiler.Status failed = variables.appendStatus("%s failed");

        success.setCounter(MachineLoader.variableContext.size());

        Map<String, Exception> failures = MachineLoader.captureFailedAttempts();

        failed.setCounter(failures.size());
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading variables!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load variables of " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        barMachinery.step("Loading Machines");

        DataLoadProfiler.StatusLine machines = profiler.createLine("Machines: ");
        success = machines.appendStatus("%s loaded");
        failed = machines.appendStatus("%s failed");

        List<DynamicMachine> found = MachineLoader.loadMachines(candidates.get(MachineLoader.FileType.MACHINE));
        success.setCounter(found.size());
        failures = MachineLoader.captureFailedAttempts();
        failed.setCounter(failures.size());
        if(failures.size() > 0) {
            ModularMachinery.log.warn("Encountered " + failures.size() + " problems while loading machinery!");
            for (String fileName : failures.keySet()) {
                ModularMachinery.log.warn("Couldn't load machinery " + fileName);
                failures.get(fileName).printStackTrace();
            }
        }
        ProgressManager.pop(barMachinery);
        profiler.printLines(player);
        return ImmutableList.copyOf(found);
    }

    public void registerMachines(Collection<DynamicMachine> machines) {
        for (DynamicMachine machine : machines) {
            REGISTRY_MACHINERY.put(machine.getRegistryName(), machine);
        }
    }

}
