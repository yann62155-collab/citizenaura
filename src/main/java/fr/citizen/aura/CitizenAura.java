package fr.citizen.aura;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Citizen Aura : un admin donne une aura de couleur à un joueur.
 * Tout se passe côté serveur (particules vanilla) : aucun mod à installer
 * chez les joueurs, compatible PC et console (Geyser / Bedrock).
 */
@Mod(CitizenAura.MOD_ID)
public class CitizenAura {
    public static final String MOD_ID = "citizenaura";

    public CitizenAura(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(AuraCommand::register);
        NeoForge.EVENT_BUS.addListener(AuraTicker::onServerTick);
    }
}
