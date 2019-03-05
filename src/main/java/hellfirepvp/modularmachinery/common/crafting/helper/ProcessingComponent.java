/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ProcessingComponent
 * Created by HellFirePvP
 * Date: 04.03.2019 / 22:44
 */
public class ProcessingComponent<T> {

    private final MachineComponent<T> component;
    private final T providedComponent;
    private final ComponentSelectorTag tag;

    public ProcessingComponent(MachineComponent<T> component, T providedComponent, ComponentSelectorTag tag) {
        this.component = component;
        this.providedComponent = providedComponent;
        this.tag = tag;
    }

    public MachineComponent<T> getComponent() {
        return component;
    }

    public T getProvidedComponent() {
        return providedComponent;
    }

    @Nullable
    public ComponentSelectorTag getTag() {
        return tag;
    }

}
