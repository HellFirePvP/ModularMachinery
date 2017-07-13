/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.util.DynamicMachineRenderContext;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiScreenBlueprint
 * Created by HellFirePvP
 * Date: 09.07.2017 / 21:08
 */
public class GuiScreenBlueprint extends GuiScreen {

    public static final ResourceLocation TEXTURE_BACKGROUND = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiblueprint.png");

    protected int guiLeft;
    protected int guiTop;
    protected int xSize = 176;
    protected int ySize = 144;

    private final DynamicMachine machine;
    private final DynamicMachineRenderContext renderContext;
    private int frameCount = 0;

    public GuiScreenBlueprint(DynamicMachine machine) {
        this.machine = machine;
        this.renderContext = DynamicMachineRenderContext.createContext(this.machine);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        frameCount++;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE_BACKGROUND);
        int x = (this.width - this.xSize) / 2;
        int z = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, z, 0, 0, this.xSize, this.ySize);

        if(renderContext.doesRenderIn3D()) {
            if (Mouse.isButtonDown(0) && frameCount > 20) {
                renderContext.rotateRender(0.25 * Mouse.getDY(), 0.25 * Mouse.getDX(), 0);
            }
        } else {
            if (Mouse.isButtonDown(0) && frameCount > 20) {
                renderContext.moveRender(0.25 * Mouse.getDX(), 0, -0.25 * Mouse.getDY());
            }
        }
        int dwheel = Mouse.getDWheel();
        if(dwheel < 0) {
            renderContext.zoomOut();
        } else if(dwheel > 0) {
            renderContext.zoomIn();
        }

        ScaledResolution res = new ScaledResolution(mc);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((guiLeft + 8) * res.getScaleFactor(), (guiTop + 43) * res.getScaleFactor(), 160 * res.getScaleFactor(), 94 * res.getScaleFactor());
        x = 88;
        z = 66;
        renderContext.renderAt(this.guiLeft + x, this.guiTop + z, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawButtons(mouseX, mouseY);

        GlStateManager.disableDepth();
        fontRenderer.drawStringWithShadow(machine.getLocalizedName(), this.guiLeft + 10, this.guiTop + 11, 0xFFFFFFFF);
        if(machine.requiresBlueprint()) {
            String reqBlueprint = I18n.format("tooltip.machinery.blueprint.required");
            fontRenderer.drawString(reqBlueprint, this.guiLeft + 10, this.guiTop + 106, 0x444444);
        }
        GlStateManager.enableDepth();
    }

    private void drawButtons(int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE_BACKGROUND);

        int add = 0;
        if(!renderContext.doesRenderIn3D()) {
            if(mouseX >= this.guiLeft + 132 && mouseX <= this.guiLeft + 132 + 16 &&
                    mouseY >= this.guiTop + 106 && mouseY <= this.guiTop + 106 + 16) {
                add = 16;
            }
        } else {
            add = 32;
        }
        this.drawTexturedModalRect(guiLeft + 132, guiTop + 106, 176 + add, 16, 16, 16);


        add = 0;
        if(renderContext.doesRenderIn3D()) {
            if(mouseX >= this.guiLeft + 132 && mouseX <= this.guiLeft + 132 + 16 &&
                    mouseY >= this.guiTop + 122 && mouseY <= this.guiTop + 122 + 16) {
                add = 16;
            }
        } else {
            add = 32;
        }
        this.drawTexturedModalRect(guiLeft + 132, guiTop + 122, 176 + add, 32, 16, 16);

        if(renderContext.doesRenderIn3D()) {
            GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if(renderContext.hasSliceUp()) {
            if(!renderContext.doesRenderIn3D() && mouseX >= this.guiLeft + 150 && mouseX <= this.guiLeft + 150 + 16 &&
                    mouseY >= this.guiTop + 102 && mouseY <= this.guiTop + 102 + 16) {
                GlStateManager.color(0.7F, 0.7F, 1.0F, 1.0F);
            }
            this.drawTexturedModalRect(guiLeft + 150, guiTop + 102, 192, 0, 16, 16);
        }
        if(renderContext.hasSliceDown()) {
            if(!renderContext.doesRenderIn3D() && mouseX >= this.guiLeft + 150 && mouseX <= this.guiLeft + 150 + 16 &&
                    mouseY >= this.guiTop + 124 && mouseY <= this.guiTop + 124 + 16) {
                GlStateManager.color(0.7F, 0.7F, 1.0F, 1.0F);
            }
            this.drawTexturedModalRect(guiLeft + 150, guiTop + 124, 176, 0, 16, 16);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int width = fontRenderer.getStringWidth(String.valueOf(renderContext.getRenderSlice()));
        fontRenderer.drawString(String.valueOf(renderContext.getRenderSlice()), guiLeft + 159 - (width / 2), guiTop + 118, 0x222222);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if(mouseButton == 0) {
            if(!renderContext.doesRenderIn3D()) {
                if(mouseX >= this.guiLeft + 132 && mouseX <= this.guiLeft + 132 + 16 &&
                        mouseY >= this.guiTop + 106 && mouseY <= this.guiTop + 106 + 16) {
                    renderContext.setTo3D();
                }
                if(renderContext.hasSliceUp() && mouseX >= this.guiLeft + 150 && mouseX <= this.guiLeft + 150 + 16 &&
                        mouseY >= this.guiTop + 102 && mouseY <= this.guiTop + 102 + 16) {
                    renderContext.sliceUp();
                }
                if(renderContext.hasSliceDown() && mouseX >= this.guiLeft + 150 && mouseX <= this.guiLeft + 150 + 16 &&
                        mouseY >= this.guiTop + 124 && mouseY <= this.guiTop + 124 + 16) {
                    renderContext.sliceDown();
                }
            } else {
                if(mouseX >= this.guiLeft + 132 && mouseX <= this.guiLeft + 132 + 16 &&
                        mouseY >= this.guiTop + 122 && mouseY <= this.guiTop + 122 + 16) {
                    renderContext.setTo2D();
                }
            }
        } else if(mouseButton == 1) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

}
