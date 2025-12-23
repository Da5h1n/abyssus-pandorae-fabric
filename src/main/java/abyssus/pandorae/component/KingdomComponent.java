package abyssus.pandorae.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class KingdomComponent implements ComponentV3 {

    public Kingdom getKingdom() { return Kingdom.NONE; }
    public void setKingdom(Kingdom kingdom) { }

    public int getFaith() {return 0;}
    public void setFaith(int faith) { };

    public SoulState getSoulState() { return null; }
    public void setSoulState(SoulState state) { }

    @Override
    public void readData(ReadView readView) {

    }

    @Override
    public void writeData(WriteView writeView) {

    }
}

class PlayerKingdomComponent extends KingdomComponent implements AutoSyncedComponent {
    private Kingdom kingdom = Kingdom.NONE;
    private int faith = 1; // 0 to 250 with 1 default
    private SoulState SoulState = abyssus.pandorae.component.SoulState.PROTECTED; // Protected , Fractured , ReKindled , Incurable

    private final PlayerEntity player;

    public PlayerKingdomComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public Kingdom getKingdom() {return kingdom;}
    @Override
    public void setKingdom(Kingdom kingdom) {
        this.kingdom = kingdom;
        // This tells CCA to send the new data to the client immediately
        ModComponents.KINGDOM.sync(this.player);
    }

    @Override
    public int getFaith() {return faith;}
    @Override
    public void setFaith(int faith) {
        //clamp value between 0, 250
        this.faith = MathHelper.clamp(faith, 0, 200);
        ModComponents.KINGDOM.sync(this.player);
    }

    @Override
    public SoulState getSoulState() {return SoulState;}
    @Override
    public void setSoulState(SoulState state) {
        this.SoulState = state;
        ModComponents.KINGDOM.sync(this.player);
    }

    @Override
    public void readData(ReadView view) {
        // "kingdom_id" is the key in the save file. "none" is the default if not found.
        this.kingdom = Kingdom.valueOf(view.getString("kingdom_id", Kingdom.NONE.name()));
        this.faith = view.getInt("faith", 1);
        this.SoulState = SoulState.valueOf(view.getString("soul_state", abyssus.pandorae.component.SoulState.PROTECTED.name()));
    }

    @Override
    public void writeData(WriteView view) {
        // Saves the string to the player's data
        view.putString("kingdom_id", this.kingdom.name());
        view.putInt("faith", this.faith);
        // Save the Enum name ("PROTECTED")
        view.putString("soul_state", this.SoulState.name());
    }
}

