package com.black_dog20.servertabinfo.common.events;

import com.black_dog20.bml.utils.network.NetworkUtils;
import com.black_dog20.servertabinfo.Config;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.common.network.packets.PacketDimensions;
import com.black_dog20.servertabinfo.common.network.packets.PacketPlayers;
import com.black_dog20.servertabinfo.common.utils.Dimension;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber( modid = ServerTabInfo.MOD_ID)
public class ServerEvents {

    private static List<Dimension> DIMENSIONS = new LinkedList<>();
    private static Map<UUID, ResourceLocation> PLAYER_DIMENSIONS = new HashMap<>();
    private static int ticks = 0;

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayerCount() == 0){
            ticks = 0;
            return;
        }

        if(ticks % Config.REFRESH_TICKS.get() == 0) {
            Pair<Integer, Double> overall = getTpsAndMean(server.getTickTimesNanos());
            DIMENSIONS.clear();
            DIMENSIONS.add(new Dimension(new ResourceLocation(ServerTabInfo.MOD_ID, "overall"), overall.getSecond(), overall.getFirst()));
            for (ServerLevel world : server.forgeGetWorldMap().values()) {
                ResourceLocation name = world.dimension().location();
                Pair<Integer, Double> tpsAndMean = getTpsAndMean(server.getTickTime(world.dimension()));
                DIMENSIONS.add(new Dimension(name, tpsAndMean.getSecond(), tpsAndMean.getFirst()));
		    }

            List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
            PLAYER_DIMENSIONS.clear();
            for (ServerPlayer player : playerList) {
                ResourceLocation name = player.getCommandSenderWorld().dimension().location();
                PLAYER_DIMENSIONS.put(player.getUUID(), name);
            }

            NetworkUtils.sendToAll(new PacketDimensions.Data(DIMENSIONS));
            NetworkUtils.sendToAll(new PacketPlayers.Data(PLAYER_DIMENSIONS));

            ticks = 1;
            return;
        }
        ticks++;
    }

    private static Pair<Integer, Double> getTpsAndMean(long[] tickArray) {

        long sum = 0L;
        if (tickArray == null) {
            return Pair.of(0, 0.0D);
        }
        for (long tickTime : tickArray) {
            sum += tickTime;
        }
        double mean = (sum / tickArray.length) * 1.0E-006D;
        int tps = (int) Math.min(1000.0D / mean, 20);

        return Pair.of(tps, mean);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ServerPlayer playerEntity = (ServerPlayer) event.getEntity();
            ResourceLocation name = playerEntity.getCommandSenderWorld().dimension().location();
            PLAYER_DIMENSIONS.put(playerEntity.getUUID(), name);

            NetworkUtils.sendTo(new PacketDimensions.Data(DIMENSIONS), playerEntity);
            NetworkUtils.sendTo(new PacketPlayers.Data(PLAYER_DIMENSIONS), playerEntity);
        }
    }
}
