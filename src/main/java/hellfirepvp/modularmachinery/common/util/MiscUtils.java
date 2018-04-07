/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import com.google.common.collect.Lists;
import net.minecraft.util.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: MiscUtils
 * Created by HellFirePvP
 * Date: 30.03.2018 / 16:50
 */
public class MiscUtils {

    public static <K, V, N> Map<K, N> remap(Map<K, V> map, Function<V, N> remapFct) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (e) -> remapFct.apply(e.getValue())));
    }

    public static List<String> splitStringBy(String str, String spl) {
        return Lists.newArrayList(str.split(spl));
    }

}
