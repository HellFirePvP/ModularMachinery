/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileItemBus
 * Created by HellFirePvP
 * Date: 09.07.2017 / 17:37
 */
public abstract class TileItemBus extends TileInventory {

    private ItemBusSize size;

    public TileItemBus() {}

    public TileItemBus(ItemBusSize size) {
        super(size.getSlotCount());
        this.size = size;
    }

    public ItemBusSize getSize() {
        return size;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.size = ItemBusSize.values()[MathHelper.clamp(compound.getInteger("busSize"), 0, ItemBusSize.values().length - 1)];
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("busSize", this.size.ordinal());
    }
}
