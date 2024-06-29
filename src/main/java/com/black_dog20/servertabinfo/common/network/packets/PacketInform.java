package com.black_dog20.servertabinfo.common.network.packets;

import com.black_dog20.bml.network.messages.api.ConfigurationPacket;
import com.black_dog20.servertabinfo.ServerTabInfo;
import com.black_dog20.servertabinfo.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PacketInform extends ConfigurationPacket<PacketInform.Data> {

    public static final ResourceLocation ID = new ResourceLocation(ServerTabInfo.MOD_ID, "inform_client");

    public static final record Data() implements CustomPacketPayload {

        public Data(FriendlyByteBuf friendlyByteBuf) {
            this();
        }

        @Override
        public void write(FriendlyByteBuf pBuffer) {

        }

        @Override
        public @NotNull ResourceLocation id() {
            return ID;
        }
    }

    @Override
    protected void handleClient(PacketInform.Data payload, IPayloadContext context) {
        context.workHandler().execute(() -> Handler.handle(payload));
    }

    @Override
    protected void handleServer(PacketInform.Data payload, IPayloadContext context) {

    }

    private static final class Handler {
        private static void handle(PacketInform.Data payload) {
            ClientDataManager.modOnServer = true;
        }
    }

    public record Task(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
        public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(new ResourceLocation(ServerTabInfo.MOD_ID, "inform_client_task"));

        @Override
        public void run(final Consumer<CustomPacketPayload> sender) {
            final Data payload = new Data();
            sender.accept(payload);
            listener.finishCurrentTask(type());
        }

        @Override
        public ConfigurationTask.@NotNull Type type() {
            return TYPE;
        }
    }
}
