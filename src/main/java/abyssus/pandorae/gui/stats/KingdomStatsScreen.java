package abyssus.pandorae.gui.stats;

import abyssus.pandorae.component.KingdomComponent;
import abyssus.pandorae.component.ModComponents;
import abyssus.pandorae.networking.AbilityPurchasePayload;
import abyssus.pandorae.util.AbilityLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class KingdomStatsScreen extends Screen {

    private static final Identifier PADLOCK_ICON = Identifier.of("minecraft", "textures/gui/sprites/widget/locked_button_disabled.png");

    private final List<AbilityData> abilityList = new ArrayList<>();
    private final Map<String, AbilityData> abilityCashe = new HashMap<>();
    private final Map<String, Vector2i> positions = new HashMap<>();
    private int lastFaith = -1;
    private int lastUnlockedCount = -1;

    private record Vector2i(int x, int y) { }

    public KingdomStatsScreen() {
        super(Text.literal("Kingdom Statistics"));
    }

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

        calculateLayout();

        for (AbilityData data : this.abilityList) {
            Vector2i pos = positions.get(data.id());
            if (pos == null) continue;

            boolean owned = component.hasAbility(data.id());
            boolean prereqMet = isPrereqMet(data, component);
            boolean hasEnoughFaith = component.getFaith() >= data.cost();
            boolean hasConflict = data.conflicts() != null && data.conflicts().stream().anyMatch(component::hasAbility);

            ButtonWidget btn = ButtonWidget.builder(Text.literal(data.name()), button -> {
                ClientPlayNetworking.send(new AbilityPurchasePayload(data.id()));
            })
                    .dimensions(this.width / 2 + pos.x - 50, this.height / 2 + pos.y - 10, 100, 20)
                    .build();

            btn.active = !owned && !hasConflict && prereqMet && hasEnoughFaith;
            this.addDrawableChild(btn);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // darken background
        this.renderInGameBackground(context);

        if (this.client == null || this.client.player == null) return;
        var component = ModComponents.KINGDOM.get(this.client.player);

        //Header Logic
        renderHeader(context, component);

        // Tree Visuals
        renderTreeLines(context, component);
        renderConflictLines(context, component);

        super.render(context, mouseX, mouseY, deltaTicks);
        renderPadlocks(context, component);
    }

    private void renderHeader(DrawContext context, KingdomComponent component) {
        int headerWidth = 160; int headerHeight = 30;
        int headerX = this.width / 2 - headerWidth / 2; int headerY = 10;
        int borderColor = 0xFFAAAAAA;
        context.fill(headerX, headerY, headerX + headerWidth, headerY + headerHeight, 0xAA000000);
        context.fill(headerX, headerY, headerX + headerWidth, headerY + 1, borderColor);
        context.fill(headerX, headerY + headerHeight - 1, headerX + headerWidth, headerY + headerHeight, borderColor);
        context.fill(headerX, headerY, headerX + 1, headerY + headerHeight, borderColor);
        context.fill(headerX + headerWidth - 1, headerY, headerX + headerWidth, headerY + headerHeight, borderColor);

        Text faithText = Text.literal("Faith: ").append(Text.literal(String.valueOf(component.getFaith())).formatted(Formatting.GOLD));
        context.drawCenteredTextWithShadow(this.textRenderer, faithText, this.width / 2, 20, -1);
    }

    private void renderTreeLines(DrawContext context, KingdomComponent component) {
        int centerX = this.width / 2; int centerY = this.height / 2;

        for (AbilityData child : abilityList) {
            Vector2i childPos = positions.get(child.id());
            if (childPos == null) continue;

            for (String preId : child.prerequisites()) {
                Vector2i parentPos = positions.get(preId);
                if (parentPos == null) continue;

                AbilityData parentData = abilityList.stream()
                        .filter(a -> a.id().equals(preId))
                        .findFirst().orElse(null);

                if (parentData == null) continue;

                int startX = centerX + parentPos.x();
                int startY = centerY + parentPos.y() - 10;
                int endX = centerX + childPos.x();
                int endY = centerY + childPos.y() + 10;

                //determine line colour
                int colour = 0xFF555555; // DEFAULT GREY

                // Check if path is "Blocked"
                boolean isBlocked = child.conflicts() != null && child.conflicts().stream().anyMatch(component::hasAbility);

                // Check if path is "Active"
                boolean parentActive = component.hasAbility(preId) && component.getFaith() >= parentData.cost();

                boolean childOwned = component.hasAbility(child.id());

                if (isBlocked) {
                    colour = 0xFF8B0000; // Dark Red (Conflict Locked)
                } else if (parentActive && childOwned) {
                    colour = 0xFFFFAA00; // GOLD (active path)
                }

                drawConnectingLine(context, startX, startY, endX, endY, colour);
            }
        }
    }

    private void renderConflictLines(DrawContext context, KingdomComponent component) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        for (AbilityData data : abilityList) {
            if (data.conflicts() == null) continue;

            Vector2i posA = positions.get(data.id());
            for (String conflitId : data.conflicts()) {
                Vector2i posB = positions.get(conflitId);
                // only draw line once
                if (posA != null && posB != null && data.id().compareTo(conflitId) < 0) {
                    int x1 = centerX + posA.x();
                    int y1 = centerY + posA.y();
                    int x2 = centerX + posB.x();
                    int y2 = centerY + posB.y();

                    // if either side of the conflict is owned make the "X" line bright red
                    boolean conflictActive = component.hasAbility(data.id()) || component.hasAbility(conflitId);
                    int lineColour = conflictActive ? 0xFFFF0000 : 0xFF770000; // bright red if choice made, Dim Red if available


                    drawDashedLine(context, x1, y1, x2, y2, lineColour);

                    // draw a small X in the line
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;
                    context.drawText(this.textRenderer, "X", midX - 3, midY - 4, lineColour, false);
                }
            }
        }
    }

    private void renderPadlocks(DrawContext context, KingdomComponent component) {
        for (AbilityData data : abilityList) {
            Vector2i pos = positions.get(data.id());
            if (pos == null) continue;

            boolean owned = component.hasAbility(data.id());
            boolean prereqMet = isPrereqMet(data, component);
            boolean hasEnoughFaith = component.getFaith() >= data.cost();

            //Draw padlock
            // centerX + pos.x is the center of the button
            // + 50 is the right edge
            if (!prereqMet || !hasEnoughFaith) {
                int x = this.width / 2 + pos.x + 35;
                int y = this.height / 2 + pos.y - 6;

                context.drawTexture(RenderPipelines.GUI_TEXTURED, PADLOCK_ICON, x, y, 0,0,12,12,12,12);
            }
        }
    }

    private void drawConnectingLine(DrawContext context, int startX, int startY, int endX, int endY, int colour) {
        // draw diagonal connector

        int dx = endX - startX;
        int dy = endY - startY;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

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

        for (int i = 0; i < distance; i += 4) { // "4" is the length of dashes
            float t = i / distance;
            float nextT = Math.min(1.0f, (i + 2) / distance);
            context.fill((int)(x1 + dx * t), (int)(y1 + dy * t),
                    (int)(x1 + dx * nextT) + 1, (int)(y1 + dy * nextT) + 1, colour);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.client == null || this.client.player == null) return;

        var component = ModComponents.KINGDOM.get(this.client.player);

        int currentFaith = component.getFaith();
        int currentUnlocks = component.getPurchasedAbilities().size();

        if (currentFaith != lastFaith || currentUnlocks != lastUnlockedCount) {
            lastFaith = currentFaith;
            lastUnlockedCount = currentUnlocks;
            this.clearAndInit();
        }
    }

    private void calculateLayout() {
        positions.clear();
        Map<Integer, List<AbilityData>> levels = new HashMap<>();

        // group abilities by depth level
        for (AbilityData data : abilityList) {
            int depth = getDepth(data);
            levels.computeIfAbsent(depth, k -> new ArrayList<>()).add(data);
        }

        int verticalSpacing = 60;
        int horizontalSpacing = 120;

        levels.keySet().stream().sorted().forEach(level -> {
            List<AbilityData> abilities = levels.get(level);
            int totalWidth = (abilities.size() - 1) * horizontalSpacing;
            int startX = -totalWidth / 2;

            for (int i = 0; i < abilities.size(); i++) {
                AbilityData data = abilities.get(i);

                // BOTTOM TO TOP
                int yPos = (level * -verticalSpacing) + 40;
                int gridX = startX + (i * horizontalSpacing);
                int xPos;

                // average parent x positions for a clean look IF More than one parent
                if (data.prerequisites().size() > 1) {
                    int sumX = 0;
                    int count = 0;
                    for (String preId : data.prerequisites()) {
                        if (positions.containsKey(preId)) {
                            sumX += positions.get(preId).x();
                            count++;
                        }
                    }
                    xPos = count > 0 ? sumX / count : gridX;
                } else {
                    xPos = gridX;
                }

                positions.put(data.id(), new Vector2i(xPos, yPos));
            }
        });
    }

    private int getDepth(AbilityData data) {
        if (data.prerequisites().isEmpty()) return 0;
        // Find parent depth and add 1
        return data.prerequisites().stream()
                .map(preId -> abilityList.stream().filter(a-> a.id().equals(preId)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .mapToInt(this::getDepth)
                .max()
                .orElse(0) + 1;
    }

    private boolean isPrereqMet(AbilityData data, KingdomComponent component) {
        if (data.prerequisites().isEmpty()) return true;
        return data.prerequisites().stream().allMatch(component::hasAbility);
    }
}


