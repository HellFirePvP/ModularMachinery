/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.tiles.TileItemInputBus;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockInputBus
 * Created by HellFirePvP
 * Date: 07.07.2017 / 17:59
 */
public class BlockInputBus extends BlockContainer implements BlockCustomName {

    private static final PropertyEnum<ItemBusSize> BUS_TYPE = PropertyEnum.create("size", ItemBusSize.class);

    public BlockInputBus() {
        super(Material.IRON);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (ItemBusSize size : ItemBusSize.values()) {
            items.add(new ItemStack(this, 1, size.ordinal()));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        ItemBusSize size = ItemBusSize.values()[MathHelper.clamp(stack.getMetadata(), 0, ItemBusSize.values().length - 1)];
        tooltip.add(TextFormatting.GRAY.toString() + size.getSlotCount() + " Slot" + (size.getSlotCount() > 1 ? "s" : ""));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BUS_TYPE, ItemBusSize.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BUS_TYPE).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BUS_TYPE);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileItemInputBus(state.getValue(BUS_TYPE));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    public String getIdentifierForMeta(int meta) {
        return getStateFromMeta(meta).getValue(BUS_TYPE).getName();
    }

}
