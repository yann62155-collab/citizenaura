package fr.citizen.aura;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sauvegarde des auras dans le monde (data/citizenaura.dat) : survit aux redémarrages. */
public class AuraData extends SavedData {
    public record Entry(String name, int color, AuraStyle style) {}

    private static final SavedData.Factory<AuraData> FACTORY =
            new SavedData.Factory<>(AuraData::new, AuraData::load, null);

    private final Map<UUID, Entry> auras = new HashMap<>();

    public static AuraData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, CitizenAura.MOD_ID);
    }

    public Map<UUID, Entry> all() { return auras; }

    public Entry get(UUID id) { return auras.get(id); }

    public void set(UUID id, Entry e) { auras.put(id, e); setDirty(); }

    public boolean remove(UUID id) {
        boolean had = auras.remove(id) != null;
        if (had) setDirty();
        return had;
    }

    private static AuraData load(CompoundTag tag, HolderLookup.Provider registries) {
        AuraData data = new AuraData();
        ListTag list = tag.getList("auras", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            AuraStyle style = AuraStyle.parse(c.getString("style"));
            data.auras.put(c.getUUID("uuid"),
                    new Entry(c.getString("name"), c.getInt("color"), style == null ? AuraStyle.ORBITE : style));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        auras.forEach((id, e) -> {
            CompoundTag c = new CompoundTag();
            c.putUUID("uuid", id);
            c.putString("name", e.name());
            c.putInt("color", e.color());
            c.putString("style", e.style().id());
            list.add(c);
        });
        tag.put("auras", list);
        return tag;
    }
}
