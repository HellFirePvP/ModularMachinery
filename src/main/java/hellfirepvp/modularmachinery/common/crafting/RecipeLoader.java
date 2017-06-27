/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeLoader
 * Created by HellFirePvP
 * Date: 27.06.2017 / 23:23
 */
public class RecipeLoader {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(MachineRecipe.class, new MachineRecipe.Deserializer())
            .create();

    private static Map<String, Exception> failedAttempts = new HashMap<>();

    public static List<File> discoverDirectory(File directory) {
        List<File> recipes = Lists.newArrayList();
        LinkedList<File> directories = Lists.newLinkedList();
        directories.add(directory);
        while (!directories.isEmpty()) {
            File dir = directories.remove(0);
            for (File f : dir.listFiles()) {
                if(f.isDirectory()) {
                    directories.addLast(f);
                } else {
                    if(f.getName().endsWith(".json")) {
                        recipes.add(f);
                    }
                }
            }
        }
        return recipes;
    }

    public static List<MachineRecipe> loadRecipes(List<File> files) {
        //TODO
        return Lists.newArrayList();
    }

    public static Map<String, Exception> captureFailedAttempts() {
        Map<String, Exception> failed = failedAttempts;
        failedAttempts = new HashMap<>();
        return failed;
    }
}
