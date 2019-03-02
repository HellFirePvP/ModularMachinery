/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PreparedRecipe
 * Created by HellFirePvP
 * Date: 03.01.2018 / 15:56
 */
public interface PreparedRecipe {

    public String getFilePath();

    public ResourceLocation getRecipeRegistryName();

    public ResourceLocation getAssociatedMachineName();

    public int getTotalProcessingTickTime();

    public int getPriority();

    public List<ComponentRequirement> getComponents();

}
