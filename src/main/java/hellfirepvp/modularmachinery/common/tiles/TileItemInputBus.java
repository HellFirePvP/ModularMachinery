/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileItemInputBus
 * Created by HellFirePvP
 * Date: 07.07.2017 / 17:54
 */
public class TileItemInputBus extends TileItemBus implements MachineComponentTile {

    public TileItemInputBus() {}

    public TileItemInputBus(ItemBusSize type) {
        super(type);
    }

    @Override
    public IOInventory buildInventory(TileInventory tile, int size) {
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return new IOInventory(tile, slots, new int[] {});
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.ItemBus(MachineComponent.IOType.INPUT) {
            @Override
            public IOInventory getContainerProvider() {
                return TileItemInputBus.this.inventory;
            }
        };
    }

}
