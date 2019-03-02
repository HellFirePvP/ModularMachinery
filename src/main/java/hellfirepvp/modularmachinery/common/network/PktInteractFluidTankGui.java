/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.client.gui.GuiContainerFluidHatch;
import hellfirepvp.modularmachinery.common.container.ContainerFluidHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktInteractFluidTankGui
 * Created by HellFirePvP
 * Date: 02.03.2019 / 14:20
 */
public class PktInteractFluidTankGui implements IMessage, IMessageHandler<PktInteractFluidTankGui, IMessage> {

    private ItemStack held;

    public PktInteractFluidTankGui() {
        this(ItemStack.EMPTY);
    }

    public PktInteractFluidTankGui(ItemStack held) {
        this.held = held;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.held = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.held);
    }

    @Override
    public IMessage onMessage(PktInteractFluidTankGui pkt, MessageContext ctx) {
        if (ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                if (player.openContainer instanceof ContainerFluidHatch) {
                    TileFluidTank hatch = ((ContainerFluidHatch) player.openContainer).getOwner();
                    ItemStack holding = player.inventory.getItemStack();
                    if (!holding.isEmpty()) {
                        FluidActionResult fas = FluidUtil.tryEmptyContainer(holding, hatch.getTank(), Fluid.BUCKET_VOLUME, player, true);
                        if (fas.isSuccess()) {
                            player.inventory.setItemStack(fas.getResult());
                        }
                    }
                }
            });
        } else {
            updateClientHand(pkt);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private void updateClientHand(PktInteractFluidTankGui pkt) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;

        if (gui instanceof GuiContainerFluidHatch) {
            EntityPlayer player = Minecraft.getMinecraft().player;

            if (!player.inventory.getItemStack().isEmpty()) { //Has to have had an item before... obviously.. right?
                player.inventory.setItemStack(pkt.held);
            }
        }
    }
}
