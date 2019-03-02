/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.modifier.ModifierReplacement;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntityRestrictedTick;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import hellfirepvp.modularmachinery.common.util.nbt.NBTHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileMachineController
 * Created by HellFirePvP
 * Date: 28.06.2017 / 17:14
 */
public class TileMachineController extends TileEntityRestrictedTick {

    public static final int BLUEPRINT_SLOT = 0;
    public static final int ACCELERATOR_SLOT = 1;

    private CraftingStatus craftingStatus = CraftingStatus.MISSING_STRUCTURE;

    private DynamicMachine foundMachine = null;
    private BlockArray foundPattern = null;
    private EnumFacing patternRotation = null;
    private DynamicMachine.ModifierReplacementMap foundReplacements = null;
    private IOInventory inventory;

    private ActiveMachineRecipe activeRecipe = null;

    private List<MachineComponent> foundComponents = Lists.newArrayList();
    private Map<BlockPos, List<ModifierReplacement>> foundModifiers = new HashMap<>();

    public TileMachineController() {
        this.inventory = buildInventory();
        this.inventory.setStackLimit(1, BLUEPRINT_SLOT);
    }

    private IOInventory buildInventory() {
        return new IOInventory(this,
                new int[] {},
                new int[] {})
                .setMiscSlots(BLUEPRINT_SLOT, ACCELERATOR_SLOT);
    }

    public IOInventory getInventory() {
        return inventory;
    }

    @Override
    public void doRestrictedTick() {
        if(!getWorld().isRemote) {
            if(getWorld().getStrongPower(getPos()) > 0) {
                return;
            }

            checkStructure();
            updateComponents();

            if(this.foundMachine != null && this.foundPattern != null && this.patternRotation != null) {
                if(this.activeRecipe == null) {
                    if(this.ticksExisted % 80 == 0) {
                        searchAndUpdateRecipe();
                        markForUpdate();
                    }
                } else {
                    RecipeCraftingContext context = this.foundMachine.createContext(this.activeRecipe.getRecipe(), this.foundComponents, MiscUtils.flatten(this.foundModifiers.values()));
                    this.craftingStatus = this.activeRecipe.tick(context); //handle energy IO and tick progression
                    if(this.activeRecipe.isCompleted(this, context)) {
                        this.activeRecipe.complete(context);
                        this.activeRecipe.reset();
                        context = this.foundMachine.createContext(this.activeRecipe.getRecipe(), this.foundComponents, MiscUtils.flatten(this.foundModifiers.values()));
                        RecipeCraftingContext.CraftingCheckResult result = context.canStartCrafting();

                        if (result.isFailure()) {
                            this.activeRecipe = null;
                            searchAndUpdateRecipe();
                        } else {
                            context.startCrafting();
                            this.craftingStatus = CraftingStatus.working();
                        }
                    }
                    markForUpdate();
                }
            } else {
                craftingStatus = CraftingStatus.MISSING_STRUCTURE;
                markForUpdate();
            }
        }
    }

    private void searchAndUpdateRecipe() {
        Iterable<MachineRecipe> availableRecipes = RecipeRegistry.getRegistry().getRecipesFor(this.foundMachine);

        MachineRecipe highestValidity = null;
        RecipeCraftingContext.CraftingCheckResult highestValidityResult = null;
        float validity = 0F;

        for (MachineRecipe recipe : availableRecipes) {
            RecipeCraftingContext context = this.foundMachine.createContext(recipe, this.foundComponents, MiscUtils.flatten(this.foundModifiers.values()));
            RecipeCraftingContext.CraftingCheckResult result = context.canStartCrafting();
            if (!result.isFailure()) {
                this.activeRecipe = new ActiveMachineRecipe(recipe);
                context.startCrafting(); //chew up start items
                break;
            } else if (highestValidity == null ||
                    (result.getValidity() >= 0.5F && result.getValidity() > validity)) {
                highestValidity = recipe;
                highestValidityResult = result;
                validity = result.getValidity();
            }
        }


        if(this.activeRecipe == null) {
            if (highestValidity != null) {
                this.craftingStatus = CraftingStatus.failure(
                        Iterables.getFirst(highestValidityResult.getUnlocalizedErrorMessages(), ""));
            } else {
                this.craftingStatus = CraftingStatus.failure(Type.NO_RECIPE.getUnlocalizedDescription());
            }
        } else {
            this.craftingStatus = CraftingStatus.working();
        }
    }

    private void checkStructure() {
        if(ticksExisted % 20 == 0) {
            if(this.foundMachine != null && this.foundPattern != null && this.patternRotation != null) {
                if(this.foundMachine.requiresBlueprint() && !this.foundMachine.equals(getBlueprintMachine())) {
                    this.activeRecipe = null;
                    this.foundMachine = null;
                    this.foundPattern = null;
                    this.patternRotation = null;
                    this.foundReplacements = null;
                    craftingStatus = CraftingStatus.MISSING_STRUCTURE;
                    markForUpdate();
                } else if(!foundPattern.matches(getWorld(), getPos(), true, this.foundReplacements)) {
                    this.activeRecipe = null;
                    this.foundMachine = null;
                    this.foundPattern = null;
                    this.patternRotation = null;
                    this.foundReplacements = null;
                    craftingStatus = CraftingStatus.MISSING_STRUCTURE;
                    markForUpdate();
                }
            }
            if(this.foundMachine == null || this.foundPattern == null || this.patternRotation == null || this.foundReplacements == null) {
                this.foundMachine = null;
                this.foundPattern = null;
                this.patternRotation = null;
                this.foundReplacements = null;

                DynamicMachine blueprint = getBlueprintMachine();
                if(blueprint != null) {
                    if(matchesRotation(blueprint.getPattern(), blueprint)) {
                        this.world.setBlockState(pos, BlocksMM.blockController.getDefaultState().withProperty(BlockController.FACING, this.patternRotation));
                        markForUpdate();

                        if(this.foundMachine.getMachineColor() != Config.machineColor) {
                            distributeCasingColor();
                        }
                    }
                } else {
                    for (DynamicMachine machine : MachineRegistry.getRegistry()) {
                        if (machine.requiresBlueprint()) continue;
                        if (matchesRotation(machine.getPattern(), machine)) {
                            this.world.setBlockState(pos, BlocksMM.blockController.getDefaultState().withProperty(BlockController.FACING, this.patternRotation));
                            markForUpdate();

                            if(this.foundMachine.getMachineColor() != Config.machineColor) {
                                distributeCasingColor();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void distributeCasingColor() {
        if(this.foundMachine != null && this.foundPattern != null) {
            int color = this.foundMachine.getMachineColor();
            tryColorize(getPos(), color);
            for (BlockPos pos : this.foundPattern.getPattern().keySet()) {
                tryColorize(this.getPos().add(pos), color);
            }
        }
    }

    private void tryColorize(BlockPos pos, int color) {
        TileEntity te = this.getWorld().getTileEntity(pos);
        if(te != null && te instanceof TileColorableMachineComponent) {
            ((TileColorableMachineComponent) te).definedColor = color;
            ((TileColorableMachineComponent) te).markForUpdate();
            getWorld().addBlockEvent(pos, getWorld().getBlockState(pos).getBlock(), 1, 1);
        }
    }

    private boolean matchesRotation(BlockArray pattern, DynamicMachine machine) {
        EnumFacing face = EnumFacing.NORTH;
        DynamicMachine.ModifierReplacementMap replacements = machine.getModifiersAsMatchingReplacements();
        do {
            if(pattern.matches(getWorld(), getPos(), false, replacements)) {
                this.foundPattern = pattern;
                this.patternRotation = face;
                this.foundMachine = machine;
                this.foundReplacements = replacements;
                return true;
            }
            face = face.rotateYCCW();
            pattern = pattern.rotateYCCW();
            replacements = replacements.rotateYCCW();
        } while (face != EnumFacing.NORTH);
        this.foundPattern = null;
        this.patternRotation = null;
        this.foundMachine = null;
        this.foundReplacements = null;
        return false;
    }

    private void updateComponents() {
        if(this.foundMachine == null || this.foundPattern == null || this.patternRotation == null || this.foundReplacements == null) {
            this.foundComponents.clear();
            this.foundModifiers.clear();
            this.foundMachine = null;
            this.foundPattern = null;
            this.patternRotation = null;
            this.foundReplacements = null;
            return;
        }
        if(ticksExisted % 20 == 0) {
            this.foundComponents = Lists.newArrayList();
            for (BlockPos potentialPosition : this.foundPattern.getPattern().keySet()) {
                BlockPos realPos = getPos().add(potentialPosition);
                TileEntity te = getWorld().getTileEntity(realPos);
                if(te instanceof MachineComponentTile) {
                    MachineComponent component = ((MachineComponentTile) te).provideComponent();
                    if(component != null) {
                        this.foundComponents.add(component);
                    }
                }
            }

            int rotations = 0;
            EnumFacing rot = EnumFacing.NORTH;
            while (rot != this.patternRotation) {
                rot = rot.rotateYCCW();
                rotations++;
            }

            this.foundModifiers = Maps.newHashMap();
            for (Map.Entry<BlockPos, List<ModifierReplacement>> offsetModifiers : this.foundMachine.getModifiers().entrySet()) {
                BlockPos at = offsetModifiers.getKey();
                for (int i = 0; i < rotations; i++) {
                    at = new BlockPos(at.getZ(), at.getY(), -at.getX());
                }
                BlockPos realAt = this.getPos().add(at);
                for (ModifierReplacement mod : offsetModifiers.getValue()) {
                    if(mod.getBlockInformation().matches(this.world, realAt, false)) {
                        this.foundModifiers.putIfAbsent(offsetModifiers.getKey(), Lists.newArrayList());
                        this.foundModifiers.get(offsetModifiers.getKey()).add(mod);
                    }
                }
            }
        }
    }

    public float getCurrentActiveRecipeProgress(float partial) {
        if(activeRecipe == null) return 0F;
        float tick = activeRecipe.getTick() + partial;
        float maxTick = activeRecipe.getRecipe().getRecipeTotalTickTime();
        return MathHelper.clamp(tick / maxTick, 0F, 1F);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Nullable
    public DynamicMachine getFoundMachine() {
        return foundMachine;
    }

    @Nullable
    public DynamicMachine getBlueprintMachine() {
        ItemStack blueprintSlotted = this.inventory.getStackInSlot(BLUEPRINT_SLOT);
        if (!blueprintSlotted.isEmpty()) {
            return ItemBlueprint.getAssociatedMachine(blueprintSlotted);
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        }
        return super.getCapability(capability, facing);
    }

    public CraftingStatus getCraftingStatus() {
        return craftingStatus;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        this.inventory = IOInventory.deserialize(this, compound.getCompoundTag("items"));
        this.inventory.setStackLimit(1, BLUEPRINT_SLOT);

        if (compound.hasKey("status")) { //Legacy support
            this.craftingStatus = new CraftingStatus(Type.values()[compound.getInteger("status")], "");
        } else {
            this.craftingStatus = CraftingStatus.deserialize(compound.getCompoundTag("statusTag"));
        }

        if(compound.hasKey("machine") && compound.hasKey("rotation")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("machine"));
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(rl);
            if(machine == null) {
                ModularMachinery.log.info("Couldn't find machine named " + rl.toString() + " for controller at " + getPos().toString());
                this.foundMachine = null;
                this.foundPattern = null;
                this.patternRotation = null;
                this.foundReplacements = null;
            } else {
                EnumFacing rot = EnumFacing.getHorizontal(compound.getInteger("rotation"));
                EnumFacing offset = EnumFacing.NORTH;
                BlockArray pattern = machine.getPattern();
                DynamicMachine.ModifierReplacementMap replacements = machine.getModifiersAsMatchingReplacements();
                while (offset != rot) {
                    replacements = replacements.rotateYCCW();
                    pattern = pattern.rotateYCCW();
                    offset = offset.rotateY();
                }
                this.patternRotation = rot;
                this.foundPattern = pattern;
                this.foundMachine = machine;
                this.foundReplacements = replacements;

                if(compound.hasKey("modifierOffsets")) {
                    NBTTagList list = compound.getTagList("modifierOffsets", Constants.NBT.TAG_COMPOUND);
                    for (int i = 0; i < list.tagCount(); i++) {
                        NBTTagCompound posTag = list.getCompoundTagAt(i);
                        BlockPos modOffset = NBTUtil.getPosFromTag(posTag.getCompoundTag("position"));
                        IBlockState state = NBTHelper.getBlockState(posTag, "state");
                        for (ModifierReplacement mod : this.foundMachine.getModifiers().getOrDefault(modOffset, Lists.newArrayList())) {
                            if (mod.getBlockInformation().matchesState(state)) {
                                this.foundModifiers.putIfAbsent(modOffset, Lists.newArrayList());
                                this.foundModifiers.get(modOffset).add(mod);
                            }
                        }
                    }
                }
            }
        } else {
            this.foundMachine = null;
            this.foundPattern = null;
            this.patternRotation = null;
            this.foundReplacements = null;
        }
        if(compound.hasKey("activeRecipe")) {
            NBTTagCompound tag = compound.getCompoundTag("activeRecipe");
            ActiveMachineRecipe recipe = new ActiveMachineRecipe(tag);
            if(recipe.getRecipe() == null) {
                ModularMachinery.log.info("Couldn't find recipe named " + tag.getString("recipeName") + " for controller at " + getPos().toString());
                this.activeRecipe = null;
            } else {
                this.activeRecipe = recipe;
            }
        } else {
            this.activeRecipe = null;
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setTag("items", this.inventory.writeNBT());
        compound.setTag("statusTag", this.craftingStatus.serialize());

        if(this.foundMachine != null && this.patternRotation != null) {
            compound.setString("machine", this.foundMachine.getRegistryName().toString());
            compound.setInteger("rotation", this.patternRotation.getHorizontalIndex());

            NBTTagList listModifierOffsets = new NBTTagList();
            for (BlockPos offset : this.foundModifiers.keySet()) {
                NBTTagCompound tag = new NBTTagCompound();

                tag.setTag("position", NBTUtil.createPosTag(offset));
                NBTHelper.setBlockState(tag, "state", world.getBlockState(getPos().add(offset)));

                listModifierOffsets.appendTag(tag);
            }
            compound.setTag("modifierOffsets", listModifierOffsets);
        }
        if(this.activeRecipe != null) {
            compound.setTag("activeRecipe", this.activeRecipe.serialize());
        }
    }

    public static class CraftingStatus {

        private static final CraftingStatus SUCCESS = new CraftingStatus(Type.CRAFTING, "");
        private static final CraftingStatus MISSING_STRUCTURE = new CraftingStatus(Type.MISSING_STRUCTURE, "");

        private final Type status;
        private final String unlocMessage;

        private CraftingStatus(Type status, String unlocMessage) {
            this.status = status;
            this.unlocMessage = unlocMessage;
        }

        public Type getStatus() {
            return status;
        }

        public String getUnlocMessage() {
            return !unlocMessage.isEmpty() ? unlocMessage : this.status.getUnlocalizedDescription();
        }

        public boolean isCrafting() {
            return this.status == Type.CRAFTING;
        }

        public static CraftingStatus working() {
            return SUCCESS;
        }

        public static CraftingStatus failure(String unlocMessage) {
            return new CraftingStatus(Type.NO_RECIPE, unlocMessage);
        }

        private NBTTagCompound serialize() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("type", this.status.ordinal());
            tag.setString("message", this.unlocMessage);
            return tag;
        }

        private static CraftingStatus deserialize(NBTTagCompound tag) {
            Type type = Type.values()[tag.getInteger("type")];
            String unlocMessage = tag.getString("message");
            return new CraftingStatus(type, unlocMessage);
        }
    }

    public static enum Type {

        MISSING_STRUCTURE,
        NO_RECIPE,
        CRAFTING;

        public String getUnlocalizedDescription() {
            return "gui.controller.status." + this.name().toLowerCase();
        }

    }

}
