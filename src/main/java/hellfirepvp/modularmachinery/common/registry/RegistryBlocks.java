/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.block.*;
import hellfirepvp.modularmachinery.common.item.ItemBlockCustomName;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

import static hellfirepvp.modularmachinery.common.lib.BlocksMM.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryBlocks
 * Created by HellFirePvP
 * Date: 28.06.2017 / 20:22
 */
public class RegistryBlocks {

    static List<Block> blocksToRegister = Lists.newArrayList();

    public static void initialize() {
        blockController = prepareRegister(new BlockController());
        prepareItemBlockRegister(blockController);

        itemInputBus = prepareRegister(new BlockInputBus());
        prepareItemBlockRegister(itemInputBus);
        itemOutputBus = prepareRegister(new BlockOutputBus());
        prepareItemBlockRegister(itemOutputBus);
        fluidInputHatch = prepareRegister(new BlockFluidInputHatch());
        prepareItemBlockRegister(fluidInputHatch);
        fluidOutputHatch = prepareRegister(new BlockFluidOutputHatch());
        prepareItemBlockRegister(fluidOutputHatch);
    }

    private static void prepareItemBlockRegister(Block block) {
        prepareItemBlockRegister(new ItemBlockCustomName(block));
    }

    private static <T extends Item> T prepareItemBlockRegister(T item) {
        String name = item.getClass().getSimpleName().toLowerCase();
        item.setRegistryName(name).setUnlocalizedName(name);
        RegistryItems.itemsToRegister.add(item);
        return item;
    }

    private static <T extends Block> T prepareRegister(T block) {
        String name = block.getClass().getSimpleName().toLowerCase();
        block.setRegistryName(name).setUnlocalizedName(name);
        blocksToRegister.add(block);
        return block;
    }

    public static void register(IForgeRegistry<Block> registry) {
        for (Block i : blocksToRegister) {
            registry.register(i);
        }
    }

}
