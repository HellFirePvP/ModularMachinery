/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.container;

import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerItemBus
 * Created by HellFirePvP
 * Date: 09.07.2017 / 17:36
 */
public class ContainerItemBus extends ContainerBase<TileItemBus> {

    public ContainerItemBus(TileItemBus owner, EntityPlayer opening) {
        super(owner, opening);

        addInventorySlots(owner.getInventory().asGUIAccess(), owner.getSize());
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            boolean changed = false;
            if (index >= 0 && index < 36) {
                if(this.mergeItemStack(itemstack1, 36, inventorySlots.size(), false)) {
                    changed = true;
                }
            }

            if(!changed) {
                if (index >= 0 && index < 27) {
                    if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 27 && index < 36) {
                    if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    private void addInventorySlots(IItemHandlerModifiable itemHandler, ItemBusSize size) {
        switch (size) {
            case TINY:
                addSlotToContainer(new SlotItemHandler(itemHandler, 0, 81, 30));
                break;
            case SMALL:
                addSlotToContainer(new SlotItemHandler(itemHandler, 0, 70, 18));
                addSlotToContainer(new SlotItemHandler(itemHandler, 1, 88, 18));
                addSlotToContainer(new SlotItemHandler(itemHandler, 2, 70, 36));
                addSlotToContainer(new SlotItemHandler(itemHandler, 3, 88, 36));
                break;
            case NORMAL:
                for (int zz = 0; zz < 2; zz++) {
                    for (int xx = 0; xx < 3; xx++) {
                        int index = zz * 3 + xx;
                        addSlotToContainer(new SlotItemHandler(itemHandler, index, 61 + xx * 18, 18 + zz * 18));
                    }
                }
                break;
            case REINFORCED:
                for (int zz = 0; zz < 3; zz++) {
                    for (int xx = 0; xx < 3; xx++) {
                        int index = zz * 3 + xx;
                        addSlotToContainer(new SlotItemHandler(itemHandler, index, 61 + xx * 18, 13 + zz * 18));
                    }
                }
                break;
            case BIG:
                for (int zz = 0; zz < 3; zz++) {
                    for (int xx = 0; xx < 4; xx++) {
                        int index = zz * 4 + xx;
                        addSlotToContainer(new SlotItemHandler(itemHandler, index, 52 + xx * 18, 18 + zz * 18));
                    }
                }
                break;
            case HUGE:
                for (int zz = 0; zz < 4; zz++) {
                    for (int xx = 0; xx < 4; xx++) {
                        int index = zz * 4 + xx;
                        addSlotToContainer(new SlotItemHandler(itemHandler, index, 53 + xx * 18, 8 + zz * 18));
                    }
                }
                break;
            case LUDICROUS:
                for (int zz = 0; zz < 4; zz++) {
                    for (int xx = 0; xx < 8; xx++) {
                        int index = zz * 8 + xx;
                        addSlotToContainer(new SlotItemHandler(itemHandler, index, 17 + xx * 18, 8 + zz * 18));
                    }
                }
                break;
        }
    }

}
