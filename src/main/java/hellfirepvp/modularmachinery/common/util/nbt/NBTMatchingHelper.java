/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTMatchingHelper
 * Created by HellFirePvP
 * Date: 19.08.2017 / 22:16
 */
public class NBTMatchingHelper {

    public static boolean matchNBTCompound(@Nullable NBTTagCompound matchNBT, @Nullable NBTTagCompound itemStackNBTToCheck) {
        if(matchNBT == null) {
            return true;
        }
        if(itemStackNBTToCheck == null) {
            return matchNBT.getKeySet().isEmpty();
        }
        return matchCompound(matchNBT, itemStackNBTToCheck);
    }

    private static boolean matchBase(NBTBase matchBase, NBTBase matchStack) {
        if(matchBase instanceof NBTComparableNumber) {
            if(matchStack instanceof NBTPrimitive) {
                if(!((NBTComparableNumber) matchBase).test((NBTPrimitive) matchStack)) {
                    return false;
                }
            } else {
                return false;
            }
        } else if(matchBase instanceof NBTTagCompound) {
            if(matchStack instanceof NBTTagCompound) {
                if(!matchCompound((NBTTagCompound) matchBase, (NBTTagCompound) matchStack)) {
                    return false;
                }
            } else {
                return false;
            }
        } else if(matchBase instanceof NBTTagList) {
            if(matchStack instanceof NBTTagList) {
                if(!matchList((NBTTagList) matchBase, (NBTTagList) matchStack)) {
                    return false;
                }
            } else {
                return false;
            }
        } else if(matchBase instanceof NBTTagByteArray) {
            if(matchStack instanceof NBTTagByteArray) {
                if(!Arrays.equals(((NBTTagByteArray) matchBase).getByteArray(), ((NBTTagByteArray) matchStack).getByteArray())) {
                    return false;
                }
            } else {
                return false;
            }
        } else if(matchBase instanceof NBTTagIntArray) {
            if(matchStack instanceof NBTTagIntArray) {
                if(!Arrays.equals(((NBTTagIntArray) matchBase).getIntArray(), ((NBTTagIntArray) matchStack).getIntArray())) {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }

    private static boolean matchCompound(NBTTagCompound matchNBT, NBTTagCompound itemStackNBTToCheck) {
        for (String keyMatch : matchNBT.getKeySet()) {
            if(itemStackNBTToCheck.hasKey(keyMatch)) {
                NBTBase baseOriginal = matchNBT.getTag(keyMatch);
                NBTBase baseStack = itemStackNBTToCheck.getTag(keyMatch);
                if(!matchBase(baseOriginal, baseStack)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean matchList(NBTTagList baseOriginal, NBTTagList baseStack) {
        if(baseOriginal.tagCount() != baseStack.tagCount()) {
            return false;
        }
        if(baseOriginal.getTagType() != baseStack.getTagType()) {
            return false;
        }
        for (int i = 0; i < baseOriginal.tagCount(); i++) {
            NBTBase entryBase = baseOriginal.get(i);
            NBTBase stackBase = baseStack.get(i);
            if(!matchBase(entryBase, stackBase)) {
                return false;
            }
        }
        return true;
    }

}
