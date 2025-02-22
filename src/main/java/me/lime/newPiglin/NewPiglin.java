package me.lime.newPiglin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public class NewPiglin extends JavaPlugin implements Listener {

    public static NewPiglin instance;
    private final Logger log = this.getLogger();
    protected FileConfiguration config = getConfig();
    private static List<String> results;
    private static Random randomGenerator = new Random();

    @Override
    public void onEnable() {
        instance = this;
    
        log.info(ChatColor.GREEN + "Creating / getting config & loading data from config...");
    
        // Сохраняем дефолтную конфигурацию, если она не существует
        saveDefaultConfig();
        
        // Перезагружаем конфигурацию из файла
        reloadConfig();
        
        // Получаем конфигурацию заново после перезагрузки
        config = getConfig();
    
        results = new ArrayList<>();
    
        getResults();
    
        log.info(ChatColor.GREEN + "Config loaded!");
        log.info(ChatColor.GREEN + "Number of results loaded: " + results.size());
    
        if (results.isEmpty()) {
            log.warning("The results list is empty. Check your configuration.");
            // Выводим содержимое конфигурации для отладки
            log.info("Config contents: " + config.saveToString());
        }
    
        log.info(ChatColor.GREEN + "Plugin is enabled!");
    
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    
    void getResults() {
        ConfigurationSection res = config.getConfigurationSection("results");
        
        if (res == null) {
            log.warning("No 'results' section found in the config. Check your configuration.");
            return;
        }
    
        log.info("Loading results from config...");
        for (String itemName : res.getKeys(false)) {
            int chance = res.getInt(itemName + ".chance");
            log.info("Item: " + itemName + ", Chance: " + chance);
            for (int i = 0; i < chance; i++) {
                results.add(itemName);
            }
        }
    
        log.info("Finished loading results. Total items: " + results.size());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        String randomItem = results.get(randomGenerator.nextInt(results.size()));
        final ItemStack newItem = new ItemStack(Material.valueOf(randomItem));
        final List<ItemStack> outcome = event.getOutcome();
        outcome.clear();
        outcome.add(newItem);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("np_reload") && sender.hasPermission("np.admin")) {
            // Перезагружаем конфигурацию
            reloadConfig();
            // Получаем новый экземпляр конфигурации
            config = getConfig();
            // Очищаем старый список результатов
            results.clear();
            // Создаем новый объект Random
            randomGenerator = new Random();
            // Заново загружаем результаты
            getResults();

            sender.sendMessage(ChatColor.GREEN + "Config reloaded successfully!");
            
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        log.info(ChatColor.GREEN + "Saving config...");
        saveConfig();
        log.info(ChatColor.GREEN + "Plugin is disabled!");
    }

}

