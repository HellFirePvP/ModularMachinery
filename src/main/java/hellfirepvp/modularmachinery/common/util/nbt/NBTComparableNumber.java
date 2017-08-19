/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.NBTPrimitive;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTComparableNumber
 * Created by HellFirePvP
 * Date: 19.08.2017 / 21:18
 */
public interface NBTComparableNumber {

    public boolean test(NBTPrimitive numberTag);

    public static enum ComparisonMode {

        LESS_EQUAL("<="),
        EQUAL("=="),
        GREATER_EQUAL(">="),

        LESS("<"),
        GREATER(">");

        private final String identifier;

        ComparisonMode(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Nullable
        public static ComparisonMode peekMode(String strModeAndValue) {
            lblModes:
            for (ComparisonMode mode : values()) {
                String id = mode.getIdentifier();

                char[] charArray = id.toCharArray();
                for (int i = 0; i < charArray.length; i++) {
                    char c = charArray[i];
                    if (strModeAndValue.charAt(i) != c) {
                        continue lblModes;
                    }
                }
                return mode;
            }
            return null;
        }

        public boolean testByte(byte original, byte toTest) {
            switch (this) {
                case LESS:
                    return toTest < original;
                case LESS_EQUAL:
                    return toTest <= original;
                case EQUAL:
                    return toTest == original;
                case GREATER_EQUAL:
                    return toTest >= original;
                case GREATER:
                    return toTest > original;
            }
            throw new IllegalStateException("What am i?");
        }

        public boolean testInt(int original, int toTest) {
            switch (this) {
                case LESS:
                    return toTest < original;
                case LESS_EQUAL:
                    return toTest <= original;
                case EQUAL:
                    return toTest == original;
                case GREATER_EQUAL:
                    return toTest >= original;
                case GREATER:
                    return toTest > original;
            }
            throw new IllegalStateException("What am i?");
        }

        public boolean testShort(short original, short toTest) {
            switch (this) {
                case LESS:
                    return toTest < original;
                case LESS_EQUAL:
                    return toTest <= original;
                case EQUAL:
                    return toTest == original;
                case GREATER_EQUAL:
                    return toTest >= original;
                case GREATER:
                    return toTest > original;
            }
            throw new IllegalStateException("What am i?");
        }

        public boolean testLong(long original, long toTest) {
            switch (this) {
                case LESS:
                    return toTest < original;
                case LESS_EQUAL:
                    return toTest <= original;
                case EQUAL:
                    return toTest == original;
                case GREATER_EQUAL:
                    return toTest >= original;
                case GREATER:
                    return toTest > original;
            }
            throw new IllegalStateException("What am i?");
        }

        public boolean testFloat(float original, float toTest) {
            switch (this) {
                case LESS:
                    return toTest < original;
                case LESS_EQUAL:
                    return toTest <= original;
                case EQUAL:
                    return toTest == original;
                case GREATER_EQUAL:
                    return toTest >= original;
                case GREATER:
                    return toTest > original;
            }
            throw new IllegalStateException("What am i?");
        }

        public boolean testDouble(double original, double toTest) {
            switch (this) {
                case LESS:
                    return toTest < original;
                case LESS_EQUAL:
                    return toTest <= original;
                case EQUAL:
                    return toTest == original;
                case GREATER_EQUAL:
                    return toTest >= original;
                case GREATER:
                    return toTest > original;
            }
            throw new IllegalStateException("What am i?");
        }

    }

}
