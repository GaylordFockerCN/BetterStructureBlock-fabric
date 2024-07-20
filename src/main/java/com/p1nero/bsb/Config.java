package com.p1nero.bsb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    public static boolean LOAD_IMMEDIATELY = false;
    public static boolean DISABLE_LOAD_MESSAGE = true;
//    public static int SEARCH_SIZE = 80;
    public static final String JSON = BetterStructureBlock.MOD_ID + ".json";

    public static void loadConfig() {
        File configFolder = new File("config" + File.separator + BetterStructureBlock.MOD_ID);
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File configFile = new File(configFolder, JSON);
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                BetterStructureBlock.LOGGER.info("Loading configuration file...");
                JsonObject config = new Gson().fromJson(reader, JsonObject.class);
                LOAD_IMMEDIATELY = config.get("LOAD_IMMEDIATELY").getAsBoolean();
                DISABLE_LOAD_MESSAGE = config.get("DISABLE_LOAD_MESSAGE").getAsBoolean();
//                SEARCH_SIZE = config.get("SEARCH_SIZE").getAsInt();
            } catch (IOException e) {
                BetterStructureBlock.LOGGER.error("Failed to load configuration file!" + e);
            }
        } else {
            try {
                BetterStructureBlock.LOGGER.info("Generating configuration file...");
                configFile.createNewFile();
                JsonObject config = new JsonObject();
                config.addProperty("LOAD_IMMEDIATELY", LOAD_IMMEDIATELY);
                config.addProperty("DISABLE_LOAD_MESSAGE", DISABLE_LOAD_MESSAGE);
//                config.addProperty("SEARCH_SIZE", SEARCH_SIZE);
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
                }
            } catch (IOException e) {
                BetterStructureBlock.LOGGER.info("Error generating configuration file!" + e);
            }
        }
    }
}