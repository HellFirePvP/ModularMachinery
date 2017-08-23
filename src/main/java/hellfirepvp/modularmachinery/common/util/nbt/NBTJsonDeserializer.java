/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import com.google.common.collect.Lists;
import net.minecraft.nbt.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTJsonDeserializer
 * Created by HellFirePvP
 * Date: 19.08.2017 / 19:34
 */
public class NBTJsonDeserializer {

    private static final Pattern DOUBLE_PATTERN_NOSUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", Pattern.CASE_INSENSITIVE);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", Pattern.CASE_INSENSITIVE);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", Pattern.CASE_INSENSITIVE);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    private final String string;
    private int cursor;

    //^.* <name> .*$ = contains

    public static NBTTagCompound deserialize(String json) throws NBTException {
        return new NBTJsonDeserializer(json).readCompound();
    }

    private NBTJsonDeserializer(String string) {
        this.string = string;
    }

    private NBTTagCompound readCompound() throws NBTException {
        NBTTagCompound nbttagcompound = this.readStruct();
        this.skipWhitespace();

        if (this.canRead()) {
            ++this.cursor;
            throw this.exception("Trailing data found");
        } else {
            return nbttagcompound;
        }
    }

    private String readKey() throws NBTException {
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected key");
        } else {
            return this.readQuotedString();
        }
    }

    private String readQuotedString() throws NBTException {
        int i = ++this.cursor;
        StringBuilder stringbuilder = null;
        boolean flag = false;

        while (this.canRead()) {
            char c0 = this.pop();

            if (flag) {
                if (c0 != '\\' && c0 != '"') {
                    throw this.exception("Invalid escape of '" + c0 + "'");
                }

                flag = false;
            } else {
                if (c0 == '\\') {
                    flag = true;

                    if (stringbuilder == null) {
                        stringbuilder = new StringBuilder(this.string.substring(i, this.cursor - 1));
                    }

                    continue;
                }

                if (c0 == '"') {
                    return stringbuilder == null ? this.string.substring(i, this.cursor - 1) : stringbuilder.toString();
                }
            }

            if (stringbuilder != null) {
                stringbuilder.append(c0);
            }
        }

        throw this.exception("Missing termination quote");
    }

    protected NBTBase readTypedValue() throws NBTException {
        this.skipWhitespace();

        String s = this.readQuotedString();

        if (s.isEmpty()) {
            throw this.exception("Expected value");
        } else {
            return this.type(s);
        }
    }

    private NBTBase type(String stringIn) {
        NBTComparableNumber.ComparisonMode peekedMode = NBTComparableNumber.ComparisonMode.peekMode(stringIn);
        if(peekedMode != null) {
            stringIn = stringIn.substring(peekedMode.getIdentifier().length());
        }
        try {
            if (FLOAT_PATTERN.matcher(stringIn).matches()) {
                return new NBTComparableFloat(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Float.parseFloat(stringIn.substring(0, stringIn.length() - 1)));
            }
            if (BYTE_PATTERN.matcher(stringIn).matches()) {
                return new NBTComparableByte(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Byte.parseByte(stringIn.substring(0, stringIn.length() - 1)));
            }
            if (LONG_PATTERN.matcher(stringIn).matches()) {
                return new NBTComparableLong(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Long.parseLong(stringIn.substring(0, stringIn.length() - 1)));
            }
            if (SHORT_PATTERN.matcher(stringIn).matches()) {
                return new NBTComparableShort(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Short.parseShort(stringIn.substring(0, stringIn.length() - 1)));
            }
            if (INT_PATTERN.matcher(stringIn).matches()) {
                return new NBTComparableInteger(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Integer.parseInt(stringIn));
            }
            if (DOUBLE_PATTERN.matcher(stringIn).matches()) {
                return new NBTComparableDouble(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Double.parseDouble(stringIn.substring(0, stringIn.length() - 1)));
            }
            if (DOUBLE_PATTERN_NOSUFFIX.matcher(stringIn).matches()) {
                return new NBTComparableDouble(peekedMode == null ? NBTComparableNumber.ComparisonMode.EQUAL : peekedMode, Double.parseDouble(stringIn));
            }
            if ("true".equalsIgnoreCase(stringIn)) {
                return new NBTTagByte((byte)1);
            }
            if ("false".equalsIgnoreCase(stringIn)) {
                return new NBTTagByte((byte)0);
            }
        } catch (NumberFormatException ignored) {}

        if(peekedMode != null) {
            stringIn = peekedMode.getIdentifier() + stringIn;
        }
        return new NBTPatternString(stringIn);
    }

    protected NBTBase readValue() throws NBTException {
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected value");
        } else {
            char c0 = this.peek();

            if (c0 == '{') {
                return this.readStruct();
            } else {
                return c0 == '[' ? this.readList() : this.readTypedValue();
            }
        }
    }

    protected NBTTagCompound readStruct() throws NBTException {
        this.expect('{');
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.skipWhitespace();

        while (this.canRead() && this.peek() != '}') {
            String s = this.readKey();

            if (s.isEmpty()) {
                throw this.exception("Expected non-empty key");
            }

            this.expect(':');
            nbttagcompound.setTag(s, this.readValue());

            if (!this.hasElementSeparator()) {
                break;
            }

            if (!this.canRead()) {
                throw this.exception("Expected key");
            }
        }

        this.expect('}');
        return nbttagcompound;
    }

    protected NBTBase readList() throws NBTException {
        return this.readListTag();
    }

    private NBTBase readListTag() throws NBTException {
        this.expect('[');
        this.skipWhitespace();

        if (!this.canRead()) {
            throw this.exception("Expected value");
        } else {
            NBTTagList nbttaglist = new NBTTagList();
            int i = -1;

            while (this.peek() != ']') {
                NBTBase nbtbase = this.readValue();
                int j = nbtbase.getId();

                if (i < 0) {
                    i = j;
                } else if (j != i) {
                    throw this.exception("Unable to insert " + NBTBase.getTagTypeName(j) + " into ListTag of type " + NBTBase.getTagTypeName(i));
                }

                nbttaglist.appendTag(nbtbase);

                if (!this.hasElementSeparator()) {
                    break;
                }

                if (!this.canRead()) {
                    throw this.exception("Expected value");
                }
            }

            this.expect(']');
            return nbttaglist;
        }
    }

    private NBTException exception(String message) {
        return new NBTException(message, this.string, this.cursor);
    }

    private void skipWhitespace() {
        while (this.canRead() && Character.isWhitespace(this.peek())) {
            ++this.cursor;
        }
    }

    private boolean hasElementSeparator() {
        this.skipWhitespace();

        if (this.canRead() && this.peek() == ',') {
            ++this.cursor;
            this.skipWhitespace();
            return true;
        } else {
            return false;
        }
    }

    private void expect(char expected) throws NBTException {
        this.skipWhitespace();
        boolean dataLeft = this.canRead();

        if (dataLeft && this.peek() == expected) {
            ++this.cursor;
        } else {
            throw new NBTException("Expected '" + expected + "' but got '" + (dataLeft ? this.peek() : "<EOF>") + "'", this.string, this.cursor + 1);
        }
    }

    protected boolean isAllowedInKey(char charIn) {
        return charIn >= '0' && charIn <= '9' || charIn >= 'A' && charIn <= 'Z' || charIn >= 'a' && charIn <= 'z' || charIn == '_' || charIn == '-' || charIn == '.' || charIn == '+';
    }

    private boolean canRead(int count) {
        return this.cursor + count < this.string.length();
    }

    private boolean canRead() {
        return this.canRead(0);
    }

    private char peek(int count) {
        return this.string.charAt(this.cursor + count);
    }

    private char peek() {
        return this.peek(0);
    }

    private char pop() {
        return this.string.charAt(this.cursor++);
    }

}
