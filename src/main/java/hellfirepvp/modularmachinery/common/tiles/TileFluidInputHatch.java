/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileFluidInputHatch
 * Created by HellFirePvP
 * Date: 07.07.2017 / 18:49
 */
public class TileFluidInputHatch extends TileFluidTank implements MachineComponentTile {

    public TileFluidInputHatch() {}

    public TileFluidInputHatch(FluidHatchSize size) {
        super(size.buildTank(true, false));
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new MachineComponent.FluidHatch(MachineComponent.IOType.INPUT) {
            @Override
            public IFluidHandler getTank() {
                return TileFluidInputHatch.this.tank;
            }
        };
    }
}
