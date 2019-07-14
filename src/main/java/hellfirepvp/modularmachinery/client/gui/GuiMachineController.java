/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.Animation;

import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiMachineController
 * Created by HellFirePvP
 * Date: 12.07.2017 / 23:34
 */
public class GuiMachineController extends GuiContainerBase<ContainerController> {

    public static final ResourceLocation TEXTURES_CONTROLLER = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guicontroller.png");

    private TileMachineController controller;

    public GuiMachineController(TileMachineController controller, EntityPlayer opening) {
        super(new ContainerController(controller, opening));
        this.controller = controller;
    }

    @Override
    protected void setWidthHeight() {}

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        GlStateManager.pushMatrix();
        double scale = 0.72;
        GlStateManager.scale(scale, scale, scale);
        int offsetX = 12;
        int offsetY = 12;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        FontRenderer fr = this.fontRenderer;

        int redstone = controller.getWorld().getStrongPower(controller.getPos());
        if(redstone > 0) {
            String drawnStop = I18n.format("gui.controller.status.redstone_stopped");
            List<String> out = fr.listFormattedStringToWidth(drawnStop, MathHelper.floor(135 * (1 / scale)));
            for (String draw : out) {
                offsetY += 10;
                fr.drawString(draw, offsetX, offsetY, 0xFFFFFF);
                offsetY += 10;
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }

        DynamicMachine machine = controller.getBlueprintMachine();
        if(machine != null) {
            String drawnHead = I18n.format("gui.controller.blueprint", "");
            List<String> out = fr.listFormattedStringToWidth(machine.getLocalizedName(), MathHelper.floor(135 * (1 / scale)));
            fr.drawString(drawnHead, offsetX, offsetY, 0xFFFFFF);
            for (String draw : out) {
                offsetY += 10;
                fr.drawString(draw, offsetX, offsetY, 0xFFFFFF);
            }
            offsetY += 15;
        } else {
            String drawnHead = I18n.format("gui.controller.blueprint", I18n.format("gui.controller.blueprint.none"));
            fr.drawString(drawnHead, offsetX, offsetY, 0xFFFFFF);
            offsetY += 15;
        }

        DynamicMachine found = controller.getFoundMachine();
        if(found != null) {
            String drawnHead = I18n.format("gui.controller.structure", "");
            List<String> out = fr.listFormattedStringToWidth(found.getLocalizedName(), MathHelper.floor(135 * (1 / scale)));
            fr.drawString(drawnHead, offsetX, offsetY, 0xFFFFFF);
            for (String draw : out) {
                offsetY += 10;
                fr.drawString(draw, offsetX, offsetY, 0xFFFFFF);
            }
            offsetY += 15;
        } else {
            String drawnHead = I18n.format("gui.controller.structure", I18n.format("gui.controller.structure.none"));
            fr.drawString(drawnHead, offsetX, offsetY, 0xFFFFFF);
            offsetY += 15;
        }

        String status = I18n.format("gui.controller.status");
        fr.drawString(status, offsetX, offsetY, 0xFFFFFF);
        String statusKey = controller.getCraftingStatus().getUnlocMessage();

        List<String> out = fr.listFormattedStringToWidth(I18n.format(statusKey), MathHelper.floor(135 * (1 / scale)));
        for (String draw : out) {
            offsetY += 10;
            fr.drawString(draw, offsetX, offsetY, 0xFFFFFF);
        }
        offsetY += 15;
        if (controller.hasActiveRecipe()) {
            int percProgress = MathHelper.floor(controller.getCurrentActiveRecipeProgress(Animation.getPartialTickTime()) * 100F);
            percProgress = MathHelper.clamp(percProgress, 0, 100);
            String progressStr = I18n.format("gui.controller.status.crafting.progress", percProgress + "%");
            fr.drawString(progressStr, offsetX, offsetY, 0xFFFFFF);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_CONTROLLER);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
