/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.crafting.tooltip.RequirementTip;
import hellfirepvp.modularmachinery.common.crafting.tooltip.TooltipEnergyInput;
import hellfirepvp.modularmachinery.common.crafting.tooltip.TooltipEnergyOutput;
import hellfirepvp.modularmachinery.common.crafting.tooltip.TooltipFuelInput;
import net.minecraft.util.ResourceLocation;

import static hellfirepvp.modularmachinery.common.lib.RequirementTipsMM.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRequirementTips
 * Created by HellFirePvP
 * Date: 14.07.2019 / 19:24
 */
public class RegistryRequirementTips {

    private RegistryRequirementTips() {}

    public static void initialize() {
        TIP_ENERGY_INPUT = register(new TooltipEnergyInput(), TIP_ENERGY_INPUT_NAME);
        TIP_ENERGY_OUTPUT = register(new TooltipEnergyOutput(), TIP_ENERGY_OUTPUT_NAME);
        TIP_FUEL_INPUT = register(new TooltipFuelInput(), TIP_FUEL_INPUT_NAME);
    }

    private static <T extends RequirementTip> T register(T requirementTip, ResourceLocation name) {
        requirementTip.setRegistryName(name);
        CommonProxy.registryPrimer.register(requirementTip);
        return requirementTip;
    }

}
