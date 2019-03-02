/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import hellfirepvp.modularmachinery.common.util.HybridGasTank;
import hellfirepvp.modularmachinery.common.util.HybridTank;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.Optional;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidHatchSize
 * Created by HellFirePvP
 * Date: 07.07.2017 / 18:31
 */
public enum FluidHatchSize implements IStringSerializable {

    TINY(100),
    SMALL(400),
    NORMAL(1000),
    REINFORCED(2000),
    BIG(4500),
    HUGE(8000),
    LUDICROUS(16000),
    VACUUM(32000);

    private int size;

    private final int defaultConfigurationValue;

    FluidHatchSize(int defaultConfigurationValue) {
        this.defaultConfigurationValue = defaultConfigurationValue;
    }

    public HybridTank buildTank(TileEntitySynchronized tileEntity, boolean canFill, boolean canDrain) {
        HybridTank tank;
        if(Mods.MEKANISM.isPresent()) {
            tank = buildMekTank(tileEntity);
        } else {
            tank = buildDefaultTank(tileEntity);
        }
        tank.setCanFill(canFill);
        tank.setCanDrain(canDrain);
        return tank;
    }

    private HybridTank buildDefaultTank(TileEntitySynchronized tileEntity) {
        return new HybridTank(this.size) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                tileEntity.markForUpdate();
            }
        };
    }

    @Optional.Method(modid = "mekanism")
    private HybridTank buildMekTank(TileEntitySynchronized tileEntity) {
        return new HybridGasTank(this.size) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                tileEntity.markForUpdate();
            }
        };
    }

    public int getSize() {
        return size;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

    public static void loadSizeFromConfig(Configuration cfg) {
        for (FluidHatchSize size : values()) {
            size.size = cfg.getInt("size", "fluidhatch." + size.name().toUpperCase(), size.defaultConfigurationValue, 1, Integer.MAX_VALUE, "Defines the tank size for the size-type of fluid hatch.");
        }
    }

}
