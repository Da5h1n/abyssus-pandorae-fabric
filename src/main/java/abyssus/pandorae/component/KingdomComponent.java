package abyssus.pandorae.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KingdomComponent implements ComponentV3 {

    public Kingdom getKingdom() { return Kingdom.NONE; }
    public void setKingdom(Kingdom kingdom) { }

    public int getFaith() {return 0;}
    public void setFaith(int faith) { };

    public SoulState getSoulState() { return null; }
    public void setSoulState(SoulState state) { }

    List<String> getPurchasedAbilities() {return null;}

    public void purchaseAbility(String id) {}

    public boolean hasAbility(String id) {return false;}

    @Override
    public void readData(ReadView readView) {

    }

    @Override
    public void writeData(WriteView writeView) {

    }
}

class PlayerKingdomComponent extends KingdomComponent implements AutoSyncedComponent {
    private Kingdom kingdom = Kingdom.NONE;
    private int faith = 1; // 0 to 200 with 1 default
    private SoulState soulState = abyssus.pandorae.component.SoulState.PROTECTED; // Protected , Fractured , ReKindled , Incurable
    //List to store purchased IDs
    private final List<String> purchasedAbilities = new ArrayList<>();
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
    public SoulState getSoulState() {return soulState;}
    @Override
    public void setSoulState(SoulState state) {
        this.soulState = state;
        ModComponents.KINGDOM.sync(this.player);
    }

    @Override
    public List<String> getPurchasedAbilities() {
        return new ArrayList<>(purchasedAbilities); //return copy
    }

    @Override
    public void purchaseAbility(String id) {
        if (!purchasedAbilities.contains(id)) {
            purchasedAbilities.add(id);
            ModComponents.KINGDOM.sync(this.player);
        }
    }

    @Override
    public boolean hasAbility(String id) {
        return purchasedAbilities.contains(id);
    }

    @Override
    public void readData(ReadView view) {
        this.kingdom = Kingdom.valueOf(view.getString("kingdom_id", Kingdom.NONE.name()));
        this.faith = view.getInt("faith", 1);
        this.soulState = SoulState.valueOf(view.getString("soul_state", SoulState.PROTECTED.name()));

        purchasedAbilities.clear();
        String abilityString = view.getString("abilities_list", "");
        if (!abilityString.isEmpty()) {
            purchasedAbilities.addAll(Arrays.asList(abilityString.split(",")));
        }
    }

    @Override
    public void writeData(WriteView view) {
        view.putString("kingdom_id", this.kingdom.name());
        view.putInt("faith", this.faith);
        view.putString("soul_state", this.soulState.name());
        // save list as comma-seperated string
        view.putString("abilities_list", String.join(",", purchasedAbilities));
    }

}

