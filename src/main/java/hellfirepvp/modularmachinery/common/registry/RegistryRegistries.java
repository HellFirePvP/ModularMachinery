/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.crafting.tooltip.RequirementTip;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.*;

import static hellfirepvp.modularmachinery.common.lib.RegistriesMM.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRegistries
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:03
 */
public class RegistryRegistries {

    private RegistryRegistries() {}

    public static void buildRegistries() {
        makeRegistry(REQUIREMENT_TYPE_REGISTRY_NAME, RequirementType.class).create();
        makeRegistry(COMPONENT_TYPE_REGISTRY_NAME, ComponentType.class).create();
        makeRegistry(ADAPTER_REGISTRY_NAME, RecipeAdapter.class).create();
        makeRegistry(REQUIREMENT_TIPS_REGISTRY_NAME, RequirementTip.class).create();

        ADAPTER_REGISTRY          = RegistryManager.ACTIVE.getRegistry(ADAPTER_REGISTRY_NAME);
        COMPONENT_TYPE_REGISTRY   = RegistryManager.ACTIVE.getRegistry(COMPONENT_TYPE_REGISTRY_NAME);
        REQUIREMENT_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(REQUIREMENT_TYPE_REGISTRY_NAME);
        REQUIREMENT_TIPS_REGISTRY = RegistryManager.ACTIVE.getRegistry(REQUIREMENT_TIPS_REGISTRY_NAME);
    }

    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceLocation name, Class<T> type) {
        return new RegistryBuilder<T>().setName(name).setType(type).setMaxID(Short.MAX_VALUE).disableSaving().allowModification();
    }

}
