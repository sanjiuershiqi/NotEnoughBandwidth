package cn.ussshenzhou.notenoughbandwidth.stat;

import cn.ussshenzhou.network.StatQuery;
import cn.ussshenzhou.notenoughbandwidth.ui.Panel;
import cn.ussshenzhou.notenoughbandwidth.ui.ProgressBar;
import cn.ussshenzhou.notenoughbandwidth.ui.Text;
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

    // UI Framework root
    private Panel rootPanel;

    // Client Data Cached Elements
    private Text clientInSpeedText;
    private Text clientInActualText;
    private Text clientInRawText;
    private ProgressBar clientInRatioBar;
    private Text clientInRatioText;

    private Text clientOutSpeedText;
    private Text clientOutActualText;
    private Text clientOutRawText;
    private ProgressBar clientOutRatioBar;
    private Text clientOutRatioText;

    // Server Data Cached Elements
    private Text serverInSpeedText;
    private Text serverInActualText;
    private Text serverInRawText;
    private ProgressBar serverInRatioBar;
    private Text serverInRatioText;

    private Text serverOutSpeedText;
    private Text serverOutActualText;
    private Text serverOutRawText;
    private ProgressBar serverOutRatioBar;
    private Text serverOutRatioText;

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
        clientTitle = Component.translatable("stat.notenoughbandwidth.client").withColor(0xFFFFAA00);
        serverTitle = Component.translatable("stat.notenoughbandwidth.server").withColor(0xFFFFAA00);
        inboundTitle = Component.translatable("stat.notenoughbandwidth.inbound").withColor(0x55FF55);
        outboundTitle = Component.translatable("stat.notenoughbandwidth.outbound").withColor(0xFF5555);
        speedLabel = Component.translatable("stat.notenoughbandwidth.speed").withColor(0xFFAAAAAA);
        actualLabel = Component.translatable("stat.notenoughbandwidth.actual").withColor(0xFFAAAAAA);
        rawLabel = Component.translatable("stat.notenoughbandwidth.raw").withColor(0xFFAAAAAA);
        ratioLabel = Component.translatable("stat.notenoughbandwidth.ratio");
    }

    @Override
    protected void init() {
        super.init();

        rootPanel = new Panel();
        rootPanel.setLayoutType(Panel.LayoutType.ABSOLUTE);
        // Don't set width and height directly like this because absolute children add x/y to parent x/y
        // rootPanel will be positioned at 0,0 implicitly
        rootPanel.setPosition(0, 0);
        rootPanel.setSize(width, height);

        Panel mainContainer = new Panel();
        mainContainer.setLayoutType(Panel.LayoutType.HORIZONTAL);
        mainContainer.setSpacing(20);

        Panel clientPanel = createPanel(clientTitle, true);
        Panel serverPanel = createPanel(serverTitle, false);

        mainContainer.add(clientPanel);
        mainContainer.add(serverPanel);

        // Dynamically compute width and height to center
        // 280 (panel) * 2 + 20 (spacing) = 580
        int mainWidth = 580;
        int mainHeight = 220;
        mainContainer.setPosition((width - mainWidth) / 2, (height - mainHeight) / 2);
        mainContainer.setSize(mainWidth, mainHeight);

        rootPanel.add(mainContainer);
        
        // Initial layout and data update
        updateData();
        updateRatioTextPositions();
        rootPanel.layout(font);
    }

    private Panel createPanel(Component title, boolean isClient) {
        Panel panel = new Panel();
        panel.setSize(280, 220); // Increased from 210 to 220 to fit increased margins
        panel.setLayoutType(Panel.LayoutType.ABSOLUTE);
        // Rounded background with transparent dark color and white border
        panel.setBackground(0x90000000, 0xAAFFFFFF, 8);

        Text titleText = new Text(title);
        titleText.setCentered(true);
        titleText.setPosition(140, 15);
        panel.add(titleText);

        Panel inboundPanel = createDataSection(true, isClient);
        inboundPanel.setPosition(15, 40);
        panel.add(inboundPanel);

        Panel outboundPanel = createDataSection(false, isClient);
        outboundPanel.setPosition(15, 130); // Increased from 125 to 130
        panel.add(outboundPanel);

        return panel;
    }

    private Panel createDataSection(boolean isInbound, boolean isClient) {
        Panel panel = new Panel();
        panel.setLayoutType(Panel.LayoutType.ABSOLUTE);
        panel.setSize(250, 70); // Increased from 60 to 70 to fit new spacing

        Component titleComp = isInbound ? inboundTitle : outboundTitle;
        Text sectionTitle = new Text(titleComp);
        sectionTitle.setPosition(0, 0);
        panel.add(sectionTitle);

        int rowY = 15;
        int valX = 100;

        Text speedLbl = new Text(speedLabel);
        speedLbl.setPosition(0, rowY);
        panel.add(speedLbl);

        Text speedVal = new Text(Component.empty());
        speedVal.setPosition(valX, rowY);
        panel.add(speedVal);

        Text actualLbl = new Text(actualLabel);
        actualLbl.setPosition(0, rowY + 12);
        panel.add(actualLbl);

        Text actualVal = new Text(Component.empty());
        actualVal.setPosition(valX, rowY + 12);
        panel.add(actualVal);

        Text rawLbl = new Text(rawLabel);
        rawLbl.setPosition(0, rowY + 24);
        panel.add(rawLbl);

        Text rawVal = new Text(Component.empty());
        rawVal.setPosition(valX, rowY + 24);
        panel.add(rawVal);

        ProgressBar bar = new ProgressBar(250, 8);
        bar.setPosition(0, 44); // Increased from 40 to 44 to avoid overlap with rawLbl
        int fillColor = isInbound ? 0xFF55FF55 : 0xFFFF5555;
        bar.setColors(0xFF333333, 0xFF555555, fillColor);
        panel.add(bar);

        Text ratioVal = new Text(Component.empty());
        // Temporary position, updated dynamically based on width
        ratioVal.setPosition(200, 33); // Increased from 30 to 33
        panel.add(ratioVal);

        if (isClient) {
            if (isInbound) {
                clientInSpeedText = speedVal;
                clientInActualText = actualVal;
                clientInRawText = rawVal;
                clientInRatioBar = bar;
                clientInRatioText = ratioVal;
            } else {
                clientOutSpeedText = speedVal;
                clientOutActualText = actualVal;
                clientOutRawText = rawVal;
                clientOutRatioBar = bar;
                clientOutRatioText = ratioVal;
            }
        } else {
            if (isInbound) {
                serverInSpeedText = speedVal;
                serverInActualText = actualVal;
                serverInRawText = rawVal;
                serverInRatioBar = bar;
                serverInRatioText = ratioVal;
            } else {
                serverOutSpeedText = speedVal;
                serverOutActualText = actualVal;
                serverOutRawText = rawVal;
                serverOutRatioBar = bar;
                serverOutRatioText = ratioVal;
            }
        }

        return panel;
    }

    @Override
    public void tick() {
        super.tick();
        if (tick % 10 == 0) {
            ClientPacketDistributor.sendToServer(new StatQuery());
            updateData();
            updateRatioTextPositions();
            rootPanel.layout(font);
        }
        tick++;
    }

    private void updateData() {
        int clientInSpeed = (int) LOCAL.inboundSpeedBaked().averageIn1s();
        long clientInActual = LOCAL.inboundBytesBaked().get();
        long clientInRaw = LOCAL.inboundBytesRaw().get();

        int clientOutSpeed = (int) LOCAL.outboundSpeedBaked().averageIn1s();
        long clientOutActual = LOCAL.outboundBytesBaked().get();
        long clientOutRaw = LOCAL.outboundBytesRaw().get();

        int serverInSpeed = (int) inboundSpeedBakedServer;
        long serverInActual = inboundBytesBakedServer;
        long serverInRaw = inboundBytesRawServer;

        int serverOutSpeed = (int) outboundSpeedBakedServer;
        long serverOutActual = outboundBytesBakedServer;
        long serverOutRaw = outboundBytesRawServer;

        clientInSpeedText.setComponent(Component.literal(getReadableSpeed(clientInSpeed)));
        clientInActualText.setComponent(Component.literal(getReadableSize(clientInActual)));
        clientInRawText.setComponent(Component.literal(getReadableSize(clientInRaw)));
        double cInRatio = calculateRatio(clientInActual, clientInRaw);
        clientInRatioBar.setRatio(cInRatio);
        clientInRatioText.setComponent(buildRatioComponent(cInRatio));

        clientOutSpeedText.setComponent(Component.literal(getReadableSpeed(clientOutSpeed)));
        clientOutActualText.setComponent(Component.literal(getReadableSize(clientOutActual)));
        clientOutRawText.setComponent(Component.literal(getReadableSize(clientOutRaw)));
        double cOutRatio = calculateRatio(clientOutActual, clientOutRaw);
        clientOutRatioBar.setRatio(cOutRatio);
        clientOutRatioText.setComponent(buildRatioComponent(cOutRatio));

        serverInSpeedText.setComponent(Component.literal(getReadableSpeed(serverInSpeed)));
        serverInActualText.setComponent(Component.literal(getReadableSize(serverInActual)));
        serverInRawText.setComponent(Component.literal(getReadableSize(serverInRaw)));
        double sInRatio = calculateRatio(serverInActual, serverInRaw);
        serverInRatioBar.setRatio(sInRatio);
        serverInRatioText.setComponent(buildRatioComponent(sInRatio));

        serverOutSpeedText.setComponent(Component.literal(getReadableSpeed(serverOutSpeed)));
        serverOutActualText.setComponent(Component.literal(getReadableSize(serverOutActual)));
        serverOutRawText.setComponent(Component.literal(getReadableSize(serverOutRaw)));
        double sOutRatio = calculateRatio(serverOutActual, serverOutRaw);
        serverOutRatioBar.setRatio(sOutRatio);
        serverOutRatioText.setComponent(buildRatioComponent(sOutRatio));
    }

    private void updateRatioTextPositions() {
        if (font == null) return;
        
        adjustRatioText(clientInRatioText);
        adjustRatioText(clientOutRatioText);
        adjustRatioText(serverInRatioText);
        adjustRatioText(serverOutRatioText);
    }

    private void adjustRatioText(Text textElement) {
        int w = textElement.getWidth();
        // Right align within the 250px container width. x = 250 - width - 5.
        // Y is set to 33 to match the new progress bar Y (44) - 11px
        textElement.setPosition(250 - w - 5, 33);
        // Important: force absolute position update since this is called after root layout in tick()
        // But since we are calling rootPanel.layout(font) AFTER updating positions, we just setPosition (relative)
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
        // Dim the background
        graphics.fill(0, 0, width, height, 0x80000000);

        if (rootPanel != null) {
            rootPanel.render(graphics, font, mouseX, mouseY, a);
        }
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