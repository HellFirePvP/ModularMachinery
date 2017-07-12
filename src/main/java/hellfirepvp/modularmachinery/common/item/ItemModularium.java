/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.CommonProxy;
import net.minecraft.item.Item;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemModularium
 * Created by HellFirePvP
 * Date: 12.07.2017 / 15:37
 */
public class ItemModularium extends Item {

    public ItemModularium() {
        setMaxStackSize(64);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

}
