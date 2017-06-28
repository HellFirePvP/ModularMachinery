/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MachineRequirement
 * Created by HellFirePvP
 * Date: 28.06.2017 / 10:33
 */
public class MachineRequirement {

    private final MachineComponent.IOType ioType;
    private final MachineComponent.ComponentType componentType;

    public MachineRequirement(MachineComponent.IOType ioType, MachineComponent.ComponentType type) {
        this.ioType = ioType;
        this.componentType = type;
    }

    public MachineComponent.IOType getRequiredIoType() {
        return ioType;
    }

    public MachineComponent.ComponentType getRequiredComponentType() {
        return componentType;
    }

}
