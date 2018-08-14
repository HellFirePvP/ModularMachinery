/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.item.*;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;
import java.util.List;

import static hellfirepvp.modularmachinery.common.lib.ItemsMM.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryItems
 * Created by HellFirePvP
 * Date: 28.06.2017 / 18:40
 */
public class RegistryItems {

    static List<Item> itemsToRegister = Lists.newArrayList();
    public static List<ItemDynamicColor> pendingDynamicColorItems = new LinkedList<>();

    public static void initialize() {
        blueprint = prepareRegister(new ItemBlueprint());
        modularium = prepareRegister(new ItemModularium());
        constructTool = prepareRegister(new ItemConstructTool());

        registerItemModels();
    }

    private static <T extends Item> T prepareRegister(T item) {
        String name = item.getClass().getSimpleName().toLowerCase();
        item.setRegistryName(name).setUnlocalizedName(ModularMachinery.MODID + '.' + name);
        itemsToRegister.add(item);
        if(item instanceof ItemDynamicColor) {
            pendingDynamicColorItems.add((ItemDynamicColor) item);
        }
        return item;
    }

    public static void register(IForgeRegistry<Item> registry) {
        for (Item i : itemsToRegister) {
            registry.register(i);
        }
    }

    private static void registerItemModels() {
        itemsToRegister.stream().filter(i -> !(i instanceof ItemBlockCustomName)).forEach(ModularMachinery.proxy::registerItemModel);
    }

}
