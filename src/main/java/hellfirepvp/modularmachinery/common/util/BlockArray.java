/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
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
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonSerializer;
import hellfirepvp.modularmachinery.common.util.nbt.NBTMatchingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    private static final ResourceLocation ic2TileBlock = new ResourceLocation("ic2", "te");
    protected Map<BlockPos, BlockInformation> pattern = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0), max = new Vec3i(0, 0, 0), size = new Vec3i(0, 0, 0);

    public BlockArray() {}

    public BlockArray(BlockArray other) {
        this.pattern = new HashMap<>(other.pattern);
        this.min = new Vec3i(other.min.getX(), other.min.getY(), other.min.getZ());
        this.max = new Vec3i(other.max.getX(), other.max.getY(), other.max.getZ());
        this.size = new Vec3i(other.size.getX(), other.size.getY(), other.size.getZ());
    }

    public BlockArray(BlockArray other, Vec3i offset) {
        for (Map.Entry<BlockPos, BlockInformation> otherEntry : other.pattern.entrySet()) {
            this.pattern.put(otherEntry.getKey().add(offset), otherEntry.getValue());
        }
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

    @SideOnly(Side.CLIENT)
    public List<ItemStack> getAsDescriptiveStacks() {
        return getAsDescriptiveStacks(Optional.empty());
    }

    @SideOnly(Side.CLIENT)
    public List<ItemStack> getAsDescriptiveStacks(Optional<Long> snapSample) {
        List<ItemStack> out = new LinkedList<>();
        for (Map.Entry<BlockPos, BlockInformation> infoEntry : pattern.entrySet()) {
            BlockArray.BlockInformation bi = infoEntry.getValue();
            ItemStack s = bi.getDescriptiveStack(snapSample);

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

    public boolean matches(World world, BlockPos center, boolean oldState, @Nullable Map<BlockPos, List<BlockInformation>> modifierReplacementPattern) {
        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            BlockPos at = center.add(entry.getKey());
            if(!entry.getValue().matches(world, at, oldState)) {
                if(modifierReplacementPattern != null && modifierReplacementPattern.containsKey(entry.getKey())) {
                    for (BlockInformation info : modifierReplacementPattern.get(entry.getKey())) {
                        if (info.matches(world, at, oldState)) {
                            continue;
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    public BlockPos getRelativeMismatchPosition(World world, BlockPos center, @Nullable Map<BlockPos, List<BlockInformation>> modifierReplacementPattern) {
        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            BlockPos at = center.add(entry.getKey());
            if(!entry.getValue().matches(world, at, false)) {
                if(modifierReplacementPattern != null && modifierReplacementPattern.containsKey(entry.getKey())) {
                    for (BlockInformation info : modifierReplacementPattern.get(entry.getKey())) {
                        if (info.matches(world, at, false)) {
                            continue;
                        }
                    }
                }
                return entry.getKey();
            }
        }
        return null;
    }

    public BlockArray rotateYCCW() {
        BlockArray out = new BlockArray();

        for (BlockPos pos : pattern.keySet()) {
            BlockInformation info = pattern.get(pos);
            out.pattern.put(new BlockPos(pos.getZ(), pos.getY(), -pos.getX()), info.copyRotateYCCW());
        }
        return out;
    }

    public String serializeAsMachineJson() {
        String newline = System.getProperty("line.separator");
        String move = "    ";

        StringBuilder sb = new StringBuilder();
        sb.append("{").append(newline);
        sb.append(move).append("\"parts\": [").append(newline);

        for (Iterator<BlockPos> iterator = this.pattern.keySet().iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            sb.append(move).append(move).append("{").append(newline);

            sb.append(move).append(move).append(move).append("\"x\": ").append(pos.getX()).append(",").append(newline);
            sb.append(move).append(move).append(move).append("\"y\": ").append(pos.getY()).append(",").append(newline);
            sb.append(move).append(move).append(move).append("\"z\": ").append(pos.getZ()).append(",").append(newline);

            BlockInformation bi = this.pattern.get(pos);
            if(bi.matchingTag != null) {
                String strTag = NBTJsonSerializer.serializeNBT(bi.matchingTag);
                sb.append(move).append(move).append(move).append("\"nbt\": ").append(strTag).append(",").append(newline);
            }

            sb.append(move).append(move).append(move).append("\"elements\": [").append(newline);
            for (Iterator<IBlockState> iterator1 = bi.samples.iterator(); iterator1.hasNext(); ) {
                IBlockState descriptor = iterator1.next();

                int meta = descriptor.getBlock().getMetaFromState(descriptor);
                String str = descriptor.getBlock().getRegistryName().toString() + "@" + meta;
                sb.append(move).append(move).append(move).append(move).append("\"").append(str).append("\"");

                if(iterator1.hasNext()) {
                    sb.append(",");
                }
                sb.append(newline);
            }

            sb.append(move).append(move).append(move).append("]").append(newline);
            sb.append(move).append(move).append("}");
            if(iterator.hasNext()) {
                sb.append(",");
            }
            sb.append(newline);
        }

        sb.append(move).append("]");
        sb.append("}");
        return sb.toString();
    }

    public static class BlockInformation {

        public static final int CYCLE_TICK_SPEED = 30;
        public List<IBlockStateDescriptor> matchingStates;
        private List<IBlockState> samples = Lists.newLinkedList();
        public NBTTagCompound matchingTag = null;

        public BlockInformation(List<IBlockStateDescriptor> matching) {
            this.matchingStates = Lists.newLinkedList(matching);
            for (IBlockStateDescriptor desc : matchingStates) {
                samples.addAll(desc.applicable);
            }
        }

        public void setMatchingTag(@Nullable NBTTagCompound matchingTag) {
            this.matchingTag = matchingTag;
        }

        public IBlockState getSampleState() {
            return getSampleState(Optional.empty());
        }

        public IBlockState getSampleState(Optional<Long> snapTick) {
            int tickSpeed = CYCLE_TICK_SPEED;
            if(samples.size() > 10) {
                tickSpeed *= 0.6;
            }
            int p = (int) (snapTick.orElse(ClientScheduler.getClientTick()) / tickSpeed);
            int part = p % samples.size();
            return samples.get(part);
        }

        @SideOnly(Side.CLIENT)
        public ItemStack getDescriptiveStack(Optional<Long> snapTick) {
            IBlockState state = getSampleState(snapTick);

            Tuple<IBlockState, TileEntity> recovered = BlockCompatHelper.transformState(state, this.matchingTag,
                    new BlockArray.TileInstantiateContext(Minecraft.getMinecraft().world, BlockPos.ORIGIN));
            state = recovered.getFirst();
            Block type = state.getBlock();
            int meta = type.getMetaFromState(state);
            ItemStack s = ItemStack.EMPTY;

            try {
                if(ic2TileBlock.equals(type.getRegistryName())) {
                    s = BlockCompatHelper.tryGetIC2MachineStack(state, recovered.getSecond());
                } else {
                    s = state.getBlock().getPickBlock(state, null, null, BlockPos.ORIGIN, null);
                }
            } catch (Exception exc) {}

            if(s.isEmpty()) {
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
                    if(i != Items.AIR) {
                        s = new ItemStack(i, 1, meta);
                    }
                }
            }
            return s;
        }

        public static IBlockStateDescriptor getDescriptor(String strElement) throws JsonParseException {
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
            BlockInformation bi =  new BlockInformation(newDescriptors);
            if(this.matchingTag != null) {
                bi.matchingTag = this.matchingTag;
            }
            return bi;
        }

        public BlockInformation copy() {
            List<IBlockStateDescriptor> descr = new ArrayList<>(this.matchingStates.size());
            for (IBlockStateDescriptor desc : this.matchingStates) {
                IBlockStateDescriptor copy = new IBlockStateDescriptor();
                copy.applicable.addAll(desc.applicable);
                descr.add(copy);
            }
            BlockInformation bi =  new BlockInformation(descr);
            if(this.matchingTag != null) {
                bi.matchingTag = this.matchingTag;
            }
            return bi;
        }

        public boolean matchesState(IBlockState state) {
            Block atBlock = state.getBlock();
            int atMeta = atBlock.getMetaFromState(state);

            for (IBlockStateDescriptor descriptor : matchingStates) {
                for (IBlockState applicable : descriptor.applicable) {
                    Block type = applicable.getBlock();
                    int meta = type.getMetaFromState(applicable);
                    if(type.equals(state.getBlock()) && meta == atMeta) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean matches(World world, BlockPos at, boolean default_) {
            if(!world.isBlockLoaded(at)) {
                return default_;
            }

            if(matchingTag != null) {
                TileEntity te = world.getTileEntity(at);
                if(te != null && matchingTag.getSize() > 0) {
                    NBTTagCompound cmp = new NBTTagCompound();
                    te.writeToNBT(cmp);
                    if(!NBTMatchingHelper.matchNBTCompound(matchingTag, cmp)) {
                        return false; //No match at this position.
                    }
                }
            }

            IBlockState state = world.getBlockState(at);
            Block atBlock = state.getBlock();
            int atMeta = atBlock.getMetaFromState(state);

            for (IBlockStateDescriptor descriptor : matchingStates) {
                for (IBlockState applicable : descriptor.applicable) {
                    Block type = applicable.getBlock();
                    int meta = type.getMetaFromState(applicable);
                    if(type.equals(state.getBlock()) && meta == atMeta) {
                        return true;
                    }
                }
            }
            return false;
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

    public static class TileInstantiateContext {

        private final World world;
        private final BlockPos pos;

        public TileInstantiateContext(World world, BlockPos pos) {
            this.world = world;
            this.pos = pos;
        }

        public World getWorld() {
            return world;
        }

        public BlockPos getPos() {
            return pos;
        }

        public void apply(TileEntity te) {
            if(te != null) {
                te.setWorld(world);
                te.setPos(pos);
            }
        }
    }

}
