package com.black_dog20.servertabinfo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ServerTabInfo.MOD_ID)
public class ServerTabInfo {

    public static final String MOD_ID = "servertabinfo";
	public static final Logger LOGGER = LogManager.getLogger();
	
	public ServerTabInfo(IEventBus event) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-client.toml"));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        Config.loadConfig(Config.SERVER_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-server.toml"));
    }

    public static String getVersion() {
        ModContainer container = ModList.get().getModContainerById(MOD_ID).orElse(null);

        if(container != null)
            return container.getModInfo().getVersion().toString();

        return "@Version@";
    }
}
