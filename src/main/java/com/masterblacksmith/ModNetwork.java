package com.masterblacksmith;

import com.masterblacksmith.network.GrindPacket;
import com.masterblacksmith.network.StrikePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Client -&gt; server packets for the forging and grinding minigames. */
public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MasterBlacksmith.MOD_ID, "main"))
                .networkProtocolVersion(() -> PROTOCOL)
                .clientAcceptedVersions(PROTOCOL::equals)
                .serverAcceptedVersions(PROTOCOL::equals)
                .simpleChannel();

        int id = 0;
        CHANNEL.messageBuilder(StrikePacket.class, id++)
                .encoder(StrikePacket::encode)
                .decoder(StrikePacket::new)
                .consumerMainThread(StrikePacket::handle)
                .add();
        CHANNEL.messageBuilder(GrindPacket.class, id++)
                .encoder(GrindPacket::encode)
                .decoder(GrindPacket::new)
                .consumerMainThread(GrindPacket::handle)
                .add();
    }
}
