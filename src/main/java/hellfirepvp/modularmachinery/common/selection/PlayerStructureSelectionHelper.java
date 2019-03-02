/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.selection;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.network.PktSyncSelection;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: PlayerStructureSelectionHelper
 * Created by HellFirePvP
 * Date: 22.08.2017 / 23:27
 */
public class PlayerStructureSelectionHelper {

    public static StructureSelection clientSelection = null;
    private static Map<UUID, StructureSelection> activeSelectionMap = new HashMap<>();

    public static void toggleInSelection(EntityPlayer player, BlockPos pos) {
        activeSelectionMap.computeIfAbsent(player.getUniqueID(), uuid -> new StructureSelection()).togglePosition(pos);
    }

    public static void purgeSelection(EntityPlayer player) {
        if(player == null) {
            return;
        }
        activeSelectionMap.remove(player.getUniqueID());
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
        INetHandlerPlayServer handlerServer = event.getHandler();
        if(handlerServer instanceof NetHandlerPlayServer) {
            purgeSelection(((NetHandlerPlayServer) handlerServer).player);
        }
    }

    public static void sendSelection(EntityPlayer player) {
        if(player instanceof EntityPlayerMP) {
            ModularMachinery.NET_CHANNEL.sendTo(
                    new PktSyncSelection(activeSelectionMap.computeIfAbsent(player.getUniqueID(), uuid -> new StructureSelection()).getSelectedPositions()),
                    (EntityPlayerMP) player);
        }
    }

    public static void finalizeSelection(EnumFacing controllerFacing, World world, BlockPos pos, EntityPlayer player) {
        StructureSelection sel = activeSelectionMap.get(player.getUniqueID());
        if(sel == null || sel.selectedPositions.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("message.structurebuild.empty"));
            return;
        }
        player.sendMessage(new TextComponentTranslation("message.structurebuild.confirmrotation", controllerFacing.getName()));
        BlockArray out = sel.compressAsArray(world, pos);
        if(controllerFacing != EnumFacing.NORTH) {
            int rotation = 0;
            EnumFacing face = controllerFacing;
            while (face != EnumFacing.NORTH) {
                face = face.rotateYCCW();
                rotation += 90;
                out = out.rotateYCCW();
            }
            player.sendMessage(new TextComponentTranslation("message.structurebuild.confirmrotation.rotating", String.valueOf(rotation)));
        }

        if(FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
            String serializedArray = out.serializeAsMachineJson();
            MinecraftServer ms = FMLCommonHandler.instance().getMinecraftServerInstance();
            if(ms.isDedicatedServer()) {
                player.sendMessage(new TextComponentTranslation("message.structurebuild.warndedicated"));
            }

            String timestampAppend = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String fileName = "machine-" + player.getName() + "-" + timestampAppend;

            File directory = CommonProxy.dataHolder.getMachineryDirectory();
            File machineOut = new File(directory, fileName + ".json");
            int increment = 0;
            while (machineOut.exists()) {
                machineOut = new File(directory, fileName + " (" + increment + ").json");
                increment++;
            }

            try {
                Files.write(serializedArray, machineOut, Charset.forName("UTF-8"));
                player.sendMessage(new TextComponentTranslation("message.structurebuild.save", machineOut.getName()));
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentTranslation("message.structurebuild.fail"));

                if(machineOut.exists()) {
                    machineOut.delete(); //Cleanup erroring/incomplete files.
                }
            }
        }
    }

    public static class StructureSelection {

        private List<BlockPos> selectedPositions = new LinkedList<>();

        private StructureSelection() {}

        public StructureSelection(List<BlockPos> selectedPositions) {
            this.selectedPositions = selectedPositions;
        }

        public List<BlockPos> getSelectedPositions() {
            return new ArrayList<>(selectedPositions);
        }

        private void togglePosition(BlockPos pos) {
            if(this.selectedPositions.contains(pos)) {
                this.selectedPositions.remove(pos);
            } else {
                this.selectedPositions.add(pos);
            }
        }

        private BlockArray compressAsArray(World world, BlockPos center) {
            BlockArray out = new BlockArray();
            for (BlockPos pos : this.selectedPositions) {
                IBlockState state = world.getBlockState(pos);
                BlockArray.IBlockStateDescriptor descr = new BlockArray.IBlockStateDescriptor(state);
                BlockArray.BlockInformation bi = new BlockArray.BlockInformation(Lists.newArrayList(descr));
                TileEntity te = world.getTileEntity(pos);
                if(te != null) {
                    NBTTagCompound cmp = new NBTTagCompound();
                    te.writeToNBT(cmp);

                    cmp.removeTag("x");
                    cmp.removeTag("y");
                    cmp.removeTag("z");

                    bi.setMatchingTag(cmp);
                }
                out.addBlock(pos.subtract(center), bi);
            }
            return out;
        }

    }

}
