package com.p1nero.bsb;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterStructureBlock implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("better_structure_block");
	public static final String MOD_ID = "better_structure_block";

	@Override
	public void onInitialize() {
		BetterStructureConfig.loadConfig();
	}

}