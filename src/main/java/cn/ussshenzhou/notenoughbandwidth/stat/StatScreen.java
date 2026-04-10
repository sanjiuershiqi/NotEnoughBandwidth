package cn.ussshenzhou.notenoughbandwidth.stat;

import cn.ussshenzhou.network.StatQuery;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import static cn.ussshenzhou.notenoughbandwidth.stat.SimpleStatManager.*;

/**
 * @author USS_Shenzhou
 */
public class StatScreen extends Screen {
    private int tick = 0;

    // Client Data
    private int clientInSpeed;
    private long clientInActual;
    private long clientInRaw;
    
    private int clientOutSpeed;
    private long clientOutActual;
    private long clientOutRaw;

    // Server Data
    private int serverInSpeed;
    private long serverInActual;
    private long serverInRaw;
    
    private int serverOutSpeed;
    private long serverOutActual;
    private long serverOutRaw;

    public StatScreen() {
        super(Component.empty());
    }

    @Override
    public void tick() {
        super.tick();
        if (tick % 10 == 0) {
            ClientPacketDistributor.sendToServer(new StatQuery());
            
            clientInSpeed = (int) LOCAL.inboundSpeedBaked().averageIn1s();
            clientInActual = LOCAL.inboundBytesBaked().get();
            clientInRaw = LOCAL.inboundBytesRaw().get();
            
            clientOutSpeed = (int) LOCAL.outboundSpeedBaked().averageIn1s();
            clientOutActual = LOCAL.outboundBytesBaked().get();
            clientOutRaw = LOCAL.outboundBytesRaw().get();

            serverInSpeed = (int) inboundSpeedBakedServer;
            serverInActual = inboundBytesBakedServer;
            serverInRaw = inboundBytesRawServer;
            
            serverOutSpeed = (int) outboundSpeedBakedServer;
            serverOutActual = outboundBytesBakedServer;
            serverOutRaw = outboundBytesRawServer;
        }
        tick++;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.fill(0, 0, width, height, 0x80000000);

        int panelWidth = 300;
        int panelHeight = 220;
        int gap = 20;

        int totalWidth = panelWidth * 2 + gap;
        int startX = (width - totalWidth) / 2;
        int startY = (height - panelHeight) / 2;

        int clientX = startX;
        int serverX = startX + panelWidth + gap;

        // Draw Panels Background
        graphics.fill(clientX, startY, clientX + panelWidth, startY + panelHeight, 0x90000000);
        graphics.fill(serverX, startY, serverX + panelWidth, startY + panelHeight, 0x90000000);

        // Draw Panels Border
        drawBorder(graphics, clientX, startY, panelWidth, panelHeight, 0xFF555555);
        drawBorder(graphics, serverX, startY, panelWidth, panelHeight, 0xFF555555);

        var textRenderer = graphics.textRenderer();
        
        // Draw Titles
        int titleY = startY + 15;
        drawCenteredString(graphics, textRenderer, Component.literal("Client"), clientX + panelWidth / 2, titleY, 0xFFFFAA00);
        drawCenteredString(graphics, textRenderer, Component.literal("Server"), serverX + panelWidth / 2, titleY, 0xFFFFAA00);
        
        // Render Data for Client
        renderDataSection(graphics, textRenderer, clientX + 15, startY + 45, panelWidth - 30, true, clientInSpeed, clientInActual, clientInRaw);
        renderDataSection(graphics, textRenderer, clientX + 15, startY + 130, panelWidth - 30, false, clientOutSpeed, clientOutActual, clientOutRaw);

        // Render Data for Server
        renderDataSection(graphics, textRenderer, serverX + 15, startY + 45, panelWidth - 30, true, serverInSpeed, serverInActual, serverInRaw);
        renderDataSection(graphics, textRenderer, serverX + 15, startY + 130, panelWidth - 30, false, serverOutSpeed, serverOutActual, serverOutRaw);
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color); // Top
        graphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        graphics.fill(x, y, x + 1, y + height, color); // Left
        graphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    private void drawCenteredString(GuiGraphicsExtractor graphics, net.minecraft.client.gui.ActiveTextCollector textRenderer, Component text, int x, int y, int color) {
        // Simple approximation for centered text, assuming average char width
        int strWidth = text.getString().length() * 6; 
        graphics.pose().pushMatrix();
        graphics.pose().translate(x - strWidth / 2.0f, y, 0);
        // Note: Actual rendering color depends on how textRenderer applies styles. We use standard Component rendering here.
        textRenderer.accept(text);
        graphics.pose().popMatrix();
    }

    private void renderDataSection(GuiGraphicsExtractor graphics, net.minecraft.client.gui.ActiveTextCollector textRenderer, int x, int y, int width, boolean isInbound, int speed, long actual, long raw) {
        String title = isInbound ? "↓ Inbound" : "↑ Outbound";
        // Minecraft Green / Minecraft Red
        Component titleComp = Component.literal(title).withColor(isInbound ? 0x55FF55 : 0xFF5555);
        
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y, 0);
        textRenderer.accept(titleComp);
        graphics.pose().popMatrix();

        int rowY = y + 15;
        int valueXOffset = 100;
        
        drawDataRow(graphics, textRenderer, x, rowY, "Speed:", getReadableSpeed(speed), valueXOffset);
        drawDataRow(graphics, textRenderer, x, rowY + 12, "Actual Total:", getReadableSize(actual), valueXOffset);
        drawDataRow(graphics, textRenderer, x, rowY + 24, "Raw Total:", getReadableSize(raw), valueXOffset);

        // Render Ratio Bar
        int barY = rowY + 40;
        int barHeight = 8;
        
        double ratio = raw == 0 ? 0 : (double) actual / raw;
        if (ratio > 1.0) ratio = 1.0; // clamp for display
        
        // Background of the bar
        graphics.fill(x, barY, x + width, barY + barHeight, 0xFF333333);
        drawBorder(graphics, x, barY, width, barHeight, 0xFF555555);
        
        // Fill of the bar
        int fillWidth = (int) (width * ratio);
        int fillColor = isInbound ? 0xFF55FF55 : 0xFFFF5555;
        if (fillWidth > 0) {
            graphics.fill(x, barY, x + fillWidth, barY + barHeight, fillColor);
        }
        
        // Ratio Text
        String ratioStr = String.format("%.2f%%", ratio * 100);
        Component ratioComp = Component.literal(ratioStr).withColor(0xFFFFFFFF); // White text
        int strWidth = ratioStr.length() * 6; // approximate width
        
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) (x + width - strWidth - 5), (float) (barY - 10), 0f);
        textRenderer.accept(ratioComp);
        graphics.pose().popMatrix();
    }

    private void drawDataRow(GuiGraphicsExtractor graphics, net.minecraft.client.gui.ActiveTextCollector textRenderer, int x, int y, String label, String value, int valueXOffset) {
        Component labelComp = Component.literal(label).withColor(0xFFAAAAAA); // Gray
        Component valueComp = Component.literal(value);
        
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y, 0);
        textRenderer.accept(labelComp);
        graphics.pose().popMatrix();
        
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + valueXOffset, y, 0);
        textRenderer.accept(valueComp);
        graphics.pose().popMatrix();
    }

    private String getReadableSpeed(int bytes) {
        if (bytes < 1000) {
            return bytes + " §7Bytes/S§r";
        } else if (bytes < 1000 * 1000) {
            return String.format("%.1f §7KiB/S§r", bytes / 1024f);
        } else {
            return String.format("%.2f §7MiB/S§r", bytes / (1024 * 1024f));
        }
    }

    private String getReadableSize(long bytes) {
        if (bytes < 1000) {
            return bytes + " §7Bytes§r";
        } else if (bytes < 1000 * 1000) {
            return String.format("%.1f §7KiB§r", bytes / 1024d);
        } else if (bytes < 1000 * 1000 * 1000) {
            return String.format("%.2f §7MiB§r", bytes / (1024 * 1024d));
        } else {
            return String.format("%.2f §7GiB§r", bytes / (1024 * 1024 * 1024d));
        }
    }
}
