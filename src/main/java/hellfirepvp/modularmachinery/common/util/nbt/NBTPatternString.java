/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.NBTTagString;

import java.util.regex.Pattern;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTPatternString
 * Created by HellFirePvP
 * Date: 19.08.2017 / 22:00
 */
public class NBTPatternString extends NBTTagString {

    private final Pattern strPattern;

    public NBTPatternString(String data) {
        this(data, Pattern.compile(data, Pattern.CASE_INSENSITIVE));
    }

    private NBTPatternString(String data, Pattern strPattern) {
        super(data);
        this.strPattern = strPattern;
    }

    @Override
    public NBTPatternString copy() {
        return new NBTPatternString(this.getString(), this.strPattern);
    }

    public boolean testString(String toTest) {
        return strPattern.matcher(toTest).matches();
    }

}
