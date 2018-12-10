package pw.amel.dungeonmod;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import vg.civcraft.mc.citadel.events.ReinforcementChangeTypeEvent;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;

public class CitadelDecayListener implements Listener {
    private DecayListener decayListener;

    CitadelDecayListener(Dungeon dungeon, int avgTime, int variance, int blockOffset) {
        decayListener = new DecayListener(dungeon, avgTime, variance, blockOffset);
        DungeonMod.getPlugin().getServer().getPluginManager().registerEvents(decayListener, DungeonMod.getPlugin());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCitadelReinforce(ReinforcementCreationEvent event) {
        decayListener.handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCitadelBreak(ReinforcementDamageEvent event) {
        decayListener.handleBlockBreak(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCitadelChange(ReinforcementChangeTypeEvent event) {
        decayListener.handleBlockBreak(event.getReinforcement().getLocation().getBlock(), false);
    }
}
