/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.ingredient;

import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridFluid
 * Created by HellFirePvP
 * Date: 26.08.2017 / 23:16
 */
public class HybridFluid {

    @Nullable
    private final FluidStack underlyingFluid;

    public HybridFluid(@Nullable FluidStack underlyingFluid) {
        this.underlyingFluid = underlyingFluid;
    }

    public int getAmount() {
        if(underlyingFluid == null) {
            return 0;
        }
        return underlyingFluid.amount;
    }

    public void setAmount(int amount) {
        if(underlyingFluid != null) {
            underlyingFluid.amount = amount;
        }
    }

    @Nullable
    public FluidStack asFluidStack() {
        return underlyingFluid;
    }

    public HybridFluid copy() {
        if(underlyingFluid == null) {
            return new HybridFluid(null);
        }
        return new HybridFluid(this.underlyingFluid.copy());
    }

}
