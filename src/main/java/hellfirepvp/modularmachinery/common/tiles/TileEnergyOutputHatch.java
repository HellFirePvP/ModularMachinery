/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchSize;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.util.IEnergyHandler;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEnergyOutputHatch
 * Created by HellFirePvP
 * Date: 08.07.2017 / 12:43
 */
@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2")
public class TileEnergyOutputHatch extends TileEnergyHatch implements IEnergySource {

    public TileEnergyOutputHatch() {}

    public TileEnergyOutputHatch(EnergyHatchSize size) {
        super(size);
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void onLoad() {
        super.onLoad();
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void invalidate() {
        super.invalidate();
        MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
    }

    @Override
    public double getOfferedEnergy() {
        return Math.min(this.size.getEnergyTransmission(), this.getCurrentEnergy() / 4);
    }

    @Override
    public void drawEnergy(double amount) {
        this.energy = MathHelper.clamp(MathHelper.floor(this.energy - (amount * 4)), 0, this.size.maxEnergy);
        markForUpdate();
    }

    @Override
    public int getSourceTier() {
        return size.energyTier;
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
        return true;
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.EnergyHatch(MachineComponent.IOType.OUTPUT) {
            @Override
            public IEnergyHandler getEnergyBuffer() {
                return TileEnergyOutputHatch.this;
            }
        };
    }

}
