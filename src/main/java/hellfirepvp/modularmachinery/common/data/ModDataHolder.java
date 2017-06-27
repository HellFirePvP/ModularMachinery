/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.data;

import com.google.gson.JsonParseException;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineLoader;

import java.io.File;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModDataHolder
 * Created by HellFirePvP
 * Date: 26.06.2017 / 22:27
 */
public class ModDataHolder {

    private File mainDir, machineryDir, recipeDir;

    public void setup(File configDir) {
        mainDir = new File(configDir, ModularMachinery.MODID);
        if(!mainDir.exists()) {
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

    public File getMachineryDirectory() {
        return machineryDir;
    }

    public File getRecipeDirectory() {
        return recipeDir;
    }

}
