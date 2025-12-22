package abyssus.pandorae.gui;

import abyssus.pandorae.AbyssusPandorae;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChooseFaithScreen extends Screen {

    private static final Identifier WINDOW_BACKGROUND = AbyssusPandorae.identifier("choose_faith/background");
    private static final Identifier WINDOW_BORDER = Identifier.of(AbyssusPandorae.MOD_ID, "choose_faith/border");
    private static final Identifier WINDOW_NAME_PLATE = AbyssusPandorae.identifier("choose_faith/name_plate");


    private static final int WINDOW_WIDTH = 176;
    private static final int WINDOW_HEIGHT = 182;
    protected int guiLeft, guiTop;

    public ChooseFaithScreen() {
        super(Text.translatable(AbyssusPandorae.MOD_ID + ".screen.choose_faith"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        guiLeft = (this.width - WINDOW_WIDTH) / 2;
        guiTop = (this.height - WINDOW_HEIGHT) / 2;

        // SELECT BUTTON
        addDrawableChild(ButtonWidget.builder(
                Text.translatable(AbyssusPandorae.MOD_ID + ".gui.select"),
                button -> {

                    this.close();

                    // BELOW ADD BUTTON FUNCTION



//                    Identifier originId = super.getCurrentOrigin().getIdentifier();
//                    Identifier layerId = getCurrentLayer().getIdentifier();

//                    if (currentOriginIndex == originSelection.size()) {
//                        ClientPlayNetworking.send(new ChooseRandomOriginC2SPacket(layerId));
//                    } else {
//                        ClientPlayNetworking.send(new ChooseOriginC2SPacket(layerId, originId));
//                    }
//
//                    openNextLayerScreen();

                }
        ).dimensions(guiLeft + WINDOW_WIDTH / 2 - 50, guiTop + WINDOW_HEIGHT + 5, 100, 20).build());

        //PREVIOUS BUTTON
        addDrawableChild(ButtonWidget.builder(
                Text.of("<"),
                button -> {

                    SystemToast.add(
                            MinecraftClient.getInstance().getToastManager(),
                            SystemToast.Type.LOW_DISK_SPACE,
                            Text.literal("Button"),
                            Text.translatable(AbyssusPandorae.MOD_ID + ".gui.previous")
                    );

//                    currentOriginIndex = (currentOriginIndex + 1) % maxSelection;
//                    Origin newOrigin = getCurrentOrigin();
//
//                    showOrigin(newOrigin, getCurrentLayer(), newOrigin == randomOrigin);

                }
        ).dimensions(guiLeft - 40, height / 2 - 10, 20, 20).build());

        //	Draw the next origin button
        addDrawableChild(ButtonWidget.builder(
                Text.of(">"),
                button -> {

                    SystemToast.add(
                            MinecraftClient.getInstance().getToastManager(),
                            SystemToast.Type.LOW_DISK_SPACE,
                            Text.literal("Button"),
                            Text.translatable(AbyssusPandorae.MOD_ID + ".gui.next")
                    );

//                    currentOriginIndex = (currentOriginIndex + 1) % maxSelection;
//                    Origin newOrigin = getCurrentOrigin();
//
//                    showOrigin(newOrigin, getCurrentLayer(), newOrigin == randomOrigin);

                }
        ).dimensions(guiLeft + WINDOW_WIDTH + 20, height / 2 - 10, 20, 20).build());


    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        //context.fill(0,0, width, height, -5, 0xFF000000); // FILL BLACK

        //this.renderBackground(context, mouseX, mouseY, delta); // BROKEN DO NOT ENABLE


        //context.drawGuiTexture(WINDOW_BACKGROUND, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);
        //context.drawGuiTexture(WINDOW_BORDER, guiLeft, guiTop, 2, WINDOW_WIDTH, WINDOW_HEIGHT);
        //context.drawGuiTexture(WINDOW_NAME_PLATE,guiLeft + 10,guiTop + 10,2, 150, 26);

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,WINDOW_BACKGROUND, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,WINDOW_BORDER, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2,  guiTop - 15, 0xFFFFFF);


    }
}
