/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryModifiable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistriesMM
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:03
 */
public class RegistriesMM {

    private RegistriesMM() {}

    public static ResourceLocation ADAPTER_REGISTRY_NAME = new ResourceLocation(ModularMachinery.MODID, "recipeadapters");
    public static ResourceLocation COMPONENT_TYPE_REGISTRY_NAME = new ResourceLocation(ModularMachinery.MODID, "componenttypes");
    public static ResourceLocation REQUIREMENT_TYPE_REGISTRY_NAME = new ResourceLocation(ModularMachinery.MODID, "requirementtypes");

    public static IForgeRegistryModifiable<RecipeAdapter> ADAPTER_REGISTRY;
    public static IForgeRegistryModifiable<ComponentType> COMPONENT_TYPE_REGISTRY;
    public static IForgeRegistryModifiable<RequirementType<?, ?>> REQUIREMENT_TYPE_REGISTRY;

}
