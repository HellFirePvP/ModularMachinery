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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementTip
 * Created by HellFirePvP
 * Date: 14.07.2019 / 19:14
 */
public abstract class RequirementTip extends IForgeRegistryEntry.Impl<RequirementTip> {

    public static final int LINE_HEIGHT = 9;
    public static final int SPLIT_HEIGHT = 2;

    /**
     * If an empty collection is returned here, {@link #buildTooltip(MachineRecipe, Collection)} isn't called
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    public abstract Collection<ComponentRequirement<?, ?>> filterRequirements(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> requirements);

    /**
     * Called with the components filtered through {@link #filterRequirements(MachineRecipe, Collection)}
     */
    @Nonnull
    @SideOnly(Side.CLIENT)
    public abstract List<String> buildTooltip(MachineRecipe recipe, Collection<ComponentRequirement<?, ?>> filteredComponents);

}
