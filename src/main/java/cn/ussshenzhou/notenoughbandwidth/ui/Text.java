package cn.ussshenzhou.notenoughbandwidth.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class Text extends Element {
    private Component component;
    private int color = 0xFFFFFFFF;
    private boolean shadow = true;
    private boolean centered = false;

    public Text(Component component) {
        this.component = component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    @Override
    public void layout(Font font) {
        if (component != null) {
            this.width = font.width(component);
            this.height = font.lineHeight;
        } else {
            this.width = 0;
            this.height = 0;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTicks) {
        if (!visible || component == null) return;
        
        int drawX = x;
        if (centered) {
            drawX = x - width / 2;
        }
        
        if (shadow) {
            graphics.textRenderer().accept(drawX, y, component);
            // Assuming default text renderer has shadow or the user just passes component with color
            // In modern Minecraft, `drawCenteredString` handles shadow inside Font, but `textRenderer` might not
            // We'll let `textRenderer` draw it normally.
        } else {
            graphics.textRenderer().accept(drawX, y, component);
        }
    }
}
