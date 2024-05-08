package nc.recipe;

import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import nc.integration.gtce.GTCERecipeHelper;
import nc.recipe.ingredient.IFluidIngredient;
import nc.recipe.ingredient.IItemIngredient;
import nc.util.NCMath;
import nc.util.NCUtil;
import nc.util.PermutationHelper;
import nc.util.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static nc.config.NCConfig.factor_recipes;
import static nc.config.NCConfig.gtce_recipe_integration;

@ZenClass("mods.nuclearcraft.BasicRecipeHandler")
@ZenRegister
public abstract class BasicRecipeHandler extends AbstractRecipeHandler<BasicRecipe> {
	
	protected final String name;
	protected final int itemInputSize, fluidInputSize, itemOutputSize, fluidOutputSize;
	protected final boolean isShapeless;
	
	public List<List<String>> validFluids = null;
	
	public BasicRecipeHandler(@Nonnull String name, int itemInputSize, int fluidInputSize, int itemOutputSize, int fluidOutputSize) {
		this(name, itemInputSize, fluidInputSize, itemOutputSize, fluidOutputSize, true);
	}
	
	public BasicRecipeHandler(@Nonnull String name, int itemInputSize, int fluidInputSize, int itemOutputSize, int fluidOutputSize, boolean isShapeless) {
		this.name = name;
		this.itemInputSize = itemInputSize;
		this.fluidInputSize = fluidInputSize;
		this.itemOutputSize = itemOutputSize;
		this.fluidOutputSize = fluidOutputSize;
		this.isShapeless = isShapeless;
		addRecipes();
	}
	
	@Override
	public void addRecipe(Object... objects) {
		List<Object> itemInputs = new ArrayList<>(), fluidInputs = new ArrayList<>(), itemOutputs = new ArrayList<>(), fluidOutputs = new ArrayList<>(), extras = new ArrayList<>();
		for (int i = 0; i < objects.length; ++i) {
			Object object = objects[i];
			if (i < itemInputSize) {
				itemInputs.add(object);
			}
			else if (i < itemInputSize + fluidInputSize) {
				fluidInputs.add(object);
			}
			else if (i < itemInputSize + fluidInputSize + itemOutputSize) {
				itemOutputs.add(object);
			}
			else if (i < itemInputSize + fluidInputSize + itemOutputSize + fluidOutputSize) {
				fluidOutputs.add(object);
			}
			else {
				extras.add(object);
			}
		}
		BasicRecipe recipe = buildRecipe(itemInputs, fluidInputs, itemOutputs, fluidOutputs, extras, isShapeless);
		addRecipe(factor_recipes ? factorRecipe(recipe) : recipe);
	}
	
	public BasicRecipe newRecipe(List<IItemIngredient> itemIngredients, List<IFluidIngredient> fluidIngredients, List<IItemIngredient> itemProducts, List<IFluidIngredient> fluidProducts, List<Object> extras, boolean shapeless) {
		return new BasicRecipe(itemIngredients, fluidIngredients, itemProducts, fluidProducts, extras, shapeless);
	}
	
	public void addGTCERecipes() {
		if (GTCE_INTEGRATION.getBoolean(name)) {
			for (BasicRecipe recipe : recipeList) {
				GTCERecipeHelper.addGTCERecipe(name, recipe);
			}
		}
	}
	
	protected abstract List<Object> fixedExtras(List<Object> extras);
	
	public static class ExtrasFixer {
		
		private final List<Object> extras;
		public final List<Object> fixed = new ArrayList<>();
		
		private final int extrasCount;
		private int currentIndex = 0;
		
		public ExtrasFixer(List<Object> extras) {
			this.extras = extras;
			extrasCount = extras.size();
		}
		
		public <T> void add(Class<? extends T> clazz, T defaultValue) {
			int index = currentIndex++;
			Object extra;
			fixed.add(index < extrasCount && clazz.isInstance(extra = extras.get(index)) ? extra : defaultValue);
		}
	}
	
	protected BasicRecipe factorRecipe(BasicRecipe recipe) {
		if (recipe == null) {
			return null;
		}
		if (!recipe.getItemIngredients().isEmpty() || !recipe.getItemProducts().isEmpty()) {
			return recipe;
		}
		
		IntList stackSizes = new IntArrayList();
		for (IFluidIngredient ingredient : recipe.getFluidIngredients()) {
			stackSizes.addAll(ingredient.getFactors());
		}
		for (IFluidIngredient ingredient : recipe.getFluidProducts()) {
			stackSizes.addAll(ingredient.getFactors());
		}
		stackSizes.addAll(getExtraFactors(recipe.getExtras()));
		
		int hcf = NCMath.hcf(stackSizes.toIntArray());
		if (hcf == 1) {
			return recipe;
		}
		
		List<IFluidIngredient> fluidIngredients = new ArrayList<>(), fluidProducts = new ArrayList<>();
		
		for (IFluidIngredient ingredient : recipe.getFluidIngredients()) {
			fluidIngredients.add(ingredient.getFactoredIngredient(hcf));
		}
		for (IFluidIngredient ingredient : recipe.getFluidProducts()) {
			fluidProducts.add(ingredient.getFactoredIngredient(hcf));
		}
		
		return newRecipe(recipe.getItemIngredients(), fluidIngredients, recipe.getItemProducts(), fluidProducts, getFactoredExtras(recipe.getExtras(), hcf), recipe.isShapeless());
	}
	
	protected IntList getExtraFactors(List<Object> extras) {
		return new IntArrayList();
	}
	
	protected List<Object> getFactoredExtras(List<Object> extras, int factor) {
		return extras;
	}
	
	private static final Object2BooleanMap<String> GTCE_INTEGRATION = new Object2BooleanOpenHashMap<>();
	
	public static void initGTCEIntegration() {
		GTCE_INTEGRATION.put("manufactory", gtce_recipe_integration[0]);
		GTCE_INTEGRATION.put("separator", gtce_recipe_integration[1]);
		GTCE_INTEGRATION.put("decay_hastener", gtce_recipe_integration[2]);
		GTCE_INTEGRATION.put("fuel_reprocessor", gtce_recipe_integration[3]);
		GTCE_INTEGRATION.put("alloy_furnace", gtce_recipe_integration[4]);
		GTCE_INTEGRATION.put("infuser", gtce_recipe_integration[5]);
		GTCE_INTEGRATION.put("melter", gtce_recipe_integration[6]);
		GTCE_INTEGRATION.put("supercooler", gtce_recipe_integration[7]);
		GTCE_INTEGRATION.put("electrolyzer", gtce_recipe_integration[8]);
		GTCE_INTEGRATION.put("assembler", gtce_recipe_integration[9]);
		GTCE_INTEGRATION.put("ingot_former", gtce_recipe_integration[10]);
		GTCE_INTEGRATION.put("pressurizer", gtce_recipe_integration[11]);
		GTCE_INTEGRATION.put("chemical_reactor", gtce_recipe_integration[12]);
		GTCE_INTEGRATION.put("salt_mixer", gtce_recipe_integration[13]);
		GTCE_INTEGRATION.put("crystallizer", gtce_recipe_integration[14]);
		GTCE_INTEGRATION.put("enricher", gtce_recipe_integration[15]);
		GTCE_INTEGRATION.put("extractor", gtce_recipe_integration[16]);
		GTCE_INTEGRATION.put("centrifuge", gtce_recipe_integration[17]);
		GTCE_INTEGRATION.put("rock_crusher", gtce_recipe_integration[18]);
		GTCE_INTEGRATION.put("electric_furnace", gtce_recipe_integration[19]);
	}
	
	@Nullable
	public BasicRecipe buildRecipe(List<?> itemInputs, List<?> fluidInputs, List<?> itemOutputs, List<?> fluidOutputs, List<Object> extras, boolean shapeless) {
		List<IItemIngredient> itemIngredients = new ArrayList<>(), itemProducts = new ArrayList<>();
		List<IFluidIngredient> fluidIngredients = new ArrayList<>(), fluidProducts = new ArrayList<>();
		for (Object obj : itemInputs) {
			if (obj != null && isValidItemInputType(obj)) {
				IItemIngredient input = RecipeHelper.buildItemIngredient(obj);
				if (input == null) {
					return null;
				}
				itemIngredients.add(input);
			}
			else {
				return null;
			}
		}
		for (Object obj : fluidInputs) {
			if (obj != null && isValidFluidInputType(obj)) {
				IFluidIngredient input = RecipeHelper.buildFluidIngredient(obj);
				if (input == null) {
					return null;
				}
				fluidIngredients.add(input);
			}
			else {
				return null;
			}
		}
		for (Object obj : itemOutputs) {
			if (obj != null && isValidItemOutputType(obj)) {
				IItemIngredient output = RecipeHelper.buildItemIngredient(obj);
				if (output == null) {
					return null;
				}
				itemProducts.add(output);
			}
			else {
				return null;
			}
		}
		for (Object obj : fluidOutputs) {
			if (obj != null && isValidFluidOutputType(obj)) {
				IFluidIngredient output = RecipeHelper.buildFluidIngredient(obj);
				if (output == null) {
					return null;
				}
				fluidProducts.add(output);
			}
			else {
				return null;
			}
		}
		if (!isValidRecipe(itemIngredients, fluidIngredients, itemProducts, fluidProducts)) {
			NCUtil.getLogger().info(name + " - a recipe failed to be registered: " + RecipeHelper.getRecipeString(itemIngredients, fluidIngredients, itemProducts, fluidProducts));
		}
		return newRecipe(itemIngredients, fluidIngredients, itemProducts, fluidProducts, fixedExtras(extras), shapeless);
	}
	
	public boolean isValidRecipe(List<IItemIngredient> itemIngredients, List<IFluidIngredient> fluidIngredients, List<IItemIngredient> itemProducts, List<IFluidIngredient> fluidProducts) {
		return itemIngredients.size() == itemInputSize && fluidIngredients.size() == fluidInputSize && itemProducts.size() == itemOutputSize && fluidProducts.size() == fluidOutputSize;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	@ZenMethod
	public List<BasicRecipe> getRecipeList() {
		return recipeList;
	}
	
	@ZenMethod
	public int getItemInputSize() {
		return itemInputSize;
	}
	
	@ZenMethod
	public int getFluidInputSize() {
		return fluidInputSize;
	}
	
	@ZenMethod
	public int getItemOutputSize() {
		return itemOutputSize;
	}
	
	@ZenMethod
	public int getFluidOutputSize() {
		return fluidOutputSize;
	}
	
	@ZenMethod
	public boolean isShapeless() {
		return isShapeless;
	}
	
	@Override
	public void init() {
		super.init();
		validFluids = RecipeHelper.validFluids(this);
	}
	
	@Override
	protected void fillHashCache() {
		for (BasicRecipe recipe : recipeList) {
			List<Pair<List<ItemStack>, List<FluidStack>>> materialListTuples = new ArrayList<>();
			
			if (!prepareMaterialListTuples(recipe, materialListTuples)) {
				continue;
			}
			
			for (Pair<List<ItemStack>, List<FluidStack>> materials : materialListTuples) {
				if (isShapeless) {
					for (List<ItemStack> items : PermutationHelper.permutations(materials.getLeft())) {
						for (List<FluidStack> fluids : PermutationHelper.permutations(materials.getRight())) {
							addToHashCache(recipe, items, fluids);
						}
					}
				}
				else {
					addToHashCache(recipe, materials.getLeft(), materials.getRight());
				}
			}
		}
	}
	
	protected void addToHashCache(BasicRecipe recipe, List<ItemStack> items, List<FluidStack> fluids) {
		long hash = RecipeHelper.hashMaterials(items, fluids);
		if (recipeCache.containsKey(hash)) {
			recipeCache.get(hash).add(recipe);
		}
		else {
			ObjectSet<BasicRecipe> set = new ObjectOpenHashSet<>();
			set.add(recipe);
			recipeCache.put(hash, set);
		}
	}
	
	public boolean isValidItemInput(int slot, ItemStack stack) {
		for (BasicRecipe recipe : recipeList) {
			if (isShapeless) {
				for (IItemIngredient input : recipe.getItemIngredients()) {
					if (input.match(stack, IngredientSorption.NEUTRAL).matches()) {
						return true;
					}
				}
			}
			else {
				if (recipe.getItemIngredients().get(slot).match(stack, IngredientSorption.NEUTRAL).matches()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isValidFluidInput(int tankNumber, FluidStack stack) {
		for (BasicRecipe recipe : recipeList) {
			if (isShapeless) {
				for (IFluidIngredient input : recipe.getFluidIngredients()) {
					if (input.match(stack, IngredientSorption.NEUTRAL).matches()) {
						return true;
					}
				}
			}
			else {
				if (recipe.getFluidIngredients().get(tankNumber).match(stack, IngredientSorption.NEUTRAL).matches()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** Smart item insertion - don't insert if matching item is already present in another input slot */
	public boolean isValidItemInput(int slot, ItemStack stack, RecipeInfo<BasicRecipe> recipeInfo, List<ItemStack> allInputs, List<ItemStack> otherInputs) {
		ItemStack slotStack = allInputs.get(slot);
		if (otherInputs.isEmpty() || (stack.isItemEqual(slotStack) && StackHelper.areItemStackTagsEqual(stack, slotStack))) {
			return isValidItemInput(slot, stack);
		}
		
		boolean othersAllEmpty = true;
		for (ItemStack otherInput : otherInputs) {
			if (!otherInput.isEmpty()) {
				othersAllEmpty = false;
				break;
			}
		}
		if (othersAllEmpty) {
			return isValidItemInput(slot, stack);
		}
		
		if (recipeInfo == null) {
			List<BasicRecipe> recipes = new ArrayList<>(recipeList);
			recipeLoop: for (BasicRecipe recipe : recipeList) {
				if (isShapeless) {
					stackLoop: for (ItemStack inputStack : allInputs) {
						if (!inputStack.isEmpty()) {
							for (IItemIngredient recipeInput : recipe.getItemIngredients()) {
								if (recipeInput.match(inputStack, IngredientSorption.NEUTRAL).matches()) {
									continue stackLoop;
								}
							}
							recipes.remove(recipe);
							continue recipeLoop;
						}
					}
				}
				else {
					for (int i = 0; i < itemInputSize; ++i) {
						ItemStack inputStack = allInputs.get(i);
						if (!inputStack.isEmpty() && !recipe.getItemIngredients().get(i).match(inputStack, IngredientSorption.NEUTRAL).matches()) {
							recipes.remove(recipe);
							continue recipeLoop;
						}
					}
				}
			}
			
			for (BasicRecipe recipe : recipes) {
				if (isValidItemInputInternal(slot, stack, recipe, otherInputs)) {
					return true;
				}
			}
			return false;
		}
		else {
			return isValidItemInputInternal(slot, stack, recipeInfo.recipe, otherInputs);
		}
	}
	
	protected boolean isValidItemInputInternal(int slot, ItemStack stack, BasicRecipe recipe, List<ItemStack> otherInputs) {
		if (isShapeless) {
			for (IItemIngredient input : recipe.getItemIngredients()) {
				if (input.match(stack, IngredientSorption.NEUTRAL).matches()) {
					for (ItemStack other : otherInputs) {
						if (!other.isEmpty() && input.match(other, IngredientSorption.NEUTRAL).matches()) {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}
		else {
			return recipe.getItemIngredients().get(slot).match(stack, IngredientSorption.NEUTRAL).matches();
		}
	}
}
