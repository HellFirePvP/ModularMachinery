/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
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
    public final Map<MachineComponent.IOType, Map<Class<?>, List<ComponentRequirement<?>>>> finalOrderedComponents = new HashMap<>();

    public DynamicRecipeWrapper(MachineRecipe recipe) {
        this.recipe = recipe;

        for (MachineComponent.IOType type : MachineComponent.IOType.values()) {
            finalOrderedComponents.put(type, new HashMap<>());
        }
        for (ComponentRequirement<?> req : recipe.getCraftingRequirements()) {
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

        long totalEnergyIn = 0;
        for (ComponentRequirement req : this.recipe.getCraftingRequirements().stream()
                        .filter(r -> r instanceof RequirementEnergy)
                        .filter(r -> r.getActionType() == MachineComponent.IOType.INPUT).collect(Collectors.toList())) {
            totalEnergyIn += ((RequirementEnergy) req).getRequiredEnergyPerTick();
        }
        if(totalEnergyIn > 0) {
            offsetY -= 36;
        }

        long totalEnergyOut = 0;
        for (ComponentRequirement req : this.recipe.getCraftingRequirements().stream()
                .filter(r -> r instanceof RequirementEnergy)
                .filter(r -> r.getActionType() == MachineComponent.IOType.OUTPUT).collect(Collectors.toList())) {
            totalEnergyOut += ((RequirementEnergy) req).getRequiredEnergyPerTick();
        }
        if(totalEnergyOut > 0) {
            offsetY -= 36;
        }

        int totalFuelIn = 0;
        for (ComponentRequirement req : this.recipe.getCraftingRequirements().stream()
                .filter(c -> c instanceof RequirementItem)
                .filter(c -> c.getActionType() == MachineComponent.IOType.INPUT)
                .filter(c -> ((RequirementItem) c).requirementType == RequirementItem.ItemRequirementType.FUEL)
                .collect(Collectors.toList())) {
            totalFuelIn += ((RequirementItem) req).fuelBurntime;
        }
        if(totalFuelIn > 0) {
            offsetY -= 26;
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        long finalTotalEnergyIn = totalEnergyIn;

        recipeCategory.inputComponents.stream()
                .filter(r -> r instanceof RecipeLayoutPart.Energy)
                .forEach(part -> ((RecipeLayoutPart.Energy) part).drawEnergy(minecraft, finalTotalEnergyIn));
        long finalTotalEnergyOut = totalEnergyOut;
        recipeCategory.outputComponents.stream()
                .filter(r -> r instanceof RecipeLayoutPart.Energy)
                .forEach(part -> ((RecipeLayoutPart.Energy) part).drawEnergy(minecraft, finalTotalEnergyOut));
        GlStateManager.color(1F, 1F, 1F, 1F);

        if(totalEnergyIn > 0) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.in"), 8,  offsetY + 10, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.in.tick", totalEnergyIn), 8,  offsetY + 20, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.in.total", totalEnergyIn * this.recipe.getRecipeTotalTickTime()), 8,  offsetY + 30, 0x222222);
            GlStateManager.color(1F, 1F, 1F, 1F);
            offsetY += 36;
        }

        if(totalFuelIn > 0) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.fuel.in"), 8,  offsetY + 10, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.fuel.in.total", totalFuelIn), 8,  offsetY + 20, 0x222222);
            GlStateManager.color(1F, 1F, 1F, 1F);
            offsetY += 26;
        }

        if(totalEnergyOut > 0) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.out"), 8,  offsetY + 10, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.out.tick", totalEnergyOut), 8,  offsetY + 20, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.out.total", totalEnergyOut * this.recipe.getRecipeTotalTickTime()), 8,  offsetY + 30, 0x222222);
            GlStateManager.color(1F, 1F, 1F, 1F);
            offsetY += 36;
        }

    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        Map<Class<?>, Map<MachineComponent.IOType, List<ComponentRequirement<?>>>> componentMap = new HashMap<>();
        for (ComponentRequirement req : this.recipe.getCraftingRequirements()) {
            if(req instanceof RequirementEnergy) continue; //Ignore. They're handled differently.

            ComponentRequirement.JEIComponent<?> comp = req.provideJEIComponent();
            componentMap.computeIfAbsent(comp.getJEIRequirementClass(), clazz -> new HashMap<>())
                    .computeIfAbsent(req.getActionType(), type -> new LinkedList<>()).add(req);
        }

        for (Class<?> clazz : componentMap.keySet()) {
            Map<MachineComponent.IOType, List<ComponentRequirement<?>>> ioGroup = componentMap.get(clazz);
            for (MachineComponent.IOType ioType : ioGroup.keySet()) {
                List<ComponentRequirement<?>> components = ioGroup.get(ioType);
                List<List<Object>> componentObjects = new ArrayList<>(components.size());
                for (ComponentRequirement req : components) {
                    componentObjects.add(req.provideJEIComponent().getJEIIORequirements());
                }
                switch (ioType) {
                    case INPUT:
                        ingredients.setInputLists(clazz, componentObjects);
                        break;
                    case OUTPUT:
                        ingredients.setOutputLists(clazz, componentObjects);
                        break;
                }
            }
        }
    }

}
