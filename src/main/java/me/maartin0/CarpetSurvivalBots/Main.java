package me.maartin0.CarpetSurvivalBots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {

    public final static String MOD_ID = "carpet-survival-bots";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Runnable stop;

    @Override
    public void onInitialize() {
        LOGGER.info("Carpet bots standing by!");
    }
}
