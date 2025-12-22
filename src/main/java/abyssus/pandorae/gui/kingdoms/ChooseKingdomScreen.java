package abyssus.pandorae.gui.kingdoms;

import abyssus.pandorae.AbyssusPandorae;
import abyssus.pandorae.gui.helper.TexturedButton;
import abyssus.pandorae.networking.KingdomSelectPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChooseKingdomScreen extends Screen {

    private static final Identifier LEFT_KINGDOM = AbyssusPandorae.identifier("kingdoms/border");
    private static final Identifier CENTER_KINGDOM = AbyssusPandorae.identifier("kingdoms/border");
    private static final Identifier RIGHT_KINGDOM = Identifier.of(AbyssusPandorae.MOD_ID, "kingdoms/border");



    public ChooseKingdomScreen() {
        super(Text.translatable(AbyssusPandorae.MOD_ID + ".screen.choose_kingdom"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        int sectionWidth = this.width / 3;

        int verticalSpacing = 60;
        int bottomPadding = 20;
        int buttonHeight = this.height - verticalSpacing - bottomPadding;

        int internalPadding = 10;
        int buttonWidth = sectionWidth - (internalPadding * 2);


        //Left Section
        addDrawableChild(new TexturedButton(internalPadding, verticalSpacing, buttonWidth, buttonHeight, LEFT_KINGDOM, button -> {
            System.out.println("Left Kingdom Clicked!");
            ClientPlayNetworking.send(new KingdomSelectPayload("left_kingdom"));
            this.close();
        }));

        //CENTER SECTION
        addDrawableChild(new TexturedButton(sectionWidth + internalPadding, verticalSpacing, buttonWidth, buttonHeight, CENTER_KINGDOM, button -> {
            System.out.println("Center Kingdom Clicked!");
            ClientPlayNetworking.send(new KingdomSelectPayload("center_kingdom"));
            this.close();
        }));

        addDrawableChild(new TexturedButton((sectionWidth * 2) + internalPadding, verticalSpacing, buttonWidth, buttonHeight, RIGHT_KINGDOM, button -> {
            System.out.println("Right Kingdom Clicked!");
            ClientPlayNetworking.send(new KingdomSelectPayload("right_kingdom"));
            this.close();
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int sectionWidth = this.width / 3;
        int verticalSpacing = 60;

        int[] colours = {0xFF808080, 0xFF005500, 0xFF000055};

        for (int i = 0; i < 3; i++) {
            int xStart = i * sectionWidth;
            int xEnd;

            if (i == 2) {

                xEnd = this.width;
            } else {
                xEnd = xStart + sectionWidth;
            }

            context.fill(xStart, 0, xEnd, this.height, colours[i]);
        }

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("abyssus-pandorae.screen.choose_kingdom.title"), this.width / 2, 15, -1);

        int texty = verticalSpacing - 15;

        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("abyssus-pandorae.screen.choose_kingdom.left_kingdom"), sectionWidth / 2, texty, -1);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("abyssus-pandorae.screen.choose_kingdom.center_kingdom"), sectionWidth + (sectionWidth / 2), texty, -1);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("abyssus-pandorae.screen.choose_kingdom.right_kingdom"), (sectionWidth * 2) + (sectionWidth / 2), texty, -1);
    }
}
