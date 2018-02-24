/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.types.ComponentEnergy;
import hellfirepvp.modularmachinery.common.crafting.types.ComponentFluid;
import hellfirepvp.modularmachinery.common.crafting.types.ComponentGas;
import hellfirepvp.modularmachinery.common.crafting.types.ComponentItem;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentType
 * Created by HellFirePvP
 * Date: 24.02.2018 / 11:56
 */
public abstract class ComponentType<R extends ComponentRequirement> {

    //A unique registry key defining this component
    @Nonnull
    public abstract String getRegistryName();

    //Should return the mod's modid if this component is dependant on some other mod
    @Nullable
    public abstract String requiresModid();

    //If parsing goes wrong, throw an appropiate exception instead. MM catches this and batches the information!
    //Will also only be called if the appropriate mod is loaded, if specified!
    @Nonnull
    public abstract R provideComponent(MachineComponent.IOType machineIOType, JsonObject jsonObject);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentType<?> that = (ComponentType<?>) o;
        return getRegistryName().equalsIgnoreCase(that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return getRegistryName().hashCode();
    }

    public static class Registry {

        private static boolean initialized = false;
        private static Map<String, ComponentType> components = new HashMap<>();

        public static void register(ComponentType type) {
            if(components.containsKey(type.getRegistryName())) {
                throw new IllegalArgumentException("Component with registry name " + type.getRegistryName() + " already exists!");
            }
            if(type.requiresModid() != null && !Loader.isModLoaded(type.requiresModid())) {
                ModularMachinery.log.info("[Modular Machinery] ignoring componenttype " + type.getRegistryName() + " because " + type.requiresModid() + " is not loaded!");
                return;
            }
            components.put(type.getRegistryName(), type);
        }

        public static void initialize() {
            if(initialized) return;

            register(new ComponentItem());
            register(new ComponentFluid());
            register(new ComponentEnergy());
            register(new ComponentGas());

            MinecraftForge.EVENT_BUS.post(new ComponentRegistryEvent());

            initialized = true;
        }

        @Nullable
        public static ComponentType getComponent(String name) {
            for (String key : components.keySet()) {
                if(key.equalsIgnoreCase(name)) {
                    return components.get(key);
                }
            }
            return null;
        }
    }

    public static class ComponentRegistryEvent extends Event {}

}
