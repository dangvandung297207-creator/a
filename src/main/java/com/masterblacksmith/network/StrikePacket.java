package com.masterblacksmith.network;

import com.masterblacksmith.blockentity.BlacksmithAnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -&gt; server: one timing-bar hammer strike. */
public class StrikePacket {
    private final BlockPos pos;
    private final float aim;
    private final float force;

    public StrikePacket(BlockPos pos, float aim, float force) {
        this.pos = pos;
        this.aim = aim;
        this.force = force;
    }

    public StrikePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.aim = buf.readFloat();
        this.force = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeFloat(aim);
        buf.writeFloat(force);
    }

    public static void handle(StrikePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && player.level().getBlockEntity(msg.pos) instanceof BlacksmithAnvilBlockEntity anvil) {
            anvil.serverStrike(player, msg.aim, msg.force);
        }
        ctx.get().setPacketHandled(true);
    }
}
