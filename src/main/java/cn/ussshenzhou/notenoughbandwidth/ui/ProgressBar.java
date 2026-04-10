package cn.ussshenzhou.notenoughbandwidth.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ProgressBar extends Element {
    
    private double ratio = 0.0;
    private int backgroundColor = 0xFF333333;
    private int borderColor = 0xFF555555;
    private int fillColor = 0xFF55FF55;
    
    public ProgressBar(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setRatio(double ratio) {
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;
        this.ratio = ratio;
    }

    public void setColors(int background, int border, int fill) {
        this.backgroundColor = background;
        this.borderColor = border;
        this.fillColor = fill;
    }
    
    public void setFillColor(int fill) {
        this.fillColor = fill;
    }

    @Override
    public void layout(Font font) {
        // Fixed size, no layout needed
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        int radius = height / 2;
        if (radius > 4) radius = 4; // limit radius

        // Background
        RenderUtil.fillRoundedRect(graphics, x, y, width, height, radius, backgroundColor);
        
        // Border (just draw slightly smaller background, actually, no need for border if we just want a clean look)
        // or we draw a border by drawing a larger rect then smaller.
        // Let's just draw the fill inside with padding
        
        // Fill
        int fillWidth = (int) ((width - 2) * ratio);
        if (fillWidth > 0) {
            // Draw rounded fill inside the progress bar
            int fillRadius = Math.min(radius, height / 2 - 1);
            if (fillWidth < fillRadius * 2) {
                // Too small to round properly, just draw rect
                graphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + height - 1, fillColor);
            } else {
                RenderUtil.fillRoundedRect(graphics, x + 1, y + 1, fillWidth, height - 2, fillRadius, fillColor);
            }
        }
    }
}
