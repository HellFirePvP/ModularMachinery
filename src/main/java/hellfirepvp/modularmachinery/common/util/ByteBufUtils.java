/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ByteBufUtils
 * Created by HellFirePvP
 * Date: 02.03.2019 / 14:49
 */
public class ByteBufUtils {

    public static void writeItemStack(ByteBuf byteBuf, @Nonnull ItemStack stack) {
        boolean defined = !stack.isEmpty();
        byteBuf.writeBoolean(defined);
        if(defined) {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            writeNBTTag(byteBuf, tag);
        }
    }

    @Nonnull
    public static ItemStack readItemStack(ByteBuf byteBuf) {
        boolean defined = byteBuf.readBoolean();
        if(defined) {
            return new ItemStack(readNBTTag(byteBuf));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static void writeNBTTag(ByteBuf byteBuf, @Nonnull NBTTagCompound tag) {
        try (DataOutputStream dos = new DataOutputStream(new ByteBufOutputStream(byteBuf))) {
            CompressedStreamTools.write(tag, dos);
        } catch (Exception exc) {}
    }

    @Nonnull
    public static NBTTagCompound readNBTTag(ByteBuf byteBuf) {
        try (DataInputStream dis = new DataInputStream(new ByteBufInputStream(byteBuf))) {
            return CompressedStreamTools.read(dis);
        } catch (Exception exc) {}
        throw new IllegalStateException("Could not load NBT Tag from incoming byte buffer!");
    }

}
