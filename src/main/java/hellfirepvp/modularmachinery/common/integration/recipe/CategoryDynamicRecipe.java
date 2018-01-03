/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluid;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidGas;
import hellfirepvp.modularmachinery.common.integration.ingredient.HybridFluidRenderer;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryDynamicRecipe
 * Created by HellFirePvP
 * Date: 11.07.2017 / 16:58
 */
public class CategoryDynamicRecipe implements IRecipeCategory<DynamicRecipeWrapper> {

    private final DynamicMachine machine;
    private final String category;
    private final String title;
    private final IDrawable sizeEmptyDrawable;

    final int realHeight;

    LinkedList<RecipeLayoutPart> inputComponents  = Lists.newLinkedList();
    LinkedList<RecipeLayoutPart> outputComponents = Lists.newLinkedList();

    private Point offsetProcessArrow;
    Rectangle rectangleProcessArrow;

    public CategoryDynamicRecipe(DynamicMachine machine) {
        this.machine = machine;
        this.category = ModIntegrationJEI.getCategoryStringFor(machine);
        this.title = machine.getLocalizedName();

        Point maxPoint = buildRecipeComponents();
        this.realHeight = maxPoint.y;
        this.sizeEmptyDrawable = ModIntegrationJEI.jeiHelpers.getGuiHelper().createBlankDrawable(maxPoint.x, this.realHeight);
    }

    private Point buildRecipeComponents() {
        Iterable<MachineRecipe> recipes = RecipeRegistry.getRegistry().getRecipesFor(this.machine);
        int offsetX = 8;
        int offsetY = 0;
        int highestY = 0;

        int energyIn = 0, energyOut = 0;
        int fluidIn  = 0, fluidOut  = 0;
        int itemIn   = 0, itemOut   = 0;

        boolean fuelItemIn = false;

        for (MachineRecipe recipe : recipes) {
            List<ComponentRequirement> energyInput = recipe.getCraftingRequirements().stream()
                    .filter(c -> c instanceof ComponentRequirement.RequirementEnergy)
                    .filter(c -> c.getActionType() == MachineComponent.IOType.INPUT)
                    .collect(Collectors.toList());
            List<ComponentRequirement> fluidInput = recipe.getCraftingRequirements().stream()
                    .filter(c -> c instanceof ComponentRequirement.RequirementFluid)
                    .filter(c -> c.getActionType() == MachineComponent.IOType.INPUT)
                    .collect(Collectors.toList());
            List<ComponentRequirement> itemInput = recipe.getCraftingRequirements().stream()
                    .filter(c -> c instanceof ComponentRequirement.RequirementItem)
                    .filter(c -> c.getActionType() == MachineComponent.IOType.INPUT)
                    .collect(Collectors.toList());
            List<ComponentRequirement> itemFuelInput = new ArrayList<>(itemInput).stream()
                    .filter(c -> ((ComponentRequirement.RequirementItem) c).requirementType == ComponentRequirement.ItemRequirementType.FUEL)
                    .collect(Collectors.toList());
            if(energyInput.size() > energyIn) {
                energyIn = energyInput.size();
            }
            if(fluidInput.size() > fluidIn) {
                fluidIn = fluidInput.size();
            }
            if(itemInput.size() > itemIn) {
                itemIn = itemInput.size();
            }
            if(!itemFuelInput.isEmpty()) {
                fuelItemIn = true;
            }

            List<ComponentRequirement> energyOutput = recipe.getCraftingRequirements().stream()
                    .filter(c -> c instanceof ComponentRequirement.RequirementEnergy)
                    .filter(c -> c.getActionType() == MachineComponent.IOType.OUTPUT)
                    .collect(Collectors.toList());
            List<ComponentRequirement> fluidOutput = recipe.getCraftingRequirements().stream()
                    .filter(c -> c instanceof ComponentRequirement.RequirementFluid)
                    .filter(c -> c.getActionType() == MachineComponent.IOType.OUTPUT)
                    .collect(Collectors.toList());
            List<ComponentRequirement> itemOutput = recipe.getCraftingRequirements().stream()
                    .filter(c -> c instanceof ComponentRequirement.RequirementItem)
                    .filter(c -> c.getActionType() == MachineComponent.IOType.OUTPUT)
                    .collect(Collectors.toList());
            if(energyOutput.size() > energyOut) {
                energyOut = energyOutput.size();
            }
            if(fluidOutput.size() > fluidOut) {
                fluidOut = fluidOutput.size();
            }
            if(itemOutput.size() > itemOut) {
                itemOut = itemOutput.size();
            }
        }

        if(energyIn > 0) {
            inputComponents.addLast(new RecipeLayoutPart.Energy(new Point(offsetX, offsetY)));
            offsetX += 22 + 4;
            if(offsetY + 63 > highestY) {
                highestY = offsetY + 63;
            }
        }

        int partOffsetX = offsetX;
        int originalOffsetX = offsetX;
        int partOffsetY = offsetY;
        for (int i = 0; i < fluidIn; i++) {
            if(i > 0 && i % 2 == 0) {
                partOffsetY += 63 + 4;
                partOffsetX = originalOffsetX;
            }
            inputComponents.add(new RecipeLayoutPart.Tank(new Point(partOffsetX, partOffsetY)));
            partOffsetX += 22 + 4;
            if(partOffsetX > offsetX) {
                offsetX = partOffsetX;
            }
            if(partOffsetY + 63 > highestY) {
                highestY = partOffsetY + 63;
            }
        }

        partOffsetX = offsetX;
        originalOffsetX = offsetX;
        partOffsetY = offsetY;
        for (int i = 0; i < itemIn; i++) {
            if(i > 0 && i % 3 == 0) {
                partOffsetY += 18;
                partOffsetX = originalOffsetX;
            }
            inputComponents.add(new RecipeLayoutPart.Item(new Point(partOffsetX, partOffsetY)));
            partOffsetX += 18;
            if(partOffsetX > offsetX) {
                offsetX = partOffsetX;
            }
            if(partOffsetY + 18 > highestY) {
                highestY = partOffsetY + 18;
            }
        }

        offsetX += 4;
        int tempArrowOffsetX = offsetX;
        offsetX += RecipeLayoutHelper.PART_PROCESS_ARROW.xSize;
        offsetX += 4;

        partOffsetX = offsetX;
        originalOffsetX = offsetX;
        partOffsetY = offsetY;
        for (int i = 0; i < itemOut; i++) {
            if(i > 0 && i % 3 == 0) {
                partOffsetY += 18;
                partOffsetX = originalOffsetX;
            }
            outputComponents.add(new RecipeLayoutPart.Item(new Point(partOffsetX, partOffsetY)));
            partOffsetX += 18;
            if(partOffsetX > offsetX) {
                offsetX = partOffsetX;
            }
            if(partOffsetY + 18 > highestY) {
                highestY = partOffsetY + 18;
            }
        }
        if(itemOut > 0) {
            offsetX += 4;
        }

        partOffsetX = offsetX;
        originalOffsetX = offsetX;
        partOffsetY = offsetY;
        for (int i = 0; i < fluidOut; i++) {
            if(i > 0 && i % 2 == 0) {
                partOffsetY += 63 + 4;
                partOffsetX = originalOffsetX;
            }
            outputComponents.add(new RecipeLayoutPart.Tank(new Point(partOffsetX, partOffsetY)));
            partOffsetX += 22 + 4;
            if(partOffsetX > offsetX) {
                offsetX = partOffsetX;
            }
            if(partOffsetY + 63 > highestY) {
                highestY = partOffsetY + 63;
            }
        }

        if(energyOut > 0) {
            outputComponents.add(new RecipeLayoutPart.Energy(new Point(offsetX, offsetY)));
            offsetX += 22 + 4;
            if(offsetY + 63 > highestY) {
                highestY = offsetY + 63;
            }
        }


        int halfY = highestY / 2;
        offsetProcessArrow = new Point(tempArrowOffsetX, halfY / 2);
        rectangleProcessArrow = new Rectangle(offsetProcessArrow.x, offsetProcessArrow.y,
                RecipeLayoutHelper.PART_PROCESS_ARROW.xSize, RecipeLayoutHelper.PART_PROCESS_ARROW.zSize);

        //Texts for input consumed/produced
        if(energyIn > 0) {
            highestY += 36;
        }
        if(energyOut > 0) {
            highestY += 36;
        }
        if(fuelItemIn) {
            highestY += 26;
        }

        return new Point(offsetX, highestY);
    }

    @Override
    public String getUid() {
        return this.category;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getModName() {
        return ModularMachinery.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return this.sizeEmptyDrawable;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        RecipeLayoutHelper.PART_PROCESS_ARROW.drawable.draw(minecraft, offsetProcessArrow.x, offsetProcessArrow.y);
        for (RecipeLayoutPart slot : this.inputComponents) {
            slot.drawBackground(minecraft);
        }
        for (RecipeLayoutPart slot : this.outputComponents) {
            slot.drawBackground(minecraft);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, DynamicRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        int amtItemInputs = 0;

        if(ModularMachinery.isMekanismLoaded) {
            addFluidStacksWithMekanism(recipeLayout, recipeWrapper, ingredients);
        } else {
            addFluidsWithoutMekanism(recipeLayout, recipeWrapper, ingredients);
        }

        int itemSlotIndex = 0;
        for (RecipeLayoutPart slot : this.inputComponents.stream().filter(c -> c instanceof RecipeLayoutPart.Item).collect(Collectors.toList())) {
            Rectangle partSize = slot.getSize();
            itemStacks.init(itemSlotIndex, true, partSize.x, partSize.y);
            itemSlotIndex++;
            amtItemInputs++;
        }
        for (RecipeLayoutPart slot : this.outputComponents.stream().filter(c -> c instanceof RecipeLayoutPart.Item).collect(Collectors.toList())) {
            Rectangle partSize = slot.getSize();
            itemStacks.init(itemSlotIndex, false, partSize.x, partSize.y);
            itemSlotIndex++;
        }

        itemStacks.set(ingredients);
        int finalAmtItemInputs = amtItemInputs;
        itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if(input) {
                if(slotIndex < 0 || slotIndex >= recipeWrapper.immulatbleOrderedInputItems.size()) {
                    return;
                }
                ComponentRequirement.RequirementItem itemReq = recipeWrapper.immulatbleOrderedInputItems.get(slotIndex);
                if(itemReq.requirementType == ComponentRequirement.ItemRequirementType.FUEL) {
                    int burn = TileEntityFurnace.getItemBurnTime(ingredient);
                    if(burn > 0) {
                        tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.machinery.fuel.item", burn));
                    }
                    tooltip.add(I18n.format("tooltip.machinery.fuel"));
                }
                if(itemReq.chance < 1F && itemReq.chance >= 0F) {
                    String chanceStr = String.valueOf(MathHelper.floor(itemReq.chance * 100F));
                    if(itemReq.chance == 0F) {
                        tooltip.add(I18n.format("tooltip.machinery.chance.in.never"));
                    } else {
                        if(itemReq.chance < 0.01F) {
                            chanceStr = "< 1";
                        }
                        chanceStr += "%";
                        tooltip.add(I18n.format("tooltip.machinery.chance.in", chanceStr));
                    }
                }
            } else {
                slotIndex -= finalAmtItemInputs;
                if(slotIndex < 0 || slotIndex >= recipeWrapper.immulatbleOrderedOutputItems.size()) {
                    return;
                }
                ComponentRequirement.RequirementItem itemReq = recipeWrapper.immulatbleOrderedOutputItems.get(slotIndex);
                if(itemReq.chance < 1F && itemReq.chance >= 0F) {
                    String chanceStr = String.valueOf(MathHelper.floor(itemReq.chance * 100F));
                    if(itemReq.chance == 0F) {
                        tooltip.add(I18n.format("tooltip.machinery.chance.out.never"));
                    } else {
                        if(itemReq.chance < 0.01F) {
                            chanceStr = "< 1";
                        }
                        chanceStr += "%";
                        tooltip.add(I18n.format("tooltip.machinery.chance.out", chanceStr));
                    }
                }
            }
        });
    }

    @Optional.Method(modid = "mekanism")
    private void addFluidStacksWithMekanism(IRecipeLayout recipeLayout, DynamicRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiIngredientGroup<HybridFluid> fluidGroup = recipeLayout.getIngredientsGroup(HybridFluid.class);
        HybridFluidRenderer<HybridFluid> hybridFluidRenderer = new HybridFluidRenderer<>();

        //IGuiIngredientGroup<HybridFluidGas> gasGroup = recipeLayout.getIngredientsGroup(HybridFluidGas.class);
        //HybridFluidRenderer<HybridFluidGas> gasRenderer = new HybridFluidRenderer<>();
        int amtFluidInputs = 0;
        int fluidIndex = 0;
        for (RecipeLayoutPart fluidTank : this.inputComponents.stream().filter(c -> c instanceof RecipeLayoutPart.Tank).collect(Collectors.toList())) {
            Rectangle partSize = fluidTank.getSize();
            HybridFluidRenderer<HybridFluid> copy = hybridFluidRenderer.
                    copyPrepareFluidRender(partSize.width, partSize.height, 1000, false, RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            copy = copy.
                    copyPrepareGasRender(partSize.width, partSize.height, 1000, false, RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            fluidGroup.init(fluidIndex, true, copy, partSize.x, partSize.y, partSize.width, partSize.height, 0, 0);
            fluidIndex++;
            amtFluidInputs++;
        }
        for (RecipeLayoutPart fluidTank : this.outputComponents.stream().filter(c -> c instanceof RecipeLayoutPart.Tank).collect(Collectors.toList())) {
            Rectangle partSize = fluidTank.getSize();
            HybridFluidRenderer<HybridFluid> copy = hybridFluidRenderer.
                    copyPrepareFluidRender(partSize.width, partSize.height, 1000, false, RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            copy = copy.
                    copyPrepareGasRender(partSize.width, partSize.height, 1000, false, RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            fluidGroup.init(fluidIndex, false, copy, partSize.x, partSize.y, partSize.width, partSize.height, 0, 0);
            fluidIndex++;
        }


        fluidGroup.set(ingredients);
        int finalAmtFluidInputs = amtFluidInputs;
        fluidGroup.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if(input) {
                if(slotIndex < 0 || slotIndex >= recipeWrapper.immulatbleOrderedInputFluids.size()) {
                    return;
                }
                ComponentRequirement.RequirementFluid fluidReq = recipeWrapper.immulatbleOrderedInputFluids.get(slotIndex);
                if(fluidReq.chance < 1F && fluidReq.chance >= 0F) {
                    String chanceStr = String.valueOf(MathHelper.floor(fluidReq.chance * 100F));
                    if(fluidReq.chance == 0F) {
                        tooltip.add(I18n.format("tooltip.machinery.chance.in.never"));
                    } else {
                        if(fluidReq.chance < 0.01F) {
                            chanceStr = "< 1";
                        }
                        chanceStr += "%";
                        tooltip.add(I18n.format("tooltip.machinery.chance.in", chanceStr));
                    }
                }
            } else {
                slotIndex -= finalAmtFluidInputs;
                if(slotIndex < 0 || slotIndex >= recipeWrapper.immulatbleOrderedOutputFluids.size()) {
                    return;
                }
                ComponentRequirement.RequirementFluid fluidReq = recipeWrapper.immulatbleOrderedOutputFluids.get(slotIndex);
                if(fluidReq.chance < 1F && fluidReq.chance >= 0F) {
                    String chanceStr = String.valueOf(MathHelper.floor(fluidReq.chance * 100F));
                    if(fluidReq.chance == 0F) {
                        tooltip.add(I18n.format("tooltip.machinery.chance.out.never"));
                    } else {
                        if(fluidReq.chance < 0.01F) {
                            chanceStr = "< 1";
                        }
                        chanceStr += "%";
                        tooltip.add(I18n.format("tooltip.machinery.chance.out", chanceStr));
                    }
                }
            }
        });
    }

    private void addFluidsWithoutMekanism(IRecipeLayout recipeLayout, DynamicRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiIngredientGroup<HybridFluid> fluidGroup = recipeLayout.getIngredientsGroup(HybridFluid.class);
        HybridFluidRenderer<HybridFluid> hybridFluidRenderer = new HybridFluidRenderer<>();

        int amtFluidInputs = 0;
        int fluidIndex = 0;
        for (RecipeLayoutPart fluidTank : this.inputComponents.stream().filter(c -> c instanceof RecipeLayoutPart.Tank).collect(Collectors.toList())) {
            Rectangle partSize = fluidTank.getSize();
            HybridFluidRenderer<HybridFluid> copy = hybridFluidRenderer.
                    copyPrepareFluidRender(partSize.width, partSize.height, 1000, false, RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            fluidGroup.init(fluidIndex, true, copy, partSize.x, partSize.y, partSize.width, partSize.height, 0, 0);
            fluidIndex++;
            amtFluidInputs++;
        }
        for (RecipeLayoutPart fluidTank : this.outputComponents.stream().filter(c -> c instanceof RecipeLayoutPart.Tank).collect(Collectors.toList())) {
            Rectangle partSize = fluidTank.getSize();
            HybridFluidRenderer<HybridFluid> copy = hybridFluidRenderer.
                    copyPrepareFluidRender(partSize.width, partSize.height, 1000, false, RecipeLayoutHelper.PART_TANK_SHELL.drawable);
            fluidGroup.init(fluidIndex, false, copy, partSize.x, partSize.y, partSize.width, partSize.height, 0, 0);
            fluidIndex++;
        }

        fluidGroup.set(ingredients);
        int finalAmtFluidInputs = amtFluidInputs;
        fluidGroup.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if(input) {
                if(slotIndex < 0 || slotIndex >= recipeWrapper.immulatbleOrderedInputFluids.size()) {
                    return;
                }
                ComponentRequirement.RequirementFluid fluidReq = recipeWrapper.immulatbleOrderedInputFluids.get(slotIndex);
                if(fluidReq.chance < 1F && fluidReq.chance >= 0F) {
                    String chanceStr = String.valueOf(MathHelper.floor(fluidReq.chance * 100F));
                    if(fluidReq.chance == 0F) {
                        tooltip.add(I18n.format("tooltip.machinery.chance.in.never"));
                    } else {
                        if(fluidReq.chance < 0.01F) {
                            chanceStr = "< 1";
                        }
                        chanceStr += "%";
                        tooltip.add(I18n.format("tooltip.machinery.chance.in", chanceStr));
                    }
                }
            } else {
                slotIndex -= finalAmtFluidInputs;
                if(slotIndex < 0 || slotIndex >= recipeWrapper.immulatbleOrderedOutputFluids.size()) {
                    return;
                }
                ComponentRequirement.RequirementFluid fluidReq = recipeWrapper.immulatbleOrderedOutputFluids.get(slotIndex);
                if(fluidReq.chance < 1F && fluidReq.chance >= 0F) {
                    String chanceStr = String.valueOf(MathHelper.floor(fluidReq.chance * 100F));
                    if(fluidReq.chance == 0F) {
                        tooltip.add(I18n.format("tooltip.machinery.chance.out.never"));
                    } else {
                        if(fluidReq.chance < 0.01F) {
                            chanceStr = "< 1";
                        }
                        chanceStr += "%";
                        tooltip.add(I18n.format("tooltip.machinery.chance.out", chanceStr));
                    }
                }
            }
        });
    }

}
