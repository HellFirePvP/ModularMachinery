/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: IOType
 * Created by HellFirePvP
 * Date: 13.07.2019 / 11:40
 */
public enum IOType {

    INPUT,
    OUTPUT;

    @Nullable
    public static IOType getByString(String name) {
        for (IOType val : values()) {
            if (val.name().equalsIgnoreCase(name)) {
                return val;
            }
        }
        return null;
    }
}
