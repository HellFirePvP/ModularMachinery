/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlueprint
 * Created by HellFirePvP
 * Date: 28.06.2017 / 18:39
 */
public class ItemBlueprint extends Item {

    public static final String DYNAMIC_MACHINE_NBT_KEY = "dynamicmachine";

    public ItemBlueprint() {
        setMaxStackSize(16);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(isInCreativeTab(tab)) {
            for (DynamicMachine machine : MachineRegistry.getRegistry()) {
                ItemStack i = new ItemStack(this);
                setAssociatedMachine(i, machine);
                items.add(i);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        DynamicMachine machine = getAssociatedMachine(stack);
        if(machine == null) {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip.machinery.empty"));
        } else {
            tooltip.add(TextFormatting.GRAY + machine.getLocalizedName());
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(worldIn.isRemote && getAssociatedMachine(player.getHeldItem(hand)) != null) {
            player.openGui(ModularMachinery.MODID, CommonProxy.GuiType.BLUEPRINT_PREVIEW.ordinal(), worldIn, hand == EnumHand.MAIN_HAND ? 0 : 1, 0, 0);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if(worldIn.isRemote && getAssociatedMachine(held) != null) {
            player.openGui(ModularMachinery.MODID, CommonProxy.GuiType.BLUEPRINT_PREVIEW.ordinal(), worldIn, hand == EnumHand.MAIN_HAND ? 0 : 1, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.PASS, held);
    }

    @Nullable
    public static DynamicMachine getAssociatedMachine(ItemStack stack) {
        return MachineRegistry.getRegistry().getMachine(getAssociatedMachineKey(stack));
    }

    @Nullable
    public static ResourceLocation getAssociatedMachineKey(ItemStack stack) {
        if(!stack.hasTagCompound()) {
            return null;
        }
        if(!stack.getTagCompound().hasKey(DYNAMIC_MACHINE_NBT_KEY)) {
            return null;
        }
        return new ResourceLocation(stack.getTagCompound().getString(DYNAMIC_MACHINE_NBT_KEY));
    }

    public static void setAssociatedMachine(ItemStack stack, DynamicMachine machine) {
        setAssociatedMachine(stack, machine.getRegistryName());
    }

    public static void setAssociatedMachine(ItemStack stack, ResourceLocation machineKey) {
        if(!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setString(DYNAMIC_MACHINE_NBT_KEY, machineKey.toString());
    }

}
