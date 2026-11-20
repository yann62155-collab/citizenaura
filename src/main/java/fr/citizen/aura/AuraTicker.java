package fr.citizen.aura;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Dessine les auras. Uniquement des particules "dust" vanilla envoyées par le serveur :
 * les joueurs PC les voient sans mod, et Geyser les traduit en particules colorées Bedrock.
 */
public final class AuraTicker {
    private static final int INTERVAL = 2; // toutes les 2 ticks (10 fois/s)
    private static final double VIEW_DIST = 48.0;

    private AuraTicker() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int tick = server.getTickCount();
        if (tick % INTERVAL != 0) return;
        AuraData data = AuraData.get(server);
        if (data.all().isEmpty() && !fr.citizen.aura.api.CitizenAuraApi.hasAny()) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            // l'aura donnee par un admin (/aura donner) passe toujours avant une aura automatique (autre mod)
            AuraData.Entry e = data.get(p.getUUID());
            if (e == null) e = fr.citizen.aura.api.CitizenAuraApi.automatic(p.getUUID());
            if (e == null || p.isSpectator() || p.isInvisible()) continue;
            // Sécurité : l'aura n'appartient qu'au joueur choisi (UUID + pseudo),
            // même si un proxy/mod donne le même UUID à plusieurs joueurs.
            if (!p.getGameProfile().getName().equalsIgnoreCase(e.name())) continue;
            List<ServerPlayer> viewers = new ArrayList<>();
            for (ServerPlayer v : p.serverLevel().players())
                if (v.distanceToSqr(p) < VIEW_DIST * VIEW_DIST) viewers.add(v);
            draw(viewers, p, e, tick);
        }
    }

    private static void draw(List<ServerPlayer> level, ServerPlayer p, AuraData.Entry e, int tick) {
        double x = p.getX(), y = p.getY(), z = p.getZ();
        float h = p.getBbHeight();
        float t = tick * 0.12f;
        var rnd = p.getRandom();

        switch (e.style()) {
            case ORBITE -> {
                // Lueur autour du corps
                for (int i = 0; i < 2; i++) {
                    double a = rnd.nextDouble() * Math.PI * 2, r = 0.35 + rnd.nextDouble() * 0.15;
                    send(level, color(e, tick, i), 1.1f, x + Math.cos(a) * r, y + rnd.nextDouble() * h, z + Math.sin(a) * r);
                }
                // 3 petites particules qui tournent à 3 hauteurs
                for (int i = 0; i < 3; i++) {
                    double a = (i % 2 == 0 ? t : -t) + i * (Math.PI * 2 / 3);
                    double hy = y + h * (0.25 + 0.3 * i);
                    send(level, color(e, tick, i + 3), 0.6f, x + Math.cos(a) * 0.75, hy, z + Math.sin(a) * 0.75);
                }
            }
            case SPIRALE -> {
                for (int k = 0; k < 2; k++) {
                    double prog = ((tick * 0.03) + k * 0.5) % 1.0;
                    double a = prog * Math.PI * 4 + k * Math.PI;
                    send(level, color(e, tick, k), 0.8f, x + Math.cos(a) * 0.6, y + prog * h, z + Math.sin(a) * 0.6);
                }
                double a = t * 1.5;
                send(level, color(e, tick, 5), 0.5f, x + Math.cos(a) * 0.85, y + h * 0.5, z + Math.sin(a) * 0.85);
            }
            case ECLAIR -> {
                // lueur legere autour du corps
                double ga = rnd.nextDouble() * Math.PI * 2;
                send(level, color(e, tick, 0), 0.9f, x + Math.cos(ga) * 0.4, y + rnd.nextDouble() * h, z + Math.sin(ga) * 0.4);
                // un eclair en zigzag de la tete aux pieds, une fois sur deux (effet de crepitement)
                if ((tick / INTERVAL) % 2 == 0) {
                    double a = rnd.nextDouble() * Math.PI * 2;
                    double px = x + Math.cos(a) * 0.55, pz = z + Math.sin(a) * 0.55, py = y + h + 0.35;
                    int steps = 6;
                    for (int s = 0; s < steps; s++) {
                        double na = a + (rnd.nextDouble() - 0.5) * 0.9;
                        double nr = 0.45 + rnd.nextDouble() * 0.35;
                        double nx = x + Math.cos(na) * nr, nz = z + Math.sin(na) * nr, ny = py - (h + 0.35) / steps;
                        for (int k = 0; k <= 2; k++) {
                            double f = k / 3.0;
                            send(level, color(e, tick, s), 0.55f, px + (nx - px) * f, py + (ny - py) * f, pz + (nz - pz) * f);
                        }
                        px = nx; py = ny; pz = nz; a = na;
                    }
                }
                // etincelle qui tourne autour de la tete
                double sa = t * 1.8;
                send(level, color(e, tick, 7), 0.5f, x + Math.cos(sa) * 0.5, y + h + 0.15, z + Math.sin(sa) * 0.5);
            }
            case HALO -> {
                for (int i = 0; i < 6; i++) {
                    double a = t * 0.5 + i * (Math.PI / 3);
                    send(level, color(e, tick, i), 0.7f, x + Math.cos(a) * 0.35, y + h + 0.3, z + Math.sin(a) * 0.35);
                }
                for (int i = 0; i < 2; i++) {
                    double a = t + i * Math.PI;
                    send(level, color(e, tick, i + 6), 0.6f, x + Math.cos(a) * 0.7, y + h * 0.5, z + Math.sin(a) * 0.7);
                }
            }
        }
    }

    private static int color(AuraData.Entry e, int tick, int offset) {
        if (e.color() != AuraColors.RAINBOW) return e.color();
        float hue = ((tick * 0.01f) + offset * 0.08f) % 1.0f;
        return Mth.hsvToRgb(hue, 1.0f, 1.0f);
    }

    private static void send(List<ServerPlayer> viewers, int rgb, float size, double x, double y, double z) {
        Vector3f c = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
        DustParticleOptions opt = new DustParticleOptions(c, Math.min(4.0f, size * 1.5f));
        // force = true : visible même si le joueur a mis les particules sur "Réduites"/"Minimales"
        for (ServerPlayer v : viewers)
            v.serverLevel().sendParticles(v, opt, true, x, y, z, 1, 0, 0, 0, 0);
    }
}
