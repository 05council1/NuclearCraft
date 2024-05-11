package nc.recipe.generator;

import com.google.common.collect.*;
import nc.radiation.RadSources;
import nc.recipe.BasicRecipeHandler;
import nc.util.OreDictHelper;

import java.util.*;

import static nc.config.NCConfig.*;

public class DecayGeneratorRecipes extends BasicRecipeHandler {
	
	public DecayGeneratorRecipes() {
		super("decay_generator", 1, 0, 1, 0);
	}
	
	@Override
	public void addRecipes() {
		addDecayRecipes("Thorium", "Lead", decay_lifetime[0], decay_power[0], RadSources.THORIUM);
		addDecayRecipes("Uranium", "Uranium238", decay_lifetime[1], decay_power[1], RadSources.URANIUM);
		
		addDecayRecipes("Uranium238", OreDictHelper.oreExists("Radium") ? "Radium" : "Lead", decay_lifetime[2], decay_power[2], RadSources.URANIUM_238);
		addDecayRecipes("Neptunium237", OreDictHelper.oreExists("Bismuth") ? "Bismuth" : "Lead", decay_lifetime[3], decay_power[3], RadSources.NEPTUNIUM_237);
		addDecayRecipes("Plutonium242", "Uranium238", decay_lifetime[4], decay_power[4], RadSources.PLUTONIUM_242);
		addDecayRecipes("Americium243", "Lead", decay_lifetime[5], decay_power[5], RadSources.AMERICIUM_243);
		addDecayRecipes("Curium246", "Plutonium242", decay_lifetime[6], decay_power[6], RadSources.CURIUM_246);
		addDecayRecipes("Berkelium247", "Americium243", decay_lifetime[7], decay_power[7], RadSources.BERKELIUM_247);
		addDecayRecipes("Californium252", "Lead", decay_lifetime[8], decay_power[8], RadSources.CALIFORNIUM_252);
	}
	
	private static final Set<String> NON_FISSION = Sets.newHashSet("Lead", "Bismuth", "Thorium");
	
	public void addDecayRecipes(String input, String output, double lifetime, double power, double radiation) {
		String inputName = "block" + input;
		if (NON_FISSION.contains(output)) {
			addRecipe(Lists.newArrayList(inputName, inputName + "Oxide", inputName + "Nitride"), "block" + output, lifetime, power, radiation);
		}
		else {
			for (String type : new String[] {"", "Carbide", "Oxide", "Nitride", "ZA"}) {
				addRecipe(inputName + type, "block" + output + type, lifetime, power, radiation);
			}
		}
	}
	
	@Override
	public List<Object> fixedExtras(List<Object> extras) {
		ExtrasFixer fixer = new ExtrasFixer(extras);
		fixer.add(Double.class, 1200D);
		fixer.add(Double.class, 0D);
		fixer.add(Double.class, 0D);
		return fixer.fixed;
	}
}
