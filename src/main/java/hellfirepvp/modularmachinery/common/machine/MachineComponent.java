/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.util.HybridTank;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import hellfirepvp.modularmachinery.common.util.IOInventory;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineComponent
 * Created by HellFirePvP
 * Date: 28.06.2017 / 10:16
 */
public abstract class MachineComponent<T> {

    private final IOType ioType;

    public MachineComponent(IOType ioType) {
        this.ioType = ioType;
    }

    public final IOType getIOType() {
        return ioType;
    }

    public abstract ComponentType getComponentType();

    public abstract T getContainerProvider();

    public static abstract class ItemBus extends MachineComponent<IOInventory> {

        public ItemBus(IOType ioType) {
            super(ioType);
        }

        @Override
        public ComponentType getComponentType() {
            return ComponentTypesMM.COMPONENT_ITEM;
        }

    }

    public static abstract class FluidHatch extends MachineComponent<HybridTank> {

        public FluidHatch(IOType ioType) {
            super(ioType);
        }

        @Override
        public ComponentType getComponentType() {
            return ComponentTypesMM.COMPONENT_FLUID;
        }

    }

    public static abstract class EnergyHatch extends MachineComponent<IEnergyHandler> {

        public EnergyHatch(IOType ioType) {
            super(ioType);
        }

        @Override
        public ComponentType getComponentType() {
            return ComponentTypesMM.COMPONENT_ENERGY;
        }

    }

}
