package com.black_dog20.servertabinfo.common.network.packets;

import com.black_dog20.bml.network.messages.api.PlayPacket;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketPlayers extends PlayPacket<PacketPlayers.Data> {

    public static final ResourceLocation ID = new ResourceLocation(ServerTabInfo.MOD_ID, "send_players");

    public static final record Data(Map<UUID, ResourceLocation> playerDims) implements CustomPacketPayload {

        public Data(FriendlyByteBuf friendlyByteBuf) {
            this(read(friendlyByteBuf));
        }

        @Override
        public void write(FriendlyByteBuf pBuffer) {
            pBuffer.writeInt(playerDims.size());
            for (Map.Entry<UUID, ResourceLocation> playerKvP : playerDims.entrySet()) {
                pBuffer.writeUUID(playerKvP.getKey());
                pBuffer.writeResourceLocation(playerKvP.getValue());
            }
        }

        @Override
        public @NotNull ResourceLocation id() {
            return ID;
        }
    }

    private static Map<UUID, ResourceLocation> read(FriendlyByteBuf friendlyByteBuf) {
        Map<UUID, ResourceLocation> map = new HashMap<>();
        int length = friendlyByteBuf.readInt();
        for (int i = 0; i < length; i++) {
            map.put(friendlyByteBuf.readUUID(), friendlyByteBuf.readResourceLocation());
        }
        return map;
    }

    @Override
    protected void handleClient(PacketPlayers.Data payload, IPayloadContext context) {
        context.workHandler().execute(() -> Handler.handle(payload));
    }

    @Override
    protected void handleServer(PacketPlayers.Data payload, IPayloadContext context) {

    }

    private static final class Handler {
        private static void handle(PacketPlayers.Data payload) {
            ClientDataManager.PLAYER_DIMENSIONS = payload.playerDims();
        }
    }
}
