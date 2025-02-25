package net.swofty.type.village.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.village.gui.GUIShopWeaponsmith;
import net.swofty.types.generic.entity.npc.NPCParameters;
import net.swofty.types.generic.entity.npc.SkyBlockNPC;

public class NPCWeaponsmith extends SkyBlockNPC {
    public NPCWeaponsmith() {
        super(new NPCParameters() {
            @Override
            public String[] holograms() {
                return new String[]{"§9Weaponsmith", "§e§lCLICK"};
            }

            @Override
            public String signature() {
                return "YNkSkn3ZTdExl/RAG/0i2ENlR5YOnCc80Yl6e1z/LpLztBiyuZtvr+7oBTaPaJKtxWdiP2sQFslC4lGT2kjCOrRRiPzKj9Ywldwh0qabjgz1m9GEdw3cCdvzHMQrwFAShpgM1pxfbpUH25NdHjpDnU423q17CsUQFT/poaCTcXgml0hGeOBq97uXh4jv9nc5D9WiKjFynh0tAFUwcUJfZjN5F5o0V0QizTv0B3ebugsGT3q/ql1A/KOpaSVPBbjYBz3PbDxbYWMViZoC0ntdeHMwxCBvKY9ITPqgolBC2D6uKSHRMkoow1GeqpVCXlecqz2bvTRhgTs6q6J62Nk/F8QLkbQ88HR0tcqSwV4+6EGbVyO+AFEHNu6q9PSoOOB9UBNFoLNZ0KtkUZMHxLiu6F5m8t2TUigpf5SO+QBWSAABFHsrlm4a48J2weAyc5t2nHZl8CA9FiEbuaHzt7VnBrUnidoFPF+bftf637CV4vSC40lVp9TKhnEbm7Iu+GhE0mnWspVKqZcn1MCh1A91f8TXweMmEi6m6ArEpxM4CK/dznYzCczp3yCbEhjQpW4OveDcfTDGxkj4D1YWn9jcb3cvNvMVOgypeeuABBO19ojJJ66ZLb4OQWzlISIWPHhrTN3opsZIG4aXFWC9UNQm2Vgqo6LGH1bPxgW2K5U9+b4=";
            }

            @Override
            public String texture() {
                return "eyJ0aW1lc3RhbXAiOjE1ODcwMzE3NTE2OTEsInByb2ZpbGVJZCI6ImRkZWQ1NmUxZWY4YjQwZmU4YWQxNjI5MjBmN2FlY2RhIiwicHJvZmlsZU5hbWUiOiJEaXNjb3JkQXBwIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zMDFiN2VmZGNmNjFmM2IwNTMyOTU3YmU0M2VmOTdkMTQ4ZTIyM2Y2ZDRhODcxMDcyMTM4M2U1ZGEwNzQ1ZDgzIn19fQ==";
            }

            @Override
            public Pos position() {
                return new Pos(-10, 68, -130, 90, 0);
            }

            @Override
            public boolean looking() {
                return true;
            }
        });
    }

    @Override
    public void onClick(PlayerClickNPCEvent e) {
        new GUIShopWeaponsmith().open(e.player());
    }
}
