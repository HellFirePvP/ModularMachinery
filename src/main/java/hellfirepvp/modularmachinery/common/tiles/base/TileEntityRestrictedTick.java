/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import net.minecraft.util.ITickable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileEntityRestrictedTick
 * Created by HellFirePvP
 * Date: 28.06.2017 / 17:57
 */
public abstract class TileEntityRestrictedTick extends TileColorableMachineComponent implements ITickable {

    private long lastUpdateWorldTick = -1;
    protected int ticksExisted = 0;

    @Override
    public final void update() {
        long currentTick = getWorld().getTotalWorldTime();
        if(lastUpdateWorldTick == currentTick) {
            return;
        }
        lastUpdateWorldTick = currentTick;
        doRestrictedTick();
        ticksExisted++;
    }

    public abstract void doRestrictedTick();
}
