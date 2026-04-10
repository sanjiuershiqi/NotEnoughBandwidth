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

    // Client Data Cached Strings
    private Component clientInSpeedStr = Component.empty();
    private Component clientInActualStr = Component.empty();
    private Component clientInRawStr = Component.empty();
    private Component clientInRatioStr = Component.empty();
    private double clientInRatio = 0.0;
    
    private Component clientOutSpeedStr = Component.empty();
    private Component clientOutActualStr = Component.empty();
    private Component clientOutRawStr = Component.empty();
    private Component clientOutRatioStr = Component.empty();
    private double clientOutRatio = 0.0;

    // Server Data Cached Strings
    private Component serverInSpeedStr = Component.empty();
    private Component serverInActualStr = Component.empty();
    private Component serverInRawStr = Component.empty();
    private Component serverInRatioStr = Component.empty();
    private double serverInRatio = 0.0;
    
    private Component serverOutSpeedStr = Component.empty();
    private Component serverOutActualStr = Component.empty();
    private Component serverOutRawStr = Component.empty();
    private Component serverOutRatioStr = Component.empty();
    private double serverOutRatio = 0.0;

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
        inboundTitle = Component.translatable("stat.notenoughbandwidth.inbound").withColor(0x55FF55);
        outboundTitle = Component.translatable("stat.notenoughbandwidth.outbound").withColor(0xFF5555);
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
            
            // Cache Formatted Strings and Components
            clientInSpeedStr = Component.literal(getReadableSpeed(clientInSpeed));
            clientInActualStr = Component.literal(getReadableSize(clientInActual));
            clientInRawStr = Component.literal(getReadableSize(clientInRaw));
            clientInRatio = calculateRatio(clientInActual, clientInRaw);
            clientInRatioStr = buildRatioComponent(clientInRatio);

            clientOutSpeedStr = Component.literal(getReadableSpeed(clientOutSpeed));
            clientOutActualStr = Component.literal(getReadableSize(clientOutActual));
            clientOutRawStr = Component.literal(getReadableSize(clientOutRaw));
            clientOutRatio = calculateRatio(clientOutActual, clientOutRaw);
            clientOutRatioStr = buildRatioComponent(clientOutRatio);

            serverInSpeedStr = Component.literal(getReadableSpeed(serverInSpeed));
            serverInActualStr = Component.literal(getReadableSize(serverInActual));
            serverInRawStr = Component.literal(getReadableSize(serverInRaw));
            serverInRatio = calculateRatio(serverInActual, serverInRaw);
            serverInRatioStr = buildRatioComponent(serverInRatio);

            serverOutSpeedStr = Component.literal(getReadableSpeed(serverOutSpeed));
            serverOutActualStr = Component.literal(getReadableSize(serverOutActual));
            serverOutRawStr = Component.literal(getReadableSize(serverOutRaw));
            serverOutRatio = calculateRatio(serverOutActual, serverOutRaw);
            serverOutRatioStr = buildRatioComponent(serverOutRatio);
        }
        tick++;
    }

    private double calculateRatio(long actual, long raw) {
        if (raw == 0) return 0.0;
        double ratio = (double) actual / raw;
        if (ratio > 1.0) ratio = 1.0; 
        if (ratio < 0.0) ratio = 0.0;
        return ratio;
    }

    private Component buildRatioComponent(double ratio) {
        String ratioStr = String.format("%.2f%%", ratio * 100);
        return ratioLabel.copy().append(" ").append(ratioStr).withColor(0xFFFFFFFF);
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
        renderDataSection(graphics, font, clientX + 15, startY + 45, panelWidth - 30, true, clientInSpeedStr, clientInActualStr, clientInRawStr, clientInRatio, clientInRatioStr);
        renderDataSection(graphics, font, clientX + 15, startY + 130, panelWidth - 30, false, clientOutSpeedStr, clientOutActualStr, clientOutRawStr, clientOutRatio, clientOutRatioStr);

        // Render Data for Server
        renderDataSection(graphics, font, serverX + 15, startY + 45, panelWidth - 30, true, serverInSpeedStr, serverInActualStr, serverInRawStr, serverInRatio, serverInRatioStr);
        renderDataSection(graphics, font, serverX + 15, startY + 130, panelWidth - 30, false, serverOutSpeedStr, serverOutActualStr, serverOutRawStr, serverOutRatio, serverOutRatioStr);
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

    private void renderDataSection(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int x, int y, int width, boolean isInbound, Component speedStr, Component actualStr, Component rawStr, double ratio, Component ratioStr) {
        // Minecraft Green / Minecraft Red
        Component titleComp = isInbound ? inboundTitle : outboundTitle;
        var textRenderer = graphics.textRenderer();
        
        textRenderer.accept(x, y, titleComp);

        int rowY = y + 15;
        int valueXOffset = 100;
        
        drawDataRow(graphics, font, x, rowY, speedLabel, speedStr, valueXOffset);
        drawDataRow(graphics, font, x, rowY + 12, actualLabel, actualStr, valueXOffset);
        drawDataRow(graphics, font, x, rowY + 24, rawLabel, rawStr, valueXOffset);

        // Render Ratio Bar
        int barY = rowY + 40;
        int barHeight = 8;
        
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
        int strWidth = font.width(ratioStr);
        textRenderer.accept(x + width - strWidth - 5, barY - 10, ratioStr);
    }

    private void drawDataRow(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int x, int y, Component labelComp, Component valueComp, int valueXOffset) {
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
