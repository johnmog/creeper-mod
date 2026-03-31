package com.johnmog.creepermod;

import com.johnmog.creepermod.events.CreeperEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(CreeperMod.MOD_ID)
public class CreeperMod {

    public static final String MOD_ID = "creepermod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreeperMod() {
        MinecraftForge.EVENT_BUS.register(new CreeperEventHandler());
        LOGGER.info("Creeper Mod initialized - Creepers now rule the world!");
    }
}
