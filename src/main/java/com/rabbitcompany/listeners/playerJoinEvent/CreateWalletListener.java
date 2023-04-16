package com.rabbitcompany.listeners.playerJoinEvent;

import com.rabbitcompany.CryptoCurrency;
import com.rabbitcompany.utils.MySql;
import com.rabbitcompany.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class CreateWalletListener implements Listener {

    private final CryptoCurrency cryptoCurrency;

    public CreateWalletListener(CryptoCurrency plugin){
        cryptoCurrency = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // Save player to database
        try {
            if (!MySql.checkConnection()) {
                // Attempt to reconnect to the database
                MySql.connect();
            }

            if (MySql.checkConnection()) {
                for (String crypto : Settings.cryptos.keySet()) {
                    if (!MySql.isPlayerInDatabase(event.getPlayer().getName(), crypto)) {
                        MySql.createPlayerWallet(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName(), crypto);
                    }
                }
            } else {
                // Log an error if we can't connect to the database
                cryptoCurrency.getLogger().severe("Could not connect to database to save player wallet for " + event.getPlayer().getName());
            }
        } catch (SQLException e) {
            cryptoCurrency.getLogger().severe("An error occurred while trying to save player wallet for " + event.getPlayer().getName() + ": " + e.getMessage());
        }
    }
}
