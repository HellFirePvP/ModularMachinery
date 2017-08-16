/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockMachineComponent
 * Created by HellFirePvP
 * Date: 15.08.2017 / 16:26
 */
public abstract class BlockMachineComponent extends BlockContainer implements BlockDynamicColor {

    protected BlockMachineComponent(Material materialIn) {
        super(materialIn);
    }

    protected BlockMachineComponent(Material materialIn, MapColor color) {
        super(materialIn, color);
    }

    @Override
    public int getColorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if(worldIn == null || pos == null) {
            return TileColorableMachineComponent.DEFAULT_COLOR;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if(te != null && te instanceof TileColorableMachineComponent) {
            return ((TileColorableMachineComponent) te).definedColor;
        }
        return TileColorableMachineComponent.DEFAULT_COLOR;
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        worldIn.markBlockRangeForRenderUpdate(pos, pos);
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileColorableMachineComponent();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileColorableMachineComponent();
    }

}
