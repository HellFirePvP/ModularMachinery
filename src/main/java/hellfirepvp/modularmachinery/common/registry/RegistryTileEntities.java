/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryTileEntities
 * Created by HellFirePvP
 * Date: 28.06.2017 / 17:51
 */
public class RegistryTileEntities {

    public static void register() {
        registerTile(TileMachineController.class);
    }

    private static void registerTile(Class<? extends TileEntity> tileClass) {
        GameRegistry.registerTileEntity(tileClass, tileClass.getSimpleName().toLowerCase());
    }

}
