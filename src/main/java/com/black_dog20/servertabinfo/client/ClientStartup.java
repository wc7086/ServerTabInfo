package com.black_dog20.servertabinfo.client;

import com.black_dog20.bml.client.overlay.OverlayRegistry;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.client.keybinds.Keybinds;
import com.black_dog20.servertabinfo.client.overlays.PlayerListOverlay;
import com.black_dog20.servertabinfo.client.overlays.TpsListOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod.EventBusSubscriber( modid = ServerTabInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientStartup {

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        OverlayRegistry.register(new PlayerListOverlay());
        OverlayRegistry.register(new TpsListOverlay());
    }

    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.SHOW);
    }
}
