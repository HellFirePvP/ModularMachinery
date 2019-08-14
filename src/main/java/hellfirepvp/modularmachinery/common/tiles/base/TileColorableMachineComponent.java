/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.data.Config;
import net.minecraft.nbt.NBTTagCompound;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileColorableMachineComponent
 * Created by HellFirePvP
 * Date: 15.08.2017 / 16:20
 */
public class TileColorableMachineComponent extends TileEntitySynchronized implements ColorableMachineTile {

    private int definedColor = Config.machineColor;

    @Override
    public void setMachineColor(int newColor) {
        this.definedColor = newColor;
        this.markForUpdate();
    }

    @Override
    public int getMachineColor() {
        return this.definedColor;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if(!compound.hasKey("casingColor")) {
            definedColor = Config.machineColor;
        } else {
            definedColor = compound.getInteger("casingColor");
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("casingColor", this.definedColor);
    }
}
