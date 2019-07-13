/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.*;
import net.minecraft.util.ResourceLocation;

import static hellfirepvp.modularmachinery.common.lib.RequirementTypesMM.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRequirementTypes
 * Created by HellFirePvP
 * Date: 13.07.2019 / 11:16
 */
public class RegistryRequirementTypes {

    private RegistryRequirementTypes() {}

    public static void initialize() {
        REQUIREMENT_ITEM = register(new RequirementTypeItem(), KEY_REQUIREMENT_ITEM);
        REQUIREMENT_FLUID = register(new RequirementTypeFluid(), KEY_REQUIREMENT_FLUID);
        REQUIREMENT_ENERGY = register(new RequirementTypeEnergy(), KEY_REQUIREMENT_ENERGY);
        REQUIREMENT_GAS = register(new RequirementTypeGas(), KEY_REQUIREMENT_GAS);

        REQUIREMENT_DURATION = register(new RequirementDuration(), KEY_REQUIREMENT_DURATION);
    }

    private static <T extends RequirementType<?, ?>> T register(T requirementType, ResourceLocation key) {
        requirementType.setRegistryName(key);
        CommonProxy.registryPrimer.register(requirementType);
        return requirementType;
    }

}
