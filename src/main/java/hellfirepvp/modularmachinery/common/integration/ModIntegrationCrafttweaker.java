/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration;

import crafttweaker.mc1120.events.ScriptRunEvent;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModIntegrationCrafttweaker
 * Created by HellFirePvP
 * Date: 18.08.2017 / 10:44
 */
public class ModIntegrationCrafttweaker {

    @SubscribeEvent
    public void onScriptsReloading(ScriptRunEvent.Pre event) {
        RecipeRegistry.getRegistry().clearLingeringRecipes();
    }

    @SubscribeEvent
    public void onScriptsReloaded(ScriptRunEvent.Post event) {
        RecipeRegistry.getRegistry().reloadAdapters();
    }

}
