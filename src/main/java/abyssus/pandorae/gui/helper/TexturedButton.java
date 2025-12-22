package abyssus.pandorae.gui.helper;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;


public class TexturedButton extends ButtonWidget {
    private final Identifier texture;

    public TexturedButton(int x, int y, int width, int height, Identifier texture, PressAction onPress) {
        super(x, y, width, height, net.minecraft.text.Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.texture = texture;
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {

        float alpha = this.isSelected() ? 1.0f : 0.7f;

        //context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        context.drawGuiTexture(
                RenderPipelines.GUI_TEXTURED,
                this.texture,
                this.getX(),
                this.getY(),
                this.width,
                this.height
        );
        //context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
