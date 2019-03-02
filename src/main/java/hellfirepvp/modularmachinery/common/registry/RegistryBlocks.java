/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.*;
import hellfirepvp.modularmachinery.common.item.ItemBlockCustomName;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponentCustomName;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import hellfirepvp.modularmachinery.common.tiles.*;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;
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
    public static List<BlockDynamicColor> pendingIBlockColorBlocks = new LinkedList<>();

    public static void initialize() {
        registerBlocks();

        registerTiles();

        registerBlockModels();
    }

    private static void registerBlocks() {
        blockController = prepareRegister(new BlockController());
        prepareItemBlockRegister(blockController);

        blockCasing = prepareRegister(new BlockCasing());
        prepareItemBlockRegister(blockCasing);

        itemInputBus = prepareRegister(new BlockInputBus());
        prepareItemBlockRegister(itemInputBus);
        itemOutputBus = prepareRegister(new BlockOutputBus());
        prepareItemBlockRegister(itemOutputBus);
        fluidInputHatch = prepareRegister(new BlockFluidInputHatch());
        prepareItemBlockRegister(fluidInputHatch);
        fluidOutputHatch = prepareRegister(new BlockFluidOutputHatch());
        prepareItemBlockRegister(fluidOutputHatch);
        energyInputHatch = prepareRegister(new BlockEnergyInputHatch());
        prepareItemBlockRegister(energyInputHatch);
        energyOutputHatch = prepareRegister(new BlockEnergyOutputHatch());
        prepareItemBlockRegister(energyOutputHatch);
    }

    private static void registerTiles() {
        registerTile(TileColorableMachineComponent.class);

        registerTile(TileMachineController.class);

        registerTile(TileFluidInputHatch.class);
        registerTile(TileFluidOutputHatch.class);
        registerTile(TileItemOutputBus.class);
        registerTile(TileItemInputBus.class);
        registerTile(TileEnergyInputHatch.class);
        registerTile(TileEnergyOutputHatch.class);
    }

    private static void registerBlockModels() {
        for (Block block : blocksToRegister) {
            ModularMachinery.proxy.registerBlockModel(block);
        }
    }

    private static void registerTile(Class<? extends TileEntity> tile, String name) {
        GameRegistry.registerTileEntity(tile, name);
    }

    private static void registerTile(Class<? extends TileEntity> tile) {
        registerTile(tile, tile.getSimpleName().toLowerCase());
    }

    private static void prepareItemBlockRegister(Block block) {
        if(block instanceof BlockMachineComponent) {
            if(block instanceof BlockCustomName) {
                prepareItemBlockRegister(new ItemBlockMachineComponentCustomName(block));
            } else {
                prepareItemBlockRegister(new ItemBlockMachineComponent(block));
            }
        } else {
            if(block instanceof BlockCustomName) {
                prepareItemBlockRegister(new ItemBlockCustomName(block));
            } else {
                prepareItemBlockRegister(new ItemBlock(block));
            }
        }
    }

    private static <T extends ItemBlock> T prepareItemBlockRegister(T item) {
        String name = item.getBlock().getClass().getSimpleName().toLowerCase();
        item.setRegistryName(name).setUnlocalizedName(ModularMachinery.MODID + '.' + name);
        RegistryItems.itemsToRegister.add(item);
        if(item instanceof ItemDynamicColor) {
            RegistryItems.pendingDynamicColorItems.add((ItemDynamicColor) item);
        }
        return item;
    }

    private static <T extends Block> T prepareRegister(T block) {
        String name = block.getClass().getSimpleName().toLowerCase();
        block.setRegistryName(name).setUnlocalizedName(ModularMachinery.MODID + '.' + name);
        blocksToRegister.add(block);
        if(block instanceof BlockDynamicColor) {
            pendingIBlockColorBlocks.add((BlockDynamicColor) block);
        }
        return block;
    }

    public static void register(IForgeRegistry<Block> registry) {
        for (Block i : blocksToRegister) {
            registry.register(i);
        }
    }

}
