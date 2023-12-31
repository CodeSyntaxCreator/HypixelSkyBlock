package net.swofty.event.actions.player.data;

import com.mongodb.client.model.Filters;
import lombok.SneakyThrows;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.swofty.SkyBlock;
import net.swofty.data.DataHandler;
import net.swofty.data.datapoints.DatapointBoolean;
import net.swofty.data.datapoints.DatapointString;
import net.swofty.data.mongodb.CoopDatabase;
import net.swofty.data.mongodb.UserDatabase;
import net.swofty.data.mongodb.ProfilesDatabase;
import net.swofty.event.EventNodes;
import net.swofty.event.EventParameters;
import net.swofty.event.SkyBlockEvent;
import net.swofty.user.SkyBlockPlayer;
import net.swofty.user.UserProfiles;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.UUID;

@EventParameters(description = "Load player data on join",
        node = EventNodes.PLAYER,
        validLocations = EventParameters.Location.EITHER,
        requireDataLoaded = false)
public class ActionPlayerDataLoad extends SkyBlockEvent {

    @Override
    public Class<? extends Event> getEvent() {
        return PlayerLoginEvent.class;
    }

    @SneakyThrows
    @Override
    public void run(Event event) {
        PlayerLoginEvent playerLoginEvent = (PlayerLoginEvent) event;

        // Ensure we use player here
        final Player player = playerLoginEvent.getPlayer();
        UUID playerUuid = player.getUuid();

        UserProfiles profiles = ((SkyBlockPlayer) playerLoginEvent.getPlayer()).getProfiles();
        UUID profileId = profiles.getCurrentlySelected();

        ProfilesDatabase profilesDatabase = new ProfilesDatabase(profileId.toString());
        DataHandler handler;

        if (profilesDatabase.exists()) {
            Document document = profilesDatabase.getDocument();
            handler = DataHandler.fromDocument(document);
            DataHandler.userCache.put(playerUuid, handler);
        } else {
            handler = DataHandler.initUserWithDefaultData(playerUuid);
            DataHandler.userCache.put(playerUuid, handler);
        }

        if (profiles.getProfiles().size() >= 2) {
            Document previousProfile = ProfilesDatabase.collection
                    .find(Filters.eq("_owner", playerUuid.toString())).into(new ArrayList<>())
                    .stream().filter(document -> !document.get("_id").equals(profileId.toString()))
                    .findFirst().get();

            DataHandler previousHandler = DataHandler.fromDocument(previousProfile);
            previousHandler.getPersistentValues().forEach((key, value) -> {
                handler.getDatapoint(key).setValue(value);
            });
        }

        if (handler.get(DataHandler.Data.IS_COOP, DatapointBoolean.class).getValue()) {
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(playerUuid);
            if (coop.members().size() == 1) {
                // Player is the only member of their coop, no need to load other members' data
                return;
            }

            DataHandler data;

            if (SkyBlock.getLoadedPlayers().stream().anyMatch(player1 -> coop.members().contains(player1.getUuid()))) {
                // A coop member is online, use their data
                SkyBlockPlayer otherCoopMember = SkyBlock.getLoadedPlayers().stream().filter(player1 -> coop.members().contains(player1.getUuid())).findFirst().get();
                data = otherCoopMember.getDataHandler();
            } else {
                // No coop members are online, use the first member's data
                data = DataHandler.fromDocument(new ProfilesDatabase(coop.memberProfiles().stream().filter(
                        uuid -> !uuid.equals(profileId)).findFirst().get().toString()).getDocument());
            }

            data.getCoopValues().forEach((key, value) -> {
                handler.getDatapoint(key).setValue(value);
            });
        }

        handler.runOnLoad();
    }
}
