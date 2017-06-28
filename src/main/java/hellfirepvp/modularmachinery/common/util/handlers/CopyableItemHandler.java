/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.handlers;

import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CopyableItemHandler
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:20
 */
public interface CopyableItemHandler extends IItemHandlerModifiable {

    public CopyableItemHandler copy();

}
