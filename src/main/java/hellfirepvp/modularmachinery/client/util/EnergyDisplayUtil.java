/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;

import java.util.function.Function;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnergyDisplayUtil
 * Created by HellFirePvP
 * Date: 04.03.2019 / 20:07
 */
public class EnergyDisplayUtil {

    public static boolean displayFETooltip = true;
    public static boolean displayIC2EUTooltip = true;
    public static boolean displayGTEUTooltip = true;

    //Available: FE, IC2_EU, GT_EU
    public static EnergyType type = EnergyType.FE;

    public static void loadFromConfig(Configuration cfg) {
        displayFETooltip = cfg.getBoolean("FE_RF_Tooltip", "display.energy", displayFETooltip, "Set to true, if the standard 'energy' FE (or RF) should be displayed in the tooltip of the energy hatch along with its transmission rates.");
        displayIC2EUTooltip = cfg.getBoolean("IC2_EU_Tooltip", "display.energy", displayIC2EUTooltip, "Set to true, if IC2's energy EU should be displayed in the tooltip of the energy hatch. Will only have effect if IC2 is installed.");
        displayGTEUTooltip = cfg.getBoolean("GT_EU_Tooltip", "display.energy", displayGTEUTooltip, "Set to true, if GT's energy EU should be displayed in the tooltip of the energy hatch. Will only have effect if GregTech (community edition) is installed.");

        type = EnergyType.getType(cfg.getString("Display_Energy_Type", "display.energy", type.name(), "Available options: 'FE', 'IC2_EU', 'GT_EU' - Default: FE - Set this to one of those 3 types to have GUI, recipe preview and energy be displayed in that type of energy in ALL ModularMachinery things."));
    }

    public static enum EnergyType {

        FE(1),
        IC2_EU(0.25F),
        GT_EU(0.25F);

        private final double multiplier;
        private final String unlocalizedFormat;

        EnergyType(double multiplier) {
            this.multiplier = multiplier;
            this.unlocalizedFormat = "tooltip.energy.type." + name().toLowerCase();
        }

        public long formatEnergyForDisplay(long energy) {
            return MathHelper.lfloor(energy * multiplier);
        }

        public String getUnlocalizedFormat() {
            return unlocalizedFormat;
        }

        public static EnergyType getType(String configValue) {
            EnergyType type = FE;
            try {
                type = EnergyType.valueOf(configValue);
            } catch (Exception ignored) {}

            return type;
        }

    }

}
