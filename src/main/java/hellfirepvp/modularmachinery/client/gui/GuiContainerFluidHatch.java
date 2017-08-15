/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerFluidHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiContainerFluidHatch
 * Created by HellFirePvP
 * Date: 09.07.2017 / 11:27
 */
public class GuiContainerFluidHatch extends GuiContainerBase<ContainerFluidHatch> {

    public static final ResourceLocation TEXTURES_FLUID_HATCH = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guibar.png");

    private TileFluidTank tank;

    public GuiContainerFluidHatch(TileFluidTank tileFluidTank, EntityPlayer opening) {
        super(new ContainerFluidHatch(tileFluidTank, opening));
        this.tank = tileFluidTank;
    }

    @Override
    protected void setWidthHeight() {}

    @Override
    protected void renderHoveredToolTip(int x, int z) {
        super.renderHoveredToolTip(x, z);

        int offsetX = (this.width - this.xSize) / 2;
        int offsetZ = (this.height - this.ySize) / 2;

        if(x >= 15 + offsetX && x <= 35 + offsetX && z >= 10 + offsetZ && z <= 71 + offsetZ) {
            List<String> text = Lists.newArrayList();

            FluidStack content = tank.getTank().getFluid();
            int amt;
            if(content == null || content.amount <= 0) {
                text.add(I18n.format("tooltip.fluidhatch.empty"));
                amt = 0;
            } else {
                text.add(content.getLocalizedName());
                amt = content.amount;
            }
            text.add(I18n.format("tooltip.fluidhatch.tank", String.valueOf(amt), String.valueOf(tank.getTank().getCapacity())));

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            drawHoveringText(text, x, z, (font == null ? fontRenderer : font));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        FluidStack content = tank.getTank().getFluid();
        if(content != null && content.amount > 0) {
            int fluidColor = content.getFluid().getColor(content);
            float red   = (fluidColor >> 16 & 0xFF) / 255F;
            float green = (fluidColor >>  8 & 0xFF) / 255F;
            float blue  = (fluidColor       & 0xFF) / 255F;

            float percFilled = ((float) content.amount) / ((float) tank.getTank().getCapacity());
            int pxFilled = MathHelper.ceil(percFilled * 61F);
            GlStateManager.color(red, green, blue, 1.0F);
            ResourceLocation rl = content.getFluid().getStill(content);
            TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(rl.toString());
            if(tas == null) {
                Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
            }
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            drawTexturedModalRect(15, 10 + 61 - pxFilled, tas, 20, pxFilled);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_FLUID_HATCH);
        this.drawTexturedModalRect(15, 10, 176, 0, 20, 61);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_FLUID_HATCH);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

}
