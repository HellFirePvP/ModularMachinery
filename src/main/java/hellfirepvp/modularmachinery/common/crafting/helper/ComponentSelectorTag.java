/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentSelectorTag
 * Created by HellFirePvP
 * Date: 04.03.2019 / 21:31
 */
// Basically a super fancy wrapped string.
public class ComponentSelectorTag {

    private final String tag;

    public ComponentSelectorTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tried to create tag object will null or empty tag!");
        }
        this.tag = tag;
    }

    @Nonnull
    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentSelectorTag that = (ComponentSelectorTag) o;
        return Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }
}
