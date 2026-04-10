package cn.ussshenzhou.notenoughbandwidth.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class Element {
    protected int x = 0;
    protected int y = 0;
    protected int relX = 0;
    protected int relY = 0;
    protected int width;
    protected int height;

    // For layout system
    protected int marginTop;
    protected int marginBottom;
    protected int marginLeft;
    protected int marginRight;

    protected boolean visible = true;

    public Element() {
    }

    public void setPosition(int x, int y) {
        this.relX = x;
        this.relY = y;
        // In most cases, relX/relY are local to parent, x/y are absolute on screen.
        // For backwards compatibility and simplicity, let's keep x/y as the absolute rendering coordinates.
        this.x = x;
        this.y = y;
    }

    public void setAbsolutePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setRelativePosition(int relX, int relY) {
        this.relX = relX;
        this.relY = relY;
    }

    public int getRelX() { return relX; }
    public int getRelY() { return relY; }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setMargin(int top, int right, int bottom, int left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    public int getMarginTop() { return marginTop; }
    public int getMarginBottom() { return marginBottom; }
    public int getMarginLeft() { return marginLeft; }
    public int getMarginRight() { return marginRight; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    /**
     * Compute layout for this element and its children.
     * Usually calculates dimensions and sets positions of children.
     */
    public abstract void layout(net.minecraft.client.gui.Font font);

    /**
     * Render the element.
     */
    public abstract void render(GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY, float partialTicks);
}
