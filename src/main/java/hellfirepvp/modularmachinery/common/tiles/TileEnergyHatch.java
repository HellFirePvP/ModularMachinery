/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEnergyHatch
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:14
 */
@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyStorage", modid = "redstoneflux")
public class TileEnergyHatch extends TileEntitySynchronized implements IEnergyStorage, IEnergyHandler, MachineComponentTile, cofh.redstoneflux.api.IEnergyStorage {

    private int energy = 0;
    private int maxEnergy;
    private MachineComponent.IOType ioType;

    public TileEnergyHatch() {}

    public TileEnergyHatch(MachineComponent.IOType type, int maxEnergy) {
        this.ioType = type;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if(!canReceive()) {
            return 0;
        }
        int insertable = this.energy + maxReceive > this.maxEnergy ? this.maxEnergy - this.energy : maxReceive;
        if(!simulate) {
            this.energy = MathHelper.clamp(this.energy + maxReceive, 0, this.maxEnergy);
        }
        return insertable;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if(!canExtract()) {
            return 0;
        }
        int extractable = this.energy - maxExtract < 0 ? this.energy : maxExtract;
        if(!simulate) {
            this.energy = MathHelper.clamp(this.energy - extractable, 0, this.maxEnergy);
        }
        return extractable;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return this.ioType == MachineComponent.IOType.OUTPUT;
    }

    @Override
    public boolean canReceive() {
        return this.ioType == MachineComponent.IOType.INPUT;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.energy = compound.getInteger("energy");
        this.maxEnergy = compound.getInteger("maxEnergy");
        this.ioType = compound.getBoolean("ioType") ? MachineComponent.IOType.INPUT : MachineComponent.IOType.OUTPUT;
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("energy", this.energy);
        compound.setInteger("maxEnergy", this.maxEnergy);
        compound.setBoolean("ioType", this.ioType == MachineComponent.IOType.INPUT);
    }

    //MM stuff

    @Override
    public int getCurrentEnergy() {
        return this.energy;
    }

    @Override
    public void setCurrentEnergy(int energy) {
        this.energy = MathHelper.clamp(energy, 0, this.maxEnergy);
    }

    @Override
    public int getMaxEnergy() {
        return this.maxEnergy;
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.EnergyHatch(this.ioType) {
            @Override
            public IEnergyHandler getEnergyBuffer() {
                return TileEnergyHatch.this;
            }
        };
    }
}
