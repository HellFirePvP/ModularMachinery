/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFluidTank
 * Created by HellFirePvP
 * Date: 07.07.2017 / 17:51
 */
public abstract class TileFluidTank extends TileEntitySynchronized implements MachineComponentTile {

    private FluidTank tank;
    private MachineComponent.IOType ioType;
    private FluidHatchSize hatchSize;

    public TileFluidTank() {}

    public TileFluidTank(FluidHatchSize size, MachineComponent.IOType type) {
        this.tank = size.buildTank(this, type == MachineComponent.IOType.INPUT, type == MachineComponent.IOType.OUTPUT);
        this.hatchSize = size;
        this.ioType = type;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.ioType = compound.getBoolean("input") ? MachineComponent.IOType.INPUT : MachineComponent.IOType.OUTPUT;
        this.hatchSize = FluidHatchSize.values()[MathHelper.clamp(compound.getInteger("size"), 0, FluidHatchSize.values().length - 1)];
        FluidTank newTank = hatchSize.buildTank(this, ioType == MachineComponent.IOType.INPUT, ioType == MachineComponent.IOType.OUTPUT);
        NBTTagCompound tankTag = compound.getCompoundTag("tank");
        newTank.readFromNBT(tankTag);
        this.tank = newTank;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setBoolean("input", ioType == MachineComponent.IOType.INPUT);
        compound.setInteger("size", this.hatchSize.ordinal());
        NBTTagCompound tankTag = new NBTTagCompound();
        this.tank.writeToNBT(tankTag);
        compound.setTag("tank", tankTag);
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.FluidHatch(ioType) {
            @Override
            public IFluidHandler getTank() {
                return TileFluidTank.this.tank;
            }
        };
    }
}
