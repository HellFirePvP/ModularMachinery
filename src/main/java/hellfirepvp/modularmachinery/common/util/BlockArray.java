/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArray
 * Created by HellFirePvP
 * Date: 27.06.2017 / 10:50
 */
public class BlockArray {

    protected static final Random STATIC_RAND = new Random();

    protected Map<BlockPos, BlockInformation> pattern = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0), max = new Vec3i(0, 0, 0), size = new Vec3i(0, 0, 0);

    public void addBlock(int x, int y, int z, @Nonnull IBlockState state) {
        addBlock(new BlockPos(x, y, z), state);
    }

    public void addBlock(BlockPos offset, @Nonnull IBlockState state) {
        addBlock(offset, new BlockInformation(Lists.newArrayList(state)));
    }

    public void addBlock(int x, int y, int z, @Nonnull BlockInformation info) {
        addBlock(new BlockPos(x, y, z), info);
    }

    public void addBlock(BlockPos offset, @Nonnull BlockInformation info) {
        pattern.put(offset, info);
        updateSize(offset);
    }

    public boolean hasBlockAt(BlockPos pos) {
        return pattern.containsKey(pos);
    }

    public boolean isEmpty() {
        return pattern.isEmpty();
    }

    public Vec3i getMax() {
        return max;
    }

    public Vec3i getMin() {
        return min;
    }

    public Vec3i getSize() {
        return size;
    }

    private void updateSize(BlockPos addedPos) {
        if(addedPos.getX() < min.getX()) {
            min = new Vec3i(addedPos.getX(), min.getY(), min.getZ());
        }
        if(addedPos.getX() > max.getX()) {
            max = new Vec3i(addedPos.getX(), max.getY(), max.getZ());
        }
        if(addedPos.getY() < min.getY()) {
            min = new Vec3i(min.getX(), addedPos.getY(), min.getZ());
        }
        if(addedPos.getY() > max.getY()) {
            max = new Vec3i(max.getX(), addedPos.getY(), max.getZ());
        }
        if(addedPos.getZ() < min.getZ()) {
            min = new Vec3i(min.getX(), min.getY(), addedPos.getZ());
        }
        if(addedPos.getZ() > max.getZ()) {
            max = new Vec3i(max.getX(), max.getY(), addedPos.getZ());
        }
        size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
    }

    public Map<BlockPos, BlockInformation> getPattern() {
        return pattern;
    }

    public List<ItemStack> getAsDescriptiveStacks(long tick) {
        List<ItemStack> out = new LinkedList<>();
        for (BlockInformation info : pattern.values()) {
            IBlockState state = info.getSampleState(tick);
            Block type = state.getBlock();
            int meta = type.getMetaFromState(state);
            ItemStack s;
            if(type instanceof BlockFluidBase) {
                s = FluidUtil.getFilledBucket(new FluidStack(((BlockFluidBase) type).getFluid(), 1000));
            } else {
                Item i = Item.getItemFromBlock(type);
                if(i == Items.AIR) continue;
                s = new ItemStack(i, 1, meta);
            }
            if(!s.isEmpty()) {
                boolean found = false;
                for (ItemStack stack : out) {
                    if(stack.getItem().getRegistryName().equals(s.getItem().getRegistryName()) && stack.getItemDamage() == s.getItemDamage()) {
                        stack.setCount(stack.getCount() + 1);
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    public boolean matches(World world, BlockPos center) {
        lblPattern:
        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            BlockInformation info = entry.getValue();
            BlockPos at = center.add(entry.getKey());
            IBlockState state = world.getBlockState(at);
            Block atBlock = state.getBlock();
            int atMeta = atBlock.getMetaFromState(state);

            for (IBlockState applicable : info.matchingStates) {
                Block type = applicable.getBlock();
                int meta = type.getMetaFromState(applicable);
                if(type.equals(state.getBlock()) && meta == atMeta) {
                    continue lblPattern; //Matches
                }
            }
            return false;
        }
        return true;
    }

    public static class BlockInformation {

        public static final int CYCLE_TICK_SPEED = 60;
        public final List<IBlockState> matchingStates;

        public BlockInformation(List<IBlockState> matching) {
            this.matchingStates = ImmutableList.copyOf(matching);
        }

        public IBlockState getSampleState(long tick) {
            int part = (int) ((tick % CYCLE_TICK_SPEED) % matchingStates.size());
            return matchingStates.get(part);
        }

        public static IBlockState getDescriptor(JsonPrimitive stringElement) throws JsonParseException {
            String strElement = stringElement.getAsString();
            int meta = -1;
            int indexMeta = strElement.indexOf('@');
            if(indexMeta != -1 && indexMeta != strElement.length() - 1) {
                try {
                    meta = Integer.parseInt(strElement.substring(indexMeta + 1));
                } catch (NumberFormatException exc) {
                    throw new JsonParseException("Expected a metadata number, got " + strElement.substring(indexMeta + 1), exc);
                }
                strElement = strElement.substring(0, indexMeta);
            }
            ResourceLocation res = new ResourceLocation(strElement);
            Block b = ForgeRegistries.BLOCKS.getValue(res);
            if(b == null || b == Blocks.AIR) {
                throw new JsonParseException("Couldn't find block with registryName '" + res.toString() + "' !");
            }
            if(meta == -1) {
                return b.getDefaultState();
            } else {
                return b.getStateFromMeta(meta);
            }
        }

    }

}
