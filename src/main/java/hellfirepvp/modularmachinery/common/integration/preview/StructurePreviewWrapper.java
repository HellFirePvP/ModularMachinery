/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.preview;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientMouseJEIGuiEventHandler;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.client.util.DynamicMachineRenderContext;
import hellfirepvp.modularmachinery.client.util.RenderingUtils;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.modifier.ModifierReplacement;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.BlockCompatHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructurePreviewWrapper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 12:38
 */
public class StructurePreviewWrapper implements IRecipeWrapper {

    private static final Field layouts;
    private static final Field recipeLayoutWrapper;

    private static final ResourceLocation ic2TileBlock = new ResourceLocation("ic2", "te");
    public static final ResourceLocation TEXTURE_BACKGROUND = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiblueprint_jei.png");

    private final IDrawable drawableArrowDown, drawableArrowUp;
    private final IDrawable drawable3DDisabled, drawable3DHover, drawable3DActive;
    private final IDrawable drawable2DDisabled, drawable2DHover, drawable2DActive;
    private final IDrawable drawablePopOutDisabled, drawablePopOutHover, drawablePopOutActive;
    private final IDrawable drawableContentsDisabled, drawableContentsHover, drawableContentsActive;
    private final IDrawable drawableUpgradesHover;

    private final DynamicMachine machine;
    private DynamicMachineRenderContext dynamnicContext;

    public static long lastRenderMs = 0;

    public StructurePreviewWrapper(DynamicMachine machine) {
        this.machine = machine;

        IGuiHelper h = ModIntegrationJEI.jeiHelpers.getGuiHelper();
        this.drawableArrowDown =  h.createDrawable(TEXTURE_BACKGROUND, 176, 0,  16, 16);
        this.drawableArrowUp =    h.createDrawable(TEXTURE_BACKGROUND, 192, 0,  16, 16);

        this.drawable3DDisabled = h.createDrawable(TEXTURE_BACKGROUND, 176, 16, 16, 16);
        this.drawable3DHover =    h.createDrawable(TEXTURE_BACKGROUND, 192, 16, 16, 16);
        this.drawable3DActive =   h.createDrawable(TEXTURE_BACKGROUND, 208, 16, 16, 16);

        this.drawable2DDisabled = h.createDrawable(TEXTURE_BACKGROUND, 176, 32, 16, 16);
        this.drawable2DHover =    h.createDrawable(TEXTURE_BACKGROUND, 192, 32, 16, 16);
        this.drawable2DActive =   h.createDrawable(TEXTURE_BACKGROUND, 208, 32, 16, 16);

        this.drawablePopOutDisabled = h.createDrawable(TEXTURE_BACKGROUND, 176, 48, 16, 16);
        this.drawablePopOutHover =    h.createDrawable(TEXTURE_BACKGROUND, 192, 48, 16, 16);
        this.drawablePopOutActive =   h.createDrawable(TEXTURE_BACKGROUND, 208, 48, 16, 16);

        this.drawableContentsDisabled = h.createDrawable(TEXTURE_BACKGROUND, 176, 64, 16, 16);
        this.drawableContentsHover =    h.createDrawable(TEXTURE_BACKGROUND, 192, 64, 16, 16);
        this.drawableContentsActive =   h.createDrawable(TEXTURE_BACKGROUND, 208, 64, 16, 16);

        this.drawableUpgradesHover = h.createDrawable(TEXTURE_BACKGROUND, 0, 145, 100, 14);
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if(dynamnicContext == null) { //If no context exists, it didn't even render yet.
            return false;
        }

        if(mouseButton == 0) {
            if(!dynamnicContext.doesRenderIn3D()) {
                if(mouseX >= 132 && mouseX <= 132 + 16 &&
                        mouseY >= 106 && mouseY <= 106 + 16) {
                    dynamnicContext.setTo3D();
                }
                if(dynamnicContext.hasSliceUp() && mouseX >= 150 && mouseX <= 150 + 16 &&
                        mouseY >= 102 && mouseY <= 102 + 16) {
                    dynamnicContext.sliceUp();
                }
                if(dynamnicContext.hasSliceDown() && mouseX >= 150 && mouseX <= 150 + 16 &&
                        mouseY >= 124 && mouseY <= 124 + 16) {
                    dynamnicContext.sliceDown();
                }
            } else {
                if(mouseX >= 132 && mouseX <= 132 + 16 &&
                        mouseY >= 122 && mouseY <= 122 + 16) {
                    dynamnicContext.setTo2D();
                }
            }
            if(GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak) &&
                    mouseX >= 116 && mouseX <= 116 + 16 &&
                    mouseY >= 106 && mouseY < 106 + 16) {
                if(ClientProxy.renderHelper.startPreview(dynamnicContext)) {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
            }
        }
        return false;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        GuiScreen current = Minecraft.getMinecraft().currentScreen;
        World clWorld = minecraft.world;
        if(current == null || clWorld == null || !(current instanceof RecipesGui)) {
            return; //Wtf. where are we rendering in.
        }
        if (dynamnicContext == null) {
            dynamnicContext = DynamicMachineRenderContext.createContext(this.machine);
        }
        if(System.currentTimeMillis() - lastRenderMs >= 500) {
            dynamnicContext.resetRender();
        }
        lastRenderMs = System.currentTimeMillis();

        if(GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak)) {
            if(dynamnicContext.getShiftSnap() == -1) {
                dynamnicContext.snapSamples();
            }
        } else {
            dynamnicContext.releaseSamples();
        }

        if(dynamnicContext.doesRenderIn3D()) {
            if (isMouseOver(mouseX, mouseY, recipeWidth, recipeHeight) && Mouse.isButtonDown(0)) {
                dynamnicContext.rotateRender(0.25 * Mouse.getDY(), 0.25 * Mouse.getDX(), 0);
            }
        } else {
            if (isMouseOver(mouseX, mouseY, recipeWidth, recipeHeight) && Mouse.isButtonDown(0)) {
                dynamnicContext.moveRender(0.25 * Mouse.getDX(), 0, -0.25 * Mouse.getDY());
            }
        }
        if (isMouseOver(mouseX, mouseY, recipeWidth, recipeHeight)){
            int dwheel = ClientMouseJEIGuiEventHandler.eventDWheelState;
            if(dwheel < 0) {
                dynamnicContext.zoomOut();
            } else if(dwheel > 0) {
                dynamnicContext.zoomIn();
            }
            ClientMouseJEIGuiEventHandler.eventDWheelState = 0;
        }
        ScaledResolution res = new ScaledResolution(minecraft);
        RecipeLayout recipeLayout = getCurrentLayout(minecraft, this);
        int recipeY = recipeLayout != null ? recipeLayout.getPosY() : 0;
        int guiLeft = (current.width - recipeWidth) / 2;
        int guiTop  = res.getScaledHeight() - recipeY - recipeHeight + 38 ;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((guiLeft + 4) * res.getScaleFactor(),guiTop * res.getScaleFactor(), 160 * res.getScaleFactor(), 94 * res.getScaleFactor());
        int x = 88;
        int z = 64;
        GlStateManager.enableBlend();
        dynamnicContext.renderAt(x,  z);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawButtons(minecraft, mouseX, mouseY, 0, 0, recipeWidth, recipeHeight);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if(!machine.getModifiers().isEmpty()) {
            minecraft.getTextureManager().bindTexture(TEXTURE_BACKGROUND);
            this.drawableUpgradesHover.draw(minecraft,  5, 124);

            GlStateManager.disableDepth();
            String reqBlueprint = I18n.format("tooltip.machinery.blueprint.upgrades");
            minecraft.fontRenderer.drawString(reqBlueprint, 10, 127, 0x444444);
            GlStateManager.enableDepth();

            if(mouseX >= 5 && mouseX <= 105 &&
                    mouseY >= 124 && mouseY <= 139) {
                List<Tuple<ItemStack, String>> descriptionList = new LinkedList<>();
                boolean first = true;
                for (List<ModifierReplacement> modifiers : machine.getModifiers().values()) {
                    for (ModifierReplacement mod : modifiers) {
                        List<String> description = mod.getDescriptionLines();
                        if (description.isEmpty()) {
                            continue;
                        }
                        if(!first) {
                            descriptionList.add(new Tuple<>(ItemStack.EMPTY, ""));
                        }
                        first = false;
                        ItemStack stack = mod.getBlockInformation().getDescriptiveStack(dynamnicContext.getShiftSnap() == -1 ? Optional.empty() : Optional.of(dynamnicContext.getShiftSnap()));
                        List<String> tooltip = stack.getTooltip(Minecraft.getMinecraft().player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips ?
                                ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                        descriptionList.add(new Tuple<>(
                                stack,
                                Iterables.getFirst(tooltip, "")));
                        for (String str : description) {
                            descriptionList.add(new Tuple<>(ItemStack.EMPTY, str));
                        }
                    }
                }

                RenderingUtils.renderBlueStackTooltip(mouseX, mouseY, descriptionList, minecraft.fontRenderer, Minecraft.getMinecraft().getRenderItem());
            }
        }

        minecraft.fontRenderer.drawString(machine.getLocalizedName(),
                4, 0,
                0x222222);
        if(machine.requiresBlueprint()) {
            String reqBlueprint = I18n.format("tooltip.machinery.blueprint.required");
            minecraft.fontRenderer.drawString(reqBlueprint, 6, 102, 0x222222);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Rectangle scissorFrame = new Rectangle(4, 4,
                160, 94);
        if(!dynamnicContext.doesRenderIn3D() && scissorFrame.contains(mouseX, mouseY)) {
            double scale = dynamnicContext.getScale();
            Vec2f offset = dynamnicContext.getCurrentRenderOffset(x, z);
            int jumpWidth = 14;
            double scaleJump = jumpWidth * scale;
            Map<BlockPos, BlockArray.BlockInformation> slice = machine.getPattern().getPatternSlice(dynamnicContext.getRenderSlice());
            if(dynamnicContext.getRenderSlice() == 0) {
                slice.put(BlockPos.ORIGIN, new BlockArray.BlockInformation(Lists.newArrayList(new BlockArray.IBlockStateDescriptor(BlocksMM.blockController.getDefaultState()))));
            }
            for (BlockPos pos : slice.keySet()) {
                int xMod = pos.getX() + 1;
                int zMod = pos.getZ() + 1;
                Rectangle.Double rct = new Rectangle2D.Double(offset.x - xMod * scaleJump, offset.y - zMod * scaleJump, scaleJump, scaleJump);
                if(rct.contains(mouseX, mouseY)) {
                    BlockArray.BlockInformation bi = slice.get(pos);
                    IBlockState state = bi.getSampleState(dynamnicContext.getShiftSnap() == -1 ? Optional.empty() : Optional.of(dynamnicContext.getShiftSnap()));
                    Tuple<IBlockState, TileEntity> recovered = BlockCompatHelper.transformState(state, bi.matchingTag,
                            new BlockArray.TileInstantiateContext(Minecraft.getMinecraft().world, pos));
                    state = recovered.getFirst();
                    Block type = state.getBlock();
                    int meta = type.getMetaFromState(state);

                    ItemStack s = ItemStack.EMPTY;
                    try {
                        if(ic2TileBlock.equals(type.getRegistryName())) {
                            s = BlockCompatHelper.tryGetIC2MachineStack(state, recovered.getSecond());
                        } else {
                            s = state.getBlock().getPickBlock(state, null, null, pos, null);
                        }
                    } catch (Exception exc) {}

                    if(s.isEmpty()) {
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
                    }

                    if(!s.isEmpty()) {
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
                    }
                    break;
                }
            }
        }
    }

    private void drawButtons(Minecraft minecraft, int mouseX, int mouseY, int guiLeft, int guiTop, int recipeWidth, int recipeHeight) {
        if(dynamnicContext == null) { //Didn't even render machine yet...
            return;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        minecraft.getTextureManager().bindTexture(TEXTURE_BACKGROUND);

        boolean drawPopoutInfo = false, drawContents = false;

        IDrawable drawable = drawable3DDisabled;
        if(!dynamnicContext.doesRenderIn3D()) {
            if(mouseX >= guiLeft + 132 && mouseX <= guiLeft + 132 + 16 &&
                    mouseY >= guiTop + 106 && mouseY < guiTop + 106 + 16) {
                drawable = drawable3DHover;
            }
        } else {
            drawable = drawable3DActive;
        }
        drawable.draw(minecraft, guiLeft + 132, guiTop + 106);

        drawable = drawablePopOutDisabled;
        if(mouseX >= guiLeft + 116 && mouseX <= guiLeft + 116 + 16 &&
                mouseY >= guiTop + 106 && mouseY < guiTop + 106 + 16) {
            if(GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak)) {
                drawable = drawablePopOutHover;
            }
            drawPopoutInfo = true;
        }
        drawable.draw(minecraft, guiLeft + 116, guiTop + 106);

        drawable = drawable2DDisabled;
        if(dynamnicContext.doesRenderIn3D()) {
            if(mouseX >= guiLeft + 132 && mouseX <= guiLeft + 132 + 16 &&
                    mouseY >= guiTop + 122 && mouseY <= guiTop + 122 + 16) {
                drawable = drawable2DHover;
            }
        } else {
            drawable = drawable2DActive;
        }
        drawable.draw(minecraft, guiLeft + 132, guiTop + 122);

        //Show amount
        drawable = drawableContentsDisabled;
        if(mouseX >= guiLeft + 116 && mouseX <= guiLeft + 116 + 16 &&
                mouseY >= guiTop + 122 && mouseY <= guiTop + 122 + 16) {
            drawable = drawableContentsHover;
            drawContents = true;
        }
        drawable.draw(minecraft, guiLeft + 116, guiTop + 122);

        if(dynamnicContext.doesRenderIn3D()) {
            GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if(dynamnicContext.hasSliceUp()) {
            if(!dynamnicContext.doesRenderIn3D() && mouseX >= guiLeft + 150 && mouseX <= guiLeft + 150 + 16 &&
                    mouseY >= guiTop + 102 && mouseY <= guiTop + 102 + 16) {
                GlStateManager.color(0.7F, 0.7F, 1.0F, 1.0F);
            }
            drawableArrowUp.draw(minecraft, guiLeft + 150, guiTop + 102);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if(dynamnicContext.hasSliceDown()) {
            if(!dynamnicContext.doesRenderIn3D() && mouseX >= guiLeft + 150 && mouseX <= guiLeft + 150 + 16 &&
                    mouseY >= guiTop + 124 && mouseY <= guiTop + 124 + 16) {
                GlStateManager.color(0.7F, 0.7F, 1.0F, 1.0F);
            }
            drawableArrowDown.draw(minecraft, guiLeft + 150, guiTop + 124);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int width = minecraft.fontRenderer.getStringWidth(String.valueOf(dynamnicContext.getRenderSlice()));
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 0, 0); //Don't ask.
        minecraft.fontRenderer.drawString(String.valueOf(dynamnicContext.getRenderSlice()), guiLeft + 158 - (width / 2), guiTop + 118, 0x222222);
        if(drawPopoutInfo) {
            ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
            java.util.List<String> out = minecraft.fontRenderer.listFormattedStringToWidth(
                    I18n.format("gui.blueprint.popout.info"),
                    Math.min(res.getScaledWidth() - mouseX, 200));
            RenderingUtils.renderBlueTooltip(mouseX, mouseY, out, minecraft.fontRenderer);
        }
        if(drawContents) {
            java.util.List<ItemStack> contents = dynamnicContext.getDescriptiveStacks();
            java.util.List<Tuple<ItemStack, String>> contentMap = Lists.newArrayList();
            ItemStack ctrl = new ItemStack(BlocksMM.blockController);
            contentMap.add(new Tuple<>(ctrl, "1x " + Iterables.getFirst(ctrl.getTooltip(Minecraft.getMinecraft().player,
                    Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), "")));
            for (ItemStack stack : contents) {
                contentMap.add(new Tuple<>(stack, stack.getCount() + "x " + Iterables.getFirst(stack.getTooltip(Minecraft.getMinecraft().player,
                        Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), "")));
            }

            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int srWidth = scaledresolution.getScaledWidth();
            int srHeight = scaledresolution.getScaledHeight();
            int rMouseX = Mouse.getX() * srWidth / Minecraft.getMinecraft().displayWidth;
            int rMouseY = srHeight - Mouse.getY() * srHeight / Minecraft.getMinecraft().displayHeight - 1;

            RecipesGui current = (RecipesGui) Minecraft.getMinecraft().currentScreen;
            RecipeLayout currentLayout = null;
            try {
                List<RecipeLayout> layoutList = (List<RecipeLayout>) layouts.get(current);
                for (RecipeLayout layout : layoutList) {
                    if (layout.isMouseOver(rMouseX, rMouseY)) {
                        currentLayout = layout;
                        break;
                    }
                }
            } catch (IllegalAccessException ignored) {}

            if(currentLayout != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-currentLayout.getPosX(), -currentLayout.getPosY(), 0);
                RenderingUtils.renderBlueStackTooltip(currentLayout.getPosX() + mouseX, currentLayout.getPosY() + mouseY,
                        contentMap,
                        minecraft.fontRenderer,
                        minecraft.getRenderItem());
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack bOut = new ItemStack(ItemsMM.blueprint);
        ItemBlueprint.setAssociatedMachine(bOut, this.machine);
        ingredients.setOutput(ItemStack.class, bOut);
    }

    private boolean isMouseOver(int mouseX, int mouseY, int width, int height){
        return 0 < mouseX && mouseX < width &&  0 < mouseY && mouseY < height;
    }

    //I blame mezz for this, making stuff not accessible and badly organizing the original values so it's horrible to draw custom stuff onto the GUI frame.
    static {
        Field f;
        try {
            f = RecipesGui.class.getDeclaredField("recipeLayouts");
            f.setAccessible(true);
        } catch (Exception exc) {
            f = null;
        }
        layouts = f;
        try {
            f = RecipeLayout.class.getDeclaredField("recipeWrapper");
            f.setAccessible(true);
        } catch (Exception exc) {
            f = null;
        }
        recipeLayoutWrapper = f;
    }

    public static RecipeLayout getCurrentLayout(Minecraft minecraft, IRecipeWrapper wrapper){
        if (minecraft.currentScreen instanceof RecipesGui){
            try {
                List<RecipeLayout> recipesRendering = (List<RecipeLayout>) layouts.get((RecipesGui) minecraft.currentScreen);
                for (RecipeLayout recipeLayout : recipesRendering) {
                    if (recipeLayoutWrapper.get(recipeLayout).equals(wrapper)){
                        return recipeLayout;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
