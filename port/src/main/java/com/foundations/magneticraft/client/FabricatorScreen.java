package com.foundations.magneticraft.client;

import com.foundations.magneticraft.manual.FabricatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private Button craft;
    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = 126;
    }
    @Override protected void init() {
        super.init();
        craft = addRenderableWidget(Button.builder(Component.translatable("gui.magneticraft.fabricator.craft"), b -> sendButton(0))
            .bounds(leftPos + 18, topPos + 96, 64, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.magneticraft.fabricator.clear"), b -> sendButton(1))
            .bounds(leftPos + 94, topPos + 96, 64, 18).build());
        craft.active = menu.ready();
    }
    private void sendButton(int button) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
    }
    @Override protected void containerTick() {
        super.containerTick();
        craft.active = menu.ready();
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF373737);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFFC6C6C6);
        for (int i = 0; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            int color = i < 9 && slot.hasItem() ? ((menu.foundMask() & (1 << i)) != 0 ? 0xFF547F45 : 0xFF9A4949) : 0xFF555555;
            int x = leftPos + slot.x, y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, color);
            graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("gui.magneticraft.fabricator.pattern"), 18, 18, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.magneticraft.fabricator.storage"), 112, 18, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.magneticraft.fabricator.hint"), 8, 84, 0x404040, false);
    }
}
