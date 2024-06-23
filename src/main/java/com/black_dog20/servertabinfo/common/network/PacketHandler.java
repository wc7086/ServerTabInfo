package com.black_dog20.servertabinfo.common.network;

import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.common.network.packets.PacketDimensions;
import com.black_dog20.servertabinfo.common.network.packets.PacketInform;
import com.black_dog20.servertabinfo.common.network.packets.PacketPlayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(modid = ServerTabInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacketHandler {

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(ServerTabInfo.MOD_ID)
				.optional()
				.configuration(PacketInform.ID, PacketInform.Data::new, new PacketInform().handlers())
				.play(PacketDimensions.ID, PacketDimensions.Data::new, new PacketDimensions().handlers())
				.play(PacketPlayers.ID, PacketPlayers.Data::new, new PacketPlayers().handlers());
	}

	@SubscribeEvent
	public static void register(final OnGameConfigurationEvent event) {
		event.register(new PacketInform.Task(event.getListener()));
	}
}
