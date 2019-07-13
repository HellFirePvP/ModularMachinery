/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementTypeItem
 * Created by HellFirePvP
 * Date: 13.07.2019 / 10:45
 */
public class RequirementTypeItem extends RequirementType<ItemStack, RequirementItem> {

    @Override
    public RequirementItem createRequirement(IOType type, JsonObject jsonObject) {
        RequirementItem req;

        if(!jsonObject.has("item") || !jsonObject.get("item").isJsonPrimitive() ||
                !jsonObject.get("item").getAsJsonPrimitive().isString()) {
            throw new JsonParseException("The ComponentType 'item' expects an 'item'-entry that defines the item!");
        }
        String itemDefinition = jsonObject.getAsJsonPrimitive("item").getAsString();
        int meta = 0;
        int indexMeta = itemDefinition.indexOf('@');
        if(indexMeta != -1 && indexMeta != itemDefinition.length() - 1) {
            try {
                meta = Integer.parseInt(itemDefinition.substring(indexMeta + 1));
            } catch (NumberFormatException exc) {
                throw new JsonParseException("Expected a metadata number, got " + itemDefinition.substring(indexMeta + 1), exc);
            }
            itemDefinition = itemDefinition.substring(0, indexMeta);
        }
        int amount = 1;
        if(jsonObject.has("amount")) {
            if(!jsonObject.get("amount").isJsonPrimitive() || !jsonObject.getAsJsonPrimitive("amount").isNumber()) {
                throw new JsonParseException("'amount', if defined, needs to be a amount-number!");
            }
            amount = MathHelper.clamp(jsonObject.getAsJsonPrimitive("amount").getAsInt(), 1, 64);
        }

        ResourceLocation res = new ResourceLocation(itemDefinition);
        if(res.getResourceDomain().equalsIgnoreCase("any") && res.getResourcePath().equalsIgnoreCase("fuel")) {
            if(type == IOType.OUTPUT) {
                throw new JsonParseException("You cannot define 'fuel' as item output! Offending item-output entry: " + res.toString());
            }
            if(!jsonObject.has("time") || !jsonObject.get("time").isJsonPrimitive() ||
                    !jsonObject.get("time").getAsJsonPrimitive().isNumber()) {
                throw new JsonParseException("If you define any:fuel as item input, you have to define the burntime required in total in the 'time' entry alongside the 'item' entry!");
            }
            int burntime = jsonObject.getAsJsonPrimitive("time").getAsInt();
            req = new RequirementItem(type, burntime);
        } else if(res.getResourceDomain().equalsIgnoreCase("ore")) {
            req = new RequirementItem(type, itemDefinition.substring(4), amount);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(res);
            if(item == null || item == Items.AIR) {
                throw new JsonParseException("Couldn't find item with registryName '" + res.toString() + "' !");
            }
            ItemStack result;
            if(meta > 0) {
                result = new ItemStack(item, amount, meta);
            } else {
                result = new ItemStack(item, amount);
            }
            req = new RequirementItem(type, result);
        }
        if(jsonObject.has("chance")) {
            if(!jsonObject.get("chance").isJsonPrimitive() || !jsonObject.getAsJsonPrimitive("chance").isNumber()) {
                throw new JsonParseException("'chance', if defined, needs to be a chance-number between 0 and 1!");
            }
            float chance = jsonObject.getAsJsonPrimitive("chance").getAsFloat();
            if(chance >= 0 && chance <= 1) {
                req.setChance(chance);
            }
        }
        if (jsonObject.has("nbt")) {
            if(!jsonObject.has("nbt") || !jsonObject.get("nbt").isJsonObject()) {
                throw new JsonParseException("The ComponentType 'nbt' expects a json compound that defines the NBT tag!");
            }
            String nbtString = jsonObject.getAsJsonObject("nbt").toString();
            try {
                req.tag = NBTJsonDeserializer.deserialize(nbtString);
            } catch (NBTException exc) {
                throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
            }

            if (jsonObject.has("nbt-display")) {
                if(!jsonObject.has("nbt-display") || !jsonObject.get("nbt-display").isJsonObject()) {
                    throw new JsonParseException("The ComponentType 'nbt-display' expects a json compound that defines the NBT tag meant to be used for displaying!");
                }
                String nbtDisplayString = jsonObject.getAsJsonObject("nbt-display").toString();
                try {
                    req.previewDisplayTag = NBTJsonDeserializer.deserialize(nbtDisplayString);
                } catch (NBTException exc) {
                    throw new JsonParseException("Error trying to parse NBTTag! Rethrowing exception...", exc);
                }
            } else {
                req.previewDisplayTag = req.tag.copy();
            }
        }
        return req;
    }

}
