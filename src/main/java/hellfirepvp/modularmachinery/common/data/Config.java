/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.data;

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

    public static void loadFrom(File file) {
        lastReadFile = file;
        lastReadConfig = new Configuration(file);

        load();
    }

    private static void load() {

    }

}
