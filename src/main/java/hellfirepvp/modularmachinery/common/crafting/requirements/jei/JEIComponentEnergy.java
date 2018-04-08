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
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementEnergy;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Fracture Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIComponentEnergy
 * Created by HellFirePvP
 * Date: 08.04.2018 / 12:47
 */
public class JEIComponentEnergy extends ComponentRequirement.JEIComponent<Long> {

    private final RequirementEnergy requirement;

    public JEIComponentEnergy(RequirementEnergy requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<Long> getJEIRequirementClass() {
        return Long.class;
    }

    @Override
    public List<Long> getJEIIORequirements() {
        long var = requirement.getRequiredEnergyPerTick();
        return Lists.newArrayList(var);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<Long> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.Energy(offset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onJEIHoverTooltip(int slotIndex, boolean input, Long ingredient, List<String> tooltip) {

    }

}
