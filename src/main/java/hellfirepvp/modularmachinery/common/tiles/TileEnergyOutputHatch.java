/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.redstoneflux.api.IEnergyStorage;
import com.brandon3055.draconicevolution.DEFeatures;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyStorageCore;
import com.google.common.collect.Iterables;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchSize;
import hellfirepvp.modularmachinery.common.integration.IntegrationIC2EventHandlerHelper;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEnergyOutputHatch
 * Created by HellFirePvP
 * Date: 08.07.2017 / 12:43
 */
@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2")
public class TileEnergyOutputHatch extends TileEnergyHatch implements IEnergySource {

    private BlockPos foundCore = null;

    public TileEnergyOutputHatch() {}

    public TileEnergyOutputHatch(EnergyHatchSize size) {
        super(size, MachineComponent.IOType.OUTPUT);
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        long transferCap = Math.min(this.size.transferLimit, this.energy);
        if (Mods.DRACONICEVOLUTION.isPresent()) {
            long transferred = attemptDECoreTransfer(transferCap);
            transferCap -= transferred;
            this.energy -= transferred;
        }
        long usableAmps = Math.min(this.size.getGtAmperage(), transferCap / 4L / this.size.getGTEnergyTransferVoltage());
        for (EnumFacing face : EnumFacing.VALUES) {
            if (transferCap > 0 && Mods.GREGTECH.isPresent() && usableAmps > 0) {
                long totalTransferred = attemptGTTransfer(face, transferCap / 4L, usableAmps) * 4L;
                usableAmps -= totalTransferred / 4L / this.size.getGTEnergyTransferVoltage();
                transferCap -= totalTransferred;
                this.energy -= totalTransferred;
            }
            if (transferCap > 0) {
                if (Mods.REDSTONEFLUXAPI.isPresent()) {
                    int transferred = attemptFERFTransfer(face, convertDownEnergy(transferCap));
                    transferCap -= transferred;
                    this.energy -= transferred;
                } else {
                    int transferred = attemptFETransfer(face, convertDownEnergy(transferCap));
                    transferCap -= transferred;
                    this.energy -= transferred;
                }
            }
            if(transferCap <= 0) {
                break;
            }
        }
    }

    @Optional.Method(modid = "draconicevolution")
    private long attemptDECoreTransfer(long transferCap) {
        TileEntity te = foundCore == null ? null : world.getTileEntity(foundCore);
        if (foundCore == null || !(te instanceof TileEnergyStorageCore)) {
            foundCore = findCore(foundCore);
        }

        if (foundCore != null && te instanceof TileEnergyStorageCore) {
            TileEnergyStorageCore core = (TileEnergyStorageCore) te;

            long energyReceived = Math.min(core.getExtendedCapacity() - core.energy.value, transferCap);
            ((TileEnergyStorageCore) te).energy.value += energyReceived;

            return energyReceived;
        }
        return 0;
    }

    @Optional.Method(modid = "draconicevolution")
    private BlockPos findCore(BlockPos before) {
        List<TileEnergyStorageCore> list = new LinkedList<>();
        int range = 24;

        Iterable<BlockPos> positions = BlockPos.getAllInBox(pos.add(-range, -range, -range), pos.add(range, range, range));

        for (BlockPos blockPos : positions) {
            if (world.getBlockState(blockPos).getBlock() == DEFeatures.energyStorageCore) {
                TileEntity tile = world.getTileEntity(blockPos);
                if (tile instanceof TileEnergyStorageCore && ((TileEnergyStorageCore) tile).active.value) {
                    list.add(((TileEnergyStorageCore) tile));
                }
            }
        }
        if (before != null) {
            list.removeIf(tile -> tile.getPos().equals(before));
        }
        Collections.shuffle(list);
        TileEnergyStorageCore first = Iterables.getFirst(list, null);
        return first == null ? null : first.getPos();
    }

    @Optional.Method(modid = "gregtech")
    private long attemptGTTransfer(EnumFacing face, long transferCap, long usedAmps) {
        long voltage = this.size.getGTEnergyTransferVoltage();
        long amperes = Math.min(usedAmps, this.size.getGtAmperage());
        int transferableAmps = 0;
        while (transferableAmps < amperes && (transferableAmps * voltage) <= transferCap) {
            transferableAmps++;
        }
        if (transferableAmps == 0) {
            return 0L;
        }

        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(face));
        EnumFacing oppositeSide = face.getOpposite();
        if(tileEntity != null && tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide)) {
            IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide);
            if (energyContainer != null && energyContainer.inputsEnergy(oppositeSide)) {
                return energyContainer.acceptEnergyFromNetwork(oppositeSide, voltage, transferableAmps)
                        * voltage;
            }
        }
        return 0L;
    }

    private int attemptFETransfer(EnumFacing face, int maxTransferLeft) {
        BlockPos at = this.getPos().offset(face);
        EnumFacing accessingSide = face.getOpposite();

        int receivedEnergy = 0;
        TileEntity te = world.getTileEntity(at);
        if(te != null && !(te instanceof TileEnergyHatch)) {
            if(te.hasCapability(CapabilityEnergy.ENERGY, accessingSide)) {
                net.minecraftforge.energy.IEnergyStorage ce = te.getCapability(CapabilityEnergy.ENERGY, accessingSide);
                if(ce != null && ce.canReceive()) {
                    try {
                    receivedEnergy = ce.receiveEnergy(maxTransferLeft, false);
                    } catch (Exception ignored) {}
                }
            }
        }
        return receivedEnergy;
    }

    @Optional.Method(modid = "redstoneflux")
    private int attemptFERFTransfer(EnumFacing face, int maxTransferLeft) {
        BlockPos at = this.getPos().offset(face);
        EnumFacing accessingSide = face.getOpposite();

        int receivedEnergy = 0;
        TileEntity te = world.getTileEntity(at);
        if(te != null && !(te instanceof TileEnergyHatch)) {
            if(te instanceof cofh.redstoneflux.api.IEnergyReceiver && ((IEnergyReceiver) te).canConnectEnergy(accessingSide)) {
                try {
                    receivedEnergy = ((IEnergyReceiver) te).receiveEnergy(accessingSide, maxTransferLeft, false);
                } catch (Exception ignored) {}
            }
            if(receivedEnergy <= 0 && te instanceof IEnergyStorage) {
                try {
                    receivedEnergy = ((IEnergyStorage) te).receiveEnergy(maxTransferLeft, false);
                } catch (Exception ignored) {}
            }
            if(receivedEnergy <= 0 && te.hasCapability(CapabilityEnergy.ENERGY, accessingSide)) {
                net.minecraftforge.energy.IEnergyStorage ce = te.getCapability(CapabilityEnergy.ENERGY, accessingSide);
                if(ce != null && ce.canReceive()) {
                    try {
                        receivedEnergy = ce.receiveEnergy(maxTransferLeft, false);
                    } catch (Exception ignored) {}
                }
            }
        }
        return receivedEnergy;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void onLoad() {
        super.onLoad();
        IntegrationIC2EventHandlerHelper.fireLoadEvent(world, this);
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void invalidate() {
        super.invalidate();
        if(!world.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
    }

    @Override
    @Optional.Method(modid = "ic2")
    public double getOfferedEnergy() {
        return Math.min(this.size.getIC2EnergyTransmission(), this.getCurrentEnergy() / 4L);
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void drawEnergy(double amount) {
        this.energy = MiscUtils.clamp(this.energy - (MathHelper.lfloor(amount) * 4L), 0, this.size.maxEnergy);
        markForUpdate();
    }

    @Override
    @Optional.Method(modid = "ic2")
    public int getSourceTier() {
        return size.ic2EnergyTier;
    }

    @Override
    @Optional.Method(modid = "ic2")
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
        return true;
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.EnergyHatch(MachineComponent.IOType.OUTPUT) {
            @Override
            public IEnergyHandler getContainerProvider() {
                return TileEnergyOutputHatch.this;
            }
        };
    }

}
