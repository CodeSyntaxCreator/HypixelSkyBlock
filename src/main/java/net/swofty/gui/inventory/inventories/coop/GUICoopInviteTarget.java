package net.swofty.gui.inventory.inventories.coop;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.SkyBlock;
import net.swofty.data.DataHandler;
import net.swofty.data.datapoints.DatapointBoolean;
import net.swofty.data.datapoints.DatapointUUID;
import net.swofty.data.mongodb.CoopDatabase;
import net.swofty.data.mongodb.ProfilesDatabase;
import net.swofty.data.mongodb.UserDatabase;
import net.swofty.gui.inventory.ItemStackCreator;
import net.swofty.gui.inventory.SkyBlockInventoryGUI;
import net.swofty.gui.inventory.item.GUIClickableItem;
import net.swofty.gui.inventory.item.GUIItem;
import net.swofty.user.SkyBlockPlayer;
import net.swofty.user.UserProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUICoopInviteTarget extends SkyBlockInventoryGUI {
    private static final Map<Integer, List<Integer>> SLOTS_MAP = new HashMap<>(
            Map.of(
                    1, List.of(13),
                    2, List.of(12, 14),
                    3, List.of(11, 13, 15),
                    4, List.of(10, 12, 14, 16),
                    5, List.of(11, 12, 13, 14, 15)
            )
    );

    public GUICoopInviteTarget(CoopDatabase.Coop coop) {
        super("Co-op Invitation", InventoryType.CHEST_5_ROW);

        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));

        int amountInProfile = coop.memberInvites().size() + coop.members().size();
        int[] slots = SLOTS_MAP.get(amountInProfile).stream().mapToInt(Integer::intValue).toArray();

        // Put everyone who is a member as TRUE and ones only invited as FALSE
        Map<UUID, Boolean> invites = new HashMap<>();
        coop.members().forEach(uuid -> invites.put(uuid, true));
        coop.memberInvites().forEach(uuid -> invites.put(uuid, false));

        // Remove originator
        invites.remove(coop.originator());

        set(new GUIItem() {
            @Override
            public int getSlot() {
                return slots[0];
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return ItemStackCreator.getStackHead(
                        SkyBlockPlayer.getDisplayName(coop.originator()), PlayerSkin.fromUuid(String.valueOf(coop.originator())), 1,
                        " ",
                        "§bCreated the invite!"
                );
            }
        });

        for (int i = 0; i < invites.size(); i++) {
            UUID target = (UUID) invites.keySet().toArray()[i];
            boolean accepted = invites.get(target);
            String displayName = SkyBlockPlayer.getDisplayName(target);

            int finalI = i;
            set(new GUIItem() {
                @Override
                public int getSlot() {
                    return slots[finalI + 1];
                }

                @Override
                public ItemStack.Builder getItem(SkyBlockPlayer player) {
                    return ItemStackCreator.getStackHead(
                            displayName, PlayerSkin.fromUuid(String.valueOf(target)), 1,
                            " ",
                            "§7Accepted: " + (accepted ? "§aYes" : "§cNot yet")
                    );
                }
            });
        }

        set(new GUIClickableItem() {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                coop.removeInvite(player.getUuid());
                coop.save();
                player.sendMessage("§b[Co-op] §eYou have denied the co-op invite!");
                player.closeInventory();

                SkyBlockPlayer target = SkyBlock.getLoadedPlayers().stream().filter(player1 -> player1.getUuid().equals(coop.originator())).findFirst().orElse(null);
                if (target != null &&
                        (coop.memberInvites().contains(target.getUuid()) || coop.members().contains(target.getUuid())))
                    target.sendMessage("§b[Co-op] §e" + player.getUsername() + " §chas denied your co-op invite!");
            }

            @Override
            public int getSlot() {
                return 33;
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return ItemStackCreator.getStack("§cDeny Invite", Material.BARRIER, (short) 0, 1);
            }
        });


        set(new GUIClickableItem() {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                coop.memberInvites().remove(player.getUuid());
                coop.members().add(player.getUuid());
                coop.save();

                UUID profileId = UUID.randomUUID();
                DataHandler handler = DataHandler.initUserWithDefaultData(player.getUuid());

                handler.get(DataHandler.Data.IS_COOP, DatapointBoolean.class).setValue(true);
                handler.get(DataHandler.Data.ISLAND_UUID, DatapointUUID.class).setValue(UUID.randomUUID());

                player.kick("§cYou must reconnect to switch profiles");

                ProfilesDatabase.collection.insertOne(handler.toDocument(profileId));
                coop.memberProfiles().add(profileId);
                coop.save();

                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    UserProfiles profiles = player.getProfiles();
                    profiles.getProfiles().add(profileId);
                    profiles.setCurrentlySelected(profileId);
                    new UserDatabase(player.getUuid()).saveProfiles(profiles);
                }, TaskSchedule.tick(5), TaskSchedule.stop());

                SkyBlockPlayer target = SkyBlock.getLoadedPlayers().stream().filter(player1 -> player1.getUuid().equals(coop.originator())).findFirst().orElse(null);
                if (target != null &&
                        (coop.memberInvites().contains(target.getUuid()) || coop.members().contains(target.getUuid())))
                    target.sendMessage("§b[Co-op] §a" + player.getUsername() + " §ahas accepted your co-op invite!");
            }

            @Override
            public int getSlot() {
                return 29;
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return ItemStackCreator.getStack("§aAccept Invite", Material.GREEN_TERRACOTTA, (short) 0, 1,
                        "§7Creates a NEW §bco-op §7profile on your",
                        "§7account with the above player.",
                        " ",
                        "§7Shared with co-op:",
                        "§8- §bPrivate Island",
                        "§8- §bCollections progress",
                        "§8- §bBanks & Auctions",
                        " ",
                        "§7Per-player:",
                        "§8- §aInventory",
                        "§8- §aPurse",
                        "§8- §aStorage",
                        "§8- §aQuests and Objectives",
                        "§8- §aPets",
                        "§7 ",
                        "§7Uses a profile slot!",
                        "§7Profiles: §e" + player.getProfiles().getProfiles().size() + "§6/§e4",
                        " ",
                        "§eClick to accept!",
                        " ",
                        "§4§lWARNING: §cCreation of profiles",
                        "§cwhich boost other profiles will",
                        "§cbe considered abusive and",
                        "§cpunished.");
            }
        });
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, SkyBlockPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
