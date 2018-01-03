/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagShort;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTComparableShort
 * Created by HellFirePvP
 * Date: 19.08.2017 / 21:43
 */
public class NBTComparableShort extends NBTTagShort implements NBTComparableNumber {

    private final ComparisonMode comparisonMode;

    public NBTComparableShort(ComparisonMode mode, short data) {
        super(data);
        this.comparisonMode = mode;
    }

    @Override
    public boolean test(NBTPrimitive nbtPrimitive) {
        return nbtPrimitive instanceof NBTTagShort && comparisonMode.testShort(this.getShort(), nbtPrimitive.getShort());
    }

}
