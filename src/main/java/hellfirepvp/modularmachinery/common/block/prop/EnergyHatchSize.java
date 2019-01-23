/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnergyHatchSize
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:25
 */
public enum EnergyHatchSize implements IStringSerializable {

    TINY      (2048,    1, 128),
    SMALL     (4096,    2, 512),
    NORMAL    (8192,    2, 512),
    REINFORCED(16384,   3, 2048),
    BIG       (32768,   4, 8192),
    HUGE      (131072,  5, 32768),
    LUDICROUS (524288,  6, 131072),
    ULTIMATE  (2097152, 6, 131072);

    public long maxEnergy;
    public int energyTier;
    public int transferLimit;

    private final int defaultConfigurationEnergy;
    private final int defaultEnergyTier;
    private final int defaultConfigurationTransferLimit;

    private EnergyHatchSize(int defaultConfigurationEnergy, int defaultEnergyTier, int defaultConfigurationTransferLimit) {
        this.defaultConfigurationEnergy = defaultConfigurationEnergy;
        this.defaultEnergyTier = defaultEnergyTier;
        this.defaultConfigurationTransferLimit = defaultConfigurationTransferLimit;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

    @Nonnull
    public String getUnlocalizedEnergyDescriptor() {
        return "tooltip.ic2.powertier." + energyTier + ".name";
    }

    public int getEnergyTransmission() {
        if(energyTier < 0) {
            return -1;
        }
        return (int) Math.pow(2, ((energyTier + 1) * 2) + 1);
    }

    public static void loadSizeFromConfig(Configuration cfg) {
        for (EnergyHatchSize size : values()) {
            size.maxEnergy = cfg.getInt("size", "energyhatch." + size.name().toUpperCase(), size.defaultConfigurationEnergy, 1, Integer.MAX_VALUE, "Energy storage size of the energy hatch.");
            size.transferLimit = cfg.getInt("limit", "energyhatch." + size.name().toUpperCase(), size.defaultConfigurationTransferLimit, 1, Integer.MAX_VALUE, "Defines the transfer limit for RF/FE things. IC2's transfer limit is defined by the voltage tier.");
            size.energyTier = cfg.getInt("tier", "energyhatch." + size.name().toUpperCase(), size.defaultEnergyTier, 0, 12, "Defines the IC2 output-voltage tier. Only affects the power the output hatches will output power as. 0 = 'ULV' = 8 EU/t, 1 = 'LV' = 32 EU/t, 2 = 'MV' = 128 EU/t, ...");
        }
    }

}
