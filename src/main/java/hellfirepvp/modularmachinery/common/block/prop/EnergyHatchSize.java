/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import gregtech.api.GTValues;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnergyHatchSize
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:25
 */
public enum EnergyHatchSize implements IStringSerializable {

    TINY      (2048,    1, 128, 1, 2),
    SMALL     (4096,    2, 512, 2, 2),
    NORMAL    (8192,    2, 512, 2, 2),
    REINFORCED(16384,   3, 2048, 3, 2),
    BIG       (32768,   4, 8192, 4, 2),
    HUGE      (131072,  5, 32768, 5, 2),
    LUDICROUS (524288,  6, 131072, 6, 2),
    ULTIMATE  (2097152, 6, 131072, 6, 2);

    public long maxEnergy;
    public long transferLimit;

    public int ic2EnergyTier;
    public int gtEnergyTier;
    public int gtAmperage;

    private final int defaultConfigurationEnergy;
    private final int defaultConfigurationTransferLimit;

    private final int defaultIC2EnergyTier;
    private final int defaultGTEnergyTier;
    private final int defaultGTAmperage;

    private EnergyHatchSize(int maxEnergy, int ic2EnergyTier, int transferLimit, int gtEnergyTier, int gtAmperage) {
        this.defaultConfigurationEnergy = maxEnergy;
        this.defaultIC2EnergyTier = ic2EnergyTier;
        this.defaultConfigurationTransferLimit = transferLimit;
        this.defaultGTEnergyTier = gtEnergyTier;
        this.defaultGTAmperage = gtAmperage;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

    @Nonnull
    public String getUnlocalizedEnergyDescriptor() {
        return "tooltip.ic2.powertier." + ic2EnergyTier + ".name";
    }

    // MM only supports GTCE tiers from ULV to UV
    public int getGTEnergyTier() {
        return MathHelper.clamp(this.gtEnergyTier, 0, 8);
    }

    public int getGtAmperage() {
        return this.gtAmperage;
    }

    @Optional.Method(modid = "gregtech")
    public String getUnlocalizedGTEnergyTier() {
        return GTValues.VN[getGTEnergyTier()];
    }

    public long getGTEnergyTransferVoltage() {
        if(getGTEnergyTier() < 0) {
            return -1;
        }
        return (int) Math.pow(2, ((getGTEnergyTier() + 1) * 2) + 1);
    }

    public int getIC2EnergyTransmission() {
        if(ic2EnergyTier < 0) {
            return -1;
        }
        return (int) Math.pow(2, ((ic2EnergyTier + 1) * 2) + 1);
    }

    public static void loadFromConfig(Configuration cfg) {
        for (EnergyHatchSize size : values()) {
            size.maxEnergy = cfg.get("energyhatch.size", size.name().toUpperCase(), size.defaultConfigurationEnergy + "", "Energy storage size of the energy hatch. [range: 0 ~ 9223372036854775807, default: " + size.defaultConfigurationEnergy + "]").getLong();
            size.maxEnergy = MiscUtils.clamp(size.maxEnergy, 1, Long.MAX_VALUE);
            size.transferLimit = cfg.get("energyhatch.limit", size.name().toUpperCase(), size.defaultConfigurationTransferLimit + "", "Defines the transfer limit for RF/FE things. IC2's transfer limit is defined by the voltage tier. [range: 1 ~ 9223372036854775806, default: " + size.defaultConfigurationEnergy + "]").getLong();
            size.transferLimit = MiscUtils.clamp(size.transferLimit, 1, Long.MAX_VALUE - 1);

            size.ic2EnergyTier = cfg.get("energyhatch.tier", size.name().toUpperCase(), size.defaultIC2EnergyTier, "Defines the IC2 output-voltage tier. Only affects the power the output hatches will output power as. 0 = 'ULV' = 8 EU/t, 1 = 'LV' = 32 EU/t, 2 = 'MV' = 128 EU/t, ... [range: 0 ~ 12, default: " + size.defaultIC2EnergyTier + "]").getInt();

            size.gtEnergyTier = cfg.get("energyhatch.gtvoltage", size.name().toUpperCase(), size.defaultGTEnergyTier, "Defines the GT voltage tier. Affects both input and output hatches of this tier. [range: 0 ~ 8, default: " + size.defaultGTEnergyTier + "]").getInt();
            size.gtEnergyTier = MathHelper.clamp(size.gtEnergyTier, 0, 8);
            size.gtAmperage = cfg.get("energyhatch.gtamperage", size.name().toUpperCase(), size.defaultGTAmperage, "Defines the GT amperage. Affects both output amperage as well as maximum input amperage. [range: 1 ~ 16, default: " + size.defaultGTAmperage + "]").getInt();
            size.gtAmperage = MathHelper.clamp(size.gtAmperage, 1, 16);
        }
    }

}
