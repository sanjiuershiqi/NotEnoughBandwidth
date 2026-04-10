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

    private Component clientTitle;
    private Component serverTitle;
    private Component inboundTitle;
    private Component outboundTitle;
    private Component speedLabel;
    private Component actualLabel;
    private Component rawLabel;
    private Component ratioLabel;

    public StatScreen() {
        super(Component.empty());
        clientTitle = Component.translatable("stat.notenoughbandwidth.client");
        serverTitle = Component.translatable("stat.notenoughbandwidth.server");
        inboundTitle = Component.translatable("stat.notenoughbandwidth.inbound");
        outboundTitle = Component.translatable("stat.notenoughbandwidth.outbound");
        speedLabel = Component.translatable("stat.notenoughbandwidth.speed").withColor(0xFFAAAAAA);
        actualLabel = Component.translatable("stat.notenoughbandwidth.actual").withColor(0xFFAAAAAA);
        rawLabel = Component.translatable("stat.notenoughbandwidth.raw").withColor(0xFFAAAAAA);
        ratioLabel = Component.translatable("stat.notenoughbandwidth.ratio");
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

        var font = this.minecraft.font;
        
        // Draw Titles
        int titleY = startY + 15;
        drawCenteredString(graphics, font, clientTitle, clientX + panelWidth / 2, titleY, 0xFFFFAA00);
        drawCenteredString(graphics, font, serverTitle, serverX + panelWidth / 2, titleY, 0xFFFFAA00);
        
        // Render Data for Client
        renderDataSection(graphics, font, clientX + 15, startY + 45, panelWidth - 30, true, clientInSpeed, clientInActual, clientInRaw);
        renderDataSection(graphics, font, clientX + 15, startY + 130, panelWidth - 30, false, clientOutSpeed, clientOutActual, clientOutRaw);

        // Render Data for Server
        renderDataSection(graphics, font, serverX + 15, startY + 45, panelWidth - 30, true, serverInSpeed, serverInActual, serverInRaw);
        renderDataSection(graphics, font, serverX + 15, startY + 130, panelWidth - 30, false, serverOutSpeed, serverOutActual, serverOutRaw);
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color); // Top
        graphics.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        graphics.fill(x, y, x + 1, y + height, color); // Left
        graphics.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    private void drawCenteredString(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, Component text, int x, int y, int color) {
        int strWidth = font.width(text);
        var textRenderer = graphics.textRenderer();
        textRenderer.accept(x - strWidth / 2, y, text);
    }

    private void renderDataSection(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int x, int y, int width, boolean isInbound, int speed, long actual, long raw) {
        // Minecraft Green / Minecraft Red
        Component titleComp = (isInbound ? inboundTitle : outboundTitle).copy().withColor(isInbound ? 0x55FF55 : 0xFF5555);
        var textRenderer = graphics.textRenderer();
        
        textRenderer.accept(x, y, titleComp);

        int rowY = y + 15;
        int valueXOffset = 100;
        
        drawDataRow(graphics, font, x, rowY, speedLabel, getReadableSpeed(speed), valueXOffset);
        drawDataRow(graphics, font, x, rowY + 12, actualLabel, getReadableSize(actual), valueXOffset);
        drawDataRow(graphics, font, x, rowY + 24, rawLabel, getReadableSize(raw), valueXOffset);

        // Render Ratio Bar
        int barY = rowY + 40;
        int barHeight = 8;
        
        // Use double calculation but keep within bounds 0.0 to 1.0
        double ratio = raw == 0 ? 0 : (double) actual / raw;
        if (ratio > 1.0) ratio = 1.0; 
        if (ratio < 0.0) ratio = 0.0;
        
        // Background of the bar
        graphics.fill(x, barY, x + width, barY + barHeight, 0xFF333333);
        drawBorder(graphics, x, barY, width, barHeight, 0xFF555555);
        
        // Fill of the bar
        int fillWidth = (int) (width * ratio);
        int fillColor = isInbound ? 0xFF55FF55 : 0xFFFF5555;
        if (fillWidth > 0) {
            graphics.fill(x, barY, x + fillWidth, barY + barHeight, fillColor);
        }
        
        // Ratio Text - Make it more obvious
        String ratioStr = String.format("%.2f%%", ratio * 100);
        Component ratioComp = ratioLabel.copy().append(" ").append(ratioStr).withColor(0xFFFFFFFF); // White text
        int strWidth = font.width(ratioComp);
        
        textRenderer.accept(x + width - strWidth - 5, barY - 10, ratioComp);
    }

    private void drawDataRow(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int x, int y, Component labelComp, String value, int valueXOffset) {
        Component valueComp = Component.literal(value);
        var textRenderer = graphics.textRenderer();
        
        textRenderer.accept(x, y, labelComp);
        textRenderer.accept(x + valueXOffset, y, valueComp);
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
