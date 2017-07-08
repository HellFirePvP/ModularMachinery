/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidTank;

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

    public FluidTank buildTank(TileEntitySynchronized tileEntity, boolean canFill, boolean canDrain) {
        FluidTank tank = new FluidTank(this.size) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                tileEntity.markForUpdate();
            }
        };
        tank.setCanFill(canFill);
        tank.setCanDrain(canDrain);
        return tank;
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
