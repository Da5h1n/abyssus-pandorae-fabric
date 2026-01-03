package abyssus.pandorae.gui.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class Configscreen extends Screen {

    private final Screen parent;

    public Configscreen(Screen parent) {
        super(Text.translatable("title.abyssus.stats"));
        this.parent = parent;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
