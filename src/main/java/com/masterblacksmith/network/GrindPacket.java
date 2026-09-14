package com.masterblacksmith.network;

import com.masterblacksmith.blockentity.GrindingWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -&gt; server: set the edge angle, optionally grinding a pass. */
public class GrindPacket {
    private final BlockPos pos;
    private final float angle;
    private final boolean grind;

    public GrindPacket(BlockPos pos, float angle, boolean grind) {
        this.pos = pos;
        this.angle = angle;
        this.grind = grind;
    }

    public GrindPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.angle = buf.readFloat();
        this.grind = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeFloat(angle);
        buf.writeBoolean(grind);
    }

    public static void handle(GrindPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && player.level().getBlockEntity(msg.pos) instanceof GrindingWheelBlockEntity wheel) {
            if (msg.grind) wheel.serverGrind(player, msg.angle);
            else wheel.setAngle(msg.angle);
        }
        ctx.get().setPacketHandled(true);
    }
}
