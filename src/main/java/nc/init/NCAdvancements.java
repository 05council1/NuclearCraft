package nc.init;

import nc.advancement.*;
import net.minecraft.advancements.CriteriaTriggers;

public class NCAdvancements {
	
	public static void init() {
		for (NCCriterionTrigger trigger : NCCriterions.CRITERION_TRIGGERS) {
			CriteriaTriggers.register(trigger);
		}
	}
}
