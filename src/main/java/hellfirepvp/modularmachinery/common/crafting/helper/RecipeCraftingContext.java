/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.command.ControllerCommandSender;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.ModifierReplacement;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.util.PriorityProvider;
import hellfirepvp.modularmachinery.common.util.ResultChance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeCraftingContext
 * Created by HellFirePvP
 * Date: 28.06.2017 / 12:23
 */
public class RecipeCraftingContext {

    private static final Random RAND = new Random();

    private final MachineRecipe recipe;
    private final TileMachineController machineController;
    private final ControllerCommandSender commandSender;

    private int currentCraftingTick = 0;
    private Map<String, Map<MachineComponent<?>, ProcessingComponent<?>>> typeComponents = new HashMap<>();
    private Map<String, List<RecipeModifier>> modifiers = new HashMap<>();

    private List<ComponentOutputRestrictor> currentRestrictions = Lists.newArrayList();

    public RecipeCraftingContext(MachineRecipe recipe, TileMachineController controller) {
        this.recipe = recipe;
        this.machineController = controller;
        this.commandSender = new ControllerCommandSender(machineController);
    }

    public TileMachineController getMachineController() {
        return machineController;
    }

    public MachineRecipe getParentRecipe() {
        return recipe;
    }

    public void setCurrentCraftingTick(int currentCraftingTick) {
        this.currentCraftingTick = currentCraftingTick;
    }

    public int getCurrentCraftingTick() {
        return currentCraftingTick;
    }

    @Nonnull
    public List<RecipeModifier> getModifiers(String target) {
        return modifiers.computeIfAbsent(target, t -> new LinkedList<>());
    }

    public float getDurationMultiplier() {
        float dur = this.recipe.getRecipeTotalTickTime();
        float result = RecipeModifier.applyModifiers(getModifiers(RecipeModifier.TARGET_DURATION), RecipeModifier.TARGET_DURATION, null, dur, false);
        return dur / result;
    }

    public void addRestriction(ComponentOutputRestrictor restrictor) {
        this.currentRestrictions.add(restrictor);
    }

    public Iterable<ProcessingComponent<?>> getComponentsFor(ComponentType type, @Nullable ComponentSelectorTag tag) {
        String key = type.getRegistryName();
        if(key.equalsIgnoreCase("gas")) {
            key = "fluid";
        }
        Collection<ProcessingComponent<?>> components = this.typeComponents.computeIfAbsent(key, s -> new HashMap<>()).values();
        if (tag == null) {
            return components;
        } else {
            return new PriorityProvider<>(components, compList -> Iterators.tryFind(compList.iterator(), comp -> tag.equals(comp.getTag())).orNull());
        }
    }

    public CraftingCheckResult ioTick(int currentTick) {
        float durMultiplier = this.getDurationMultiplier();

        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(!(requirement instanceof ComponentRequirement.PerTick) ||
                    requirement.getActionType() == MachineComponent.IOType.OUTPUT) continue;
            ComponentRequirement.PerTick perTickRequirement = (ComponentRequirement.PerTick) requirement;

            perTickRequirement.resetIOTick(this);
            perTickRequirement.startIOTick(this, durMultiplier);

            for (ProcessingComponent<?> component : getComponentsFor(requirement.getRequiredComponentType(), requirement.getTag())) {
                CraftCheck result = perTickRequirement.doIOTick(component.getComponent(), this);
                if (result.isSuccess()) {
                    break;
                }
            }

            CraftCheck result = perTickRequirement.resetIOTick(this);
            if(!result.isSuccess()) {
                CraftingCheckResult res = new CraftingCheckResult();
                res.addError(result.getUnlocalizedMessage());
                return res;
            }
        }

        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(!(requirement instanceof ComponentRequirement.PerTick) ||
                    requirement.getActionType() == MachineComponent.IOType.INPUT) continue;
            ComponentRequirement.PerTick perTickRequirement = (ComponentRequirement.PerTick) requirement;

            perTickRequirement.resetIOTick(this);
            perTickRequirement.startIOTick(this, durMultiplier);

            for (ProcessingComponent<?> component : getComponentsFor(requirement.getRequiredComponentType(), requirement.getTag())) {
                CraftCheck result = perTickRequirement.doIOTick(component.getComponent(), this);
                if (result.isSuccess()) {
                    break;
                }
            }
            perTickRequirement.resetIOTick(this);
        }

        this.recipe.getCommandContainer().runTickCommands(this.commandSender, currentTick);

        return CraftingCheckResult.SUCCESS;
    }

    public void startCrafting() {
        startCrafting(RAND.nextLong());
    }

    public void startCrafting(long seed) {
        ResultChance chance = new ResultChance(seed);
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getActionType() == MachineComponent.IOType.OUTPUT) continue;

            requirement.startRequirementCheck(chance, this);
            for (ProcessingComponent<?> component : getComponentsFor(requirement.getRequiredComponentType(), requirement.getTag())) {
                if(requirement.startCrafting(component.getComponent(), this, chance)) {
                    requirement.endRequirementCheck();
                    break;
                }
            }
            requirement.endRequirementCheck();
        }

        this.recipe.getCommandContainer().runStartCommands(this.commandSender);
    }

    public void finishCrafting() {
        finishCrafting(RAND.nextLong());
    }

    public void finishCrafting(long seed) {
        ResultChance chance = new ResultChance(seed);
        for (ComponentRequirement requirement : this.recipe.getCraftingRequirements()) {
            if(requirement.getActionType() == MachineComponent.IOType.INPUT) continue;

            requirement.startRequirementCheck(chance, this);
            for (ProcessingComponent<?> component : getComponentsFor(requirement.getRequiredComponentType(), requirement.getTag())) {
                if(requirement.finishCrafting(component.getComponent(), this, chance)) {
                    requirement.endRequirementCheck();
                    break;
                }
            }
            requirement.endRequirementCheck();
        }

        this.recipe.getCommandContainer().runFinishCommands(this.commandSender);
    }

    public CraftingCheckResult canStartCrafting() {
        currentRestrictions.clear();
        CraftingCheckResult result = new CraftingCheckResult();
        float successfulRequirements = 0;
        float requirements = recipe.getCraftingRequirements().size();

        lblRequirements:
        for (ComponentRequirement requirement : recipe.getCraftingRequirements()) {

            requirement.startRequirementCheck(ResultChance.GUARANTEED, this);

            Iterable<ProcessingComponent<?>> components = getComponentsFor(requirement.getRequiredComponentType(), requirement.getTag());
            if (!Iterables.isEmpty(components)) {
                for (ProcessingComponent<?> component : components) {
                    CraftCheck check = requirement.canStartCrafting(component.getComponent(), this, this.currentRestrictions);

                    if (check.isSuccess()) {
                        requirement.endRequirementCheck();
                        successfulRequirements += 1;
                        continue lblRequirements;
                    }
                    if (!check.isInvalid() && !check.getUnlocalizedMessage().isEmpty()) {
                        result.addError(check.getUnlocalizedMessage());
                    }
                }
            } else {
                // No component found that would apply for the given requirement
                result.addError(requirement.getRequiredComponentType()
                        .getMissingComponentErrorMessage(requirement.getActionType()));
            }

            requirement.endRequirementCheck();
        }
        result.setValidity(successfulRequirements / requirements);

        currentRestrictions.clear();
        return result;
    }

    public <T> void addComponent(MachineComponent<T> component, @Nullable ComponentSelectorTag tag) {
        Map<MachineComponent<?>, ProcessingComponent<?>> components = this.typeComponents
                .computeIfAbsent(component.getComponentType().getRegistryName(), (s) -> new HashMap<>());

        components.put(component, new ProcessingComponent<>(component, component.getContainerProvider(), tag));
    }

    public void addModifier(ModifierReplacement modifier) {
        List<RecipeModifier> modifiers = modifier.getModifiers();
        for (RecipeModifier mod : modifiers) {
            this.modifiers.computeIfAbsent(mod.getTarget(), target -> new LinkedList<>()).add(mod);
        }
    }

    @Nullable
    public Object getProvidedCraftingComponent(MachineComponent component) {
        Map<MachineComponent<?>, ProcessingComponent<?>> components = this.typeComponents
                .computeIfAbsent(component.getComponentType().getRegistryName(), (s) -> new HashMap<>());

        ProcessingComponent<?> processingComponent = components.getOrDefault(component, null);
        return processingComponent != null ? processingComponent.getProvidedComponent() : null;
    }

    public static class CraftingCheckResult {

        private static final CraftingCheckResult SUCCESS = new CraftingCheckResult();

        private Map<String, Integer> unlocErrorMessages = new HashMap<>();
        private float validity = 0F;

        private CraftingCheckResult() {}

        private void setValidity(float validity) {
            this.validity = validity;
        }

        private void addError(String unlocError) {
            if (!unlocError.isEmpty()) {
                int count = this.unlocErrorMessages.getOrDefault(unlocError, 0);
                count++;
                this.unlocErrorMessages.put(unlocError, count);
            }
        }

        public float getValidity() {
            return validity;
        }

        public List<String> getUnlocalizedErrorMessages() {
            return this.unlocErrorMessages.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        public boolean isFailure() {
            return !this.unlocErrorMessages.isEmpty();
        }

    }

}
