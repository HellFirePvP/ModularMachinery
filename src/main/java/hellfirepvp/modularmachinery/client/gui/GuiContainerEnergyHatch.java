/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.container.ContainerEnergyHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiContainerEnergyHatch
 * Created by HellFirePvP
 * Date: 09.07.2017 / 14:26
 */
public class GuiContainerEnergyHatch extends GuiContainerBase<ContainerEnergyHatch> {

    public static final ResourceLocation TEXTURES_ENERGY_HATCH = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guibar.png");

    private TileEnergyHatch energyHatch;

    public GuiContainerEnergyHatch(TileEnergyHatch tileFluidTank, EntityPlayer opening) {
        super(new ContainerEnergyHatch(tileFluidTank, opening));
        this.energyHatch = tileFluidTank;
    }

    @Override
    protected void setWidthHeight() {}

    @Override
    protected void renderHoveredToolTip(int x, int z) {
        super.renderHoveredToolTip(x, z);

        int offsetX = (this.width - this.xSize) / 2;
        int offsetZ = (this.height - this.ySize) / 2;

        if(x >= 15 + offsetX && x <= 35 + offsetX && z >= 10 + offsetZ && z <= 71 + offsetZ) {
            long currentEnergy = EnergyDisplayUtil.type.formatEnergyForDisplay(energyHatch.getCurrentEnergy());
            long maxEnergy = EnergyDisplayUtil.type.formatEnergyForDisplay(energyHatch.getMaxEnergy());

            List<String> text = Lists.newArrayList();
            text.add(I18n.format("tooltip.energyhatch.charge",
                    String.valueOf(currentEnergy),
                    String.valueOf(maxEnergy),
                    I18n.format(EnergyDisplayUtil.type.getUnlocalizedFormat())));

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            drawHoveringText(text, x, z, (font == null ? fontRenderer : font));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        float percFilled = ((float) energyHatch.getCurrentEnergy()) / ((float) energyHatch.getMaxEnergy());
        int pxFilled = MathHelper.ceil(percFilled * 61F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_ENERGY_HATCH);
        this.drawTexturedModalRect(15, 10 + 61 - pxFilled, 196, 61 - pxFilled, 20, pxFilled);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_ENERGY_HATCH);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

}
