/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemDebugStruct
 * Created by HellFirePvP
 * Date: 29.10.2017 / 20:39
 */
public class ItemDebugStruct extends Item {

    public ItemDebugStruct() {
        setMaxStackSize(1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(worldIn.isRemote) return EnumActionResult.SUCCESS;
        TileEntity te = worldIn.getTileEntity(pos);
        if(te != null && te instanceof TileMachineController) {
            DynamicMachine dm = ((TileMachineController) te).getBlueprintMachine();
            if(dm != null) {
                BlockArray pattern = dm.getPattern();
                if(pattern != null) {

                    EnumFacing face = EnumFacing.NORTH;
                    player.sendMessage(new TextComponentString("Attempting structure matching:"));
                    player.sendMessage(new TextComponentString("Structure is facing: " + worldIn.getBlockState(pos).getValue(BlockController.FACING).name()));
                    DynamicMachine.ModifierReplacementMap replacements = dm.getModifiersAsMatchingReplacements();
                    do {
                        if(face == worldIn.getBlockState(pos).getValue(BlockController.FACING)) {
                            BlockPos mismatch = pattern.getRelativeMismatchPosition(worldIn, pos, replacements);
                            if(mismatch != null) {
                                player.sendMessage(new TextComponentString("Failed at relative position: " + mismatch.toString()));
                            }
                        }
                        face = face.rotateYCCW();
                        pattern = pattern.rotateYCCW();
                        replacements = replacements.rotateYCCW();
                    } while (face != EnumFacing.NORTH);
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

}
