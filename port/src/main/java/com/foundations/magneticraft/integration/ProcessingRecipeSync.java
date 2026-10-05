package com.foundations.magneticraft.integration;

import com.foundations.magneticraft.FoundationsMagneticraft;
import com.foundations.magneticraft.manual.CrushingRecipes;
import com.foundations.magneticraft.manual.SluiceRecipes;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ProcessingRecipeSync {
    public record Payload(String json) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FoundationsMagneticraft.MOD_ID, "processing_recipes"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.composite(ByteBufCodecs.stringUtf8(ProcessingCatalog.MAX_JSON), Payload::json, Payload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    private static long lastRevision = Long.MIN_VALUE;
    public static void register(IEventBus bus) {
        bus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToClient(Payload.TYPE, Payload.CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                try { ProcessingCatalog.acceptClient(ProcessingCatalog.decode(payload.json())); }
                catch (RuntimeException error) { ProcessingCatalog.acceptClient(java.util.List.of()); LogUtils.getLogger().warn("Rejected invalid processing recipe catalog", error); }
            })));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            MinecraftServer server = event.getPlayerList().getServer();
            Payload payload = catalog(server);
            if (event.getPlayer() != null) PacketDistributor.sendToPlayer(event.getPlayer(), payload);
            else for (var player : event.getPlayerList().getPlayers()) PacketDistributor.sendToPlayer(player, payload);
            lastRevision = RecipeOverrides.revision();
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            long revision = RecipeOverrides.revision();
            if (lastRevision == revision) return;
            lastRevision = revision;
            if (event.getServer().getPlayerList().getPlayers().isEmpty()) return;
            Payload payload = catalog(event.getServer());
            for (var player : event.getServer().getPlayerList().getPlayers()) PacketDistributor.sendToPlayer(player, payload);
        });
    }
    private static Payload catalog(MinecraftServer server) {
        try {
        var entries = new ArrayList<ProcessingCatalog.Entry>();
        var level = server.overworld();
        for (var recipe : CrushingRecipes.all(level)) entries.add(new ProcessingCatalog.Entry("crushing", recipe.id(), recipe.ingredient(), recipe.miningLevel(),
            java.util.List.of(new ProcessingCatalog.Output(BuiltInRegistries.ITEM.getKey(recipe.output().getItem()).toString(), recipe.output().getCount(), 1))));
        for (var recipe : SluiceRecipes.all(level)) entries.add(new ProcessingCatalog.Entry("sluice", recipe.id(), recipe.ingredient(), 0,
            recipe.outputs().stream().map(output -> new ProcessingCatalog.Output(BuiltInRegistries.ITEM.getKey(output.stack().getItem()).toString(), output.stack().getCount(), output.chance())).toList()));
        return new Payload(ProcessingCatalog.encode(entries));
        }
        catch (IllegalArgumentException error) { LogUtils.getLogger().warn("Processing recipe catalog exceeds sync limits; clearing client displays", error); return new Payload("[]"); }
    }
    private ProcessingRecipeSync() {}
}
