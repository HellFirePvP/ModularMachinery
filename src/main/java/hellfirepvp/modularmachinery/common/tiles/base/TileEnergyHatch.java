/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchSize;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyInputHatch;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
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
public abstract class TileEnergyHatch extends TileEntitySynchronized implements IEnergyStorage, IEnergyHandler, MachineComponentTile, cofh.redstoneflux.api.IEnergyStorage {

    protected int energy = 0;
    protected EnergyHatchSize size;

    public TileEnergyHatch() {}

    public TileEnergyHatch(EnergyHatchSize size) {
        this.size = size;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if(!canReceive()) {
            return 0;
        }
        int insertable = this.energy + maxReceive > this.size.maxEnergy ? this.size.maxEnergy - this.energy : maxReceive;
        insertable = Math.min(insertable, size.transferLimit);
        if(!simulate) {
            this.energy = MathHelper.clamp(this.energy + maxReceive, 0, this.size.maxEnergy);
            markForUpdate();
        }
        return insertable;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if(!canExtract()) {
            return 0;
        }
        int extractable = this.energy - maxExtract < 0 ? this.energy : maxExtract;
        extractable = Math.min(extractable, size.transferLimit);
        if(!simulate) {
            this.energy = MathHelper.clamp(this.energy - extractable, 0, this.size.maxEnergy);
            markForUpdate();
        }
        return extractable;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.size.maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return this instanceof TileEnergyOutputHatch;
    }

    @Override
    public boolean canReceive() {
        return this instanceof TileEnergyInputHatch;
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
        this.size = EnergyHatchSize.values()[compound.getInteger("hatchSize")];
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("energy", this.energy);
        compound.setInteger("hatchSize", this.size.ordinal());
    }

    //MM stuff

    @Override
    public int getCurrentEnergy() {
        return this.energy;
    }

    @Override
    public void setCurrentEnergy(int energy) {
        this.energy = MathHelper.clamp(energy, 0, this.size.maxEnergy);
        markForUpdate();
    }

    @Override
    public int getMaxEnergy() {
        return this.size.maxEnergy;
    }

}
