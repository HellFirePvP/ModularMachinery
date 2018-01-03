/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RedstoneHelper
 * Created by HellFirePvP
 * Date: 14.07.2017 / 16:40
 */
public class RedstoneHelper {

    public static int getRedstoneLevel(TileEntity sync) {
        if(sync == null) return 0;
        if(sync instanceof TileInventory) {
            return ItemHandlerHelper.calcRedstoneFromInventory(((TileInventory) sync).getInventory());
        } else if(sync instanceof TileFluidTank) {
            FluidTank tank = ((TileFluidTank) sync).getTank();
            float cap = tank.getCapacity();
            float cur = tank.getFluidAmount();
            return MathHelper.clamp(Math.round(15F * (cur / cap)), 0, 15);
        } else if(sync instanceof TileEnergyHatch) {
            float cap = ((TileEnergyHatch) sync).getMaxEnergy();
            float cur = ((TileEnergyHatch) sync).getCurrentEnergy();
            return MathHelper.clamp(Math.round(15F * (cur / cap)), 0, 15);
        }
        return 0;
    }

}
