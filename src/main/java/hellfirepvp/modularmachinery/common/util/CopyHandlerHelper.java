/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidTank;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CopyHandlerHelper
 * Created by HellFirePvP
 * Date: 12.07.2017 / 21:37
 */
public class CopyHandlerHelper {

    public static FluidTank copyTank(FluidTank tank) {
        NBTTagCompound cmp = new NBTTagCompound();
        tank.writeToNBT(cmp);
        FluidTank newTank = new FluidTank(tank.getCapacity());
        newTank.readFromNBT(cmp);
        return newTank;
    }

    public static IOInventory copyInventory(IOInventory inventory) {
        return IOInventory.deserialize(inventory.getOwner(), inventory.writeNBT());
    }

}
