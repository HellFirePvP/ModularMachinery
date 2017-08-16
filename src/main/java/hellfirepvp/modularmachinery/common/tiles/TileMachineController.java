/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntityRestrictedTick;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

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
    private IOInventory inventory;

    private ActiveMachineRecipe activeRecipe = null;

    private List<MachineComponent> foundComponents = Lists.newArrayList();

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
                        searchMatchingRecipe();
                        if(this.activeRecipe == null) {
                            craftingStatus = CraftingStatus.NO_RECIPE;
                        } else {
                            craftingStatus = CraftingStatus.CRAFTING;
                        }
                        markForUpdate();
                    }
                } else {
                    RecipeCraftingContext context = this.foundMachine.createContext(this.activeRecipe.getRecipe(), this.foundComponents);
                    this.craftingStatus = this.activeRecipe.tick(context); //handle energy IO and tick progression
                    if(this.activeRecipe.isCompleted(this)) {
                        this.activeRecipe.complete(context);
                        this.activeRecipe.reset();
                        context = this.foundMachine.createContext(this.activeRecipe.getRecipe(), this.foundComponents);
                        if(context.canStartCrafting()) {
                            context.startCrafting();
                            this.craftingStatus = CraftingStatus.CRAFTING;
                        } else {
                            this.activeRecipe = null;
                            searchMatchingRecipe();
                            if(this.activeRecipe == null) {
                                this.craftingStatus = CraftingStatus.NO_RECIPE;
                            } else {
                                this.craftingStatus = CraftingStatus.CRAFTING;
                            }
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

    private void searchMatchingRecipe() {
        List<MachineRecipe> availableRecipes = RecipeRegistry.getRegistry().getRecipesFor(this.foundMachine);
        for (MachineRecipe recipe : availableRecipes) {
            RecipeCraftingContext context = this.foundMachine.createContext(recipe, this.foundComponents);
            if(context.canStartCrafting()) {
                this.activeRecipe = new ActiveMachineRecipe(recipe);
                context.startCrafting(); //chew up start items
                return;
            }
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
                    craftingStatus = CraftingStatus.MISSING_STRUCTURE;
                    markForUpdate();
                } else if(!foundPattern.matches(getWorld(), getPos(), true)) {
                    this.activeRecipe = null;
                    this.foundMachine = null;
                    this.foundPattern = null;
                    this.patternRotation = null;
                    craftingStatus = CraftingStatus.MISSING_STRUCTURE;
                    markForUpdate();
                }
            }
            if(this.foundMachine == null || this.foundPattern == null || this.patternRotation == null) {
                this.foundMachine = null;
                this.foundPattern = null;
                this.patternRotation = null;

                DynamicMachine blueprint = getBlueprintMachine();
                if(blueprint != null) {
                    Tuple<EnumFacing, BlockArray> res = matchesRotation(blueprint.getPattern());
                    if(res != null) {
                        this.foundMachine = blueprint;
                        this.foundPattern = res.getSecond();
                        this.patternRotation = res.getFirst();
                        this.world.setBlockState(pos, BlocksMM.blockController.getDefaultState().withProperty(BlockController.FACING, res.getFirst()));
                        markForUpdate();

                        if(this.foundMachine.getMachineColor() != TileColorableMachineComponent.DEFAULT_COLOR) {
                            distributeCasingColor();
                        }
                    }
                } else {
                    for (DynamicMachine machine : MachineRegistry.getRegistry()) {
                        if (machine.requiresBlueprint()) continue;
                        Tuple<EnumFacing, BlockArray> res = matchesRotation(machine.getPattern());
                        if (res != null) {
                            this.foundMachine = machine;
                            this.foundPattern = res.getSecond();
                            this.patternRotation = res.getFirst();
                            this.world.setBlockState(pos, BlocksMM.blockController.getDefaultState().withProperty(BlockController.FACING, res.getFirst()));
                            markForUpdate();

                            if(this.foundMachine.getMachineColor() != TileColorableMachineComponent.DEFAULT_COLOR) {
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

    @Nullable
    private Tuple<EnumFacing, BlockArray> matchesRotation(BlockArray pattern) {
        EnumFacing face = EnumFacing.NORTH;
        do {
            if(pattern.matches(getWorld(), getPos(), false)) {
                return new Tuple<>(face, pattern);
            }
            face = face.rotateYCCW();
            pattern = pattern.rotateYCCW();
        } while (face != EnumFacing.NORTH);
        return null;
    }

    private void updateComponents() {
        if(this.foundMachine == null || this.foundPattern == null || this.patternRotation == null) {
            this.foundComponents.clear();
            this.foundMachine = null;
            this.foundPattern = null;
            this.patternRotation = null;
            return;
        }
        if(ticksExisted % 20 == 0) {
            this.foundComponents = Lists.newArrayList();
            for (BlockPos potentialPosition : this.foundPattern.getPattern().keySet()) {
                BlockPos realPos = getPos().add(potentialPosition);
                TileEntity te = getWorld().getTileEntity(realPos);
                if(te != null && te instanceof MachineComponentTile) {
                    MachineComponent component = ((MachineComponentTile) te).provideComponent();
                    if(component != null) {
                        this.foundComponents.add(component);
                    }
                }
            }
        }
    }

    public float getCurrentActiveRecipeProgress(float patial) {
        if(activeRecipe == null) return 0F;
        float tick = activeRecipe.getTick() + patial;
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
        this.craftingStatus = CraftingStatus.values()[compound.getInteger("status")];

        if(compound.hasKey("machine") && compound.hasKey("rotation")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("machine"));
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(rl);
            if(machine == null) {
                ModularMachinery.log.info("Couldn't find machine named " + rl.toString() + " for controller at " + getPos().toString());
                this.foundMachine = null;
                this.foundPattern = null;
                this.patternRotation = null;
            } else {
                EnumFacing rot = EnumFacing.getHorizontal(compound.getInteger("rotation"));
                EnumFacing offset = EnumFacing.NORTH;
                BlockArray pattern = machine.getPattern();
                while (offset != rot) {
                    pattern = pattern.rotateYCCW();
                    offset = offset.rotateY();
                }
                this.patternRotation = rot;
                this.foundPattern = pattern;
                this.foundMachine = machine;
            }
        } else {
            this.foundMachine = null;
            this.foundPattern = null;
            this.patternRotation = null;
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
        compound.setInteger("status", this.craftingStatus.ordinal());

        if(this.foundMachine != null && this.patternRotation != null) {
            compound.setString("machine", this.foundMachine.getRegistryName().toString());
            compound.setInteger("rotation", this.patternRotation.getHorizontalIndex());
        }
        if(this.activeRecipe != null) {
            compound.setTag("activeRecipe", this.activeRecipe.serialize());
        }
    }

    public static enum CraftingStatus {

        MISSING_STRUCTURE,
        NO_RECIPE,
        NO_ENERGY,
        CRAFTING;

    }

}
