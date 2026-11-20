package fr.citizen.aura.api;

import fr.citizen.aura.AuraColors;
import fr.citizen.aura.AuraData;
import fr.citizen.aura.AuraStyle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API pour les autres mods Citizen (ex. Citizen Factions : aura selon le grade).
 * Les auras AUTOMATIQUES ne sont pas sauvegardees : le mod appelant les reapplique regulierement.
 * Une aura donnee par un admin avec /aura donner reste prioritaire.
 * Appelable par reflexion (aucune dependance de compilation necessaire) ; a utiliser depuis le thread serveur.
 */
public final class CitizenAuraApi {
    /** joueur -> (source -> aura) */
    private static final Map<UUID, Map<String, AuraData.Entry>> AUTO = new ConcurrentHashMap<>();

    private CitizenAuraApi() {}

    /**
     * @param rgb   couleur 0xRRGGBB
     * @param style orbite, spirale, halo ou eclair
     * @return false si le style est inconnu
     */
    public static boolean setAutomatic(UUID player, String playerName, String source, int rgb, String style) {
        AuraStyle st = AuraStyle.parse(style);
        if (st == null) return false;
        AUTO.computeIfAbsent(player, k -> new LinkedHashMap<>()).put(source, new AuraData.Entry(playerName, rgb & 0xFFFFFF, st));
        return true;
    }

    public static void clearAutomatic(UUID player, String source) {
        Map<String, AuraData.Entry> m = AUTO.get(player);
        if (m == null) return;
        m.remove(source);
        if (m.isEmpty()) AUTO.remove(player);
    }

    public static void clearSource(String source) {
        for (UUID id : new ArrayList<>(AUTO.keySet())) clearAutomatic(id, source);
    }

    public static AuraData.Entry automatic(UUID player) {
        Map<String, AuraData.Entry> m = AUTO.get(player);
        if (m == null || m.isEmpty()) return null;
        return m.values().iterator().next();
    }

    public static boolean hasAny() { return !AUTO.isEmpty(); }

    public static List<String> describeAutomatic() {
        List<String> out = new ArrayList<>();
        AUTO.forEach((id, m) -> m.forEach((src, e) -> out.add(e.name() + " : " + AuraColors.describe(e.color())
                + " (" + e.style().id() + ", automatique : " + src + ")")));
        return out;
    }
}
