/*******************************************************************************
 * HellFirePvP / Modular Machinery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.preview;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientMouseJEIGuiEventHandler;
import hellfirepvp.modularmachinery.client.util.DynamicMachineRenderContext;
import hellfirepvp.modularmachinery.client.util.RenderingUtils;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.List;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructurePreviewWrapper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 12:38
 */
public class StructurePreviewWrapper implements IRecipeWrapper {

    public static final ResourceLocation TEXTURE_BACKGROUND = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiblueprint.png");

    private final IDrawable drawableArrowDown, drawableArrowUp;
    private final IDrawable drawable3DDisabled, drawable3DHover, drawable3DActive;
    private final IDrawable drawable2DDisabled, drawable2DHover, drawable2DActive;

    private final DynamicMachine machine;
    private final DynamicMachineRenderContext context;

    public static long lastRenderMs = 0;
    public static DynamicMachine lastPreviewedMachine = null;

    public StructurePreviewWrapper(DynamicMachine machine) {
        this.machine = machine;
        this.context = DynamicMachineRenderContext.createContext(this.machine);

        IGuiHelper h = ModIntegrationJEI.jeiHelpers.getGuiHelper();
        this.drawableArrowDown =  h.createDrawable(TEXTURE_BACKGROUND, 176, 0,  16, 16);
        this.drawableArrowUp =    h.createDrawable(TEXTURE_BACKGROUND, 192, 0,  16, 16);

        this.drawable3DDisabled = h.createDrawable(TEXTURE_BACKGROUND, 176, 16, 16, 16);
        this.drawable3DHover =    h.createDrawable(TEXTURE_BACKGROUND, 192, 16, 16, 16);
        this.drawable3DActive =   h.createDrawable(TEXTURE_BACKGROUND, 208, 16, 16, 16);

        this.drawable2DDisabled = h.createDrawable(TEXTURE_BACKGROUND, 176, 32, 16, 16);
        this.drawable2DHover =    h.createDrawable(TEXTURE_BACKGROUND, 192, 32, 16, 16);
        this.drawable2DActive =   h.createDrawable(TEXTURE_BACKGROUND, 208, 32, 16, 16);
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) {
            if(!context.doesRenderIn3D()) {
                if(mouseX >= 132 && mouseX <= 132 + 16 &&
                        mouseY >= 106 && mouseY <= 106 + 16) {
                    context.setTo3D();
                }
                if(context.hasSliceUp() && mouseX >= 150 && mouseX <= 150 + 16 &&
                        mouseY >= 102 && mouseY <= 102 + 16) {
                    context.sliceUp();
                }
                if(context.hasSliceDown() && mouseX >= 150 && mouseX <= 150 + 16 &&
                        mouseY >= 124 && mouseY <= 124 + 16) {
                    context.sliceDown();
                }
            } else {
                if(mouseX >= 132 && mouseX <= 132 + 16 &&
                        mouseY >= 122 && mouseY <= 122 + 16) {
                    context.setTo2D();
                }
            }
        }
        return false;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        GuiScreen current = Minecraft.getMinecraft().currentScreen;
        if(current == null) {
            return; //Wtf. where are we rendering in.
        }

        if(System.currentTimeMillis() - lastRenderMs >= 500 || lastPreviewedMachine == null || !lastPreviewedMachine.equals(machine)) {
            context.resetRender();
            lastPreviewedMachine = this.machine;
        }
        lastRenderMs = System.currentTimeMillis();

        if(context.doesRenderIn3D()) {
            if (Mouse.isButtonDown(0)) {
                context.rotateRender(0.25 * Mouse.getDY(), 0.25 * Mouse.getDX(), 0);
            }
        } else {
            if (Mouse.isButtonDown(0)) {
                context.moveRender(0.25 * Mouse.getDX(), 0, -0.25 * Mouse.getDY());
            }
        }
        int dwheel = ClientMouseJEIGuiEventHandler.eventDWheelState;
        if(dwheel < 0) {
            context.zoomOut();
        } else if(dwheel > 0) {
            context.zoomIn();
        }
        ClientMouseJEIGuiEventHandler.eventDWheelState = 0;

        int guiLeft = (current.width - recipeWidth) / 2;
        int guiTop  = (current.height - recipeHeight) / 2;

        ScaledResolution res = new ScaledResolution(minecraft);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((guiLeft + 4) * res.getScaleFactor(), (guiTop + 14) * res.getScaleFactor(),
                160 * res.getScaleFactor(), 94 * res.getScaleFactor());
        int x = 88;
        int z = 64;
        GlStateManager.enableBlend();
        context.renderAt(x,  z);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawButtons(minecraft, mouseX, mouseY, 0, 0);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.fontRenderer.drawString(machine.getLocalizedName(),
                4, -7,
                0x222222);
        if(machine.requiresBlueprint()) {
            String reqBlueprint = I18n.format("tooltip.machinery.blueprint.required");
            minecraft.fontRenderer.drawString(reqBlueprint, 6, 102, 0x222222);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Rectangle scissorFrame = new Rectangle(4, 4,
                160, 94);
        if(!context.doesRenderIn3D() && scissorFrame.contains(mouseX, mouseY)) {
            double scale = context.getScale();
            Vec2f offset = context.getCurrentRenderOffset(x, z);
            int jumpWidth = 14;
            double scaleJump = jumpWidth * scale;
            Map<BlockPos, BlockArray.BlockInformation> slice = machine.getPattern().getPatternSlice(context.getRenderSlice());
            if(context.getRenderSlice() == 0) {
                slice.put(BlockPos.ORIGIN, new BlockArray.BlockInformation(Lists.newArrayList(new BlockArray.IBlockStateDescriptor(BlocksMM.blockController.getDefaultState()))));
            }
            for (BlockPos pos : slice.keySet()) {
                int xMod = pos.getX() + 1;
                int zMod = pos.getZ() + 1;
                Rectangle.Double rct = new Rectangle2D.Double(offset.x - xMod * scaleJump, offset.y - zMod * scaleJump, scaleJump, scaleJump);
                if(rct.contains(mouseX, mouseY)) {
                    IBlockState state = slice.get(pos).getSampleState();
                    Block type = state.getBlock();
                    int meta = type.getMetaFromState(state);
                    ItemStack s;
                    if(type instanceof BlockFluidBase) {
                        s = FluidUtil.getFilledBucket(new FluidStack(((BlockFluidBase) type).getFluid(), 1000));
                    } else if(type instanceof BlockLiquid) {
                        Material m = state.getMaterial();
                        if(m == Material.WATER) {
                            s = new ItemStack(Items.WATER_BUCKET);
                        } else if(m == Material.LAVA) {
                            s = new ItemStack(Items.LAVA_BUCKET);
                        } else {
                            s = ItemStack.EMPTY;
                        }
                    } else {
                        Item i = Item.getItemFromBlock(type);
                        if(i == Items.AIR) continue;
                        if(i.getHasSubtypes()) {
                            s = new ItemStack(i, 1, meta);
                        } else {
                            s = new ItemStack(i);
                        }
                    }
                    java.util.List<String> tooltip = s.getTooltip(Minecraft.getMinecraft().player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips ?
                            ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                    java.util.List<Tuple<ItemStack, String>> stacks = new LinkedList<>();
                    boolean first = true;
                    for (String str : tooltip) {
                        if(first) {
                            stacks.add(new Tuple<>(s, str));
                            first = false;
                        } else {
                            stacks.add(new Tuple<>(ItemStack.EMPTY, str));
                        }
                    }

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(mouseX, mouseY, 0);
                    GlStateManager.disableDepth();
                    GlStateManager.disableBlend();
                    RenderingUtils.renderBlueStackTooltip(0, 0, stacks, Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getRenderItem());
                    GlStateManager.enableBlend();
                    GlStateManager.enableDepth();
                    GlStateManager.popMatrix();
                    break;
                }
            }
        }
    }

    private void drawButtons(Minecraft minecraft, int mouseX, int mouseY, int guiLeft, int guiTop) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        minecraft.getTextureManager().bindTexture(TEXTURE_BACKGROUND);

        IDrawable drawable = drawable3DDisabled;
        if(!context.doesRenderIn3D()) {
            if(mouseX >= guiLeft + 132 && mouseX <= guiLeft + 132 + 16 &&
                    mouseY >= guiTop + 106 && mouseY <= guiTop + 106 + 16) {
                drawable = drawable3DHover;
            }
        } else {
            drawable = drawable3DActive;
        }
        drawable.draw(minecraft, guiLeft + 132, guiTop + 106);


        drawable = drawable2DDisabled;
        if(context.doesRenderIn3D()) {
            if(mouseX >= guiLeft + 132 && mouseX <= guiLeft + 132 + 16 &&
                    mouseY >= guiTop + 122 && mouseY <= guiTop + 122 + 16) {
                drawable = drawable2DHover;
            }
        } else {
            drawable = drawable2DActive;
        }
        drawable.draw(minecraft, guiLeft + 132, guiTop + 122);

        if(context.doesRenderIn3D()) {
            GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if(context.hasSliceUp()) {
            if(!context.doesRenderIn3D() && mouseX >= guiLeft + 150 && mouseX <= guiLeft + 150 + 16 &&
                    mouseY >= guiTop + 102 && mouseY <= guiTop + 102 + 16) {
                GlStateManager.color(0.7F, 0.7F, 1.0F, 1.0F);
            }
            drawableArrowUp.draw(minecraft, guiLeft + 150, guiTop + 102);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if(context.hasSliceDown()) {
            if(!context.doesRenderIn3D() && mouseX >= guiLeft + 150 && mouseX <= guiLeft + 150 + 16 &&
                    mouseY >= guiTop + 124 && mouseY <= guiTop + 124 + 16) {
                GlStateManager.color(0.7F, 0.7F, 1.0F, 1.0F);
            }
            drawableArrowDown.draw(minecraft, guiLeft + 150, guiTop + 124);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int width = minecraft.fontRenderer.getStringWidth(String.valueOf(context.getRenderSlice()));
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 0, 0); //Don't ask.
        minecraft.fontRenderer.drawString(String.valueOf(context.getRenderSlice()), guiLeft + 158 - (width / 2), guiTop + 118, 0x222222);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {}

}
