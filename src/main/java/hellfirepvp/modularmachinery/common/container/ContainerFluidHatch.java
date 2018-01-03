/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.container;

import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerFluidHatch
 * Created by HellFirePvP
 * Date: 09.07.2017 / 11:27
 */
public class ContainerFluidHatch extends ContainerBase<TileFluidTank> {

    public ContainerFluidHatch(TileFluidTank owner, EntityPlayer opening) {
        super(owner, opening);
    }

}
