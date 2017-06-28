/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

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
            if(foundMachine != null) {
                //TODO
            }
        }
    }

    @Nullable
    public DynamicMachine getFoundMachine() {
        return foundMachine;
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
