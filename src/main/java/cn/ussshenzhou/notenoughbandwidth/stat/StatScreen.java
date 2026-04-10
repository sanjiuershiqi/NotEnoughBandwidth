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
        rootPanel.setPosition(0, 0);
        rootPanel.setSize(width, height);

        Panel mainContainer = new Panel();
        // If the screen is too narrow (e.g. GUI Scale 5 or 6 on smaller resolutions), stack them vertically
        // Assume minimum width for horizontal layout is 600
        int singlePanelWidth = Math.min(280, width - 20); // 10 padding on each side
        if (width < singlePanelWidth * 2 + 40) {
            mainContainer.setLayoutType(Panel.LayoutType.VERTICAL);
        } else {
            mainContainer.setLayoutType(Panel.LayoutType.HORIZONTAL);
        }
        mainContainer.setSpacing(20);

        Panel clientPanel = createPanel(clientTitle, true);
        Panel serverPanel = createPanel(serverTitle, false);

        mainContainer.add(clientPanel);
        mainContainer.add(serverPanel);

        // Dynamically compute width and height to center
        int mainWidth;
        int mainHeight;
        
        int panelWidth = Math.min(280, width - 20);
        int panelHeight = Math.min(220, (height - 20) / (width < (panelWidth * 2 + 40) ? 2 : 1));
        if (panelHeight < 150) panelHeight = 150;

        if (width < panelWidth * 2 + 40) {
            mainWidth = panelWidth; // Single panel width
            mainHeight = panelHeight * 2 + 20; // Two panels stacked + spacing
        } else {
            mainWidth = panelWidth * 2 + 20;
            mainHeight = panelHeight;
        }
        
        // Handle vertical overflow for extremely small heights (e.g. GUI Scale 5)
        int startY = (height - mainHeight) / 2;
        if (startY < 0) {
            // Let it be scrollable? No, let's just make it start from top
            startY = 5;
        }
        
        // Scale down the entire container using PoseStack/GuiGraphics scale?
        // Let's just adjust spacing or height if it still overlaps.
        // Actually, the main container is now dynamically sized. Let's make sure elements inside are properly constrained.
        
        // Ensure width isn't negative
        int startX = (width - mainWidth) / 2;
        if (startX < 0) startX = 10;
        
        mainContainer.setPosition(startX, startY);
        mainContainer.setSize(mainWidth, mainHeight);

        rootPanel.add(mainContainer);
        
        // Initial layout and data update
        updateData();
        rootPanel.layout(font);
        updateRatioTextPositions();
        rootPanel.layout(font);
    }

    private Panel createPanel(Component title, boolean isClient) {
        Panel panel = new Panel();
        // Dynamically scale panel width if the screen is narrower than the default 280
        int panelWidth = Math.min(280, width - 20); // 10 padding on each side
        // If height is very small, we might want to shrink the panel height and text spacing
        int panelHeight = Math.min(220, (height - 20) / (width < (panelWidth * 2 + 40) ? 2 : 1));
        if (panelHeight < 150) panelHeight = 150; // Minimum usable height
        
        panel.setSize(panelWidth, panelHeight); 
        panel.setLayoutType(Panel.LayoutType.ABSOLUTE);
        // Rounded background with transparent dark color and white border
        panel.setBackground(0x90000000, 0xAAFFFFFF, 8);

        Text titleText = new Text(title);
        titleText.setCentered(true);
        titleText.setPosition(panelWidth / 2, 10);
        panel.add(titleText);

        int sectionHeight = (panelHeight - 30) / 2; // Remaining height for 2 sections
        
        Panel inboundPanel = createDataSection(true, isClient, panelWidth, sectionHeight);
        inboundPanel.setPosition(10, 30);
        panel.add(inboundPanel);

        Panel outboundPanel = createDataSection(false, isClient, panelWidth, sectionHeight);
        outboundPanel.setPosition(10, 30 + sectionHeight);
        panel.add(outboundPanel);

        return panel;
    }

    private Panel createDataSection(boolean isInbound, boolean isClient, int panelWidth, int maxSectionHeight) {
        Panel panel = new Panel();
        panel.setLayoutType(Panel.LayoutType.ABSOLUTE);
        int innerWidth = panelWidth - 20; // 10 padding on each side of the panel
        
        // Dynamically adjust vertical spacing to prevent overlap
        int lineGap = Math.max(9, (maxSectionHeight - 40) / 3); 
        int titleY = 0;
        int rowY = titleY + 12;
        int barY = rowY + lineGap * 3 + 4; // Give it 4px gap below rawLabel
        
        panel.setSize(innerWidth, barY + 12); 

        Component titleComp = isInbound ? inboundTitle : outboundTitle;
        Text sectionTitle = new Text(titleComp);
        sectionTitle.setPosition(0, titleY);
        panel.add(sectionTitle);

        // Dynamically compute the value X offset based on innerWidth
        int valX = Math.min(100, innerWidth / 2 + 10);

        Text speedLbl = new Text(speedLabel);
        speedLbl.setPosition(0, rowY);
        panel.add(speedLbl);

        Text speedVal = new Text(Component.empty());
        speedVal.setPosition(valX, rowY);
        panel.add(speedVal);

        Text actualLbl = new Text(actualLabel);
        actualLbl.setPosition(0, rowY + lineGap);
        panel.add(actualLbl);

        Text actualVal = new Text(Component.empty());
        actualVal.setPosition(valX, rowY + lineGap);
        panel.add(actualVal);

        Text rawLbl = new Text(rawLabel);
        rawLbl.setPosition(0, rowY + lineGap * 2);
        panel.add(rawLbl);

        Text rawVal = new Text(Component.empty());
        rawVal.setPosition(valX, rowY + lineGap * 2);
        panel.add(rawVal);

        ProgressBar bar = new ProgressBar(innerWidth, 8);
        bar.setPosition(0, barY);
        int fillColor = isInbound ? 0xFF55FF55 : 0xFFFF5555;
        bar.setColors(0xFF333333, 0xFF555555, fillColor);
        panel.add(bar);

        Text ratioVal = new Text(Component.empty());
        ratioVal.setPosition(innerWidth - 50, barY - 11);
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
            rootPanel.layout(font);
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
        
        int panelWidth = Math.min(280, width - 20);
        int innerWidth = panelWidth - 20; // 10 padding on each side of panel
        
        int panelHeight = Math.min(220, (height - 20) / (width < (panelWidth * 2 + 40) ? 2 : 1));
        if (panelHeight < 150) panelHeight = 150;
        int sectionHeight = (panelHeight - 30) / 2;
        int lineGap = Math.max(9, (sectionHeight - 40) / 3); 
        int titleY = 0;
        int rowY = titleY + 12;
        int barY = rowY + lineGap * 3 + 4;
        
        adjustRatioText(clientInRatioText, innerWidth, barY);
        adjustRatioText(clientOutRatioText, innerWidth, barY);
        adjustRatioText(serverInRatioText, innerWidth, barY);
        adjustRatioText(serverOutRatioText, innerWidth, barY);
    }

    private void adjustRatioText(Text textElement, int innerWidth, int barY) {
        int w = textElement.getWidth();
        // Right align within the dynamically computed innerWidth.
        textElement.setPosition(innerWidth - w - 5, barY - 11);
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