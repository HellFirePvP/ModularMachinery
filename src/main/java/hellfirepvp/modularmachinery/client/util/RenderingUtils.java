/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderingUtils
 * Created by HellFirePvP
 * Date: 18.08.2017 / 14:02
 */
public class RenderingUtils {

    static void drawWhiteOutlineCubes(List<BlockPos> positions, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1F, 1F, 1F, 0.4F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorMaterial();
        GlStateManager.disableCull();

        Entity player = Minecraft.getMinecraft().getRenderViewEntity();
        if(player == null) {
            player = Minecraft.getMinecraft().player;
        }

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
        for (BlockPos pos : positions) {
            AxisAlignedBB box = Block.FULL_BLOCK_AABB.offset(pos).grow(0.002).offset(-dX, -dY, -dZ);

            vb.pos(box.minX, box.minY, box.minZ).endVertex();
            vb.pos(box.maxX, box.minY, box.minZ).endVertex();
            vb.pos(box.maxX, box.minY, box.maxZ).endVertex();
            vb.pos(box.minX, box.minY, box.maxZ).endVertex();

            vb.pos(box.minX, box.maxY, box.maxZ).endVertex();
            vb.pos(box.maxX, box.maxY, box.maxZ).endVertex();
            vb.pos(box.maxX, box.maxY, box.minZ).endVertex();
            vb.pos(box.minX, box.maxY, box.minZ).endVertex();


            vb.pos(box.maxX, box.minY, box.minZ).endVertex();
            vb.pos(box.maxX, box.maxY, box.minZ).endVertex();
            vb.pos(box.maxX, box.maxY, box.maxZ).endVertex();
            vb.pos(box.maxX, box.minY, box.maxZ).endVertex();

            vb.pos(box.minX, box.minY, box.maxZ).endVertex();
            vb.pos(box.minX, box.maxY, box.maxZ).endVertex();
            vb.pos(box.minX, box.maxY, box.minZ).endVertex();
            vb.pos(box.minX, box.minY, box.minZ).endVertex();


            vb.pos(box.minX, box.maxY, box.minZ).endVertex();
            vb.pos(box.maxX, box.maxY, box.minZ).endVertex();
            vb.pos(box.maxX, box.minY, box.minZ).endVertex();
            vb.pos(box.minX, box.minY, box.minZ).endVertex();

            vb.pos(box.minX, box.minY, box.maxZ).endVertex();
            vb.pos(box.maxX, box.minY, box.maxZ).endVertex();
            vb.pos(box.maxX, box.maxY, box.maxZ).endVertex();
            vb.pos(box.minX, box.maxY, box.maxZ).endVertex();
        }

        vb.sortVertexData(
                (float) TileEntityRendererDispatcher.staticPlayerX,
                (float) TileEntityRendererDispatcher.staticPlayerY,
                (float) TileEntityRendererDispatcher.staticPlayerZ);
        tes.draw();

        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableColorMaterial();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }



    public static void renderBlueStackTooltip(int x, int y, List<Tuple<ItemStack, String>> tooltipData, FontRenderer fr, RenderItem ri) {
        renderStackTooltip(x, y, tooltipData, new Color(0x000058), new Color(0x000000), Color.WHITE, fr, ri);
    }

    public static void renderStackTooltip(int x, int y, List<Tuple<ItemStack, String>> tooltipData, Color color, Color colorFade, Color strColor, FontRenderer fr, RenderItem ri) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (!tooltipData.isEmpty()) {
            int esWidth = 0;
            for (Tuple<ItemStack, String> toolTip : tooltipData) {
                int width = fr.getStringWidth(toolTip.getSecond()) + 17;
                if (width > esWidth)
                    esWidth = width;
            }
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if(x + 15 + esWidth > sr.getScaledWidth()) {
                x -= esWidth + 24;
            }
            int pX = x + 12;
            int pY = y - 12;
            int sumLineHeight = 8;
            int lastAdded = 0;
            if (tooltipData.size() > 1) {
                sumLineHeight += 2;
                for (Tuple<ItemStack, String> tooltipEntry : tooltipData) {
                    int height = tooltipEntry.getFirst().isEmpty() ? 10 : 17;
                    sumLineHeight += height;
                    lastAdded = height;
                }
                sumLineHeight -= lastAdded;
            }
            float z = 300F;

            GlStateManager.disableDepth();
            drawGradientRect(pX - 3,           pY - 4,                 z, pX + esWidth + 3, pY - 3,                 color, colorFade);
            drawGradientRect(pX - 3,           pY + sumLineHeight + 3, z, pX + esWidth + 3, pY + sumLineHeight + 4, color, colorFade);
            drawGradientRect(pX - 3,           pY - 3,                 z, pX + esWidth + 3, pY + sumLineHeight + 3, color, colorFade);
            drawGradientRect(pX - 4,           pY - 3,                 z, pX - 3,           pY + sumLineHeight + 3, color, colorFade);
            drawGradientRect(pX + esWidth + 3, pY - 3,                 z, pX + esWidth + 4, pY + sumLineHeight + 3, color, colorFade);

            int rgb = color.getRGB();
            int col = (rgb & 0x00FFFFFF) | rgb & 0xFF000000;
            Color colOp = new Color(col);
            drawGradientRect(pX - 3,           pY - 3 + 1,             z, pX - 3 + 1,       pY + sumLineHeight + 3 - 1, color, colOp);
            drawGradientRect(pX + esWidth + 2, pY - 3 + 1,             z, pX + esWidth + 3, pY + sumLineHeight + 3 - 1, color, colOp);
            drawGradientRect(pX - 3,           pY - 3,                 z, pX + esWidth + 3, pY - 3 + 1,                 colOp, colOp);
            drawGradientRect(pX - 3,           pY + sumLineHeight + 2, z, pX + esWidth + 3, pY + sumLineHeight + 3,     color, color);

            for (Tuple<ItemStack, String> stackDesc : tooltipData) {
                if(!stackDesc.getFirst().isEmpty()) {
                    fr.drawString(stackDesc.getSecond(), pX + 17, pY, strColor.getRGB());
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();
                    ri.renderItemAndEffectIntoGUI(stackDesc.getFirst(), pX - 1, pY - 5);
                    GlStateManager.popMatrix();
                    pY += 17;
                } else {
                    fr.drawString(stackDesc.getSecond(), pX, pY, strColor.getRGB());
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    pY += 10;
                }
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
        }

        GlStateManager.enableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    public static void renderBlueTooltip(int x, int y, List<String> tooltipData, FontRenderer fontRenderer) {
        renderTooltip(x, y, tooltipData, new Color(0x000058), new Color(0x000000), Color.WHITE, fontRenderer);
    }

    public static void renderTooltip(int x, int y, List<String> tooltipData, Color color, Color colorFade, Color strColor, FontRenderer fontRenderer) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
        if (lighting)
            RenderHelper.disableStandardItemLighting();

        if (!tooltipData.isEmpty()) {
            int esWidth = 0;
            for (String toolTip : tooltipData) {
                int width = fontRenderer.getStringWidth(toolTip);
                if (width > esWidth)
                    esWidth = width;
            }
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if(x + 15 + esWidth > sr.getScaledWidth()) {
                x -= esWidth + 24;
            }
            int pX = x + 12;
            int pY = y - 12;
            int sumLineHeight = 8;
            if (tooltipData.size() > 1)
                sumLineHeight += 2 + (tooltipData.size() - 1) * 10;
            float z = 300F;

            drawGradientRect(pX - 3,           pY - 4,                 z, pX + esWidth + 3, pY - 3,                 color, colorFade);
            drawGradientRect(pX - 3,           pY + sumLineHeight + 3, z, pX + esWidth + 3, pY + sumLineHeight + 4, color, colorFade);
            drawGradientRect(pX - 3,           pY - 3,                 z, pX + esWidth + 3, pY + sumLineHeight + 3, color, colorFade);
            drawGradientRect(pX - 4,           pY - 3,                 z, pX - 3,           pY + sumLineHeight + 3, color, colorFade);
            drawGradientRect(pX + esWidth + 3, pY - 3,                 z, pX + esWidth + 4, pY + sumLineHeight + 3, color, colorFade);

            int rgb = color.getRGB();
            int col = (rgb & 0x00FFFFFF) | rgb & 0xFF000000;
            Color colOp = new Color(col);
            drawGradientRect(pX - 3,           pY - 3 + 1,             z, pX - 3 + 1,       pY + sumLineHeight + 3 - 1, color, colOp);
            drawGradientRect(pX + esWidth + 2, pY - 3 + 1,             z, pX + esWidth + 3, pY + sumLineHeight + 3 - 1, color, colOp);
            drawGradientRect(pX - 3,           pY - 3,                 z, pX + esWidth + 3, pY - 3 + 1,                 colOp, colOp);
            drawGradientRect(pX - 3,           pY + sumLineHeight + 2, z, pX + esWidth + 3, pY + sumLineHeight + 3,     color, color);

            GlStateManager.disableDepth();
            for (int i = 0; i < tooltipData.size(); ++i) {
                String str = tooltipData.get(i);
                fontRenderer.drawString(str, pX, pY, strColor.getRGB());
                if (i == 0)
                    pY += 2;
                pY += 10;
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
        }

        if (lighting)
            RenderHelper.enableStandardItemLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    public static void drawGradientRect(int x, int y, float z, int toX, int toY, Color color, Color colorFade) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(toX, y,   z).color(color.getRed(),     color.getGreen(),     color.getBlue(),     color.getAlpha())    .endVertex();
        vb.pos(x,   y,   z).color(color.getRed(),     color.getGreen(),     color.getBlue(),     color.getAlpha())    .endVertex();
        vb.pos(x,   toY, z).color(colorFade.getRed(), colorFade.getGreen(), colorFade.getBlue(), colorFade.getAlpha()).endVertex();
        vb.pos(toX, toY, z).color(colorFade.getRed(), colorFade.getGreen(), colorFade.getBlue(), colorFade.getAlpha()).endVertex();
        tes.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

}
