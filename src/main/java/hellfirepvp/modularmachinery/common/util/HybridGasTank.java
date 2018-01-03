/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridGasTank
 * Created by HellFirePvP
 * Date: 26.08.2017 / 19:03
 */
public class HybridGasTank extends HybridTank implements IGasHandler {

    @Nullable
    protected GasStack gas;

    public HybridGasTank(int capacity) {
        super(capacity);
    }

    @Override
    public void setFluid(@Nullable FluidStack fluid) {
        super.setFluid(fluid);
        if(getFluid() != null) {
            setGas(null);
        }
    }

    @Override
    public int fillInternal(FluidStack resource, boolean doFill) {
        if(getGas() != null && getGas().amount > 0) {
            return 0;
        }
        return super.fillInternal(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        if(getGas() != null && getGas().amount > 0) {
            return null;
        }
        return super.drainInternal(maxDrain, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        if(getGas() != null && getGas().amount > 0) {
            return null;
        }
        return super.drainInternal(resource, doDrain);
    }

    public void setGas(@Nullable GasStack stack) {
        if(stack != null) {
            this.gas = stack.copy();
            setFluid(null);
        } else {
            this.gas = null;
        }
    }

    @Nullable
    public GasStack getGas() {
        return this.gas;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (stack == null || stack.amount <= 0) {
            return 0;
        }

        if (fluid != null && fluid.amount > 0) {
            return 0; //We don't collide with the internal fluids
        }

        if (!doTransfer) {
            if (getGas() == null) {
                return Math.min(capacity, stack.amount);
            }

            if (!getGas().isGasEqual(stack)) {
                return 0;
            }

            return Math.min(capacity - getGas().amount, stack.amount);
        }

        if (getGas() == null) {
            setGas(new GasStack(stack.getGas(), Math.min(capacity, stack.amount)));

            onContentsChanged();
            return getGas().amount;
        }

        if (!getGas().isGasEqual(stack)) {
            return 0;
        }
        int filled = capacity - getGas().amount;

        if (getGas().amount < filled) {
            getGas().amount += stack.amount;
            filled = stack.amount;
        } else {
            getGas().amount = capacity;
        }

        onContentsChanged();

        return filled;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (getGas() == null || amount <= 0) {
            return null;
        }
        if (getFluid() != null && getFluid().amount > 0) {
            return null; //We don't collide with the internal fluids
        }

        int drained = amount;
        if (getGas().amount < drained) {
            drained = getGas().amount;
        }

        GasStack stack = new GasStack(getGas().getGas(), drained);
        if (doTransfer) {
            getGas().amount -= drained;
            if (getGas().amount <= 0) {
                setGas(null);
            }

            onContentsChanged();

        }
        return stack;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return super.canFill();
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return super.canDrain();
    }

    public void readGasFromNBT(NBTTagCompound nbt) {
        NBTTagCompound subGas = nbt.getCompoundTag("gasTag");
        if (subGas.getSize() > 0) {
            if (!subGas.hasKey("Empty")) {
                setGas(GasStack.readFromNBT(subGas));
            } else {
                setGas(null);
            }
        } else {
            setGas(null);
        }
    }

    public void writeGasToNBT(NBTTagCompound nbt) {
        NBTTagCompound subGas = new NBTTagCompound();
        if (gas != null) {
            gas.write(subGas);
        } else {
            subGas.setString("Empty", "");
        }
        nbt.setTag("gasTag", subGas);
    }

}
