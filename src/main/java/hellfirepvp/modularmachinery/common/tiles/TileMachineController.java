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
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

    private DynamicMachine foundMachine = null;
    private IOInventory inventory;

    private ActiveMachineRecipe activeRecipe = null;

    private List<MachineComponent> foundComponents = Lists.newArrayList();

    public TileMachineController() {
        this.inventory = buildInventory();
    }

    private IOInventory buildInventory() {
        return new IOInventory(this,
                new int[] {},
                new int[] {})
                .setMiscSlots(BLUEPRINT_SLOT, ACCELERATOR_SLOT);
    }

    @Override
    public void doRestrictedTick() {
        if(!getWorld().isRemote) {
            checkStructure();
            updateComponents();

            if(this.foundMachine != null) {
                if(this.activeRecipe == null) {
                    searchRecipe();
                } else {
                    RecipeCraftingContext context = this.foundMachine.createContext(this.activeRecipe.getRecipe(), this.foundComponents);
                    this.activeRecipe.tick(context);
                    if(this.activeRecipe.isCompleted(this)) {
                        this.activeRecipe.complete(context);
                    }
                }
            }
        }
    }

    private void searchRecipe() {
        if(this.ticksExisted % 80 == 0) {
            List<MachineRecipe> availableRecipes = RecipeRegistry.getRegistry().getRecipesFor(this.foundMachine);
            for (MachineRecipe recipe : availableRecipes) {
                RecipeCraftingContext context = this.foundMachine.createContext(recipe, this.foundComponents);
                if(context.isValid()) {
                    this.activeRecipe = new ActiveMachineRecipe(recipe);
                    return;
                }
            }
        }
    }

    private void checkStructure() {
        if(ticksExisted % 20 == 0) {
            if(this.foundMachine != null) {
                BlockArray pattern = this.foundMachine.getPattern();
                if(!pattern.matches(getWorld(), getPos())) {
                    this.foundMachine = null;
                }
            }
            if(this.foundMachine == null) {
                DynamicMachine blueprint = getBlueprintMachine();
                if(blueprint != null) {
                    if(blueprint.getPattern().matches(getWorld(), getPos())) {
                        this.foundMachine = blueprint;
                        markForUpdate();
                    }
                } else {
                    for (DynamicMachine machine : MachineRegistry.getRegistry()) {
                        if (machine.requiresBlueprint()) continue;
                        if (machine.getPattern().matches(getWorld(), getPos())) {
                            this.foundMachine = machine;
                            markForUpdate();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateComponents() {
        if(this.foundMachine == null) {
            this.foundComponents.clear();
            return;
        }
        if(ticksExisted % 20 == 0) {
            this.foundComponents = Lists.newArrayList();
            for (BlockPos potentialPosition : this.foundMachine.getPattern().getPattern().keySet()) {
                BlockPos realPos = getPos().add(potentialPosition);
                TileEntity te = getWorld().getTileEntity(realPos);
                if(te != null && te instanceof MachineComponentTile) {
                    MachineComponent component = ((MachineComponentTile) te).provideComponent();
                    if(component != null) {
                        this.foundComponents.add(component);
                    }
                }
            }

            if(this.activeRecipe != null) {
                RecipeCraftingContext context = this.foundMachine.createContext(this.activeRecipe.getRecipe(), this.foundComponents);
                if(!context.isValid()) {
                    this.activeRecipe = null;
                }
            }
        }
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

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        this.inventory = IOInventory.deserialize(this, compound.getCompoundTag("items"));

        if(compound.hasKey("machine")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("machine"));
            DynamicMachine machine = MachineRegistry.getRegistry().getMachine(rl);
            if(machine == null) {
                ModularMachinery.log.info("Couldn't find machine named " + rl.toString() + " for controller at " + getPos().toString());
            } else {
                this.foundMachine = machine;
            }
        } else {
            this.foundMachine = null;
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setTag("items", this.inventory.writeNBT());

        if(this.foundMachine != null) {
            compound.setString("machine", this.foundMachine.getRegistryName().toString());
        }
    }

}
