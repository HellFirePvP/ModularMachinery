/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagFloat;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTComparableFloat
 * Created by HellFirePvP
 * Date: 19.08.2017 / 21:24
 */
public class NBTComparableFloat extends NBTTagFloat implements NBTComparableNumber {

    private final ComparisonMode comparisonMode;

    public NBTComparableFloat(ComparisonMode mode, float data) {
        super(data);
        this.comparisonMode = mode;
    }

    @Override
    public boolean test(NBTPrimitive nbtPrimitive) {
        return nbtPrimitive instanceof NBTTagFloat && comparisonMode.testFloat(this.getFloat(), nbtPrimitive.getFloat());
    }

}
