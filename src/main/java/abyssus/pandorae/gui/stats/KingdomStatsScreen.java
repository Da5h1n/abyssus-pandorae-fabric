package abyssus.pandorae.gui.stats;

import abyssus.pandorae.component.KingdomComponent;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.networking.AbilityPurchasePayload;
import abyssus.pandorae.util.AbilityLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class KingdomStatsScreen extends Screen {

    private static final Identifier PADLOCK_ICON = Identifier.of("minecraft", "textures/gui/sprites/widget/locked_button_disabled.png");
    private static final Identifier BACKGROUND_TEX = Identifier.of("minecraft", "textures/block/stone_bricks.png");

    private final List<AbilityData> abilityList = new ArrayList<>();
    private final Map<String, AbilityData> abilityCashe = new HashMap<>();
    private final Map<String, Vector2i> positions = new HashMap<>();
    private int lastFaith = -1;
    private int lastUnlockedCount = -1;

    private record Vector2i(int x, int y) { }

    public KingdomStatsScreen() {
        super(Text.literal("Kingdom Statistics"));
    }

    private double scrollX = 0;
    private double scrollY = 0;
    private float zoomScale = 1.0f; // Default zoom
    private final int hSpacing = 120;
    private final int vSpacing = 60;

    @Override
    protected void init() {
        if (this.client == null || this.client.player == null) return;

        var component = ModComponents.KINGDOM.get(this.client.player);
        this.abilityList.clear();
        this.abilityCashe.clear();

        List<AbilityData> loaded = AbilityLoader.loadForKingdom(component.getKingdom());
        this.abilityList.addAll(loaded);
        for (AbilityData data : loaded) {
            abilityCashe.put(data.id(), data);
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (AbilityData data : this.abilityList) {

            int renderX = centerX + (data.gridX() * hSpacing) - 50;
            int renderY = centerY - (data.gridY() * vSpacing) - 10;

            boolean owned = component.hasAbility(data.id());
            boolean prereqMet = isPrereqMet(data, component);
            boolean hasEnoughFaith = component.getFaith() >= data.cost();
            boolean hasConflict = data.conflicts() != null && data.conflicts().stream().anyMatch(component::hasAbility);

            ButtonWidget btn = ButtonWidget.builder(Text.literal(data.name()), button -> {
                ClientPlayNetworking.send(new AbilityPurchasePayload(data.id()));
            })
                    .dimensions(renderX, renderY, 100, 20)
                    .build();

            btn.active = !owned && !hasConflict && prereqMet && hasEnoughFaith;
            this.addDrawableChild(btn);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.client == null || this.client.player == null) return;

        renderParallaxBackground(context);

        // tint over background
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        // save screen state
        context.getMatrices().pushMatrix();

        // move to mouse, scale and then move back
        context.getMatrices().translate(this.width / 2f, this.height / 2f);
        context.getMatrices().scale(zoomScale, zoomScale);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f);

        context.getMatrices().translate((float) scrollX, (float) scrollY);

        double sx = (mouseX - this.width / 2.0) / zoomScale + (this.width / 2.0) - scrollX;
        double sy = (mouseY - this.height / 2.0) / zoomScale + (this.height / 2.0) - scrollY;

        var component = ModComponents.KINGDOM.get(this.client.player);
        renderTreeLines(context, component);
        renderConflictLines(context, component);

        super.render(context, (int) sx, (int) sy, deltaTicks);
        renderPadlocks(context, component);

        //Tooltip logic
        for (AbilityData data : abilityList) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // calculate button position and bounds in world space
            int btnX = centerX + (data.gridX() * hSpacing) - 50;
            int btnY = centerY - (data.gridY() * vSpacing) - 10;
            int btnW = 100;
            int btnH = 20;

            // check if the scaled/planned mouse (sx, sy) is inside this buttons area
            if (sx >= btnX && sx <= btnX + btnW && sy >= btnY && sy <= btnY + btnH) {
                renderAbilityTooltip(context, mouseX, mouseY, data, component);
                break;
            }
        }

        context.getMatrices().popMatrix();
        //Header Logic
        renderHeader(context, component);
    }

    private void renderHeader(DrawContext context, KingdomComponent component) {
        int headerWidth = 140;
        int headerHeight = 30;
        int headerY = 10;
        int spacingFromCenter = 10;
        int borderColor = 0xFFAAAAAA;

        //Left header: Faith

        int faithX = (this.width / 2) - headerWidth - spacingFromCenter;
        drawHeaderBox(context, faithX, headerY, headerWidth, headerHeight, borderColor);

        Text faithText = Text.literal("Faith: ").append(Text.literal(String.valueOf(component.getFaith())).formatted(Formatting.GOLD));
        context.drawCenteredTextWithShadow(this.textRenderer, faithText, faithX + (headerWidth / 2), headerY + 10, -1);

        //Right Header: Soul State
        int soulX = (this.width / 2) + spacingFromCenter;
        drawHeaderBox(context, soulX, headerY, headerWidth, headerHeight, borderColor);

        Text soulText = Text.literal( component.getSoulState().getIconChar().formatted(Formatting.WHITE) + " Soul: ").append(Text.literal(String.valueOf(component.getSoulState().getDisplayName())).formatted(Formatting.AQUA));
        context.drawCenteredTextWithShadow(this.textRenderer, soulText, soulX + (headerWidth / 2), headerY + 10, -1);
    }

    private void drawHeaderBox(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + h, 0xAA000000); // Background
        context.fill(x, y, x + w, y + 1, color); // Top border
        context.fill(x, y + h - 1, x + w, y + h, color); //Bottom border
        context.fill(x, y, x + 1, y + h, color); // Left border
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void renderTreeLines(DrawContext context, KingdomComponent component) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (AbilityData child : abilityList) {
            for (String preId : child.prerequisites()) {
                AbilityData parentData = abilityCashe.get(preId);
                if (parentData == null) continue;

                int startX = centerX + (parentData.gridX() * hSpacing);
                int startY = centerY - (parentData.gridY() * vSpacing);
                int endX = centerX + (child.gridX() * hSpacing);
                int endY = centerY - (child.gridY() * vSpacing);

                //determine line colour
                int colour = getLineColour(child, parentData, component);

                drawConnectingLine(context, startX, startY - 10, endX, endY + 10, colour);
            }
        }
    }

    private void renderConflictLines(DrawContext context, KingdomComponent component) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (AbilityData data : abilityList) {
            if (data.conflicts() == null) continue;
            for (String conflitId : data.conflicts()) {
                AbilityData conflictData = abilityCashe.get(conflitId);
                // only draw line once by comparing IDs
                if (conflictData != null && data.id().compareTo(conflitId) < 0) {
                    int x1 = centerX + (data.gridX() * hSpacing);
                    int y1 = centerY - (data.gridY() * vSpacing);
                    int x2 = centerX + (conflictData.gridX() * hSpacing);
                    int y2 = centerY - (conflictData.gridY() * vSpacing);

                    // if either side of the conflict is owned make the "X" line bright red
                    boolean conflictActive = component.hasAbility(data.id()) || component.hasAbility(conflitId);
                    int lineColour = conflictActive ? 0xFFFF0000 : 0xFF770000; // bright red if choice made, Dim Red if available

                    drawDashedLine(context, x1, y1, x2, y2, lineColour);
                    context.drawText(this.textRenderer, "X", (x1 + x2) / 2 - 3, (y1 + y2) / 2 - 4, lineColour, false);
                }
            }
        }
    }

    private void renderPadlocks(DrawContext context, KingdomComponent component) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (AbilityData data : abilityList) {
            if (!isPrereqMet(data, component) || component.getFaith() < data.cost()) {
                int x = centerX + (data.gridX() * hSpacing) + 35;
                int y = centerY - (data.gridY() * vSpacing) - 6;

                if (x > -500 && x < this.width + 500 && y > -500 && y < this.height + 500) {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, PADLOCK_ICON, x, y, 0, 0, 12, 12, 12, 12);
                }
            }
        }
    }

    private void renderAbilityTooltip(DrawContext context, int x, int y, AbilityData data, KingdomComponent component) {
        List<Text> lines = new ArrayList<>();

        //Title
        lines.add(Text.literal(data.name()).formatted(Formatting.BOLD, Formatting.WHITE));

        //Description
        lines.add(Text.literal(data.description()).formatted(Formatting.GRAY));

        lines.add(Text.empty()); // Spacer

        //Cost
        Formatting costColour = component.getFaith() >= data.cost() ? Formatting.GOLD : Formatting.RED;
        lines.add(Text.literal("Cost: " + data.cost() + " Faith").formatted(costColour));

        // Status / Requirements
        if (component.hasAbility(data.id())) {
            lines.add(Text.literal("ALREADY UNLOCKED").formatted(Formatting.GREEN));
        } else if (!isPrereqMet(data, component)) {
            lines.add(Text.literal("LOCKED: Missing Prerequisites").formatted(Formatting.RED));
        }

        context.drawTooltip(this.textRenderer, lines, x, y);
    }

    private void renderParallaxBackground(DrawContext context) {
        // multiply scroll by a small number
        float bgScrollX = (float) (scrollX * 0.15);
        float bgScrollY = (float) (scrollY * 0.15);

        // draw tiled texture across the screen
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEX,
                0, 0,
                bgScrollX, bgScrollY,
                this.width, this.height,
                32, 32
        );
    }

    private void drawConnectingLine(DrawContext context, int startX, int startY, int endX, int endY, int colour) {
        // draw diagonal connector

        int dx = endX - startX;
        int dy = endY - startY;
        int steps = Math.max(Math.abs(dx), Math.abs(dy)) * 2;

        for (int i = 0; i <= steps; i++) {
            float ratio = (float) i / steps;
            int px = (int) (startX + (dx * ratio));
            int py = (int) (startY + (dy * ratio));
            context.fill(px - 1, py - 1, px + 1, py + 1, colour);
        }
    }

    private void drawDashedLine(DrawContext context, int x1, int y1, int x2, int y2, int colour) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        for (float i = 0; i < distance; i += 6) {// "4" is the length of dashes
            for (float j = 0; j < 3; j++){
                float t = (i + j) / distance;
                if (t > 1.0f) break;

                int px = (int) (x1 + dx * t);
                int py = (int) (y1 + dy * t);

                context.fill(px - 1, py - 1, px + 1, py + 1, colour);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // zoom in when scrolling up, out when scrolling down
        float zoomSpeed = 0.1f;
        if (verticalAmount > 0 ) {
            zoomScale += zoomSpeed;
        } else {
            zoomScale -= zoomSpeed;
        }

        zoomScale = Math.max(0.5f, Math.min(zoomScale, 2.5f));


        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (click.button() == 0) {
            this.scrollX += offsetX / zoomScale;
            this.scrollY += offsetY / zoomScale;
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double sx = (click.x() - (this.width / 2.0)) / zoomScale + (this.width / 2.0) - scrollX;
        double sy = (click.y() - (this.height / 2.0)) / zoomScale + (this.height / 2.0) - scrollY;

        Click scaledClick = new Click(sx, sy, click.buttonInfo());
        return super.mouseClicked(scaledClick, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double sx = (click.x() - (this.width / 2.0)) / zoomScale + (this.width / 2.0) - scrollX;
        double sy = (click.y() - (this.height / 2.0)) / zoomScale + (this.height / 2.0) - scrollY;

        Click scaledClick = new Click(sx, sy, click.buttonInfo());
        return super.mouseReleased(scaledClick);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.client.player == null) return;
        var component = ModComponents.KINGDOM.get(this.client.player);

        if (component.getFaith() != lastFaith || component.getPurchasedAbilities().size() != lastUnlockedCount) {
            lastFaith = component.getFaith();
            lastUnlockedCount = component.getPurchasedAbilities().size();
            this.clearAndInit();
        }
    }

    private int getLineColour(AbilityData child, AbilityData parent, KingdomComponent component) {
        boolean isBlocked = child.conflicts() != null && child.conflicts().stream().anyMatch(component::hasAbility);
        boolean parentOwned = component.hasAbility(parent.id());
        boolean childOwned = component.hasAbility(child.id());

        if (isBlocked) return  0xFF8B0000;  //DARK RED
        if (parentOwned && childOwned) return 0xFFFFAA00; // GOLD
        return 0xFF555555; // Grey
    }

    private boolean isPrereqMet(AbilityData data, KingdomComponent component) {
        if (data.prerequisites().isEmpty()) return true;
        return data.prerequisites().stream().allMatch(component::hasAbility);
    }
}


