/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GTEnergyContainer
 * Created by HellFirePvP
 * Date: 02.03.2019 / 16:41
 */
@Optional.Interface(iface = "gregtech.api.capability.IEnergyContainer", modid = "gregtech")
public class GTEnergyContainer implements IEnergyContainer {

    private final TileEnergyHatch hatch;
    private final MachineComponent.IOType ioType;

    public GTEnergyContainer(TileEnergyHatch hatch, MachineComponent.IOType ioType) {
        this.hatch = hatch;
        this.ioType = ioType;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (ioType == MachineComponent.IOType.INPUT && amperage > 0 && voltage > 0) {
            long available = hatch.getMaxEnergy() - hatch.getCurrentEnergy();
            long maxAmperage = Math.min(getInputAmperage(), amperage);

            if (voltage > getInputVoltage()) {
                if (ConfigHolder.doExplosions) {
                    BlockPos pos = hatch.getPos();
                    hatch.getWorld().createExplosion(null,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            GTUtility.getTierByVoltage(voltage), true);
                }
                return maxAmperage;
            }


            long acceptingAmperage = 0;
            while (acceptingAmperage < maxAmperage && (acceptingAmperage * voltage) <= available) {
                acceptingAmperage++;
            }

            hatch.setCurrentEnergy(available + (acceptingAmperage * voltage));

            return acceptingAmperage;
        }
        return 0;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public boolean inputsEnergy(EnumFacing side) {
        return ioType == MachineComponent.IOType.INPUT;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public boolean outputsEnergy(EnumFacing side) {
        return ioType == MachineComponent.IOType.OUTPUT;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long changeEnergy(long differenceAmount) {
        return 0;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long getEnergyStored() {
        return hatch.getCurrentEnergy();
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long getEnergyCapacity() {
        return hatch.getTier().maxEnergy;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long getOutputAmperage() {
        return ioType == MachineComponent.IOType.OUTPUT ? hatch.getTier().getGtAmperage() : 0L;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long getOutputVoltage() {
        return ioType == MachineComponent.IOType.OUTPUT ? hatch.getTier().getGTEnergyTransferVoltage() : 0L;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long getInputAmperage() {
        return ioType == MachineComponent.IOType.INPUT ? hatch.getTier().getGtAmperage() : 0L;
    }

    @Override
    @Optional.Method(modid = "gregtech")
    public long getInputVoltage() {
        return ioType == MachineComponent.IOType.INPUT ? hatch.getTier().getGTEnergyTransferVoltage() : 0L;
    }
}
