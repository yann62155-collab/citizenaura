package fr.citizen.aura;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * Dessine les auras. Uniquement des particules "dust" vanilla envoyées par le serveur :
 * les joueurs PC les voient sans mod, et Geyser les traduit en particules colorées Bedrock.
 */
public final class AuraTicker {
    private static final int INTERVAL = 2; // toutes les 2 ticks (10 fois/s)

    private AuraTicker() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int tick = server.getTickCount();
        if (tick % INTERVAL != 0) return;
        AuraData data = AuraData.get(server);
        if (data.all().isEmpty()) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            AuraData.Entry e = data.get(p.getUUID());
            if (e == null || p.isSpectator() || p.isInvisible()) continue;
            draw(p.serverLevel(), p, e, tick);
        }
    }

    private static void draw(ServerLevel level, ServerPlayer p, AuraData.Entry e, int tick) {
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

    private static void send(ServerLevel level, int rgb, float size, double x, double y, double z) {
        Vector3f c = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
        // Envoyé à tous les joueurs proches (32 blocs), y compris le joueur lui-même
        level.sendParticles(new DustParticleOptions(c, size), x, y, z, 1, 0, 0, 0, 0);
    }
}
