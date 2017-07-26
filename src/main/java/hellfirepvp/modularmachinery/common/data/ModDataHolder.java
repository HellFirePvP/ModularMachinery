/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.data;

import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModDataHolder
 * Created by HellFirePvP
 * Date: 26.06.2017 / 22:27
 */
public class ModDataHolder {

    private File mainDir, machineryDir, recipeDir;
    private boolean requiresDefaultMachinery;

    public void setup(File configDir) {
        mainDir = new File(configDir, ModularMachinery.MODID);
        if(!mainDir.exists()) {
            requiresDefaultMachinery = true;
            mainDir.mkdirs();
        }

        machineryDir = new File(mainDir, "machinery");
        if(!machineryDir.exists()) {
            machineryDir.mkdirs();
        }

        recipeDir = new File(mainDir, "recipes");
        if(!recipeDir.exists()) {
            recipeDir.mkdirs();
        }

        Config.loadFrom(new File(mainDir, ModularMachinery.MODID + ".cfg"));
    }

    public boolean requiresDefaultMachinery() {
        boolean old = requiresDefaultMachinery;
        if(ModularMachinery.isRunningInDevEnvironment()) {
            old = true;
        }
        requiresDefaultMachinery = false;
        return old;
    }

    public File getMachineryDirectory() {
        return machineryDir;
    }

    public File getRecipeDirectory() {
        return recipeDir;
    }

    public void copyDefaultMachinery() {
        copy("default_machinery", machineryDir);
        copy("default_recipes", recipeDir);

        File defaultVariableDir = new File(machineryDir, "variables");
        if(!defaultVariableDir.exists()) {
            defaultVariableDir.mkdirs();
        }
        copy("default_variables", defaultVariableDir);
    }

    private void copy(String assetDirFrom, File directoryTo) {
        ModContainer thisMod = Loader.instance().getIndexedModList().get(ModularMachinery.MODID);
        if(thisMod == null) {
            ModContainer active = Loader.instance().activeModContainer();
            if(active != null && active.getModId().equalsIgnoreCase(ModularMachinery.MODID)) {
                thisMod = active;
            }
        }
        if(thisMod == null) {
            return;
        }
        FileSystem fs = null;
        try {
            File modSource = thisMod.getSource();
            Path root = null;
            if (modSource.isFile()) {
                try {
                    fs = FileSystems.newFileSystem(modSource.toPath(), null);
                    root = fs.getPath("/assets/" + ModularMachinery.MODID + "/" + assetDirFrom);
                } catch (IOException e) {
                    ModularMachinery.log.error("Error loading FileSystem from jar: ", e);
                    return;
                }
            } else if (modSource.isDirectory()) {
                root = modSource.toPath().resolve("assets/" + ModularMachinery.MODID + "/" + assetDirFrom);
            }
            if (root == null || !Files.exists(root)) {
                return;
            }
            Iterator<Path> itr;
            try {
                itr = Files.walk(root).iterator();
            } catch (IOException e) {
                ModularMachinery.log.error("Error iterating through " + assetDirFrom + " Skipping copying default setup!", e);
                return;
            }
            while (itr != null && itr.hasNext()) {
                Path filePath = itr.next();
                if(!filePath.getFileName().toString().endsWith(".json")) continue;

                File target = new File(directoryTo, filePath.getFileName().toString());
                try (FileOutputStream fos = new FileOutputStream(target)) {
                    Files.copy(filePath, fos);
                } catch (Exception exc) {
                    ModularMachinery.log.error("Couldn't copy file from " + filePath);
                }
            }
        } finally {
            IOUtils.closeQuietly(fs);
        }
    }

}
