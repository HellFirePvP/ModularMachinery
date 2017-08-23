/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemConstructTool
 * Created by HellFirePvP
 * Date: 22.08.2017 / 22:04
 */
public class ItemConstructTool extends Item {

    public ItemConstructTool() {
        setMaxStackSize(1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.constructtool.creative"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote && player.isCreative()) {
            IBlockState clicked = worldIn.getBlockState(pos);
            Block block = clicked.getBlock();
            if(block.equals(BlocksMM.blockController)) {
                PlayerStructureSelectionHelper.finalizeSelection(clicked.getValue(BlockController.FACING), worldIn, pos, player);

                PlayerStructureSelectionHelper.purgeSelection(player);
                PlayerStructureSelectionHelper.sendSelection(player);
            } else {
                PlayerStructureSelectionHelper.toggleInSelection(player, pos);
                PlayerStructureSelectionHelper.sendSelection(player);
            }
        }
        return EnumActionResult.SUCCESS;
    }

}
