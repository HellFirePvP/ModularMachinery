/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import net.minecraft.util.IStringSerializable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnergyHatchSize
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:25
 */
public enum  EnergyHatchSize implements IStringSerializable {

    TINY(200),
    SMALL(600),
    NORMAL(1000),
    REINFORCED(1500),
    BIG(2500),
    HUGE(5000);

    public final int maxEnergy;

    private EnergyHatchSize(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

}
