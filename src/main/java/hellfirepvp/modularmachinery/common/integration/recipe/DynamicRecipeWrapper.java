/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.crafting.tooltip.RequirementTip;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.Animation;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicRecipeWrapper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 16:58
 */
public class DynamicRecipeWrapper implements IRecipeWrapper {

    private final MachineRecipe recipe;
    public final Map<IOType, Map<Class<?>, List<ComponentRequirement<?, ?>>>> finalOrderedComponents = new HashMap<>();

    public DynamicRecipeWrapper(MachineRecipe recipe) {
        this.recipe = recipe;

        for (IOType type : IOType.values()) {
            finalOrderedComponents.put(type, new HashMap<>());
        }
        for (ComponentRequirement<?, ?> req : recipe.getCraftingRequirements()) {
            ComponentRequirement.JEIComponent<?> comp = req.provideJEIComponent();
            finalOrderedComponents.get(req.getActionType())
                    .computeIfAbsent(comp.getJEIRequirementClass(), clazz -> new LinkedList<>()).add(req);
        }
    }

    @Override
    @Nonnull
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltips = Lists.newArrayList();
        CategoryDynamicRecipe recipeCategory = ModIntegrationJEI.getCategory(recipe.getOwningMachine());
        if(recipeCategory != null) {
            if(recipeCategory.rectangleProcessArrow.contains(mouseX, mouseY)) {
                tooltips.add(I18n.format("tooltip.machinery.duration", recipe.getRecipeTotalTickTime()));
            }
        }

        return tooltips;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        CategoryDynamicRecipe recipeCategory = ModIntegrationJEI.getCategory(recipe.getOwningMachine());
        if(recipeCategory == null) return;

        int totalDur = this.recipe.getRecipeTotalTickTime();
        int tick = (int) (ClientScheduler.getClientTick() % totalDur);
        int pxPart = MathHelper.ceil(((float) tick + Animation.getPartialTickTime()) / ((float) totalDur) * RecipeLayoutHelper.PART_PROCESS_ARROW_ACTIVE.xSize);
        ModIntegrationJEI.jeiHelpers.getGuiHelper()
                .createDrawable(RecipeLayoutHelper.LOCATION_JEI_ICONS, 84, 15, pxPart, RecipeLayoutHelper.PART_PROCESS_ARROW_ACTIVE.zSize)
                .draw(minecraft, recipeCategory.rectangleProcessArrow.x, recipeCategory.rectangleProcessArrow.y);

        int offsetY = recipeCategory.realHeight;

        int lineHeight = RequirementTip.LINE_HEIGHT;
        int splitHeight = RequirementTip.SPLIT_HEIGHT;

        List<List<String>> tooltips = new ArrayList<>();
        for (RequirementTip tip : RegistriesMM.REQUIREMENT_TIPS_REGISTRY) {
            Collection<ComponentRequirement<?, ?>> requirements = tip.filterRequirements(this.recipe, this.recipe.getCraftingRequirements());
            if (!requirements.isEmpty()) {
                tooltips.add(tip.buildTooltip(this.recipe, requirements));
            }
        }

        for (List<String> tTip : tooltips) {
            offsetY -= lineHeight * tTip.size();
            offsetY -= splitHeight;
        }

        FontRenderer fr = minecraft.fontRenderer;
        GlStateManager.color(1, 1, 1, 1);
        for (List<String> tTip : tooltips) {
            for (String tip : tTip) {
                fr.drawString(tip, 8, offsetY, 0x222222);
                offsetY += lineHeight;
            }
            offsetY += splitHeight;
        }
        GlStateManager.color(1, 1, 1, 1);


        //TODO Rework this along with the ingredient for energy stuffs
        long totalEnergyIn = 0;
        for (ComponentRequirement req : this.recipe.getCraftingRequirements().stream()
                .filter(r -> r instanceof RequirementEnergy)
                .filter(r -> r.getActionType() == IOType.INPUT)
                .collect(Collectors.toList())) {
            totalEnergyIn += ((RequirementEnergy) req).getRequiredEnergyPerTick();
        }
        long totalEnergyOut = 0;
        for (ComponentRequirement req : this.recipe.getCraftingRequirements().stream()
                .filter(r -> r instanceof RequirementEnergy)
                .filter(r -> r.getActionType() == IOType.OUTPUT)
                .collect(Collectors.toList())) {
            totalEnergyOut += ((RequirementEnergy) req).getRequiredEnergyPerTick();
        }

        long finalTotalEnergyIn = totalEnergyIn;
        recipeCategory.inputComponents.stream()
                .filter(r -> r instanceof RecipeLayoutPart.Energy)
                .forEach(part -> ((RecipeLayoutPart.Energy) part).drawEnergy(minecraft, finalTotalEnergyIn));
        long finalTotalEnergyOut = totalEnergyOut;
        recipeCategory.outputComponents.stream()
                .filter(r -> r instanceof RecipeLayoutPart.Energy)
                .forEach(part -> ((RecipeLayoutPart.Energy) part).drawEnergy(minecraft, finalTotalEnergyOut));
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        Map<IIngredientType, Map<IOType, List<ComponentRequirement>>> componentMap = new HashMap<>();
        for (ComponentRequirement<?, ?> req : this.recipe.getCraftingRequirements()) {
            if(req instanceof RequirementEnergy) continue; //Ignore. They're handled differently. I should probably rework this...

            ComponentRequirement.JEIComponent<?> comp = req.provideJEIComponent();
            IIngredientType type = ModIntegrationJEI.ingredientRegistry.getIngredientType(comp.getJEIRequirementClass());
            componentMap.computeIfAbsent(type, t -> new HashMap<>())
                    .computeIfAbsent(req.getActionType(), tt -> new LinkedList<>()).add(req);
        }

        for (IIngredientType type : componentMap.keySet()) {
            Map<IOType, List<ComponentRequirement>> ioGroup = componentMap.get(type);
            for (IOType ioType : ioGroup.keySet()) {
                List<ComponentRequirement> components = ioGroup.get(ioType);
                List<List<Object>> componentObjects = new ArrayList<>(components.size());
                for (ComponentRequirement req : components) {
                    componentObjects.add(req.provideJEIComponent().getJEIIORequirements());
                }
                switch (ioType) {
                    case INPUT:
                        ingredients.setInputLists(type, componentObjects);
                        break;
                    case OUTPUT:
                        ingredients.setOutputLists(type, componentObjects);
                        break;
                }
            }
        }
    }

}
