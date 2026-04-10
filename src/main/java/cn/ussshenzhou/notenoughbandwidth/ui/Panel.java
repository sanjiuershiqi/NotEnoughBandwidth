package cn.ussshenzhou.notenoughbandwidth.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Panel extends Element {
    
    public enum LayoutType {
        VERTICAL, HORIZONTAL, ABSOLUTE
    }
    
    public enum Alignment {
        START, CENTER, END
    }

    private final List<Element> children = new CopyOnWriteArrayList<>();
    private LayoutType layoutType = LayoutType.VERTICAL;
    private Alignment alignment = Alignment.START;
    private int spacing = 0;
    private int padding = 0;
    
    // Background properties
    private int backgroundColor = 0x00000000;
    private int borderColor = 0x00000000;
    private boolean drawBackground = false;
    private int borderRadius = 0;
    private int borderThickness = 1;

    public Panel() {
    }

    public void add(Element child) {
        children.add(child);
    }

    public void remove(Element child) {
        children.remove(child);
    }

    public void clear() {
        children.clear();
    }

    public void setLayoutType(LayoutType type) {
        this.layoutType = type;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setBackground(int color, int border, int radius) {
        this.backgroundColor = color;
        this.borderColor = border;
        this.borderRadius = radius;
        this.drawBackground = true;
    }
    
    public void setBackground(int color, int border) {
        setBackground(color, border, 0);
    }
    
    public void disableBackground() {
        this.drawBackground = false;
    }

    @Override
    public void layout(net.minecraft.client.gui.Font font) {
        if (!visible) return;
        
        int currentX = x + padding;
        int currentY = y + padding;
        
        int maxWidth = 0;
        int maxHeight = 0;

        // First pass: layout children and calculate our size
        for (Element child : children) {
            if (!child.isVisible()) continue;
            
            if (layoutType == LayoutType.VERTICAL) {
                currentY += child.getMarginTop();
                child.setAbsolutePosition(currentX + child.getMarginLeft(), currentY);
                child.layout(font);
                
                int childTotalWidth = child.getWidth() + child.getMarginLeft() + child.getMarginRight();
                if (childTotalWidth > maxWidth) maxWidth = childTotalWidth;
                
                currentY += child.getHeight() + child.getMarginBottom() + spacing;
            }
            else if (layoutType == LayoutType.HORIZONTAL) {
                currentX += child.getMarginLeft();
                child.setAbsolutePosition(currentX, currentY + child.getMarginTop());
                child.layout(font);
                
                int childTotalHeight = child.getHeight() + child.getMarginTop() + child.getMarginBottom();
                if (childTotalHeight > maxHeight) maxHeight = childTotalHeight;
                
                currentX += child.getWidth() + child.getMarginRight() + spacing;
            }
            else {
                child.setAbsolutePosition(x + child.getRelX() + padding, y + child.getRelY() + padding);
                child.layout(font);
                if (child.getRelX() + child.getWidth() + padding > maxWidth) maxWidth = child.getRelX() + child.getWidth() + padding;
                if (child.getRelY() + child.getHeight() + padding > maxHeight) maxHeight = child.getRelY() + child.getHeight() + padding;
            }
        }
        
        // Auto-size if width/height is 0
        if (width <= 0) {
            if (layoutType == LayoutType.VERTICAL) {
                this.width = maxWidth + padding * 2;
            } else if (layoutType == LayoutType.HORIZONTAL) {
                this.width = currentX - x + padding - spacing;
            }
        }
        if (height <= 0) {
            if (layoutType == LayoutType.VERTICAL) {
                this.height = currentY - y + padding - spacing;
            } else if (layoutType == LayoutType.HORIZONTAL) {
                this.height = maxHeight + padding * 2;
            }
        }
        
        // Second pass: align children if needed
        if (alignment != Alignment.START) {
            for (Element child : children) {
                if (!child.isVisible()) continue;
                if (layoutType == LayoutType.VERTICAL) {
                    int availableSpace = this.width - padding * 2;
                    int childTotalWidth = child.getWidth() + child.getMarginLeft() + child.getMarginRight();
                    if (alignment == Alignment.CENTER) {
                        child.setAbsolutePosition(x + padding + (availableSpace - childTotalWidth) / 2 + child.getMarginLeft(), child.getY());
                    } else if (alignment == Alignment.END) {
                        child.setAbsolutePosition(x + width - padding - child.getWidth() - child.getMarginRight(), child.getY());
                    }
                    // Relayout child if its internal elements need adjustment, but since its size didn't change, we skip for now.
                    // If we want deep relayout, we call child.layout(font) again, but it's not strictly necessary if position doesn't affect its own layout.
                    // However, absolute children might need it. Let's just adjust X and call layout again if it's a Panel.
                    if (child instanceof Panel) {
                        child.layout(font);
                    }
                } else if (layoutType == LayoutType.HORIZONTAL) {
                    int availableSpace = this.height - padding * 2;
                    int childTotalHeight = child.getHeight() + child.getMarginTop() + child.getMarginBottom();
                    if (alignment == Alignment.CENTER) {
                        child.setAbsolutePosition(child.getX(), y + padding + (availableSpace - childTotalHeight) / 2 + child.getMarginTop());
                    } else if (alignment == Alignment.END) {
                        child.setAbsolutePosition(child.getX(), y + height - padding - child.getHeight() - child.getMarginBottom());
                    }
                    if (child instanceof Panel) {
                        child.layout(font);
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        
        if (drawBackground) {
            // Draw background fill
            if (backgroundColor != 0x00000000) {
                RenderUtil.fillRoundedRect(graphics, x, y, width, height, borderRadius, backgroundColor);
            }
            // Draw border
            if (borderColor != 0x00000000) {
                RenderUtil.fillRoundedBorder(graphics, x, y, width, height, borderRadius, borderThickness, borderColor);
            }
        }
        
        for (Element child : children) {
            child.render(graphics, font, mouseX, mouseY, partialTicks);
        }
    }
}
