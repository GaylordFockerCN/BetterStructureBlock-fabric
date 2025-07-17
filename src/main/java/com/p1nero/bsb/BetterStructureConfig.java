package com.p1nero.bsb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BetterStructureConfig {
    public static boolean LOAD_IMMEDIATELY = false;
    public static boolean DISABLE_LOAD_MESSAGE = false;
    public static boolean DESTROY_AFTER_LOAD = false;
    public static final String JSON = BetterStructureBlock.MOD_ID + ".json";

    public static final Logger LOGGER = LoggerFactory.getLogger("better_structure_block");
    public static void loadConfig() {
        File configFolder = new File("config" + File.separator + BetterStructureBlock.MOD_ID);
        if (!configFolder.exists()) {
            if(!configFolder.mkdirs()){
                BetterStructureBlock.LOGGER.info("Failed to create config folder.");
                return;
            }
        }

        File configFile = new File(configFolder, JSON);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                BetterStructureBlock.LOGGER.info("Loading configuration file...");
                JsonObject config = new Gson().fromJson(reader, JsonObject.class);
                LOAD_IMMEDIATELY = config.get("LOAD_IMMEDIATELY").getAsBoolean();
                DISABLE_LOAD_MESSAGE = config.get("DISABLE_LOAD_MESSAGE").getAsBoolean();
                DESTROY_AFTER_LOAD = config.get("DESTROY_AFTER_LOAD").getAsBoolean();
            } catch (IOException e) {
                BetterStructureBlock.LOGGER.error("Failed to load configuration file!{}", String.valueOf(e));
            }
        } else {
            try {
                BetterStructureBlock.LOGGER.info("Generating configuration file...");
                if(configFile.createNewFile()){
                    JsonObject config = new JsonObject();
                    config.addProperty("LOAD_IMMEDIATELY", LOAD_IMMEDIATELY);
                    config.addProperty("DISABLE_LOAD_MESSAGE", DISABLE_LOAD_MESSAGE);
                    config.addProperty("DESTROY_AFTER_LOAD", DESTROY_AFTER_LOAD);
                    try (FileWriter writer = new FileWriter(configFile)) {
                        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
                    }
                } else {
                    BetterStructureBlock.LOGGER.info("Error generating configuration file!");
                }
            } catch (IOException e) {
                BetterStructureBlock.LOGGER.info("Error generating configuration file!{}", String.valueOf(e));
            }
        }
    }
}