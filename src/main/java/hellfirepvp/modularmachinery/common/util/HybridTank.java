/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridTank
 * Created by HellFirePvP
 * Date: 26.08.2017 / 18:57
 */
@Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "mekanism")
public class HybridTank extends FluidTank {

    public HybridTank(int capacity) {
        super(capacity);
    }

}
