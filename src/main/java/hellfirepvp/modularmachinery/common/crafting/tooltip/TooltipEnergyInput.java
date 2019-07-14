/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.tooltip;

import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TooltipEnergyInput
 * Created by HellFirePvP
 * Date: 14.07.2019 / 19:22
 */
public class TooltipEnergyInput extends RequirementTip {

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public Collection<ComponentRequirement<?, ?>> filterRequirements(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> requirements) {
        return requirements.stream()
                .filter(r -> r instanceof RequirementEnergy)
                .filter(r -> r.getActionType() == IOType.INPUT)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public List<String> buildTooltip(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> filteredComponents) {
        long totalEnergyIn = 0;
        for (ComponentRequirement<?, ?> energy : filteredComponents) {
            totalEnergyIn += ((RequirementEnergy) energy).getRequiredEnergyPerTick();
        }
        List<String> tooltip = new ArrayList<>();
        if (totalEnergyIn > 0) {
            String energyType = I18n.format(EnergyDisplayUtil.type.getUnlocalizedFormat());
            long energyIn = EnergyDisplayUtil.type.formatEnergyForDisplay(totalEnergyIn);

            tooltip.add(I18n.format("tooltip.machinery.energy.in"));
            tooltip.add(I18n.format("tooltip.machinery.energy.in.tick", energyIn, energyType));
            tooltip.add(I18n.format("tooltip.machinery.energy.in.total", energyIn * recipe.getRecipeTotalTickTime(), energyType));
        }
        return tooltip;
    }
}
