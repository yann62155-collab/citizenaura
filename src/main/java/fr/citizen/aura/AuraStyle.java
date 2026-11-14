package fr.citizen.aura;

import java.util.Locale;

public enum AuraStyle {
    /** Lueur autour du corps + petites particules qui tournent autour. */
    ORBITE,
    /** Deux spirales qui montent autour du joueur. */
    SPIRALE,
    /** Anneau au-dessus de la tête + petites particules qui tournent. */
    HALO;

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public static AuraStyle parse(String s) {
        for (AuraStyle st : values()) if (st.id().equalsIgnoreCase(s)) return st;
        return null;
    }
}
