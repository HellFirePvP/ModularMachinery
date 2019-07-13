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
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentTypesMM
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:02
 */
public class ComponentTypesMM {

    private ComponentTypesMM() {}

    public static final ResourceLocation KEY_COMPONENT_ITEM = new ResourceLocation(ModularMachinery.MODID, "item");
    public static final ResourceLocation KEY_COMPONENT_FLUID = new ResourceLocation(ModularMachinery.MODID, "fluid");
    public static final ResourceLocation KEY_COMPONENT_GAS = new ResourceLocation(ModularMachinery.MODID, "gas");
    public static final ResourceLocation KEY_COMPONENT_ENERGY = new ResourceLocation(ModularMachinery.MODID, "energy");

    public static ComponentType COMPONENT_ITEM;
    public static ComponentType COMPONENT_FLUID;
    public static ComponentType COMPONENT_ENERGY;

    public static ComponentType COMPONENT_GAS;

}
