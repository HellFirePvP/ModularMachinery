/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import gregtech.api.capability.GregtechCapabilities;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchSize;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyInputHatch;
import hellfirepvp.modularmachinery.common.tiles.TileEnergyOutputHatch;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
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
public abstract class TileEnergyHatch extends TileColorableMachineComponent implements ITickable, IEnergyStorage, IEnergyHandler, MachineComponentTile, cofh.redstoneflux.api.IEnergyStorage {

    protected long energy = 0;
    protected EnergyHatchSize size;

    private GTEnergyContainer energyContainer;

    public TileEnergyHatch() {}

    public TileEnergyHatch(EnergyHatchSize size, IOType ioType) {
        this.size = size;
        this.energyContainer = new GTEnergyContainer(this, ioType);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if(!canReceive()) {
            return 0;
        }
        int insertable = this.energy + maxReceive > this.size.maxEnergy ? convertDownEnergy(this.size.maxEnergy - this.energy) : maxReceive;
        insertable = Math.min(insertable, convertDownEnergy(size.transferLimit));
        if(!simulate) {
            this.energy = MiscUtils.clamp(this.energy + insertable, 0, this.size.maxEnergy);
            markForUpdate();
        }
        return insertable;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if(!canExtract()) {
            return 0;
        }
        int extractable = this.energy - maxExtract < 0 ? convertDownEnergy(this.energy) : maxExtract;
        extractable = Math.min(extractable, convertDownEnergy(size.transferLimit));
        if(!simulate) {
            this.energy = MiscUtils.clamp(this.energy - extractable, 0, this.size.maxEnergy);
            markForUpdate();
        }
        return extractable;
    }

    @Override
    public int getEnergyStored() {
        return convertDownEnergy(this.energy);
    }

    @Override
    public int getMaxEnergyStored() {
        return convertDownEnergy(this.size.maxEnergy);
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
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return (T) this;
        }
        if (Mods.GREGTECH.isPresent() &&
                capability == getGTEnergyCapability()) {
            return (T) this.energyContainer;
        }

        return super.getCapability(capability, facing);
    }

    @Optional.Method(modid = "gregtech")
    private Capability<?> getGTEnergyCapability() {
        return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        NBTBase energyTag = compound.getTag("energy");
        if (energyTag instanceof NBTPrimitive) {
            this.energy = ((NBTPrimitive) energyTag).getLong();
        }
        this.size = EnergyHatchSize.values()[compound.getInteger("hatchSize")];
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setLong("energy", this.energy);
        compound.setInteger("hatchSize", this.size.ordinal());
    }

    protected int convertDownEnergy(long energy) {
        return energy >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
    }

    //MM stuff

    public EnergyHatchSize getTier() {
        return size;
    }

    @Override
    public long getCurrentEnergy() {
        return this.energy;
    }

    @Override
    public void setCurrentEnergy(long energy) {
        this.energy = MiscUtils.clamp(energy, 0, getMaxEnergy());
        markForUpdate();
    }

    @Override
    public long getMaxEnergy() {
        return this.size.maxEnergy;
    }

}
