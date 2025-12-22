package abyssus.pandorae.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class KingdomComponent implements ComponentV3 {
    public String getKingdom() {
        return null;
    }

    public void setKingdom(String kingdom) {

    }

    @Override
    public void readData(ReadView readView) {

    }

    @Override
    public void writeData(WriteView writeView) {

    }
}

class PlayerKingdomComponent extends KingdomComponent implements AutoSyncedComponent {
    private String kingdomId = "none";
    private final PlayerEntity player;

    public PlayerKingdomComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public String getKingdom() {
        return this.kingdomId;
    }

    public void setKingdom(String kingdom) {
        this.kingdomId = kingdom;
        // This tells CCA to send the new data to the client immediately
        ModComponents.KINGDOM.sync(this.player);
    }

    @Override
    public void readData(ReadView view) {
        // "kingdom_id" is the key in the save file. "none" is the default if not found.
        this.kingdomId = view.getString("kingdom_id", "none");
    }

    @Override
    public void writeData(WriteView view) {
        // Saves the string to the player's data
        view.putString("kingdom_id", this.kingdomId);
    }
}