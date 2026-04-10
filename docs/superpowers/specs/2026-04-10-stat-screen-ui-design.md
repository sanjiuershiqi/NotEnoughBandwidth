# Not Enough Bandwidth (NEB) StatScreen UI Design Specification

## Overview

This specification details the UI overhaul for the in-game statistics screen (`StatScreen.java`) of the Not Enough Bandwidth (NEB) Minecraft mod. The goal is to transform the current plain-text debug output into a modern, user-friendly dashboard (Option A from brainstorming) that clearly visualizes network optimization metrics.

## Design Approach: The "Dashboard" Layout

The new design will adopt a dual-panel dashboard layout, separating Client and Server metrics into distinct visual blocks. This approach emphasizes readability and introduces visual indicators (progress bars) to instantly convey compression efficiency.

### Layout Structure

1. **Background**: Retain the default transparent dark overlay (`0x80000000`).
2. **Main Container**: A centered container split into two equal-width columns (Left: Client, Right: Server).
3. **Panels**:
    - Each panel (Client / Server) will have a distinct background box with a border (e.g., `0x90000000` with a `0xFF555555` border) to group related information.
4. **Sections**:
    - Each panel will be divided into two main sections: **↓ Inbound** and **↑ Outbound**.

### Data Presentation

Instead of long, concatenated strings, data will be presented in aligned rows (Key-Value pairs):

- **Speed**: Current transmission rate.
- **Actual Total**: Compressed data sent/received.
- **Raw Total**: Original uncompressed data size.

### Visual Elements (The "Ratio Bar")

To make the compression ratio intuitive, a visual progress bar will be rendered beneath the Inbound and Outbound data blocks in each panel.

- **Background**: A dark, hollow rectangle.
- **Fill**: A solid rectangle representing the compression ratio (`Actual / Raw`).
- **Color Coding**:
    - **Inbound (↓)**: Green (`0xFF55FF55`) to indicate receiving data.
    - **Outbound (↑)**: Red (`0xFFFF5555`) to indicate sending data.
- **Text**: The percentage (e.g., `18.30%`) will be rendered inside or just above the bar.

## Implementation Strategy (`StatScreen.java`)

### 1. Data Fetching

- Retain the existing `tick()` logic to query the server every 10 ticks (`tick % 10 == 0`).
- Extract the raw numerical values (bytes and speeds) into local variables within the `StatScreen` class rather than pre-formatting them into long, single strings in the `tick()` method. This allows the rendering method to place them dynamically.

### 2. Rendering (`extractRenderState`)

- **Screen Dimensions**: Utilize the screen's `width` and `height` to dynamically center the UI.
- **Boxes**: Use `graphics.fill()` to draw the panel backgrounds and borders.
- **Text**: Use `graphics.drawString()` (or `graphics.textRenderer().accept()`) to render labels and values with specific colors and alignments.
    - Labels: Gray (`0xFFAAAAAA`).
    - Values: White (`0xFFFFFFFF`).
    - Titles: Gold (`0xFFFFAA00`).
- **Progress Bars**: Use `graphics.fill()` to draw the background and the calculated fill width based on the ratio.

### 3. Utility Methods

- Keep and utilize `getReadableSpeed(int bytes)` and `getReadableSize(long bytes)`.
- Ensure they return strings without hardcoded layout spacing, focusing only on the value and unit.

## Acceptance Criteria

- [ ] The UI displays Client metrics on the left and Server metrics on the right.
- [ ] Inbound and Outbound sections are clearly distinguished by headers and colors.
- [ ] Compression ratios are visually represented by filled bars (Green for Inbound, Red for Outbound).
- [ ] The UI scales and centers properly on different window sizes.
- [ ] The data updates dynamically (every 10 ticks) as it currently does.