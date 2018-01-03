/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.command;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.network.PktCopyToClipboard;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonSerializer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandHand
 * Created by HellFirePvP
 * Date: 19.08.2017 / 13:13
 */
public class CommandHand extends CommandBase {

    @Override
    public String getName() {
        return "mm-hand";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.modularmachinery.hand";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack held = player.getHeldItemMainhand();
        if(held.isEmpty()) {
            held = player.getHeldItemOffhand();
        }
        if(held.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("command.modularmachinery.hand.empty"));
            return;
        }
        Item i = held.getItem();
        StringBuilder sb = new StringBuilder();
        sb.append(i.getRegistryName().toString());
        if(i.getHasSubtypes()) {
            sb.append("@").append(held.getItemDamage());
        }
        NBTTagCompound cmp = held.serializeNBT();
        if(cmp.hasKey("tag")) {
            String json = NBTJsonSerializer.serializeNBT(cmp.getTag("tag"));
            if(!json.isEmpty()) {
                sb.append(" (with nbt: ").append(json).append(" )");
            }
        }

        String str = sb.toString();
        player.sendMessage(new TextComponentString(str));
        ModularMachinery.NET_CHANNEL.sendTo(new PktCopyToClipboard(str), player);

        int burn = TileEntityFurnace.getItemBurnTime(held);
        if(burn > 0) {
            player.sendMessage(new TextComponentString("Fuel BurnTime: " + burn));
        }
    }

}
