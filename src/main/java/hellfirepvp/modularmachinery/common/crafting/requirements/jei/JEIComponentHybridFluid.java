/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirements.jei;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Fracture Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIComponentHybridFluid
 * Created by HellFirePvP
 * Date: 08.04.2018 / 12:49
 */
public class JEIComponentHybridFluid extends ComponentRequirement.JEIComponent<HybridFluid> {

    private final RequirementFluid requirement;

    public JEIComponentHybridFluid(RequirementFluid requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<HybridFluid> getJEIRequirementClass() {
        return HybridFluid.class;
    }

    @Override
    public List<HybridFluid> getJEIIORequirements() {
        return Lists.newArrayList(requirement.required);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<HybridFluid> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.Tank(offset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onJEIHoverTooltip(int slotIndex, boolean input, HybridFluid ingredient, List<String> tooltip) {
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
