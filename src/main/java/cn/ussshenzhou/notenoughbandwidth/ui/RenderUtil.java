package cn.ussshenzhou.notenoughbandwidth.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class RenderUtil {
    
    public static void fillRoundedRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
        if (radius <= 0) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }
        
        // Draw the center large rect
        graphics.fill(x + radius, y, x + width - radius, y + height, color);
        // Draw the left middle rect
        graphics.fill(x, y + radius, x + radius, y + height - radius, color);
        // Draw the right middle rect
        graphics.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
        
        // Draw the 4 corners using horizontal strips
        for (int i = 0; i < radius; i++) {
            int yDist = radius - i;
            int xDist = (int) Math.round(Math.sqrt(radius * radius - yDist * yDist));
            int offset = radius - xDist;
            
            // Top Left
            graphics.fill(x + offset, y + i, x + radius, y + i + 1, color);
            // Top Right
            graphics.fill(x + width - radius, y + i, x + width - offset, y + i + 1, color);
            // Bottom Left
            graphics.fill(x + offset, y + height - 1 - i, x + radius, y + height - i, color);
            // Bottom Right
            graphics.fill(x + width - radius, y + height - 1 - i, x + width - offset, y + height - i, color);
        }
    }

    public static void fillRoundedBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int thickness, int color) {
        if (radius <= 0) {
            graphics.fill(x, y, x + width, y + thickness, color); // Top
            graphics.fill(x, y + height - thickness, x + width, y + height, color); // Bottom
            graphics.fill(x, y, x + thickness, y + height, color); // Left
            graphics.fill(x + width - thickness, y, x + width, y + height, color); // Right
            return;
        }
        
        // Let's just do a simple outer minus inner
        // Since we can't easily do boolean ops, we just draw lines
        // Top edge
        graphics.fill(x + radius, y, x + width - radius, y + thickness, color);
        // Bottom edge
        graphics.fill(x + radius, y + height - thickness, x + width - radius, y + height, color);
        // Left edge
        graphics.fill(x, y + radius, x + thickness, y + height - radius, color);
        // Right edge
        graphics.fill(x + width - thickness, y + radius, x + width, y + height - radius, color);
        
        // Corners - we can just draw them pixel by pixel for thickness, but it's simpler to just not support complex border rounding
        // Instead we can just draw a rounded rect, and then draw an inner rounded rect with the background color to fake it, 
        // but that requires knowing the background color. 
        // Let's just use a simple approach for border corners: draw small rects.
        for (int i = 0; i < radius; i++) {
            int yDist = radius - i;
            int xDistOut = (int) Math.round(Math.sqrt(radius * radius - yDist * yDist));
            int offsetOut = radius - xDistOut;
            
            int innerRadius = radius - thickness;
            int offsetIn = radius;
            if (innerRadius > 0 && i >= thickness) {
                int yDistIn = innerRadius - (i - thickness);
                int xDistIn = (int) Math.round(Math.sqrt(innerRadius * innerRadius - yDistIn * yDistIn));
                offsetIn = radius - innerRadius + (innerRadius - xDistIn);
            }
            
            // Top Left
            graphics.fill(x + offsetOut, y + i, x + offsetIn, y + i + 1, color);
            // Top Right
            graphics.fill(x + width - offsetIn, y + i, x + width - offsetOut, y + i + 1, color);
            // Bottom Left
            graphics.fill(x + offsetOut, y + height - 1 - i, x + offsetIn, y + height - i, color);
            // Bottom Right
            graphics.fill(x + width - offsetIn, y + height - 1 - i, x + width - offsetOut, y + height - i, color);
        }
    }
}
