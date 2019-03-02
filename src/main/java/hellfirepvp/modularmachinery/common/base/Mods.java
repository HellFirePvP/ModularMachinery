/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.base;

import net.minecraftforge.fml.common.Loader;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: Mods
 * Created by HellFirePvP
 * Date: 02.03.2019 / 17:42
 */
public enum Mods {

    CRAFTTWEAKER("crafttweaker"),
    JEI("jei"),
    GREGTECH("gregtech"),
    DRACONICEVOLUTION("draconicevolution"),
    REDSTONEFLUXAPI("redstoneflux"),
    MEKANISM("mekanism"),
    IC2("ic2");

    public final String modid;
    private final boolean loaded;

    private Mods(String modName) {
        this.modid = modName;
        this.loaded = Loader.isModLoaded(this.modid);
    }

    public boolean isPresent() {
        return loaded;
    }

}
