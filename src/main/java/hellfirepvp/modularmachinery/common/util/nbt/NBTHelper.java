/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import com.google.common.base.Optional;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTHelper
 * Created by HellFirePvP
 * Date: 28.11.2018 / 16:53
 */
public class NBTHelper {

    public static void setBlockState(NBTTagCompound cmp, String key, IBlockState state) {
        NBTTagCompound serialized = getBlockStateNBTTag(state);
        if (serialized != null) {
            cmp.setTag(key, serialized);
        }
    }

    @Nullable
    public static IBlockState getBlockState(NBTTagCompound cmp, String key) {
        return getBlockStateFromTag(cmp.getCompoundTag(key));
    }

    @Nullable
    public static NBTTagCompound getBlockStateNBTTag(IBlockState state) {
        if(state.getBlock().getRegistryName() == null) return null;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("registryName", state.getBlock().getRegistryName().toString());
        NBTTagList properties = new NBTTagList();
        for (IProperty property : state.getPropertyKeys()) {
            NBTTagCompound propTag = new NBTTagCompound();
            try {
                propTag.setString("value", property.getName(state.getValue(property)));
            } catch (Exception exc) {
                return null;
            }
            propTag.setString("property", property.getName());
            properties.appendTag(propTag);
        }
        tag.setTag("properties", properties);
        return tag;
    }

    @Nullable
    public static <T extends Comparable<T>> IBlockState getBlockStateFromTag(NBTTagCompound cmp) {
        ResourceLocation key = new ResourceLocation(cmp.getString("registryName"));
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if(block == null || block == Blocks.AIR) return null;
        IBlockState state = block.getDefaultState();
        Collection<IProperty<?>> properties = state.getPropertyKeys();
        NBTTagList list = cmp.getTagList("properties", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound propertyTag = list.getCompoundTagAt(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            IProperty<T> match = (IProperty<T>) MiscUtils.iterativeSearch(properties, prop -> prop.getName().equalsIgnoreCase(propertyStr));
            if(match != null) {
                try {
                    Optional<T> opt = match.parseValue(valueStr);
                    if(opt.isPresent()) {
                        state = state.withProperty(match, opt.get());
                    }
                } catch (Exception exc) {}
            }
        }
        return state;
    }

}
