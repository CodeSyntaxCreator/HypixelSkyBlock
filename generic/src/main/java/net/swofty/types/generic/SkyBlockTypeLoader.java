package net.swofty.types.generic;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.types.generic.entity.mob.MobRegistry;
import net.swofty.types.generic.entity.mob.SkyBlockMob;
import net.swofty.types.generic.entity.npc.SkyBlockNPC;
import net.swofty.types.generic.entity.villager.SkyBlockVillagerNPC;
import net.swofty.types.generic.event.SkyBlockEvent;
import net.swofty.types.generic.tab.TablistManager;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

public interface SkyBlockTypeLoader {

    ServerType getType();

    void onInitialize(MinecraftServer server);

    void afterInitialize(MinecraftServer server);

    LoaderValues getLoaderValues();

    TablistManager getTablistManager();

    List<SkyBlockEvent> getTraditionalEvents();

    List<MobRegistry> getMobs();

    List<SkyBlockEvent> getCustomEvents();

    List<SkyBlockNPC> getNPCs();

    List<SkyBlockVillagerNPC> getVillagerNPCs();

    List<ServiceType> getRequiredServices();

    @Nullable CustomWorlds getMainInstance();

    record LoaderValues(Pos spawnPosition, boolean announceDeathMessages) { }
}
