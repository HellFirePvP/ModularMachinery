/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.ingredient;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mekanism.api.gas.GasStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: HybridStackHelper
 * Created by HellFirePvP
 * Date: 27.08.2017 / 10:17
 */
public class HybridStackHelper<T extends HybridFluid> implements IIngredientHelper<T> {

    @Override
    public List<T> expandSubtypes(List<T> ingredients) {
        return ingredients;
    }

    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        if(ModularMachinery.isMekanismLoaded) {
            T gasMatch = attemptGasStackMatch(ingredients, ingredientToMatch);
            if(gasMatch != null) {
                return gasMatch;
            }
        }
        return matchFluidStack(ingredients, ingredientToMatch);
    }

    private T matchFluidStack(Iterable<T> ingredients, T ingredientToMatch) {
        if(Iterables.isEmpty(ingredients)) {
            return null;
        }
        FluidStack stack = ingredientToMatch.asFluidStack();
        if(stack == null) {
            return null;
        }
        Fluid fluidMatch = stack.getFluid();
        for (T hybridFluid : ingredients) {
            FluidStack hybridFluidStack = hybridFluid.asFluidStack();
            if(hybridFluidStack == null) {
                continue;
            }
            if(hybridFluidStack.getFluid() == fluidMatch) {
                return hybridFluid;
            }
        }
        return null;
    }

    @Optional.Method(modid = "mekanism")
    private T attemptGasStackMatch(Iterable<T> ingredients, T ingredientToMatch) {
        if(ingredientToMatch instanceof HybridFluidGas) {
            if(!Iterables.isEmpty(ingredients)) {
                GasStack toMatch = ((HybridFluidGas) ingredientToMatch).asGasStack();
                for (T hybridFluid : ingredients) {
                    if(hybridFluid != null && hybridFluid instanceof HybridFluidGas) {
                        HybridFluidGas hybridFluidGas = (HybridFluidGas) hybridFluid;
                        if(hybridFluidGas.asGasStack().getGas() == toMatch.getGas()) { //Identical check to the GasStackHelper in mek
                            return (T) hybridFluidGas;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            String display = getGasDisplayName(ingredient);
            if(display != null) {
                return display;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return "";
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getDisplayName(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private String getGasDisplayName(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getDisplayName(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }

    @Override
    public String getUniqueId(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            String uniqueId = gasGasUniqueId(ingredient);
            if(uniqueId != null) {
                return uniqueId;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return "";
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getUniqueId(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private String gasGasUniqueId(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getUniqueId(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }

    @Override
    public String getWildcardId(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            String wildcard = getGasWildcardId(ingredient);
            if(wildcard != null) {
                return wildcard;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return "";
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getUniqueId(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private String getGasWildcardId(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getWildcardId(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }

    @Override
    public String getModId(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            String modid = getGasModId(ingredient);
            if(modid != null) {
                return modid;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return "";
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getModId(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private String getGasModId(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getModId(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }

    @Override
    public Iterable<Color> getColors(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            Iterable<Color> gasColors = getGasColors(ingredient);
            if(gasColors != null) {
                return gasColors;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return Lists.newArrayList();
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getColors(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private Iterable<Color> getGasColors(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getColors(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }

    @Override
    public String getResourceId(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            String id = getGasResourceId(ingredient);
            if(id != null) {
                return id;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return "";
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getResourceId(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private String getGasResourceId(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getResourceId(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }

    @Override
    public T copyIngredient(T ingredient) {
        return (T) ingredient.copy();
    }

    @Override
    public ItemStack cheatIngredient(T ingredient, boolean fullStack) {
        if(ModularMachinery.isMekanismLoaded) {
            ItemStack cheated = getGasCheatIngredient(ingredient, fullStack);
            if(cheated != null) {
                return cheated;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return ItemStack.EMPTY;
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.cheatIngredient(fluidStack, fullStack);
    }

    @Optional.Method(modid = "mekanism")
    private ItemStack getGasCheatIngredient(T ingredient, boolean fullStack) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.cheatIngredient(((HybridFluidGas) ingredient).asGasStack(), fullStack);
        }
        return null;
    }

    @Override
    public String getErrorInfo(T ingredient) {
        if(ModularMachinery.isMekanismLoaded) {
            String id = getGasErrorInfo(ingredient);
            if(id != null) {
                return id;
            }
        }
        FluidStack fluidStack = ingredient.asFluidStack();
        if(fluidStack == null) {
            return "";
        }
        IIngredientHelper<FluidStack> fluidHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(FluidStack.class);
        return fluidHelper.getErrorInfo(fluidStack);
    }

    @Optional.Method(modid = "mekanism")
    private String getGasErrorInfo(T ingredient) {
        if(ingredient instanceof HybridFluidGas) {
            IIngredientHelper<GasStack> gasHelper = ModIntegrationJEI.ingredientRegistry.getIngredientHelper(GasStack.class);
            return gasHelper.getErrorInfo(((HybridFluidGas) ingredient).asGasStack());
        }
        return null;
    }
}
