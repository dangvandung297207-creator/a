package com.truemetallurgy.network;

import com.truemetallurgy.blockentity.AssemblyTableBlockEntity;
import com.truemetallurgy.blockentity.BlacksmithAnvilBlockEntity;
import com.truemetallurgy.blockentity.GrindingWheelBlockEntity;
import com.truemetallurgy.blockentity.QuenchingBarrelBlockEntity;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * All packets. C2S packets carry intent only (positions, button ids) - the
 * server re-validates distance, workstation, item, state and cooldown, then
 * computes every result. The client never sends quality numbers.
 */
public final class ModNetworking {
    private ModNetworking() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(AnvilActionPayload.TYPE, AnvilActionPayload.STREAM_CODEC, AnvilActionPayload::handle);
        registrar.playToServer(GrindActionPayload.TYPE, GrindActionPayload.STREAM_CODEC, GrindActionPayload::handle);
        registrar.playToServer(AssemblyCraftPayload.TYPE, AssemblyCraftPayload.STREAM_CODEC, AssemblyCraftPayload::handle);
        registrar.playToServer(QuenchActionPayload.TYPE, QuenchActionPayload.STREAM_CODEC, QuenchActionPayload::handle);
        registrar.playToServer(InspectPayload.TYPE, InspectPayload.STREAM_CODEC, InspectPayload::handle);
        registrar.playToClient(StrikeFeedbackPayload.TYPE, StrikeFeedbackPayload.STREAM_CODEC, StrikeFeedbackPayload::handle);
        registrar.playToClient(OpenJournalPayload.TYPE, OpenJournalPayload.STREAM_CODEC, OpenJournalPayload::handle);
    }

    /** Shared C2S validation: loaded chunk, right BE type, player in reach. */
    private static <T extends BlockEntity> T validate(ServerPlayer player, BlockPos pos, Class<T> type) {
        if (player == null || player.level() == null) return null;
        if (!player.level().isLoaded(pos)) return null;
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) > 64.0) return null;
        BlockEntity be = player.level().getBlockEntity(pos);
        if (!type.isInstance(be)) return null;
        return type.cast(be);
    }

    // ---------------- anvil ----------------

    /** action 0-3 select kind, 4 take workpiece. */
    public record AnvilActionPayload(BlockPos pos, int action) implements CustomPacketPayload {
        public static final Type<AnvilActionPayload> TYPE = new Type<>(TMUtil.rl("anvil_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AnvilActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AnvilActionPayload::pos,
            ByteBufCodecs.VAR_INT, AnvilActionPayload::action,
            AnvilActionPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player() instanceof ServerPlayer player)) return;
                BlacksmithAnvilBlockEntity anvil = validate(player, pos, BlacksmithAnvilBlockEntity.class);
                if (anvil == null) return;
                if (action >= 0 && action <= 3) {
                    String kind = switch (action) {
                        case 1 -> "axe_head";
                        case 2 -> "pickaxe_head";
                        case 3 -> "spear_head";
                        default -> "sword_blade";
                    };
                    anvil.selectKind(player, kind);
                } else if (action == 4) {
                    anvil.takeWorkpiece(player);
                }
            });
        }
    }

    // ---------------- grinding ----------------

    /** action 0 select angle (value 0-2), 1 start grinding. */
    public record GrindActionPayload(BlockPos pos, int action, int value) implements CustomPacketPayload {
        public static final Type<GrindActionPayload> TYPE = new Type<>(TMUtil.rl("grind_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, GrindActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, GrindActionPayload::pos,
            ByteBufCodecs.VAR_INT, GrindActionPayload::action,
            ByteBufCodecs.VAR_INT, GrindActionPayload::value,
            GrindActionPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player() instanceof ServerPlayer player)) return;
                GrindingWheelBlockEntity wheel = validate(player, pos, GrindingWheelBlockEntity.class);
                if (wheel == null) return;
                if (action == 0) {
                    wheel.selectAngle(player, value);
                } else if (action == 1) {
                    wheel.startGrinding(player);
                }
            });
        }
    }

    // ---------------- assembly ----------------

    public record AssemblyCraftPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<AssemblyCraftPayload> TYPE = new Type<>(TMUtil.rl("assembly_craft"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AssemblyCraftPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AssemblyCraftPayload::pos,
            AssemblyCraftPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player() instanceof ServerPlayer player)) return;
                AssemblyTableBlockEntity table = validate(player, pos, AssemblyTableBlockEntity.class);
                if (table == null) return;
                table.craft(player);
            });
        }
    }

    // ---------------- quenching ----------------

    public record QuenchActionPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<QuenchActionPayload> TYPE = new Type<>(TMUtil.rl("quench_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, QuenchActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, QuenchActionPayload::pos,
            QuenchActionPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player() instanceof ServerPlayer player)) return;
                QuenchingBarrelBlockEntity barrel = validate(player, pos, QuenchingBarrelBlockEntity.class);
                if (barrel == null) return;
                barrel.quench(player);
            });
        }
    }

    // ---------------- inspect ----------------

    /** hand 0 main, 1 off. Server validates the held finished item. */
    public record InspectPayload(int hand) implements CustomPacketPayload {
        public static final Type<InspectPayload> TYPE = new Type<>(TMUtil.rl("inspect"));
        public static final StreamCodec<RegistryFriendlyByteBuf, InspectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, InspectPayload::hand,
            InspectPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player() instanceof ServerPlayer player)) return;
                ItemStack held = player.getItemInHand(hand == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                com.truemetallurgy.components.FinishedData data =
                    held.get(com.truemetallurgy.registry.ModDataComponents.FINISHED.get());
                if (data == null) return;
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                    new OpenJournalPayload(held.getHoverName().getString(), data.score(), data.materialId(),
                        data.quenchId(), data.grindAngle(), data.weightKg(), data.crafterName()));
            });
        }
    }

    // ---------------- server -> client ----------------

    /** Broadcast after a resolved strike: drives shake + GUI flash. */
    public record StrikeFeedbackPayload(BlockPos pos, int grade, boolean cracked) implements CustomPacketPayload {
        public static final Type<StrikeFeedbackPayload> TYPE = new Type<>(TMUtil.rl("strike_feedback"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StrikeFeedbackPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StrikeFeedbackPayload::pos,
            ByteBufCodecs.VAR_INT, StrikeFeedbackPayload::grade,
            ByteBufCodecs.BOOL, StrikeFeedbackPayload::cracked,
            StrikeFeedbackPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> com.truemetallurgy.client.ClientHooks.onStrikeFeedback(pos, grade, cracked));
        }
    }

    /** Open the journal inspect page with server-validated item data. */
    public record OpenJournalPayload(String itemName, int score, String materialId, String quenchId,
            int edge, float weight, String crafter) implements CustomPacketPayload {
        public static final Type<OpenJournalPayload> TYPE = new Type<>(TMUtil.rl("open_journal"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenJournalPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenJournalPayload::itemName,
            ByteBufCodecs.VAR_INT, OpenJournalPayload::score,
            ByteBufCodecs.STRING_UTF8, OpenJournalPayload::materialId,
            ByteBufCodecs.STRING_UTF8, OpenJournalPayload::quenchId,
            ByteBufCodecs.VAR_INT, OpenJournalPayload::edge,
            ByteBufCodecs.FLOAT, OpenJournalPayload::weight,
            ByteBufCodecs.STRING_UTF8, OpenJournalPayload::crafter,
            OpenJournalPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(IPayloadContext ctx) {
            ctx.enqueueWork(() -> com.truemetallurgy.client.ClientHooks.openInspect(
                itemName, score, materialId, quenchId, edge, weight, crafter));
        }
    }
}
