/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.*;
import hellfirepvp.modularmachinery.client.util.BlockArrayPreviewRenderHelper;
import hellfirepvp.modularmachinery.client.util.DebugOverlayHelper;
import hellfirepvp.modularmachinery.client.util.SelectionBoxRenderHelper;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockDynamicColor;
import hellfirepvp.modularmachinery.common.block.BlockVariants;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.registry.RegistryBlocks;
import hellfirepvp.modularmachinery.common.registry.RegistryItems;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientProxy
 * Created by HellFirePvP
 * Date: 26.06.2017 / 21:01
 */
public class ClientProxy extends CommonProxy {

    public static ClientScheduler clientScheduler = new ClientScheduler();
    public static BlockArrayPreviewRenderHelper renderHelper = new BlockArrayPreviewRenderHelper();

    private List<Block> blockModelsToRegister = Lists.newLinkedList();
    private List<Item> itemModelsToRegister = Lists.newLinkedList();

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(clientScheduler);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DebugOverlayHelper());
        MinecraftForge.EVENT_BUS.register(new SelectionBoxRenderHelper());
        if(Mods.JEI.isPresent()) {
            registerJEIEventHandler();
        }

        super.preInit();
    }

    @Optional.Method(modid = "jei")
    private void registerJEIEventHandler() {
        MinecraftForge.EVENT_BUS.register(new ClientMouseJEIGuiEventHandler());
    }

    @SubscribeEvent
    public void onModelRegister(ModelRegistryEvent event) {
        registerModels();
    }

    private void registerModels() {
        for (Block block : blockModelsToRegister) {
            Item i = Item.getItemFromBlock(block);
            if(block instanceof BlockVariants) {
                for (IBlockState state : ((BlockVariants) block).getValidStates()) {
                    String unlocName = block.getClass().getSimpleName().toLowerCase();
                    String name = unlocName + "_" + ((BlockVariants) block).getBlockStateName(state);
                    ModelBakery.registerItemVariants(i, new ResourceLocation(ModularMachinery.MODID, name));
                    ModelLoader.setCustomModelResourceLocation(i, block.getMetaFromState(state),
                            new ModelResourceLocation(ModularMachinery.MODID + ":" + name, "inventory"));
                }
            } else {
                ModelBakery.registerItemVariants(i, new ResourceLocation(ModularMachinery.MODID, block.getClass().getSimpleName().toLowerCase()));
                ModelLoader.setCustomModelResourceLocation(i, 0,
                        new ModelResourceLocation(ModularMachinery.MODID + ":" + block.getClass().getSimpleName().toLowerCase(), "inventory"));
            }
        }
        for (Item item : itemModelsToRegister) {
            String name = item.getClass().getSimpleName().toLowerCase();
            if(item instanceof ItemBlock) {
                name = ((ItemBlock) item).getBlock().getClass().getSimpleName().toLowerCase();
            }
            NonNullList<ItemStack> list = NonNullList.create();
            item.getSubItems(item.getCreativeTab(), list);
            if (list.size() > 0) {
                for (ItemStack i : list) {
                    ModelLoader.setCustomModelResourceLocation(item, i.getItemDamage(),
                            new ModelResourceLocation(ModularMachinery.MODID + ":" + name, "inventory"));
                }
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                        new ModelResourceLocation(ModularMachinery.MODID + ":" + name, "inventory"));
            }
        }
    }

    private void registerPendingIBlockColorBlocks() {
        BlockColors colors = Minecraft.getMinecraft().getBlockColors();
        for (BlockDynamicColor b : RegistryBlocks.pendingIBlockColorBlocks) {
            colors.registerBlockColorHandler(b::getColorMultiplier, (Block) b);
        }
    }

    private void registerPendingIItemColorItems() {
        ItemColors colors = Minecraft.getMinecraft().getItemColors();
        for (ItemDynamicColor i : RegistryItems.pendingDynamicColorItems) {
            colors.registerItemColorHandler(i::getColorFromItemstack, (Item) i);
        }
    }

    @Override
    public void init() {
        super.init();

        registerPendingIBlockColorBlocks();
        registerPendingIItemColorItems();
    }

    @Override
    public void postInit() {
        super.postInit();
    }

    @Override
    public void registerBlockModel(Block block) {
        blockModelsToRegister.add(block);
    }

    @Override
    public void registerItemModel(Item item) {
        itemModelsToRegister.add(item);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
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
                return new GuiMachineController((TileMachineController) present, player);
            case BUS_INVENTORY:
                return new GuiContainerItemBus((TileItemBus) present, player);
            case TANK_INVENTORY:
                return new GuiContainerFluidHatch((TileFluidTank) present, player);
            case ENERGY_INVENTORY:
                return new GuiContainerEnergyHatch((TileEnergyHatch) present, player);
            case BLUEPRINT_PREVIEW:
                ItemStack stack;
                if(x == 0) {
                    stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
                } else {
                    stack = Minecraft.getMinecraft().player.getHeldItemOffhand();
                }
                DynamicMachine machine = ItemBlueprint.getAssociatedMachine(stack);
                if(machine != null) {
                    return new GuiScreenBlueprint(machine);
                }
                break;
        }
        return null;
    }
}
