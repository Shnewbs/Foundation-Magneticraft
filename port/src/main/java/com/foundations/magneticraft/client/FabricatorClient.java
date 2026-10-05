package com.foundations.magneticraft.client;

import com.foundations.magneticraft.FoundationsMagneticraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = FoundationsMagneticraft.MOD_ID, dist = Dist.CLIENT)
public final class FabricatorClient {
    public FabricatorClient(IEventBus bus) { bus.addListener(FabricatorClient::registerScreens);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) ->
            com.foundations.magneticraft.integration.ProcessingCatalog.acceptClient(java.util.List.of())); }
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FoundationsMagneticraft.FABRICATOR_MENU.get(), FabricatorScreen::new);
    }
}
