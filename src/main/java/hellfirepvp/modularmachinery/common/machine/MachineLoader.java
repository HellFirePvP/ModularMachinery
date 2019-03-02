/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hellfirepvp.modularmachinery.common.modifier.ModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.BlockInformationVariable;
import net.minecraft.util.JsonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineLoader
 * Created by HellFirePvP
 * Date: 27.06.2017 / 11:53
 */
public class MachineLoader {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(DynamicMachine.class, new DynamicMachine.MachineDeserializer())
            .registerTypeHierarchyAdapter(BlockInformationVariable.class, new BlockInformationVariable.Deserializer())
            .registerTypeHierarchyAdapter(ModifierReplacement.class, new ModifierReplacement.Deserializer())
            .registerTypeHierarchyAdapter(RecipeModifier.class, new RecipeModifier.Deserializer())
            .create();

    private static Map<String, Exception> failedAttempts = new HashMap<>();
    public static Map<String, BlockArray.BlockInformation> variableContext = new HashMap<>();

    public static Map<FileType, List<File>> discoverDirectory(File directory) {
        Map<FileType, List<File>> candidates = new HashMap<>();
        for (FileType type : FileType.values()) {
            candidates.put(type, Lists.newLinkedList());
        }
        LinkedList<File> directories = Lists.newLinkedList();
        directories.add(directory);
        while (!directories.isEmpty()) {
            File dir = directories.remove(0);
            for (File f : dir.listFiles()) {
                if(f.isDirectory()) {
                    directories.addLast(f);
                } else {
                    //I am *not* taking chances with this ordering
                    if(FileType.VARIABLES.accepts(f.getName())) {
                        candidates.get(FileType.VARIABLES).add(f);
                    } else if(FileType.MACHINE.accepts(f.getName())) {
                        candidates.get(FileType.MACHINE).add(f);
                    }
                }
            }
        }
        return candidates;
    }


    public static List<DynamicMachine> loadMachines(List<File> machineCandidates) {
        List<DynamicMachine> loadedMachines = Lists.newArrayList();
        for (File f : machineCandidates) {
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                loadedMachines.add(JsonUtils.fromJson(GSON, isr, DynamicMachine.class));
            } catch (Exception exc) {
                failedAttempts.put(f.getPath(), exc);
            }
        }
        return loadedMachines;
    }

    public static Map<String, Exception> captureFailedAttempts() {
        Map<String, Exception> failed = failedAttempts;
        failedAttempts = new HashMap<>();
        return failed;
    }

    public static void prepareContext(List<File> files) {
        variableContext.clear();

        for (File f : files) {
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(f))) {
                Map<String, BlockArray.BlockInformation> variables = JsonUtils.fromJson(GSON, isr, BlockInformationVariable.class).getDefinedVariables();
                for (String key : variables.keySet()) {
                    variableContext.put(key, variables.get(key));
                }
            } catch (Exception exc) {
                failedAttempts.put(f.getPath(), exc);
            }
        }
    }

    public static enum FileType {

        VARIABLES,
        MACHINE;

        public boolean accepts(String fileName) {
            switch (this) {
                case VARIABLES:
                    return fileName.endsWith(".var.json");
                case MACHINE:
                default:
                    return fileName.endsWith(".json");
            }
        }

    }

}
