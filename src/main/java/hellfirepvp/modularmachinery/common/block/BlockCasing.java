/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCasing
 * Created by HellFirePvP
 * Date: 29.06.2017 / 17:32
 */
public class BlockCasing extends BlockMachineComponent implements BlockCustomName, BlockVariants {

    private static final PropertyEnum<CasingType> CASING = PropertyEnum.create("casing", CasingType.class);

    public BlockCasing() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (CasingType type : CasingType.values()) {
            items.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CASING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(CASING, CasingType.values()[MathHelper.clamp(meta, 0, CasingType.values().length - 1)]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CASING);
    }

    @Override
    public String getIdentifierForMeta(int meta) {
        return CasingType.values()[MathHelper.clamp(meta, 0, CasingType.values().length - 1)].getName();
    }

    @Override
    public Iterable<IBlockState> getValidStates() {
        List<IBlockState> ret = new LinkedList<>();
        for (CasingType type : CasingType.values()) {
            ret.add(getDefaultState().withProperty(CASING, type));
        }
        return ret;
    }

    @Override
    public String getBlockStateName(IBlockState state) {
        return state.getValue(CASING).getName();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    //@Override
    //public BlockRenderLayer getBlockLayer() {
    //    return BlockRenderLayer.CUTOUT;
    //}

    public static enum CasingType implements IStringSerializable {

        PLAIN,
        VENT,
        FIREBOX,
        GEARBOX,
        REINFORCED,
        CIRCUITRY;

        @Override
        public String getName() {
            return name().toLowerCase();
        }

    }

}
