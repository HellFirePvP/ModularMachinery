/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlockMachineComponent
 * Created by HellFirePvP
 * Date: 15.08.2017 / 16:31
 */
public class ItemBlockMachineComponent extends ItemBlock implements ItemDynamicColor {

    public ItemBlockMachineComponent(Block block) {
        super(block);
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        if(stack.isEmpty()) {
            return 0;
        }
        return Config.machineColor;
    }

}
