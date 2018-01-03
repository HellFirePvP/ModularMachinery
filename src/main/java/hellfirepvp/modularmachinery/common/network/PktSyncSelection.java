/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktSyncSelection
 * Created by HellFirePvP
 * Date: 22.08.2017 / 23:38
 */
public class PktSyncSelection implements IMessage, IMessageHandler<PktSyncSelection, IMessage> {

    private List<BlockPos> positions = new ArrayList<>();

    public PktSyncSelection() {}

    public PktSyncSelection(List<BlockPos> positions) {
        this.positions = positions;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        this.positions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            this.positions.add(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(positions.size());
        for (BlockPos pos : this.positions) {
            buf.writeInt(pos.getX());
            buf.writeInt(pos.getY());
            buf.writeInt(pos.getZ());
        }
    }

    @Override
    public IMessage onMessage(PktSyncSelection message, MessageContext ctx) {
        PlayerStructureSelectionHelper.clientSelection = new PlayerStructureSelectionHelper.StructureSelection(message.positions);
        return null;
    }

}
