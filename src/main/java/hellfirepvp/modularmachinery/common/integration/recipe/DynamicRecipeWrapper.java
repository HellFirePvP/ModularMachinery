/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirements.RequirementItem;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.FuelItemHelper;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static hellfirepvp.modularmachinery.common.crafting.requirements.RequirementItem.ItemRequirementType.ITEMSTACKS;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: DynamicRecipeWrapper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 16:58
 */
public class DynamicRecipeWrapper implements IRecipeWrapper {

    private final MachineRecipe recipe;
    public final List<RequirementFluid> immulatbleOrderedInputFluids;
    public final List<RequirementFluid> immulatbleOrderedOutputFluids;
    public final List<RequirementItem> immulatbleOrderedInputItems;
    public final List<RequirementItem> immulatbleOrderedOutputItems;

    public DynamicRecipeWrapper(MachineRecipe recipe) {
        this.recipe = recipe;

        List<ComponentRequirement> requirements = this.recipe.getCraftingRequirements();

        List<ComponentRequirement> components = requirements.stream()
                .filter(c -> c instanceof RequirementFluid)
                .filter(c -> c.getActionType() == MachineComponent.IOType.INPUT)
                .collect(Collectors.toList());
        List<RequirementFluid> fluidsInCopy = Lists.newLinkedList();
        components.forEach(r -> fluidsInCopy.add((RequirementFluid) r));
        this.immulatbleOrderedInputFluids = ImmutableList.copyOf(fluidsInCopy);

        components = requirements.stream()
                .filter(c -> c instanceof RequirementFluid)
                .filter(c -> c.getActionType() == MachineComponent.IOType.OUTPUT)
                .collect(Collectors.toList());
        List<RequirementFluid> fluidsOutCopy = Lists.newLinkedList();
        components.forEach(r -> fluidsOutCopy.add((RequirementFluid) r));
        this.immulatbleOrderedOutputFluids = ImmutableList.copyOf(fluidsOutCopy);

        components = requirements.stream()
                .filter(c -> c instanceof RequirementItem)
                .filter(c -> c.getActionType() == MachineComponent.IOType.INPUT)
                .collect(Collectors.toList());
        List<RequirementItem> itemsInCopy = Lists.newLinkedList();
        components.forEach(r -> itemsInCopy.add((RequirementItem) r));
        this.immulatbleOrderedInputItems = ImmutableList.copyOf(itemsInCopy);

        components = requirements.stream()
                .filter(c -> c instanceof RequirementItem)
                .filter(c -> c.getActionType() == MachineComponent.IOType.OUTPUT)
                .collect(Collectors.toList());
        List<RequirementItem> itemsOutCopy = Lists.newLinkedList();
        components.forEach(r -> itemsOutCopy.add((RequirementItem) r));
        this.immulatbleOrderedOutputItems = ImmutableList.copyOf(itemsOutCopy);
    }

    @Override
    @Nonnull
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltips = Lists.newArrayList();
        CategoryDynamicRecipe recipeCategory = ModIntegrationJEI.getCategory(recipe.getOwningMachine());
        if(recipeCategory != null && recipeCategory.rectangleProcessArrow.contains(mouseX, mouseY)) {
            tooltips.add(I18n.format("tooltip.machinery.duration", recipe.getRecipeTotalTickTime()));
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

        int totalEnergyIn = 0;
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

        if(totalEnergyIn > 0) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            recipeCategory.inputComponents.stream()
                    .filter(r -> r instanceof RecipeLayoutPart.Energy).forEach(r ->
                    RecipeLayoutHelper.PART_ENERGY_FOREGROUND.drawable.draw(minecraft, r.getOffset().x, r.getOffset().y));

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
            recipeCategory.outputComponents.stream()
                    .filter(r -> r instanceof RecipeLayoutPart.Energy).forEach(r ->
                    RecipeLayoutHelper.PART_ENERGY_FOREGROUND.drawable.draw(minecraft, r.getOffset().x, r.getOffset().y));
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.out"), 8,  offsetY + 10, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.out.tick", totalEnergyOut), 8,  offsetY + 20, 0x222222);
            minecraft.fontRenderer.drawString(I18n.format("tooltip.machinery.energy.out.total", totalEnergyOut * this.recipe.getRecipeTotalTickTime()), 8,  offsetY + 30, 0x222222);
            GlStateManager.color(1F, 1F, 1F, 1F);
            offsetY += 36;
        }

    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        List<List<ItemStack>> applicableStacksIn = Lists.newLinkedList();


        for (RequirementItem itemIn : this.immulatbleOrderedInputItems) {
            applicableStacksIn.add(getIIngredientComponents(itemIn));
        }
        ingredients.setInputLists(ItemStack.class, applicableStacksIn);

        List<HybridFluid> applicableFluidsIn = Lists.newLinkedList();
        for (RequirementFluid fluidIn : this.immulatbleOrderedInputFluids) {
            applicableFluidsIn.add(fluidIn.required);
        }
        ingredients.setInputs(HybridFluid.class, applicableFluidsIn);



        List<List<ItemStack>> applicableStacksOut = Lists.newLinkedList();
        for (RequirementItem itemOut : this.immulatbleOrderedOutputItems) {
            applicableStacksOut.add(getIIngredientComponents(itemOut));
        }
        ingredients.setOutputLists(ItemStack.class, applicableStacksOut);

        List<HybridFluid> applicableFluidsOut = Lists.newLinkedList();
        for (RequirementFluid fluidIn : this.immulatbleOrderedOutputFluids) {
            applicableFluidsOut.add(fluidIn.required);
        }
        ingredients.setOutputs(HybridFluid.class, applicableFluidsOut);
    }

    private List<ItemStack> getIIngredientComponents(RequirementItem itemRequirement) {
        switch (itemRequirement.requirementType) {
            case ITEMSTACKS:
                ItemStack stack = ItemUtils.copyStackWithSize(itemRequirement.required, itemRequirement.required.getCount());
                if(itemRequirement.previewDisplayTag != null) {
                    stack.setTagCompound(itemRequirement.previewDisplayTag);
                } else if(itemRequirement.tag != null) {
                    itemRequirement.previewDisplayTag = itemRequirement.tag.copy();
                    stack.setTagCompound(itemRequirement.previewDisplayTag.copy());
                }
                return Lists.newArrayList(stack);
            case OREDICT:
                NonNullList<ItemStack> stacks = OreDictionary.getOres(itemRequirement.oreDictName);
                NonNullList<ItemStack> out = NonNullList.create();
                for (ItemStack oreDictIn : stacks) {
                    if (oreDictIn.getItemDamage() == OreDictionary.WILDCARD_VALUE && !oreDictIn.isItemStackDamageable() && oreDictIn.getItem().getCreativeTab() != null) {
                        oreDictIn.getItem().getSubItems(oreDictIn.getItem().getCreativeTab(), out);
                    } else {
                        out.add(oreDictIn);
                    }
                }
                NonNullList<ItemStack> stacksOut = NonNullList.create();
                for (ItemStack s : out) {
                    ItemStack copy = s.copy();
                    copy.setCount(itemRequirement.oreDictItemAmount);
                    stacksOut.add(copy);
                }
                return stacksOut;
            case FUEL:
                return FuelItemHelper.getFuelItems();
        }
        return Lists.newArrayList();
    }

}
