/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import hellfirepvp.modularmachinery.common.util.MiscUtils;
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
    public long transferLimit;

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
            size.maxEnergy = cfg.get("energyhatch.size", size.name().toUpperCase(), size.defaultConfigurationEnergy + "", "Energy storage size of the energy hatch. [range: 0 ~ 9223372036854775807, default: " + size.defaultConfigurationEnergy + "]").getLong();
            size.maxEnergy = MiscUtils.clamp(size.maxEnergy, 1, Long.MAX_VALUE);
            size.transferLimit = cfg.get("energyhatch.limit", size.name().toUpperCase(), size.defaultConfigurationTransferLimit + "", "Defines the transfer limit for RF/FE things. IC2's transfer limit is defined by the voltage tier. [range: 1 ~ 9223372036854775806, default: " + size.defaultConfigurationEnergy + "]").getLong();
            size.transferLimit = MiscUtils.clamp(size.transferLimit, 1, Long.MAX_VALUE - 1);
            size.energyTier = cfg.getInt("energyhatch.tier", size.name().toUpperCase(), size.defaultEnergyTier, 0, 12, "Defines the IC2 output-voltage tier. Only affects the power the output hatches will output power as. 0 = 'ULV' = 8 EU/t, 1 = 'LV' = 32 EU/t, 2 = 'MV' = 128 EU/t, ...");
        }
    }

}
