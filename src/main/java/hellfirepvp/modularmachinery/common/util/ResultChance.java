/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import java.util.Random;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResultChance
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:58
 */
public class ResultChance {

    public static ResultChance GUARANTEED = new ResultChance(0) {
        @Override
        public boolean canProduce(float chance) {
            return true;
        }
    };

    private final Random rand;

    public ResultChance(long seed) {
        this.rand = new Random(seed);
    }

    public boolean canProduce(float chance) {
        return chance <= rand.nextFloat();
    }

}
