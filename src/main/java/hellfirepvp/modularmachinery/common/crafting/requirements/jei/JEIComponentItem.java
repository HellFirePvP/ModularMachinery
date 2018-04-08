/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirements.jei;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.util.FuelItemHelper;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Fracture Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIComponentItem
 * Created by HellFirePvP
 * Date: 08.04.2018 / 12:44
 */
public class JEIComponentItem extends ComponentRequirement.JEIComponent<ItemStack> {

    private final RequirementItem requirement;

    public JEIComponentItem(RequirementItem requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<ItemStack> getJEIRequirementClass() {
        return ItemStack.class;
    }

    @Override
    public List<ItemStack> getJEIIORequirements() {
        switch (requirement.requirementType) {
            case ITEMSTACKS:
                ItemStack stack = ItemUtils.copyStackWithSize(requirement.required, requirement.required.getCount());
                if(requirement.previewDisplayTag != null) {
                    stack.setTagCompound(requirement.previewDisplayTag);
                } else if(requirement.tag != null) {
                    requirement.previewDisplayTag = requirement.tag.copy();
                    stack.setTagCompound(requirement.previewDisplayTag.copy());
                }
                return Lists.newArrayList(stack);
            case OREDICT:
                NonNullList<ItemStack> stacks = OreDictionary.getOres(requirement.oreDictName);
                NonNullList<ItemStack> out = NonNullList.create();
                for (ItemStack oreDictIn : stacks) {
                    if (oreDictIn.getItemDamage() == OreDictionary.WILDCARD_VALUE && !oreDictIn.isItemStackDamageable() && oreDictIn.getItem().getCreativeTab() != null) {
                        oreDictIn.getItem().getSubItems(oreDictIn.getItem().getCreativeTab(), out);
                    } else {
                        out.add(oreDictIn);
                    }
                }
                NonNullList<ItemStack> stacksOut = NonNullList.create();
                for (ItemStack s : out) {
                    ItemStack copy = s.copy();
                    copy.setCount(requirement.oreDictItemAmount);
                    stacksOut.add(copy);
                }
                return stacksOut;
            case FUEL:
                return FuelItemHelper.getFuelItems();
        }
        return Lists.newArrayList();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<ItemStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.Item(offset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onJEIHoverTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if(requirement.requirementType == RequirementItem.ItemRequirementType.FUEL) {
            int burn = TileEntityFurnace.getItemBurnTime(ingredient);
            if(burn > 0) {
                tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.machinery.fuel.item", burn));
            }
            tooltip.add(I18n.format("tooltip.machinery.fuel"));
        }
        if(requirement.chance < 1F && requirement.chance >= 0F) {
            String keyNever = input ? "tooltip.machinery.chance.in.never" : "tooltip.machinery.chance.out.never";
            String keyChance = input ? "tooltip.machinery.chance.in" : "tooltip.machinery.chance.out";

            String chanceStr = String.valueOf(MathHelper.floor(requirement.chance * 100F));
            if(requirement.chance == 0F) {
                tooltip.add(I18n.format(keyNever));
            } else {
                if(requirement.chance < 0.01F) {
                    chanceStr = "< 1";
                }
                chanceStr += "%";
                tooltip.add(I18n.format(keyChance, chanceStr));
            }
        }
    }

}
