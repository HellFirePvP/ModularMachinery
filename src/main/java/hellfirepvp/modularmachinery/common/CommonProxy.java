/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerController;
import hellfirepvp.modularmachinery.common.container.ContainerEnergyHatch;
import hellfirepvp.modularmachinery.common.container.ContainerFluidHatch;
import hellfirepvp.modularmachinery.common.container.ContainerItemBus;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapterRegistry;
import hellfirepvp.modularmachinery.common.data.ModDataHolder;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.registry.RegistrationBus;
import hellfirepvp.modularmachinery.common.registry.RegistryBlocks;
import hellfirepvp.modularmachinery.common.registry.RegistryItems;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.io.File;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommonProxy
 * Created by HellFirePvP
 * Date: 26.06.2017 / 21:00
 */
public class CommonProxy implements IGuiHandler {

    public static CreativeTabs creativeTabModularMachinery;
    public static ModDataHolder dataHolder = new ModDataHolder();

    public void loadModData(File configDir) {
        dataHolder.setup(configDir);
        if(dataHolder.requiresDefaultMachinery()) {
            dataHolder.copyDefaultMachinery();
        }
    }

    public void preInit() {
        creativeTabModularMachinery = new CreativeTabs(ModularMachinery.MODID) {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(BlocksMM.blockController);
            }
        };

        MachineRegistry.getRegistry().buildRegistry();
        RecipeRegistry.getRegistry().buildRegistry();
        MinecraftForge.EVENT_BUS.register(new RegistrationBus());

        RegistryBlocks.initialize();
        RegistryItems.initialize();

        NetworkRegistry.INSTANCE.registerGuiHandler(ModularMachinery.MODID, this);
    }

    public void init() {
        RecipeAdapterRegistry.initDefaultAdapters();

        MachineRegistry.getRegistry().initializeAndLoad();
        RecipeRegistry.getRegistry().initializeAndLoad();
    }

    public void postInit() {}

    public void registerBlockModel(Block block) {}

    public void registerItemModel(Item item) {}

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiType type = GuiType.values()[MathHelper.clamp(ID, 0, GuiType.values().length - 1)];
        Class<? extends TileEntity> required = type.requiredTileEntity;
        TileEntity present = null;
        if(required != null) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if(te != null && required.isAssignableFrom(te.getClass())) {
                present = te;
            } else {
                return null;
            }
        }
        switch (type) {
            case CONTROLLER:
                return new ContainerController((TileMachineController) present, player);
            case BUS_INVENTORY:
                return new ContainerItemBus((TileItemBus) present, player);
            case TANK_INVENTORY:
                return new ContainerFluidHatch((TileFluidTank) present, player);
            case ENERGY_INVENTORY:
                return new ContainerEnergyHatch((TileEnergyHatch) present, player);
            case BLUEPRINT_PREVIEW:
                break;
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public static enum GuiType {

        CONTROLLER(TileMachineController.class),
        BUS_INVENTORY(TileItemBus.class),
        TANK_INVENTORY(TileFluidTank.class),
        ENERGY_INVENTORY(TileEnergyHatch.class),
        BLUEPRINT_PREVIEW(null);

        public final Class<? extends TileEntity> requiredTileEntity;

        private GuiType(@Nullable Class<? extends TileEntity> requiredTileEntity) {
            this.requiredTileEntity = requiredTileEntity;
        }
    }

}
