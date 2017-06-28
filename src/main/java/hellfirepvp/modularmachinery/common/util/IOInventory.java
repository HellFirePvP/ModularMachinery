/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import hellfirepvp.modularmachinery.common.tiles.TileEntitySynchronized;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: IOInventory
 * Created by HellFirePvP
 * Date: 28.06.2017 / 17:42
 */
public class IOInventory implements IItemHandlerModifiable {

    public boolean allowAnySlots = false;
    private final TileEntitySynchronized owner;

    private Map<Integer, Integer> slotLimits = new HashMap<>(); //Value not present means default, aka 64.
    private Map<Integer, SlotStackHolder> inventory = new HashMap<>();
    private int[] inSlots = new int[0], outSlots = new int[0], miscSlots = new int[0];

    private InventoryUpdateListener listener = null;
    public List<EnumFacing> accessibleSides = new ArrayList<>();

    private IOInventory(TileEntitySynchronized owner) {
        this.owner = owner;
    }

    public IOInventory(TileEntitySynchronized owner, int[] inSlots, int[] outSlots) {
        this(owner, inSlots, outSlots, EnumFacing.VALUES);
    }

    public IOInventory(TileEntitySynchronized owner, int[] inSlots, int[] outSlots, EnumFacing... accessibleFrom) {
        this.owner = owner;
        this.inSlots = inSlots;
        this.outSlots = outSlots;
        for (Integer slot : inSlots) {
            this.inventory.put(slot, new SlotStackHolder(slot));
        }
        for (Integer slot : outSlots) {
            this.inventory.put(slot, new SlotStackHolder(slot));
        }
        this.accessibleSides = Arrays.asList(accessibleFrom);
    }

    public IOInventory setMiscSlots(int... miscSlots) {
        this.miscSlots = miscSlots;
        for (Integer slot : miscSlots) {
            this.inventory.put(slot, new SlotStackHolder(slot));
        }
        return this;
    }

    public IOInventory setStackLimit(int limit, int... slots) {
        for (int slot : slots) {
            this.slotLimits.put(slot, limit);
        }
        return this;
    }

    public IOInventory setListener(InventoryUpdateListener listener) {
        this.listener = listener;
        return this;
    }

    public TileEntitySynchronized getOwner() {
        return owner;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if(this.inventory.containsKey(slot)) {
            this.inventory.get(slot).itemStack = stack;
            getOwner().markForUpdate();
            if(listener != null) {
                listener.onChange();
            }
        }
    }

    @Override
    public int getSlots() {
        return inventory.size();
    }

    @Override
    public int getSlotLimit(int slot) {
        if(slotLimits.containsKey(slot)) {
            return slotLimits.get(slot);
        }
        return 64;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return inventory.containsKey(slot) ? inventory.get(slot).itemStack : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(stack.isEmpty()) return stack;
        if (!allowAnySlots) {
            if (!arrayContains(inSlots, slot)) return stack;
        }
        if (!this.inventory.containsKey(slot)) return stack; //Shouldn't happen anymore here tho

        SlotStackHolder holder = this.inventory.get(slot);
        ItemStack toInsert = copyWithSize(stack, stack.getCount());
        if(!holder.itemStack.isEmpty()) {
            ItemStack existing = copyWithSize(holder.itemStack, holder.itemStack.getCount());
            int max = Math.min(existing.getMaxStackSize(), getSlotLimit(slot));
            if (existing.getCount() >= max || !canMergeItemStacks(existing, toInsert)) {
                return stack;
            }
            int movable = Math.min(max - existing.getCount(), stack.getCount());
            if (!simulate) {
                holder.itemStack.grow(movable);
                getOwner().markForUpdate();
                if(listener != null) {
                    listener.onChange();
                }
            }
            if (movable >= stack.getCount()) {
                return ItemStack.EMPTY;
            } else {
                ItemStack copy = stack.copy();
                copy.shrink(movable);
                return copy;
            }
        } else {
            int max = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (max >= stack.getCount()) {
                if (!simulate) {
                    holder.itemStack = stack.copy();
                    getOwner().markForUpdate();
                    if(listener != null) {
                        listener.onChange();
                    }
                }
                return ItemStack.EMPTY;
            } else {
                ItemStack copy = stack.copy();
                copy.setCount(max);
                if (!simulate) {
                    holder.itemStack = copy;
                    getOwner().markForUpdate();
                    if(listener != null) {
                        listener.onChange();
                    }
                }
                copy = stack.copy();
                copy.shrink(max);
                return copy;
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowAnySlots) {
            if (!arrayContains(outSlots, slot)) return ItemStack.EMPTY;
        }
        if (!this.inventory.containsKey(slot)) return ItemStack.EMPTY; //Shouldn't happen anymore here tho
        SlotStackHolder holder = this.inventory.get(slot);
        if(holder.itemStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack extract = copyWithSize(holder.itemStack, Math.min(amount, holder.itemStack.getCount()));
        if(extract.isEmpty()) return ItemStack.EMPTY;
        if(!simulate) {
            holder.itemStack = copyWithSize(holder.itemStack, holder.itemStack.getCount() - extract.getCount());
            if(listener != null) {
                listener.onChange();
            }
        }
        getOwner().markForUpdate();
        return extract;
    }

    @Nonnull
    private ItemStack copyWithSize(@Nonnull ItemStack stack, int amount) {
        if (stack.isEmpty()|| amount <= 0) return ItemStack.EMPTY;
        ItemStack s = stack.copy();
        s.setCount(Math.min(amount, stack.getMaxStackSize()));
        return s;
    }

    private boolean arrayContains(int[] array, int i) {
        for (int id : array) {
            if(id == i) return true;
        }
        return false;
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("inSlots", this.inSlots);
        tag.setIntArray("outSlots", this.outSlots);
        tag.setIntArray("miscSlots", this.miscSlots);

        NBTTagList inv = new NBTTagList();
        for (Integer slot : this.inventory.keySet()) {
            SlotStackHolder holder = this.inventory.get(slot);
            NBTTagCompound holderTag = new NBTTagCompound();
            holderTag.setBoolean("holderEmpty", holder.itemStack.isEmpty());
            holderTag.setInteger("holderId", slot);
            if(!holder.itemStack.isEmpty()) {
                holder.itemStack.writeToNBT(holderTag);
            }
            inv.appendTag(holderTag);
        }
        tag.setTag("inventoryArray", inv);

        int[] sides = new int[accessibleSides.size()];
        for (int i = 0; i < accessibleSides.size(); i++) {
            EnumFacing side = accessibleSides.get(i);
            sides[i] = side.ordinal();
        }
        tag.setIntArray("sides", sides);
        return tag;
    }

    public void readNBT(NBTTagCompound tag) {
        this.inSlots = tag.getIntArray("inSlots");
        this.outSlots = tag.getIntArray("outSlots");
        this.miscSlots = tag.getIntArray("miscSlots");

        this.inventory.clear();
        NBTTagList list = tag.getTagList("inventoryArray", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound holderTag = list.getCompoundTagAt(i);
            int slot = holderTag.getInteger("holderId");
            boolean isEmpty = holderTag.getBoolean("holderEmpty");
            ItemStack stack = ItemStack.EMPTY;
            if(!isEmpty) {
                stack = new ItemStack(holderTag);
            }
            SlotStackHolder holder = new SlotStackHolder(slot);
            holder.itemStack = stack;
            this.inventory.put(slot, holder);
        }

        int[] sides = tag.getIntArray("sides");
        for (int i : sides) {
            this.accessibleSides.add(EnumFacing.values()[i]);
        }

        if(listener != null) {
            listener.onChange();
        }
    }

    private boolean canMergeItemStacks(@Nonnull ItemStack stack, @Nonnull ItemStack other) {
        if (stack.isEmpty() || other.isEmpty() || !stack.isStackable() || !other.isStackable()) {
            return false;
        }
        return stack.isItemEqual(other) && ItemStack.areItemStackTagsEqual(stack, other);
    }

    public static IOInventory deserialize(TileEntitySynchronized owner, NBTTagCompound tag) {
        IOInventory inv = new IOInventory(owner);
        inv.readNBT(tag);
        return inv;
    }

    public boolean hasCapability(EnumFacing facing) {
        return facing == null || accessibleSides.contains(facing);
    }

    public IItemHandlerModifiable getCapability(EnumFacing facing) {
        if(hasCapability(facing)) {
            return this;
        }
        return null;
    }

    public int calcRedstoneFromInventory() {
        int i = 0;
        float f = 0.0F;
        for (int j = 0; j < getSlots(); ++j) {
            ItemStack itemstack = getStackInSlot(j);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) Math.min(getSlotLimit(j), itemstack.getMaxStackSize());
                ++i;
            }
        }
        f = f / (float) getSlots();
        return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);

    }

    public static IOInventory mergeBuild(TileEntitySynchronized tile, IOInventory... inventories) {
        IOInventory merged = new IOInventory(tile);
        int slotOffset = 0;
        for (IOInventory inventory : inventories) {
            for (Integer key : inventory.inventory.keySet()) {
                merged.inventory.put(key + slotOffset, inventory.inventory.get(key));
            }
            for (Integer key : inventory.slotLimits.keySet()) {
                merged.slotLimits.put(key + slotOffset, inventory.slotLimits.get(key));
            }
            slotOffset += inventory.inventory.size();
        }
        return merged;
    }

    private static class SlotStackHolder {

        private final int slotId;
        @Nonnull
        private ItemStack itemStack = ItemStack.EMPTY;

        private SlotStackHolder(int slotId) {
            this.slotId = slotId;
        }

    }

}
