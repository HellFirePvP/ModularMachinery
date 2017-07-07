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
 * Class: ItemBusSize
 * Created by HellFirePvP
 * Date: 07.07.2017 / 18:31
 */
public enum ItemBusSize implements IStringSerializable {

    SMALL(1),
    NORMAL(4),
    REINFORCED(6),
    BIG(9),
    HUGE(12),
    LUDICROUS(16),
    GARGANTUAN(25);

    public final int slots;

    ItemBusSize(int slotSize) {
        this.slots = slotSize;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

}
