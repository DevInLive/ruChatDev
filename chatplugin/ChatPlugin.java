/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Smile
 */
public class ChatPlugin extends JavaPlugin {
//==============================================================================    
    public static final Logger log = Logger.getLogger("Minecraft");
//==============================================================================    
    @Override
    public void onEnable(){
        File fconfig = new File(getDataFolder(), "config.yml");
        if(!fconfig.exists()){
        InputStream resourceAsStream = ChatPlugin.class.getResourceAsStream("/chatplugin/config.yml");
        getDataFolder().mkdirs();
        try {
        FileOutputStream fos = new FileOutputStream(fconfig);
        byte[] buff = new byte[65536];
        int n;
        while((n = resourceAsStream.read(buff)) > 0){
            fos.write(buff, 0, n);
            fos.flush();
        }
        fos.close();
        buff = null;
        } catch (Exception e) {
        e.printStackTrace();
    }
}
        FileConfiguration config = YamlConfiguration.loadConfiguration(fconfig);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(config), this);
    }
//==============================================================================    
    @Override
    public void onDisable(){
        log.info("[ruChat] Disable!");
    }
//==============================================================================               
}
