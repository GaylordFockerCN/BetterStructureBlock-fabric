package com.p1nero.bsb;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterStructureBlock implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("better_structure_block");
	public static final String MOD_ID = "better_structure_block";

	//指令不太ok，world不存在。
//	public static final GameRules.Key<GameRules.BooleanRule> LOAD_IMMEDIATELY=
//			GameRuleRegistry.register("loadStructureWhenLoadStructureBlock", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
//	public static final GameRules.Key<GameRules.BooleanRule> DISABLE_LOAD_MESSAGE=
//			GameRuleRegistry.register("disableStructureBlockLoadSuccessMessage", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));


	@Override
	public void onInitialize() {
		Config.loadConfig();
	}

}