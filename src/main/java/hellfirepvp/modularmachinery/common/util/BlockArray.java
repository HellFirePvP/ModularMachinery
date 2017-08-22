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
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.util.nbt.NBTMatchingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArray
 * Created by HellFirePvP
 * Date: 27.06.2017 / 10:50
 */
public class BlockArray {

    protected Map<BlockPos, BlockInformation> pattern = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0), max = new Vec3i(0, 0, 0), size = new Vec3i(0, 0, 0);

    public BlockArray() {}

    public BlockArray(BlockArray other) {
        this.pattern = new HashMap<>(other.pattern);
        this.min = new Vec3i(other.min.getX(), other.min.getY(), other.min.getZ());
        this.max = new Vec3i(other.max.getX(), other.max.getY(), other.max.getZ());
        this.size = new Vec3i(other.size.getX(), other.size.getY(), other.size.getZ());
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

    public Map<BlockPos, BlockInformation> getPatternSlice(int slice) {
        Map<BlockPos, BlockInformation> copy = new HashMap<>();
        for (BlockPos pos : pattern.keySet()) {
            if(pos.getY() == slice) {
                copy.put(pos, pattern.get(pos));
            }
        }
        return copy;
    }

    public List<ItemStack> getAsDescriptiveStacks() {
        List<ItemStack> out = new LinkedList<>();
        for (BlockInformation info : pattern.values()) {
            IBlockState state = info.getSampleState();
            Block type = state.getBlock();
            int meta = type.getMetaFromState(state);
            ItemStack s;
            if(type instanceof BlockFluidBase) {
                s = FluidUtil.getFilledBucket(new FluidStack(((BlockFluidBase) type).getFluid(), 1000));
            } else if(type instanceof BlockLiquid) {
                Material m = state.getMaterial();
                if(m == Material.LAVA) {
                    s = new ItemStack(Items.LAVA_BUCKET);
                } else if(m == Material.WATER) {
                    s = new ItemStack(Items.WATER_BUCKET);
                } else {
                    s = ItemStack.EMPTY;
                }
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

    public boolean matches(World world, BlockPos center, boolean oldState) {
        lblPattern:
        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            BlockInformation info = entry.getValue();
            BlockPos at = center.add(entry.getKey());
            if(!world.isBlockLoaded(at)) { //We can't say if it's actually properly formed, but it didn't get changed from the last check so....
                return oldState;
            }

            if(info.matchingTag != null) {
                TileEntity te = world.getTileEntity(at);
                if(te != null && info.matchingTag.getSize() > 0) {
                    NBTTagCompound cmp = new NBTTagCompound();
                    te.writeToNBT(cmp);
                    if(!NBTMatchingHelper.matchNBTCompound(info.matchingTag, cmp)) {
                        return false; //No match at this position.
                    }
                }
            }

            IBlockState state = world.getBlockState(at);
            Block atBlock = state.getBlock();
            int atMeta = atBlock.getMetaFromState(state);

            for (IBlockStateDescriptor descriptor : info.matchingStates) {
                for (IBlockState applicable : descriptor.applicable) {
                    Block type = applicable.getBlock();
                    int meta = type.getMetaFromState(applicable);
                    if(type.equals(state.getBlock()) && meta == atMeta) {
                        continue lblPattern; //Matches
                    }
                }
            }
            return false;
        }
        return true;
    }

    public BlockArray rotateYCCW() {
        BlockArray out = new BlockArray();

        Matrix2f rotation = new Matrix2f();
        rotation.m00 = 0;
        rotation.m01 = -1;
        rotation.m10 = 1;
        rotation.m11 = 0;

        for (BlockPos pos : pattern.keySet()) {
            BlockInformation info = pattern.get(pos);
            Vector2f vec = new Vector2f(pos.getX(), pos.getZ());
            Vector2f dst = Matrix2f.transform(rotation, vec, null);
            out.pattern.put(new BlockPos(dst.x, pos.getY(), dst.y), info.copyRotateYCCW());
        }
        return out;
    }

    public static class BlockInformation {

        public static final int CYCLE_TICK_SPEED = 30;
        public final List<IBlockStateDescriptor> matchingStates;
        private final List<IBlockState> samples = Lists.newLinkedList();
        private NBTTagCompound matchingTag = null;

        public BlockInformation(List<IBlockStateDescriptor> matching) {
            this.matchingStates = ImmutableList.copyOf(matching);
            for (IBlockStateDescriptor desc : matchingStates) {
                samples.addAll(desc.applicable);
            }
        }

        public void setMatchingTag(@Nullable NBTTagCompound matchingTag) {
            this.matchingTag = matchingTag;
        }

        public IBlockState getSampleState() {
            int tickSpeed = CYCLE_TICK_SPEED;
            if(samples.size() > 10) {
                tickSpeed *= 0.6;
            }
            int p = (int) (ClientScheduler.getClientTick() / tickSpeed);
            int part = p % samples.size();
            return samples.get(part);
        }

        public static IBlockStateDescriptor getDescriptor(JsonPrimitive stringElement) throws JsonParseException {
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
                return new IBlockStateDescriptor(b);
            } else {
                return new IBlockStateDescriptor(b.getStateFromMeta(meta));
            }
        }

        private BlockInformation copyRotateYCCW() {
            List<IBlockStateDescriptor> newDescriptors = new ArrayList<>(this.matchingStates.size());
            for (IBlockStateDescriptor desc : this.matchingStates) {
                IBlockStateDescriptor copy = new IBlockStateDescriptor();
                for (IBlockState applicableState : desc.applicable) {
                    copy.applicable.add(applicableState.withRotation(Rotation.COUNTERCLOCKWISE_90));
                }
                newDescriptors.add(copy);
            }
            return new BlockInformation(newDescriptors);
        }

        public BlockInformation copy() {
            List<IBlockStateDescriptor> descr = new ArrayList<>(this.matchingStates.size());
            for (IBlockStateDescriptor desc : this.matchingStates) {
                IBlockStateDescriptor copy = new IBlockStateDescriptor();
                copy.applicable.addAll(desc.applicable);
                descr.add(copy);
            }
            return new BlockInformation(descr);
        }

    }

    public static class IBlockStateDescriptor {

        public final List<IBlockState> applicable = Lists.newArrayList();

        private IBlockStateDescriptor() {}

        private IBlockStateDescriptor(Block block) {
            List<Integer> usedMetas = Lists.newArrayList();
            if(!(block instanceof BlockLiquid) && !(block instanceof BlockFluidBase)) {
                for (IBlockState state : block.getBlockState().getValidStates()) {
                    int meta = block.getMetaFromState(state);
                    if(!usedMetas.contains(meta)) {
                        usedMetas.add(meta);
                        this.applicable.add(state);
                    }
                }
            }
            if(applicable.isEmpty()) {
                applicable.add(block.getDefaultState());
            }
        }

        public IBlockStateDescriptor(IBlockState state) {
            this.applicable.add(state);
        }

    }

}
