package com.black_dog20.servertabinfo.common.network.packets;

import com.black_dog20.bml.network.messages.api.PlayPacket;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.client.ClientDataManager;
import com.black_dog20.servertabinfo.common.utils.Dimension;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PacketDimensions extends PlayPacket<PacketDimensions.Data> {

    public static final ResourceLocation ID = new ResourceLocation(ServerTabInfo.MOD_ID, "send_dimensions");

    public static final record Data(List<Dimension> dimensions) implements CustomPacketPayload {

        public Data(FriendlyByteBuf friendlyByteBuf) {
            this(read(friendlyByteBuf));
        }

        @Override
        public void write(FriendlyByteBuf pBuffer) {
            pBuffer.writeInt(dimensions.size());
            for (Dimension dimension : dimensions) {
                pBuffer.writeResourceLocation(dimension.name);
                pBuffer.writeDouble(dimension.meanTickTime);
                pBuffer.writeInt(dimension.tps);
            }
        }

        @Override
        public @NotNull ResourceLocation id() {
            return ID;
        }
    }

    private static List<Dimension> read(FriendlyByteBuf friendlyByteBuf) {
        List<Dimension> dimensions = new LinkedList<>();
        int length = friendlyByteBuf.readInt();
        for (int i = 0; i < length; i++) {
            dimensions.add(new Dimension(friendlyByteBuf.readResourceLocation(), friendlyByteBuf.readDouble(), friendlyByteBuf.readInt()));
        }
        return dimensions;
    }

    @Override
    protected void handleClient(PacketDimensions.Data payload, IPayloadContext context) {
        context.workHandler().execute(() -> Handler.handle(payload));
    }

    @Override
    protected void handleServer(PacketDimensions.Data payload, IPayloadContext context) {

    }

    private static final class Handler {
        private static void handle(PacketDimensions.Data payload) {
            ClientDataManager.DIMENSIONS = payload.dimensions;
        }
    }
}
