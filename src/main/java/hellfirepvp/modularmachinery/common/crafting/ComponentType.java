/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentType
 * Created by HellFirePvP
 * Date: 24.02.2018 / 11:56
 */
public abstract class ComponentType extends IForgeRegistryEntry.Impl<ComponentType> {

    //Should return the mod's modid if this component is dependent on some other mod
    //Return null if no other mod/only vanilla is required.
    @Nullable
    public abstract String requiresModid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentType that = (ComponentType) o;
        return getRegistryName().equals(that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return getRegistryName().hashCode();
    }

}
