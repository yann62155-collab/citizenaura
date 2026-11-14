package fr.citizen.aura;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AuraColors {
    /** Valeur spéciale : couleurs qui défilent. */
    public static final int RAINBOW = -1;

    public static final Map<String, Integer> NAMED = new LinkedHashMap<>();
    static {
        NAMED.put("rouge", 0xE02020);
        NAMED.put("bleu", 0x2060FF);
        NAMED.put("vert", 0x20D040);
        NAMED.put("jaune", 0xFFE020);
        NAMED.put("or", 0xD4A017);
        NAMED.put("orange", 0xFF8020);
        NAMED.put("violet", 0x9030E0);
        NAMED.put("rose", 0xFF60C0);
        NAMED.put("cyan", 0x20E0E0);
        NAMED.put("blanc", 0xFFFFFF);
        NAMED.put("noir", 0x151515);
        NAMED.put("argent", 0xC0C0C8);
        NAMED.put("arcenciel", RAINBOW);
    }

    private AuraColors() {}

    /** Nom français, ou code hexa (ff8800 / #ff8800). Renvoie null si invalide. */
    public static Integer parse(String input) {
        String s = input.toLowerCase(Locale.ROOT).trim();
        if (NAMED.containsKey(s)) return NAMED.get(s);
        if (s.startsWith("#")) s = s.substring(1);
        if (s.matches("[0-9a-f]{6}")) return Integer.parseInt(s, 16);
        return null;
    }

    public static String describe(int color) {
        for (Map.Entry<String, Integer> e : NAMED.entrySet())
            if (e.getValue() == color) return e.getKey();
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
