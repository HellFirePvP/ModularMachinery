/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import hellfirepvp.modularmachinery.common.util.nbt.NBTMatchingHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemUtils
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:42
 */
public class ItemUtils {

    public static void decrStackInInventory(ItemStackHandler handler, int slot) {
        if (slot < 0 || slot >= handler.getSlots()) return;
        ItemStack st = handler.getStackInSlot(slot);
        if (st.isEmpty()) return;
        st.setCount(st.getCount() - 1);
        if (st.getCount() <= 0) {
            handler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    //Negative amount: overhead fuel burnt
    //Positive amount: Failure/couldn't find enough fuel
    public static int consumeFromInventoryFuel(IItemHandlerModifiable handler, int fuelAmtToConsume, boolean simulate, @Nullable NBTTagCompound matchNBTTag) {
        Map<Integer, ItemStack> contents = findItemsIndexedInInventoryFuel(handler, matchNBTTag);
        if (contents.isEmpty()) {
            return fuelAmtToConsume;
        }

        for (int slot : contents.keySet()) {
            ItemStack inSlot = contents.get(slot);
            if(inSlot.getItem().hasContainerItem(inSlot)) {
                if(inSlot.getCount() > 1) {
                    continue; //uh... rip. we won't consume 16 buckets at once.
                }
                ItemStack stack = ForgeHooks.getContainerItem(inSlot);
                fuelAmtToConsume -= TileEntityFurnace.getItemBurnTime(inSlot);
                if (!simulate) {
                    handler.setStackInSlot(slot, stack.copy());
                }
                if (fuelAmtToConsume <= 0) {
                    break;
                }
            }
            int fuelPer = TileEntityFurnace.getItemBurnTime(inSlot);
            int toConsumeDiv = fuelAmtToConsume / fuelPer;
            int fuelMod = fuelAmtToConsume % fuelPer;

            int toConsume = toConsumeDiv + (fuelMod > 0 ? 1 : 0);
            int toRemove = toConsume > inSlot.getCount() ? inSlot.getCount() : toConsume;

            fuelAmtToConsume -= toRemove * fuelPer;
            if (!simulate) {
                handler.setStackInSlot(slot, copyStackWithSize(inSlot, inSlot.getCount() - toRemove));
            }
            if (fuelAmtToConsume <= 0) {
                break;
            }
        }
        return fuelAmtToConsume;
    }

    public static boolean consumeFromInventory(IItemHandlerModifiable handler, ItemStack toConsume, boolean simulate, @Nullable NBTTagCompound matchNBTTag) {
        Map<Integer, ItemStack> contents = findItemsIndexedInInventory(handler, toConsume, false, matchNBTTag);
        if (contents.isEmpty()) return false;

        int cAmt = toConsume.getCount();
        for (int slot : contents.keySet()) {
            ItemStack inSlot = contents.get(slot);
            if(inSlot.getItem().hasContainerItem(inSlot)) {
                if(inSlot.getCount() > 1) {
                    continue; //uh... rip. we won't consume 16 buckets at once.
                }
                ItemStack stack = ForgeHooks.getContainerItem(inSlot);
                cAmt--;
                if (!simulate) {
                    handler.setStackInSlot(slot, stack.copy());
                }
                if (cAmt <= 0) {
                    break;
                }
            }
            int toRemove = cAmt > inSlot.getCount() ? inSlot.getCount() : cAmt;
            cAmt -= toRemove;
            if (!simulate) {
                handler.setStackInSlot(slot, copyStackWithSize(inSlot, inSlot.getCount() - toRemove));
            }
            if (cAmt <= 0) {
                break;
            }
        }
        return cAmt <= 0;
    }

    public static boolean consumeFromInventoryOreDict(IItemHandlerModifiable handler, String oreName, int amount, boolean simulate, @Nullable NBTTagCompound matchNBTTag) {
        Map<Integer, ItemStack> contents = findItemsIndexedInInventoryOreDict(handler, oreName, matchNBTTag);
        if (contents.isEmpty()) return false;

        int cAmt = amount;
        for (int slot : contents.keySet()) {
            ItemStack inSlot = contents.get(slot);
            if(inSlot.getItem().hasContainerItem(inSlot)) {
                if(inSlot.getCount() > 1) {
                    continue; //uh... rip. we won't consume 16 buckets at once.
                }
                ItemStack stack = ForgeHooks.getContainerItem(inSlot);
                cAmt--;
                if (!simulate) {
                    handler.setStackInSlot(slot, stack.copy());
                }
                if (cAmt <= 0) {
                    break;
                }
            }
            int toRemove = cAmt > inSlot.getCount() ? inSlot.getCount() : cAmt;
            cAmt -= toRemove;
            if (!simulate) {
                handler.setStackInSlot(slot, copyStackWithSize(inSlot, inSlot.getCount() - toRemove));
            }
            if (cAmt <= 0) {
                break;
            }
        }
        return cAmt <= 0;
    }

    //Returns the amount inserted
    public static int tryPlaceItemInInventory(@Nonnull ItemStack stack, IItemHandlerModifiable handler, boolean simulate) {
        return tryPlaceItemInInventory(stack, handler, 0, handler.getSlots(), simulate);
    }

    public static int tryPlaceItemInInventory(@Nonnull ItemStack stack, IItemHandlerModifiable handler, int start, int end, boolean simulate) {
        ItemStack toAdd = stack.copy();
        if (!hasInventorySpace(toAdd, handler, start, end)) {
            return 0;
        }
        int insertedAmt = 0;
        int max = toAdd.getMaxStackSize();

        for (int i = start; i < end; i++) {
            ItemStack in = handler.getStackInSlot(i);
            if (in.isEmpty()) {
                int added = Math.min(stack.getCount(), max);
                stack.setCount(stack.getCount() - added);
                if(!simulate) {
                    handler.setStackInSlot(i, copyStackWithSize(toAdd, added));
                }
                insertedAmt += added;
                if (stack.getCount() <= 0)
                    return insertedAmt;
            } else {
                if (stackEqualsNonNBT(toAdd, in) && matchTags(toAdd, in)) {
                    int space = max - in.getCount();
                    int added = Math.min(stack.getCount(), space);
                    insertedAmt =+ added;
                    stack.setCount(stack.getCount() - added);
                    if(!simulate) {
                        handler.getStackInSlot(i).setCount(handler.getStackInSlot(i).getCount() + added);
                    }
                    if (stack.getCount() <= 0)
                        return insertedAmt;
                }
            }
        }
        return insertedAmt;
    }

    public static boolean hasInventorySpace(@Nonnull ItemStack stack, IItemHandler handler, int rangeMin, int rangeMax) {
        int size = stack.getCount();
        int max = stack.getMaxStackSize();
        for (int i = rangeMin; i < rangeMax && size > 0; i++) {
            ItemStack in = handler.getStackInSlot(i);
            if (in.isEmpty()) {
                size -= max;
            } else {
                if (stackEqualsNonNBT(stack, in) && matchTags(stack, in)) {
                    int space = max - in.getCount();
                    size -= space;
                }
            }
        }
        return size <= 0;
    }

    public static boolean stackEqualsNonNBT(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
        if (stack.isEmpty() && other.isEmpty())
            return true;
        if (stack.isEmpty() || other.isEmpty())
            return false;
        Item sItem = stack.getItem();
        Item oItem = other.getItem();
        if (sItem.getHasSubtypes() || oItem.getHasSubtypes()) {
            return sItem.equals(other.getItem()) &&
                    (stack.getItemDamage() == other.getItemDamage() ||
                            stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ||
                            other.getItemDamage() == OreDictionary.WILDCARD_VALUE);
        } else {
            return sItem.equals(other.getItem());
        }
    }

    public static boolean matchTags(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
        return ItemStack.areItemStackTagsEqual(stack, other);
    }

    public static ItemStack copyStackWithSize(@Nonnull ItemStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack s = stack.copy();
        s.setCount(amount);
        return s;
    }

    public static Map<Integer, ItemStack> findItemsIndexedInInventoryFuel(IItemHandlerModifiable handler, @Nullable NBTTagCompound matchNBTTag) {
        Map<Integer, ItemStack> stacksOut = new HashMap<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (TileEntityFurnace.getItemBurnTime(s) > 0 && NBTMatchingHelper.matchNBTCompound(matchNBTTag, s.getTagCompound())) {
                stacksOut.put(j, s.copy());
            }
        }
        return stacksOut;
    }

    public static Map<Integer, ItemStack> findItemsIndexedInInventoryOreDict(IItemHandlerModifiable handler, String oreDict, @Nullable NBTTagCompound matchNBTTag) {
        Map<Integer, ItemStack> stacksOut = new HashMap<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if(s.isEmpty()) continue;
            int[] ids = OreDictionary.getOreIDs(s);
            for (int id : ids) {
                if(OreDictionary.getOreName(id).equals(oreDict) && NBTMatchingHelper.matchNBTCompound(matchNBTTag, s.getTagCompound())) {
                    stacksOut.put(j, s.copy());
                }
            }
        }
        return stacksOut;
    }

    public static Map<Integer, ItemStack> findItemsIndexedInInventory(IItemHandlerModifiable handler, ItemStack match, boolean strict, @Nullable NBTTagCompound matchNBTTag) {
        Map<Integer, ItemStack> stacksOut = new HashMap<>();
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if ((strict ? matchStacks(s, match) : matchStackLoosely(s, match)) && NBTMatchingHelper.matchNBTCompound(matchNBTTag, s.getTagCompound())) {
                stacksOut.put(j, s.copy());
            }
        }
        return stacksOut;
    }

    public static boolean matchStacks(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
        if (!ItemStack.areItemsEqual(stack, other)) return false;
        return ItemStack.areItemStackTagsEqual(stack, other);
    }

    public static boolean matchStackLoosely(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
        if (stack.isEmpty()) return other.isEmpty();
        return OreDictionary.itemMatches(other, stack, false);
    }

}
