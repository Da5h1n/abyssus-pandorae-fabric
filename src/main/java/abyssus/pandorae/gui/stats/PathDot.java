package abyssus.pandorae.gui.stats;

import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class PathDot {
    private float progress = 0;
    private final List<AbilityData> chain;
    private int currentSegment = 0;
    private final int colour;
    private float delayTicks;

    private static final float PIXELS_PER_SECOND = 60.0f;

    public PathDot(List<AbilityData> chain, int colour, float delay) {
        this.chain = chain;
        this.colour = colour;
        this.delayTicks = delay;
    }

    private KingdomStatsScreen.Vector2i getPos(int nodeIdx, int screenWidth, int screenHeight, int hSpacing, int vSpacing, boolean isStartOfSegment) {
        AbilityData node = chain.get(nodeIdx);

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int localX = centerX + (node.gridX() * hSpacing);
        int localY = centerY - (node.gridY() * vSpacing);

        int offset = isStartOfSegment ? -10 : 10;

        return new KingdomStatsScreen.Vector2i(localX, localY + offset);
    }

    public void update(float delta, int screenWidth, int screenHeight, int hSpacing, int vSpacing) {
        if (delayTicks > 0) {
            delayTicks -= delta;
            return;
        }

        int totalSegments = chain.size() - 1;
        if (currentSegment >= totalSegments) {
            reset();
            return;
        }

        KingdomStatsScreen.Vector2i start = getPos(currentSegment, screenWidth, screenHeight, hSpacing, vSpacing, true);
        KingdomStatsScreen.Vector2i end = getPos(currentSegment + 1, screenWidth, screenHeight, hSpacing, vSpacing, false);

        float dx = end.x() - start.x();
        float dy = end.y() - start.y();
        float segDist = (float) Math.sqrt(dx * dx + dy * dy);
        float moveStep = (PIXELS_PER_SECOND / 20.0f) * delta;

        if (segDist > 0.1f) {
            progress += (moveStep / segDist);
        } else {
            progress = 1.0f;
        }

        if (progress >= 1.0f) {
            progress -= 1.0f;
            currentSegment++;
            if (currentSegment >= chain.size() - 1) reset();
        }
    }

    public void render(DrawContext context, int screenWidth, int screenHeight, int hs, int vs) {
        if (delayTicks > 0 || currentSegment >= chain.size() - 1) return;

        KingdomStatsScreen.Vector2i start = getPos(currentSegment, screenWidth, screenHeight, hs, vs, true);
        KingdomStatsScreen.Vector2i end = getPos(currentSegment + 1, screenWidth, screenHeight, hs, vs, false);

        float x = (start.x() + (end.x() - start.x()) * progress);
        float y = (start.y() + (end.y() - start.y()) * progress);

        if (isInsideButton(x, y, screenWidth, screenHeight, hs, vs)) return;

        int alpha = calculateAlpha();
        int fadeColour = (alpha << 24) | (colour & 0x00FFFFFF);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);

        context.fill(-2, -2, 2, 2, fadeColour);

        context.getMatrices().popMatrix();
    }

    private boolean isInsideButton(float x, float y, int screenWidth, int screenHeight, int hs, int vs) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        for (AbilityData node : chain) {
            int bx = centerX + (node.gridX() * hs);
            int by = centerY - (node.gridY() * vs);
            if (x >= bx - 50 && x <= bx + 50 && y >= by - 10 && y <= by + 10) {
                return true;
            }
        }
        return false;
    }

    public int calculateAlpha() {
        int minAlpha = 70;
        int maxAlpha = chain.size() <= 2 ? 180 : 255;
        int finalAlpha;

        if (chain.size() <= 2){
            finalAlpha = (int) (minAlpha + (maxAlpha - minAlpha) * Math.sin(progress * Math.PI));
        } else {
            if (currentSegment == 0) {
                finalAlpha = (int) (minAlpha + (maxAlpha - minAlpha) * progress);
            } else if (currentSegment == chain.size() - 2) {
                finalAlpha = (int) (maxAlpha - (maxAlpha - minAlpha) * progress);
            } else {
                finalAlpha = maxAlpha;
            }
        }
        return finalAlpha;
    }

    public void reset() {
        this.progress = 0;
        this.currentSegment = 0;
    }
}
