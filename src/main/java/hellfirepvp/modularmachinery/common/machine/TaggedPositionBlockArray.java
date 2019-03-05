/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TaggedPositionBlockArray
 * Created by HellFirePvP
 * Date: 04.03.2019 / 21:33
 */
public class TaggedPositionBlockArray extends BlockArray {

    private Map<BlockPos, ComponentSelectorTag> taggedPositions = new HashMap<>();

    public void setTag(BlockPos pos, ComponentSelectorTag tag) {
        this.taggedPositions.put(pos, tag);
    }

    @Nullable
    public ComponentSelectorTag getTag(BlockPos pos) {
        return this.taggedPositions.get(pos);
    }

    @Override
    public TaggedPositionBlockArray rotateYCCW() {
        TaggedPositionBlockArray out = new TaggedPositionBlockArray();

        for (BlockPos pos : pattern.keySet()) {
            BlockInformation info = pattern.get(pos);
            out.pattern.put(new BlockPos(pos.getZ(), pos.getY(), -pos.getX()), info.copyRotateYCCW());
        }
        for (BlockPos pos : taggedPositions.keySet()) {
            out.taggedPositions.put(new BlockPos(pos.getZ(), pos.getY(), -pos.getX()), taggedPositions.get(pos));
        }
        return out;
    }
}
