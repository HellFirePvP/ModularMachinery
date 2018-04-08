/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirements;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentOutputRestrictor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirements.jei.JEIComponentItem;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementItem
 * Created by HellFirePvP
 * Date: 24.02.2018 / 12:35
 */
public class RequirementItem extends ComponentRequirement<ItemStack> implements ComponentRequirement.ChancedRequirement {

    public final ItemRequirementType requirementType;

    public final ItemStack required;

    public final String oreDictName;
    public final int oreDictItemAmount;

    public final int fuelBurntime;

    public int countIOBuffer = 0;

    public NBTTagCompound tag = null;
    public NBTTagCompound previewDisplayTag = null;

    public float chance = 1F;

    public RequirementItem(MachineComponent.IOType ioType, ItemStack item) {
        super(ComponentType.Registry.getComponent("item"), ioType);
        this.requirementType = ItemRequirementType.ITEMSTACKS;
        this.required = item.copy();
        this.oreDictName = null;
        this.oreDictItemAmount = 0;
        this.fuelBurntime = 0;
    }

    public RequirementItem(MachineComponent.IOType ioType, String oreDictName, int oreDictAmount) {
        super(ComponentType.Registry.getComponent("item"), ioType);
        this.requirementType = ItemRequirementType.OREDICT;
        this.oreDictName = oreDictName;
        this.oreDictItemAmount = oreDictAmount;
        this.required = ItemStack.EMPTY;
        this.fuelBurntime = 0;
    }

    public RequirementItem(MachineComponent.IOType actionType, int fuelBurntime) {
        super(ComponentType.Registry.getComponent("item"), actionType);
        this.requirementType = ItemRequirementType.FUEL;
        this.fuelBurntime = fuelBurntime;
        this.oreDictName = null;
        this.oreDictItemAmount = 0;
        this.required = ItemStack.EMPTY;
    }

    @Override
    public ComponentRequirement deepCopy() {
        RequirementItem item;
        switch (this.requirementType) {
            case OREDICT:
                item = new RequirementItem(this.getActionType(), this.oreDictName, this.oreDictItemAmount);
                break;

            case FUEL:
                item = new RequirementItem(this.getActionType(), this.fuelBurntime);
                break;

            default:
            case ITEMSTACKS:
                item = new RequirementItem(this.getActionType(), this.required.copy());
                break;
        }
        item.chance = this.chance;
        if(this.tag != null) {
            item.tag = this.tag.copy();
        }
        if(this.previewDisplayTag != null) {
            item.previewDisplayTag = this.previewDisplayTag.copy();
        }
        return item;
    }

    @Override
    public JEIComponent<ItemStack> provideJEIComponent() {
        return new JEIComponentItem(this);
    }

    @Override
    public void startRequirementCheck(ResultChance contextChance, RecipeCraftingContext context) {
        switch (this.requirementType) {
            case ITEMSTACKS:
                this.countIOBuffer = this.required.getCount();
                break;
            case OREDICT:
                this.countIOBuffer = this.oreDictItemAmount;
                break;
            case FUEL:
                this.countIOBuffer = this.fuelBurntime;
                break;
        }
        this.countIOBuffer = Math.round(context.applyModifiers(this, getActionType(), this.countIOBuffer, false));
    }

    @Override
    public void endRequirementCheck() {
        this.countIOBuffer = 0;
    }

    @Override
    public RecipeLayoutPart provideRenderableLayoutPart(Point componentOffset) {
        return new RecipeLayoutPart.Item(componentOffset);
    }

    @Override
    public void setChance(float chance) {
        this.chance = chance;
    }

    @Override
    public CraftCheck canStartCrafting(MachineComponent component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        if(!component.getComponentType().equals(this.getRequiredComponentType()) ||
                !(component instanceof MachineComponent.ItemBus) ||
                component.getIOType() != getActionType()) return CraftCheck.INVALID_SKIP;
        IOInventory handler = (IOInventory) context.getProvidedCraftingComponent(component);
        switch (getActionType()) {
            case INPUT:
                switch (this.requirementType) {
                    case ITEMSTACKS:
                        ItemStack inReq = this.required.copy();
                        int amt = Math.round(context.applyModifiers(this, getActionType(), inReq.getCount(), false));
                        inReq.setCount(amt);
                        if(ItemUtils.consumeFromInventory(handler, inReq, true, this.tag)) {
                            return CraftCheck.SUCCESS;
                        }
                        break;
                    case OREDICT:
                        int inOreAmt = Math.round(context.applyModifiers(this, getActionType(), this.oreDictItemAmount, false));
                        if(ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, inOreAmt, true, this.tag)) {
                            return CraftCheck.SUCCESS;
                        }
                        break;
                    case FUEL:
                        int inFuel = Math.round(context.applyModifiers(this, getActionType(), this.fuelBurntime, false));
                        if(ItemUtils.consumeFromInventoryFuel(handler, inFuel, true, this.tag) <= 0) {
                            return CraftCheck.SUCCESS;
                        }
                        break;
                }
                break;
            case OUTPUT:
                handler = CopyHandlerHelper.copyInventory(handler);

                for (ComponentOutputRestrictor restrictor : restrictions) {
                    if(restrictor instanceof ComponentOutputRestrictor.RestrictionInventory) {
                        ComponentOutputRestrictor.RestrictionInventory inv = (ComponentOutputRestrictor.RestrictionInventory) restrictor;

                        if(inv.exactComponent.equals(component)) {
                            ItemUtils.tryPlaceItemInInventory(inv.inserted.copy(), handler, false);
                        }
                    }
                }

                ItemStack stack;
                if(oreDictName != null) {
                    stack = Iterables.getFirst(OreDictionary.getOres(oreDictName), ItemStack.EMPTY);
                    stack = ItemUtils.copyStackWithSize(stack, this.countIOBuffer);
                } else {
                    stack = ItemUtils.copyStackWithSize(required, this.countIOBuffer);
                }

                if(stack.isEmpty()) {
                    return CraftCheck.FAILURE_MISSING_INPUT;
                }

                if(tag != null) {
                    stack.setTagCompound(tag.copy());
                }
                int inserted = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, true);
                if(inserted > 0) {
                    context.addRestriction(new ComponentOutputRestrictor.RestrictionInventory(ItemUtils.copyStackWithSize(stack, inserted), component));
                }
                this.countIOBuffer -= inserted;
                if(this.countIOBuffer <= 0) {
                    return CraftCheck.SUCCESS;
                }
        }
        return CraftCheck.FAILURE_MISSING_INPUT;
    }

    @Override
    public boolean startCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
        if(!component.getComponentType().equals(this.getRequiredComponentType()) ||
                !(component instanceof MachineComponent.ItemBus) ||
                component.getIOType() != getActionType()) return false;
        IOInventory handler = (IOInventory) context.getProvidedCraftingComponent(component);
        float productionChance = context.applyModifiers(this, getActionType(), this.chance, true);
        switch (getActionType()) {
            case INPUT:
                switch (this.requirementType) {
                    //If it doesn't consume the item, we only need to see if it's actually there.
                    case ITEMSTACKS:
                        ItemStack stackRequired = this.required.copy();
                        int amt = Math.round(context.applyModifiers(this, getActionType(), stackRequired.getCount(), false));
                        stackRequired.setCount(amt);
                        boolean can = ItemUtils.consumeFromInventory(handler, stackRequired, true, this.tag);
                        if(chance.canProduce(productionChance)) {
                            return can;
                        }
                        return can && ItemUtils.consumeFromInventory(handler, stackRequired, false, this.tag);
                    case OREDICT:
                        int requiredOredict = Math.round(context.applyModifiers(this, getActionType(), this.oreDictItemAmount, false));
                        can = ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, requiredOredict, true, this.tag);
                        if(chance.canProduce(productionChance)) {
                            return can;
                        }
                        return can && ItemUtils.consumeFromInventoryOreDict(handler, this.oreDictName, requiredOredict, false, this.tag);
                    case FUEL:
                        int requiredBurnTime = Math.round(context.applyModifiers(this, getActionType(), this.fuelBurntime, false));
                        can = ItemUtils.consumeFromInventoryFuel(handler, requiredBurnTime, true, this.tag) <= 0;
                        if(chance.canProduce(productionChance)) {
                            return can;
                        }
                        return can && ItemUtils.consumeFromInventoryFuel(handler, requiredBurnTime, false, this.tag) <= 0;
                }
        }
        return false;
    }

    @Override
    public boolean finishCrafting(MachineComponent component, RecipeCraftingContext context, ResultChance chance) {
        if(!component.getComponentType().equals(this.getRequiredComponentType()) ||
                !(component instanceof MachineComponent.ItemBus) ||
                component.getIOType() != getActionType()) return false;

        if(fuelBurntime > 0 && oreDictName == null && required.isEmpty()) {
            throw new IllegalStateException("Can't output fuel burntime...");
        }
        IOInventory handler = (IOInventory) context.getProvidedCraftingComponent(component);
        switch (getActionType()) {
            case OUTPUT:
                ItemStack stack;
                if(oreDictName != null) {
                    stack = Iterables.getFirst(OreDictionary.getOres(oreDictName), ItemStack.EMPTY);
                    stack = ItemUtils.copyStackWithSize(stack, this.countIOBuffer);
                } else {
                    stack = ItemUtils.copyStackWithSize(required, this.countIOBuffer);
                }

                if(stack.isEmpty()) {
                    return true;
                }
                if(tag != null) {
                    stack.setTagCompound(tag);
                }
                //If we don't produce the item, we only need to see if there would be space for it at all.
                int inserted = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, true);
                if(chance.canProduce(context.applyModifiers(this, getActionType(), this.chance, true))) {
                    return inserted > 0;
                }
                if(inserted > 0) {
                    int actual = ItemUtils.tryPlaceItemInInventory(stack.copy(), handler, false);
                    this.countIOBuffer -= actual;
                    return this.countIOBuffer <= 0;
                }
                return false;
        }
        return false;
    }

    public enum ItemRequirementType {

        ITEMSTACKS,
        OREDICT,
        FUEL

    }

}
