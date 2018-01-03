/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.config.Configuration;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBusSize
 * Created by HellFirePvP
 * Date: 07.07.2017 / 18:31
 */
public enum ItemBusSize implements IStringSerializable {

    TINY(1),
    SMALL(4),
    NORMAL(6),
    REINFORCED(9),
    BIG(12),
    HUGE(16),
    LUDICROUS(32);

    private int slots;

    private final int defaultConfigSize;

    private ItemBusSize(int defaultConfigSize) {
        this.defaultConfigSize = defaultConfigSize;
        this.slots = this.defaultConfigSize; //Temp. TODO configurable and GUI building
    }

    public int getSlotCount() {
        return slots;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

}
