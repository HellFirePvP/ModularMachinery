/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFluidOutputHatch
 * Created by HellFirePvP
 * Date: 07.07.2017 / 18:59
 */
public class TileFluidOutputHatch extends TileFluidTank implements MachineComponentTile {

    public TileFluidOutputHatch() {}

    public TileFluidOutputHatch(FluidHatchSize size) {
        super(size, IOType.OUTPUT);
    }

}
