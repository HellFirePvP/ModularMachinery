/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.tooltip;

import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TooltipFuelInput
 * Created by HellFirePvP
 * Date: 14.07.2019 / 19:37
 */
public class TooltipFuelInput extends RequirementTip {

    @Nonnull
    @Override
    public Collection<ComponentRequirement<?, ?>> filterRequirements(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> requirements) {
        return requirements.stream()
                .filter(c -> c instanceof RequirementItem)
                .filter(c -> c.getActionType() == IOType.INPUT)
                .filter(c -> ((RequirementItem) c).requirementType == RequirementItem.ItemRequirementType.FUEL)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<String> buildTooltip(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> filteredComponents) {
        int totalFuelIn = 0;
        for (ComponentRequirement<?, ?> fuel : filteredComponents) {
            totalFuelIn += ((RequirementItem) fuel).fuelBurntime;
        }
        List<String> tooltip = new ArrayList<>();
        if (totalFuelIn > 0) {
            tooltip.add(I18n.format("tooltip.machinery.fuel.in"));
            tooltip.add(I18n.format("tooltip.machinery.fuel.in.total", totalFuelIn));
        }
        return tooltip;
    }
}
