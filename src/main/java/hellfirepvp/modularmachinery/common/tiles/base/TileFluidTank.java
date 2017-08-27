/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.HybridGasTank;
import hellfirepvp.modularmachinery.common.util.HybridTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFluidTank
 * Created by HellFirePvP
 * Date: 07.07.2017 / 17:51
 */
@Optional.InterfaceList({
        @Optional.Interface(modid = "mekanism", iface = "mekanism.api.gas.IGasHandler"),
        @Optional.Interface(modid = "mekanism", iface = "mekanism.api.gas.ITubeConnection")
})
public abstract class TileFluidTank extends TileColorableMachineComponent implements MachineComponentTile, IGasHandler, ITubeConnection {

    private HybridTank tank;
    private MachineComponent.IOType ioType;
    private FluidHatchSize hatchSize;

    public TileFluidTank() {}

    public TileFluidTank(FluidHatchSize size, MachineComponent.IOType type) {
        this.tank = size.buildTank(this, type == MachineComponent.IOType.INPUT, type == MachineComponent.IOType.OUTPUT);
        this.hatchSize = size;
        this.ioType = type;
    }

    public HybridTank getTank() {
        return tank;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        if(ModularMachinery.isMekanismLoaded) {
            if(checkMekanismGasCapabilities(capability)) {
                return true;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        if(ModularMachinery.isMekanismLoaded) {
            if(checkMekanismGasCapabilities(capability)) {
                return (T) this;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Optional.Method(modid = "mekanism")
    private boolean checkMekanismGasCapabilities(Capability<?> capability) {
        Object defaultInstance = capability.getDefaultInstance();
        return defaultInstance instanceof IGasHandler || defaultInstance instanceof ITubeConnection;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.ioType = compound.getBoolean("input") ? MachineComponent.IOType.INPUT : MachineComponent.IOType.OUTPUT;
        this.hatchSize = FluidHatchSize.values()[MathHelper.clamp(compound.getInteger("size"), 0, FluidHatchSize.values().length - 1)];
        HybridTank newTank = hatchSize.buildTank(this, ioType == MachineComponent.IOType.INPUT, ioType == MachineComponent.IOType.OUTPUT);
        NBTTagCompound tankTag = compound.getCompoundTag("tank");
        newTank.readFromNBT(tankTag);
        this.tank = newTank;
        if(ModularMachinery.isMekanismLoaded) {
            this.readMekGasData(tankTag);
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setBoolean("input", ioType == MachineComponent.IOType.INPUT);
        compound.setInteger("size", this.hatchSize.ordinal());
        NBTTagCompound tankTag = new NBTTagCompound();
        this.tank.writeToNBT(tankTag);
        if(ModularMachinery.isMekanismLoaded) {
            this.writeMekGasData(tankTag);
        }
        compound.setTag("tank", tankTag);
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.FluidHatch(ioType) {
            @Override
            public HybridTank getTank() {
                return TileFluidTank.this.tank;
            }
        };
    }

    //Mek things


    @Override
    @Optional.Method(modid = "mekanism")
    public boolean canTubeConnect(EnumFacing side) {
        return true;
    }

    @Optional.Method(modid = "mekanism")
    private void writeMekGasData(NBTTagCompound compound) {
        if(this.tank instanceof HybridGasTank) {
            ((HybridGasTank) this.tank).writeGasToNBT(compound);
        }
    }

    @Optional.Method(modid = "mekanism")
    private void readMekGasData(NBTTagCompound compound) {
        if(this.tank instanceof HybridGasTank) {
            ((HybridGasTank) this.tank).readGasFromNBT(compound);
        }
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if(this.tank instanceof HybridGasTank) {
            return ((HybridGasTank) this.tank).receiveGas(side, stack, doTransfer);
        }
        return 0;
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if(this.tank instanceof HybridGasTank) {
            return ((HybridGasTank) this.tank).drawGas(side, amount, doTransfer);
        }
        return null;
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return this.tank instanceof HybridGasTank && ((HybridGasTank) this.tank).canReceiveGas(side, type);
    }

    @Override
    @Optional.Method(modid = "mekanism")
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return this.tank instanceof HybridGasTank && ((HybridGasTank) this.tank).canDrawGas(side, type);
    }
}
