/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.item.ItemStack;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentOutputRestrictor
 * Created by HellFirePvP
 * Date: 12.07.2017 / 21:19
 */
abstract class ComponentOutputRestrictor {

    static class RestrictionTank extends ComponentOutputRestrictor {

        final HybridFluid inserted;
        final MachineComponent exactComponent;

        RestrictionTank(HybridFluid inserted, MachineComponent exactComponent) {
            this.inserted = inserted;
            this.exactComponent = exactComponent;
        }

    }

    static class RestrictionInventory extends ComponentOutputRestrictor {

        final ItemStack inserted;
        final MachineComponent exactComponent;

        RestrictionInventory(ItemStack inserted, MachineComponent exactComponent) {
            this.inserted = inserted;
            this.exactComponent = exactComponent;
        }

    }

}
