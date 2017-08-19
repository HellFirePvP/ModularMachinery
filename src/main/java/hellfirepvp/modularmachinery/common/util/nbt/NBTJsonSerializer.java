/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTJsonSerializer
 * Created by HellFirePvP
 * Date: 19.08.2017 / 13:56
 */
public class NBTJsonSerializer {

    public static String serializeNBT(NBTBase nbtBase) {
        StringBuilder sb = new StringBuilder();
        switch (nbtBase.getId()) {
            case Constants.NBT.TAG_BYTE: {
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            }
            case Constants.NBT.TAG_SHORT: {
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            }
            case Constants.NBT.TAG_INT: {
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            }
            case Constants.NBT.TAG_LONG: {
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            }
            case Constants.NBT.TAG_FLOAT: {
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            }
            case Constants.NBT.TAG_DOUBLE: {
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            }
            case Constants.NBT.TAG_BYTE_ARRAY: {
                NBTTagByteArray ba = (NBTTagByteArray) nbtBase;
                byte[] data = ba.getByteArray();
                StringBuilder stringbuilder = new StringBuilder("[B;");
                for (int i = 0; i < data.length; ++i) {
                    if (i != 0) {
                        stringbuilder.append(',');
                    }
                    stringbuilder.append(data[i]);
                }
                sb.append(stringbuilder.append(']'));
                break;
            }
            case Constants.NBT.TAG_STRING: {
                sb.append(nbtBase.toString());
                break;
            }
            case Constants.NBT.TAG_LIST: {
                StringBuilder stringbuilder = new StringBuilder("[");
                NBTTagList listTag = (NBTTagList) nbtBase;

                for (int i = 0; i < listTag.tagCount(); ++i) {
                    if (i != 0) {
                        stringbuilder.append(',');
                    }

                    stringbuilder.append(serializeNBT(listTag.get(i)));
                }
                sb.append(stringbuilder.append(']'));
                break;
            }
            case Constants.NBT.TAG_COMPOUND: {
                StringBuilder stringbuilder = new StringBuilder("{");
                NBTTagCompound cmpTag = (NBTTagCompound) nbtBase;
                Set<String> collection = cmpTag.getKeySet();

                for (String s : collection) {
                    if (stringbuilder.length() != 1) {
                        stringbuilder.append(',');
                    }

                    stringbuilder.append(NBTTagString.quoteAndEscape(s)).append(':').append(serializeNBT(cmpTag.getTag(s)));
                }

                sb.append(stringbuilder.append('}'));
                break;
            }
            case Constants.NBT.TAG_INT_ARRAY: {
                NBTTagIntArray ba = (NBTTagIntArray) nbtBase;
                int[] data = ba.getIntArray();
                StringBuilder stringbuilder = new StringBuilder("[I;");

                for (int i = 0; i < data.length; ++i) {
                    if (i != 0) {
                        stringbuilder.append(',');
                    }

                    stringbuilder.append(data[i]);
                }

                sb.append(stringbuilder.append(']'));
                break;
            }
        }
        return sb.toString();
    }

}
