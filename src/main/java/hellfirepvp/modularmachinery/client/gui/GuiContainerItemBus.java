/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.container.ContainerItemBus;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiContainerItemBus
 * Created by HellFirePvP
 * Date: 09.07.2017 / 18:42
 */
public class GuiContainerItemBus extends GuiContainerBase<ContainerItemBus> {

    public GuiContainerItemBus(TileItemBus itemBus, EntityPlayer opening) {
        super(new ContainerItemBus(itemBus, opening));
    }

    private ResourceLocation getTextureInventory() {
        ItemBusSize size = this.container.getOwner().getSize();
        return new ResourceLocation(ModularMachinery.MODID, "textures/gui/inventory_" + size.name().toLowerCase() + ".png");
    }

    @Override
    protected void setWidthHeight() {}

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(getTextureInventory());
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
