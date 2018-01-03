/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.ingredient;

import mekanism.api.gas.GasStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridFluidGas
 * Created by HellFirePvP
 * Date: 26.08.2017 / 23:16
 */
public class HybridFluidGas extends HybridFluid {

    @Nonnull
    private final GasStack underlyingGas;

    public HybridFluidGas(@Nonnull GasStack gasStack) {
        super(null);
        this.underlyingGas = gasStack;
    }

    @Override
    public int getAmount() {
        return underlyingGas.amount;
    }

    @Override
    public void setAmount(int amount) {
        underlyingGas.amount = amount;
    }

    @Nonnull
    public GasStack asGasStack() {
        return underlyingGas;
    }

    @Override
    public HybridFluid copy() {
        return new HybridFluidGas(this.underlyingGas.copy());
    }
}
