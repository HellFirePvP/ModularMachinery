/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.data;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchSize;
import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: Config
 * Created by HellFirePvP
 * Date: 26.06.2017 / 22:34
 */
public class Config {

    private static File lastReadFile;
    private static Configuration lastReadConfig;

    public static int machineColor;

    public static void loadFrom(File file) {
        lastReadFile = file;
        lastReadConfig = new Configuration(file);

        load();

        if(lastReadConfig.hasChanged()) {
            lastReadConfig.save();
        }
    }

    private static void load() {
        FluidHatchSize.loadSizeFromConfig(lastReadConfig);
        EnergyHatchSize.loadSizeFromConfig(lastReadConfig);

        String strColor = lastReadConfig.getString("general-casing-color", "general", "FF4900", "Defines the _default_ color for machine casings as items or blocks. (Hex color without alpha) Has to be defined both server and clientside!");
        int col = 0xff921e; //TODO uh
        try {
            col = Integer.parseInt(strColor, 16);
        } catch (Exception exc) {
            ModularMachinery.log.error("Machine-Casing color defined in the config is not a hex color: " + strColor);
            ModularMachinery.log.error("Using default color instead...");
        }
        machineColor = col;
    }

}
