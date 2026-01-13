package abyssus.pandorae.gui.stats;

import abyssus.pandorae.AbyssusPandorae;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class KingdomStatsScreen extends Screen {

    private static final Identifier PADLOCK_ICON = Identifier.of("minecraft", "textures/gui/sprites/widget/locked_button_disabled.png");
    private static final Identifier BACKGROUND_TEX = Identifier.of("minecraft", "textures/block/stone_bricks.png");

    private final List<AbilityData> abilityList = new ArrayList<>();
    private final Map<String, AbilityData> abilityCashe = new HashMap<>();
    private final Map<String, Vector2i> positions = new HashMap<>();
    private int lastFaith = -1;
    private int lastUnlockedCount = -1;

    public record Vector2i(int x, int y) { }

    public KingdomStatsScreen() {
        super(Text.literal("Kingdom Statistics"));
    }

    private double scrollX = 0;
    private double scrollY = 0;
    private float zoomScale = 1.0f; // Default zoom
    private final int hSpacing = 120;
    private final int vSpacing = 60;

    private AbilityData selectedAbility = null;
    private double targetScrollX = 0;
    private double targetScrollY = 0;
    private static final float LERP_SPEED = 0.15f;

    private boolean isConfirming = false;
    private AbilityData pendingAbility = null;
    private ButtonWidget confirmationButton;
    private ButtonWidget denyButton;

    private final List<PathDot> activeDots = new ArrayList<>();

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

        this.targetScrollX = 0;
        this.targetScrollY = 0;

        this.scrollX = 0;
        this.scrollY = 0;

        int buttonYOffset = 25;

        this.confirmationButton = ButtonWidget.builder(Text.translatable("abyssus-pandorae.button.unlock"), button -> {
            if (pendingAbility != null) {
                ClientPlayNetworking.send(new AbilityPurchasePayload(pendingAbility.id()));
                closeConfirmation();
            }
        }).dimensions(this.width / 2 - 105, this.height / 2 + buttonYOffset, 100, 20).build();

        this.denyButton = ButtonWidget.builder(Text.translatable("abyssus-pandorae.button.cancel"), b -> closeConfirmation())
                .dimensions(this.width / 2 + 5, this.height / 2 + buttonYOffset, 100, 20).build();

        this.addDrawableChild(confirmationButton);
        this.addDrawableChild(denyButton);
        updateButtonVisiblility();

    }

    private void updateButtonVisiblility() {
        confirmationButton.visible = isConfirming;
        denyButton.visible = isConfirming;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.client == null || this.client.player == null) return;

        this.scrollX = MathHelper.lerp(LERP_SPEED, this.scrollX, this.targetScrollX);
        this.scrollY = MathHelper.lerp(LERP_SPEED, this.scrollY, this.targetScrollY);

        renderParallaxBackground(context);
        // draw dark background
        context.fill(0, 0, this.width, this.height, AbyssusPandorae.config.skillTreeBg); //0xAA000000

        // save screen state
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(this.width / 2f, this.height / 2f);
        context.getMatrices().scale(zoomScale, zoomScale);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f);

        context.getMatrices().translate((float) scrollX, (float) scrollY);

        var component = ModComponents.KINGDOM.get(this.client.player);
        renderTreeLines(context, component);
        renderConflictLines(context, component);

        // MOVE PATH DOTS
        for (PathDot dot : activeDots) {
            dot.update(deltaTicks, this.width, this.height, hSpacing, vSpacing);
            dot.render(context, this.width, this.height, hSpacing, vSpacing);
        }

        for (AbilityData data : abilityList) {
            renderAbilityButton(context, data, component);
        }

        renderPadlocks(context, component);

        //Tooltip logic
        double sx = (mouseX - this.width / 2.0) / zoomScale + (this.width / 2.0) - scrollX;
        double sy = (mouseY - this.height / 2.0) / zoomScale + (this.height / 2.0) - scrollY;
        if (!isConfirming) {
            for (AbilityData data : abilityList) {
                int centerX = this.width / 2;
                int centerY = this.height / 2;

                // calculate button position and bounds in world space
                int btnX = centerX + (data.gridX() * hSpacing) - 50;
                int btnY = centerY - (data.gridY() * vSpacing) - 10;
                // check if the scaled/planned mouse (sx, sy) is inside this buttons area
                if (sx >= btnX && sx <= btnX + 100 && sy >= btnY && sy <= btnY + 20) {
                    renderAbilityTooltip(context, mouseX, mouseY, data, component);
                    break;
                }
            }
        }

        context.getMatrices().popMatrix();

        if (isConfirming && pendingAbility != null) {
            // darken the tree
            context.fill(0, 0, this.width, this.height, 0x88000000); //0x88000000

            int boxW = 240;
            int boxH = 100;
            int bx = (this.width - boxW) / 2;
            int by = (this.height - boxH) / 2;

            drawHeaderBox(context, bx, by, boxW, boxH, AbyssusPandorae.config.confirmdialogborder, AbyssusPandorae.config.confirmdialogBg); // 0xFFFFAA00 0xFF000000
            // Title
            context.drawCenteredTextWithShadow(textRenderer, "Unlock " + pendingAbility.name() + "?", this.width / 2, by + 10, -1);
            // Description
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(pendingAbility.description()).formatted(Formatting.ITALIC, Formatting.GRAY), this.width / 2, by + 28, -1);
            // Requires
            context.drawCenteredTextWithShadow(textRenderer, "Requires: " + pendingAbility.cost() + " Faith", this.width / 2, by + 45, 0xFFFFAA00);
        }

        //Header Logic
        renderHeader(context, component);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderAbilityButton(DrawContext context, AbilityData data, KingdomComponent component) {
        int x = (this.width / 2) + (data.gridX() * hSpacing) - 50;
        int y = (this.height / 2) - (data.gridY() * vSpacing) - 10;

        boolean owned = component.hasAbility(data.id());

        int InactiveskillBorder = AbyssusPandorae.config.InactiveskillBorder;
        int ActiveskillBorder = AbyssusPandorae.config.ActiveskillBorder;

        int borderColour = selectedAbility == data ? 0xFFFFFFFF : (owned ? ActiveskillBorder : InactiveskillBorder);

        drawHeaderBox(context, x, y, 100, 20, borderColour, 0xAA000000);

        int textColour = owned ? ActiveskillBorder  : (isPrereqMet(data, component) ? 0xFFFFFFFF : InactiveskillBorder);
        context.drawCenteredTextWithShadow(this.textRenderer, data.name(), x + 50, y + 6, textColour);
    }

    private void renderHeader(DrawContext context, KingdomComponent component) {
        int headerWidth = 140;
        int headerHeight = 30;
        int headerY = 10;
        int spacingFromCenter = 10;

        int borderColour = AbyssusPandorae.config.headerBorder;
        int bgColour = AbyssusPandorae.config.headerbg;


        //Left header: Faith
        int faithX = (this.width / 2) - headerWidth - spacingFromCenter;
        drawHeaderBox(context, faithX, headerY, headerWidth, headerHeight, borderColour, bgColour);
        Text faithText = Text.literal("Faith: ").append(Text.literal(String.valueOf(component.getFaith())).formatted(Formatting.GOLD));
        context.drawCenteredTextWithShadow(this.textRenderer, faithText, faithX + (headerWidth / 2), headerY + 10, -1);

        //Right Header: Soul State
        int soulX = (this.width / 2) + spacingFromCenter;
        drawHeaderBox(context, soulX, headerY, headerWidth, headerHeight, borderColour, bgColour);
        Text soulText = Text.literal( component.getSoulState().getIconChar().formatted(Formatting.WHITE) + " Soul: ").append(Text.literal(String.valueOf(component.getSoulState().getDisplayName())).formatted(Formatting.AQUA));
        context.drawCenteredTextWithShadow(this.textRenderer, soulText, soulX + (headerWidth / 2), headerY + 10, -1);
    }

    private void drawHeaderBox(DrawContext context, int x, int y, int w, int h, int borderColor, int backgroundColour) {
        context.fill(x, y, x + w, y + h, backgroundColour); // Background
        context.fill(x, y, x + w, y + 1, borderColor); // Top border
        context.fill(x, y + h - 1, x + w, y + h, borderColor); //Bottom border
        context.fill(x, y, x + 1, y + h, borderColor); // Left border
        context.fill(x + w - 1, y, x + w, y + h, borderColor);
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
                    int lineColour = conflictActive ? AbyssusPandorae.config.Conflictlineon : AbyssusPandorae.config.Conflictlineoff; // bright red if choice made, Dim Red if available

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

        context.getMatrices().pushMatrix();

        float dx = endX - startX;
        float dy = endY - startY;
        float angle = (float) Math.atan2(dy, dx);
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        context.getMatrices().translate(startX, startY);
        context.getMatrices().rotateAbout(angle, 0,0);

        context.fill(0, -1, (int) len, 1, colour);

        context.getMatrices().popMatrix();
    }

    private List<Vector2i> calculatePathToRoot(AbilityData current) {
        List<Vector2i> path = new ArrayList<>();
        AbilityData active = current;
        List<AbilityData> chain = new ArrayList<>();

        while (active != null) {
            chain.add(active);
            if (active.prerequisites().isEmpty()) break;
            active = abilityCashe.get(active.prerequisites().getFirst());
        }

        Collections.reverse(chain);

        for (AbilityData node : chain) {
            int centerX = (this.width / 2) + (node.gridX() * hSpacing);
            int centerY = (this.height / 2) - (node.gridY() * vSpacing);

            path.add(new Vector2i(centerX, centerY + 10));
            path.add(new Vector2i(centerX, centerY - 10));
        }
        return path;
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
    public boolean mouseClicked(Click click, boolean doubled) {
        if (isConfirming) return super.mouseClicked(click, doubled);

        double sx = (click.x() - (this.width / 2.0)) / zoomScale + (this.width / 2.0) - scrollX;
        double sy = (click.y() - (this.height / 2.0)) / zoomScale + (this.height / 2.0) - scrollY;

        var component = ModComponents.KINGDOM.get(this.client.player);

        for (AbilityData data : abilityList) {
            int btnX = (this.width / 2) + (data.gridX() * hSpacing) - 50;
            int btnY = (this.height / 2) - (data.gridY() * vSpacing) - 10;

            if (sx >= btnX && sx <= btnX + 100 && sy >= btnY && sy <= btnY + 20) {
                boolean owned = component.hasAbility(data.id());
                boolean canAfford = component.getFaith() >= data.cost();
                boolean prereqMet = isPrereqMet(data, component);

                if (this.selectedAbility == data) {
                    if (!owned && canAfford && prereqMet) {
                        openCustomConfirmationDialog(data);
                    }
                } else {
                    this.selectedAbility = data;
                    this.targetScrollX = -(data.gridX() * hSpacing);
                    this.targetScrollY = (data.gridY() * vSpacing);

                    // Spawn Dots
                    activeDots.clear();
                    List<AbilityData> chain = new ArrayList<>();
                    AbilityData active = data;
                    while (active != null) {
                        chain.add(active);
                        if (component.hasAbility(active.id())) break;
                        if (active.prerequisites().isEmpty()) break;
                        active = abilityCashe.get(active.prerequisites().getFirst());
                    }
                    Collections.reverse(chain);

                    if (chain.size() > 1) {
                        float pixelsPerTick = 60.0f / 20.0f;
                        int totalSegments = chain.size() - 1;

                        float totalDist = 0;
                        for (int i = 0; i < totalSegments; i++) {
                            AbilityData s = chain.get(i);
                            AbilityData e = chain.get(i+1);
                            double dx = (e.gridX() - s.gridX()) * hSpacing;
                            double dy = (-(e.gridY() * vSpacing) + 10) - (-(s.gridY() * vSpacing) - 10);
                            totalDist += (float) Math.sqrt(dx * dx + dy * dy);
                        }

                        float spacingLerp = MathHelper.clamp((totalDist - 60f) / 400f, 0f, 1f);
                        float dynamicSpacing = MathHelper.lerp(spacingLerp, 30.0f, 45.0f);

                        int dotCount = Math.round(totalDist / dynamicSpacing);
                        dotCount = MathHelper.clamp(dotCount, 2, 15);

                        float averageSpacing = totalDist / (float) dotCount;
                        float delayPerDot = averageSpacing / pixelsPerTick;

                        for (int i = 0; i < dotCount; i++) {
                            activeDots.add(new PathDot(chain, 0xFFFFAA00, i * delayPerDot));
                        }
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (isConfirming) return false;

        //if the player starts dragging "unlock" selection
        if (Math.abs(offsetX) > 0.5 || Math.abs(offsetY) > 0.5) {
            this.selectedAbility = null;
        }

        if (click.button() == 0) {
            this.targetScrollX += offsetX / zoomScale;
            this.targetScrollY += offsetY / zoomScale;
            this.scrollX = targetScrollX;
            this.scrollY = targetScrollY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        double sx = (click.x() - (this.width / 2.0)) / zoomScale + (this.width / 2.0) - scrollX;
        double sy = (click.y() - (this.height / 2.0)) / zoomScale + (this.height / 2.0) - scrollY;

        Click scaledClick = new Click(sx, sy, click.buttonInfo());
        return super.mouseReleased(scaledClick);
    }

    private void openConfirmationDialog(AbilityData data) {
        if (this.client == null) return;

        this.client.setScreen(new net.minecraft.client.gui.screen.ConfirmScreen(
                (confirmed) -> {
                    if (confirmed) {
                        // if yes is pressed send packet
                        ClientPlayNetworking.send(new AbilityPurchasePayload(data.id()));
                    }
                    // return to stats screen
                    this.client.setScreen(this);
                },
                Text.literal("Unlock " + data.name() + "?").formatted(Formatting.YELLOW), // TITLE
                Text.literal("Are you sure you want to spend " + data.cost() + " Faith on this Ability?")
        ));
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

        if (parentOwned && childOwned) {
            return AbyssusPandorae.config.PathlineOn;
        }


        if (isBlocked) {
            return  AbyssusPandorae.config.Conflictlineoff;  // 0xFF8B0000 DARK RED
        }

        return AbyssusPandorae.config.PathlineOff; //  0xFF555555 Grey
    }

    private boolean isPrereqMet(AbilityData data, KingdomComponent component) {
        if (data.prerequisites().isEmpty()) return true;
        return data.prerequisites().stream().allMatch(component::hasAbility);
    }

    private void openCustomConfirmationDialog(AbilityData data) {
        this.isConfirming = true;
        this.pendingAbility = data;
        updateButtonVisiblility();
    }

    private void closeConfirmation() {
        this.isConfirming = false;
        this.pendingAbility = null;
        this.selectedAbility = null;
        updateButtonVisiblility();
    }
}


