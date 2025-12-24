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

    private static final Identifier PADLOCK_ICON = Identifier.of("minecraft", "textures/gui/sprites/spectator/teleport_to_team.png");

    private final List<AbilityData> abilityList = new ArrayList<>();
    private final Map<String, Vector2i> positions = new HashMap<>();

    private record Vector2i(int x, int y) { }

    public KingdomStatsScreen() {
        super(Text.literal("Kingdom Statistics"));
    }

    @Override
    protected void init() {
        if (this.client == null || this.client.player == null) return;

        var component = ModComponents.KINGDOM.get(this.client.player);
        this.abilityList.clear();
        this.abilityList.addAll(AbilityLoader.loadForKingdom(component.getKingdom()));

        calculateLayout();

        for (AbilityData data : this.abilityList) {
            Vector2i pos = positions.get(data.id());
            boolean owned = component.hasAbility(data.id());
            boolean prereqMet = isPrereqMet(data, component);

            ButtonWidget btn = ButtonWidget.builder(Text.literal(data.name()), button -> {
                ClientPlayNetworking.send(new AbilityPurchasePayload(data.id()));
            })
                    .dimensions(this.width / 2 + pos.x - 50, this.height / 2 + pos.y - 10, 100, 20)
                    .build();

            btn.active = !owned && prereqMet && (component.getFaith() >= data.cost());
            this.addDrawableChild(btn);
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // darken background
        this.renderInGameBackground(context);


        int headerWidth = 160;
        int headerHeight = 30;
        int headerX = this.width / 2 - headerWidth / 2;
        int headerY = 10;
        int borderColor = 0xFFAAAAAA;

        context.fill(headerX, headerY, headerX + headerWidth, headerY + headerHeight, 0xAA000000);
        context.fill(headerX, headerY, headerX + headerWidth, headerY + 1, borderColor);
        context.fill(headerX, headerY + headerHeight - 1, headerX + headerWidth, headerY + headerHeight, borderColor);
        context.fill(headerX, headerY, headerX + 1, headerY + headerHeight, borderColor);
        context.fill(headerX + headerWidth - 1, headerY, headerX + headerWidth, headerY + headerHeight, borderColor);

        var component = ModComponents.KINGDOM.get(this.client.player);
        Text faithText = Text.literal("Faith: ").append(Text.literal(String.valueOf(component.getFaith())).formatted(Formatting.GOLD));
        context.drawCenteredTextWithShadow(this.textRenderer, faithText, this.width / 2, 20, -1);

        // tree logic
        renderTreeLines(context);
        super.render(context, mouseX, mouseY, deltaTicks);
        renderPadlocks(context);
    }

    private void renderPadlocks(DrawContext context) {
        var component = ModComponents.KINGDOM.get(this.client.player);
        for (AbilityData data : abilityList) {
            if (!isPrereqMet(data, component) && !component.hasAbility(data.id())) {
                Vector2i pos = positions.get(data.id());
                if (pos != null) {
                    //Draw padlock on left side of button
                    int x = this.width / 2 + pos.x - 45;
                    int y = this.height / 2 + pos.y - 6;
                    //context.drawTexture(PADLOCK_ICON, x, y, 0, 0, 12, 12, 12, 12);
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, PADLOCK_ICON, x, y, 0,0,12,12,12,12);
                }
            }
        }
    }

    private void renderTreeLines(DrawContext context) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        var component = ModComponents.KINGDOM.get(this.client.player);

        for (AbilityData child : abilityList) {
            Vector2i childPos = positions.get(child.id());
            if (childPos == null) continue;
            for (String preId : child.prerequisites()) {
                Vector2i parentPos = positions.get(preId);
                if (parentPos != null) {
                    int startX = centerX + parentPos.x();
                    int startY = centerY + parentPos.y() + 10;
                    int endX = centerX + childPos.x();
                    int endY = centerY + childPos.y() - 10;

                    int colour = component.hasAbility(preId) ? 0xFFFFAA00 : 0xFF555555;
                    drawConnectingLine(context, startX, startY, endX, endY, colour);
                }
            }
        }
    }

    private boolean isPrereqMet(AbilityData data, KingdomComponent component) {
        if (data.prerequisites().isEmpty()) return true;
        return data.prerequisites().stream().allMatch(component::hasAbility);
    }

    private void drawConnectingLine(DrawContext context, int startX, int startY, int endX, int endY, int colour) {
        // Draw "L" shaped connector
        int midX = startX + (endX - startX) / 2;

        context.fill(startX, startY - 1, midX, startY + 1, colour); // horizontal out
        context.fill(midX - 1, startY, midX + 1, endY, colour); // Vertical drop
        context.fill(midX, endY - 1, endX, endY + 1, colour); // Horizontal in
    }
}


