/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementType
 * Created by HellFirePvP
 * Date: 13.07.2019 / 10:38
 */
public abstract class RequirementType<T, V extends ComponentRequirement<T, ? extends RequirementType<T, V>>> extends IForgeRegistryEntry.Impl<RequirementType<?, ?>> {

    private final ComponentType componentType;

    public RequirementType(@Nullable ComponentType requiredType) {
        this.componentType = requiredType;
    }

    @Nullable
    public ComponentType getComponentType() {
        return componentType;
    }

    public abstract ComponentRequirement<T, ? extends RequirementType<T, V>> createRequirement(IOType type, JsonObject jsonObject);

    @Nullable
    public String requiresModid() {
        return null;
    }

}
