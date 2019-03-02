/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import com.google.common.collect.Iterables;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCompatHelper
 * Created by HellFirePvP
 * Date: 27.11.2017 / 20:49
 */
public class BlockCompatHelper {

    private static final ResourceLocation ic2TileBlock = new ResourceLocation("ic2", "te");

    private static final Method getITeBlockIc2, getTeClassIc2, getTeBlockState, getITEgetSupportedFacings, getTEBlockId, getITeBlockIc2Class;
    private static final IProperty<EnumFacing> facingPropertyField;
    private static final Field teBlockItemField;

    @Nonnull
    @net.minecraftforge.fml.common.Optional.Method(modid = "ic2")
    public static ItemStack tryGetIC2MachineStack(IBlockState state, Object tile) {
        try {
            Object tileITBlock = getITeBlockIc2Class.invoke(null, tile.getClass());
            int id = (int) getTEBlockId.invoke(tileITBlock);
            if(id != -1) {
                Item i = (Item) teBlockItemField.get(state.getBlock());
                return new ItemStack(i, 1, id);
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static Tuple<IBlockState, TileEntity> transformState(IBlockState state, @Nullable NBTTagCompound matchTag, BlockArray.TileInstantiateContext context) {
        ResourceLocation blockRes = state.getBlock().getRegistryName();
        if(ic2TileBlock.equals(blockRes) && matchTag != null) {
            Tuple<IBlockState, TileEntity> ret = tryRecoverTileState(state, matchTag, context);
            if(ret != null) {
                return ret;
            }
        }
        TileEntity te = state.getBlock().hasTileEntity(state) ? state.getBlock().createTileEntity(context.getWorld(), state) : null;
        if(te != null) {
            context.apply(te);
        }
        return new Tuple<>(state, te);
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "ic2")
    private static Tuple<IBlockState, TileEntity> tryRecoverTileState(IBlockState state, @Nonnull NBTTagCompound matchTag, BlockArray.TileInstantiateContext context) {
        if(getTeClassIc2 == null || getITeBlockIc2 == null || getTeBlockState == null
                || getITEgetSupportedFacings == null || facingPropertyField == null) {
            return null;
        }

        ResourceLocation ic2TileBlock = new ResourceLocation("ic2", "te");
        if(ic2TileBlock.equals(state.getBlock().getRegistryName())) {
            if(matchTag.hasKey("id")) {
                ResourceLocation key = new ResourceLocation(matchTag.getString("id"));
                if(key.getResourceDomain().equalsIgnoreCase("ic2")) {
                    String name = key.getResourcePath();
                    try {
                        Object o = getITeBlockIc2.invoke(null, name);
                        Object oClazz = getTeClassIc2.invoke(o);
                        if(oClazz instanceof Class) {
                            TileEntity te =  (TileEntity) ((Class) oClazz).newInstance();
                            if(te != null) {
                                context.apply(te);
                                te.readFromNBT(matchTag);

                                IBlockState st = (IBlockState) getTeBlockState.invoke(te);
                                EnumFacing applicable = Iterables.getFirst((Collection<EnumFacing>) getITEgetSupportedFacings.invoke(o), EnumFacing.NORTH);
                                st = st.withProperty(facingPropertyField, applicable);
                                return new Tuple<>(st, te);
                            }
                        }
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    static {
        Method m = null, m2 = null, m3 = null, m4 = null, m5 = null, m6 = null;
        IProperty<EnumFacing> f = null;
        Field f1 = null;
        if(Loader.isModLoaded("ic2")) {
            try {
                Class c = Class.forName("ic2.core.block.TeBlockRegistry");
                m = c.getDeclaredMethod("get", String.class);
                m6 = c.getDeclaredMethod("get", Class.class);
                c = Class.forName("ic2.core.block.ITeBlock");
                m2 = c.getDeclaredMethod("getTeClass");
                m4 = c.getDeclaredMethod("getSupportedFacings");
                c = Class.forName("ic2.core.block.state.IIdProvider");
                m5 = c.getDeclaredMethod("getId");
                m5.setAccessible(true);
                c = Class.forName("ic2.core.block.TileEntityBlock");
                m3 = c.getDeclaredMethod("getBlockState");
                c = Class.forName("ic2.core.block.BlockTileEntity");
                f1 = c.getDeclaredField("item");
                f1.setAccessible(true);
                f = (IProperty<EnumFacing>) c.getDeclaredField("facingProperty").get(null);
            } catch (Throwable ignored) {}
        }
        getITeBlockIc2 = m;
        getITeBlockIc2Class = m6;
        getTeClassIc2 = m2;
        getTeBlockState = m3;
        facingPropertyField = f;
        getITEgetSupportedFacings = m4;
        getTEBlockId = m5;
        teBlockItemField = f1;
    }

}
