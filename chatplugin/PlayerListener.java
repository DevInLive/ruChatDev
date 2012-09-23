/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chatplugin;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author Smile
 */
public class PlayerListener implements Listener{
     protected String Display_Name_Format = "%prefix%player%suffix";
     public String Message_Format = "%prefix %player: %message";
     public String Global_Message_Format = "%prefix %player: %message";
     public String Join_Message = "%prefix %player &f зашел на сервер!";
     public String Quit_Message = "%prefix %player &f покинул сервер!";
     public String Join_Message_To_Player = "Добро пожаловать на сервер %prefix %player!";
     protected String optionMessageFormat = "ChatMessageFormat";
     protected String optionDisplayname = "DisplayNameFormat";
     public final static Boolean RANGED_MODE = false;
     public boolean Death_Message = false;
     public String Global_Message_Start_With = "!";
     protected String optionRangedMode = "RangedMode";
     protected String optionGlobalMessageFormat = "GlobalMessageFormat";
     protected String optionChatRange = "ChatRange";
     public double Chat_Range = 100d;     
//==============================================================================     
     public String messageFormat = Message_Format;
     public String globalMessageFormat = Global_Message_Format;
     public String joinMessage = Join_Message;
     public String quitMessage = Quit_Message;
     public String jmtp = Join_Message_To_Player;
     public String gmsw = Global_Message_Start_With;
     public String displayNameFormat = Display_Name_Format;
     public boolean deathMessage = Death_Message;
     protected boolean rangedMode = RANGED_MODE;
     protected double chatRange = Chat_Range;
//==============================================================================
     public PlayerListener (FileConfiguration config){
       this.displayNameFormat = config.getString("DisplayNameFormat", this.displayNameFormat);
       this.messageFormat = config.getString("ChatMessageFormat",this.messageFormat);
       this.joinMessage = config.getString("JoinMessageFormat",this.joinMessage);
       this.quitMessage = config.getString("QuitMessageFormat", this.quitMessage);
       this.jmtp = config.getString("JoinMessageToPlayer", this.jmtp);
       this.deathMessage = config.getBoolean("DeathMessage",this.deathMessage);
       this.chatRange = config.getDouble("ChatRange", this.chatRange);
       this.globalMessageFormat = config.getString("GlobalMessageFormat", this.globalMessageFormat);
       this.gmsw = config.getString("GlobalMessageStartWith", this.gmsw);
       this.rangedMode = config.getBoolean("RangedMode", this.rangedMode);
    }
//==============================================================================     
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event){
         if (event.isCancelled()) {
	 return;
}
                Player player = event.getPlayer();

		String worldName = player.getWorld().getName();

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		if (user == null) {
			return;
		}
                String message = user.getOption(this.optionMessageFormat, worldName, messageFormat);
                boolean localChat = user.getOptionBoolean(this.optionRangedMode, worldName, rangedMode);
                String chatMessage = event.getMessage();
		if (chatMessage.startsWith("!") && user.has("ruChat.globalmessage", worldName)) {
			localChat = false;
			chatMessage = chatMessage.substring(1);

			message = user.getOption(this.optionGlobalMessageFormat, worldName, globalMessageFormat);
		}
                message = this.translateColorCodes(message);
		chatMessage = this.translateColorCodes(chatMessage, user, worldName);
		message = message.replace("%message", "%2$s").replace("%displayname", "%1$s");
		message = this.replacePlayerPlaceholders(player, message);
		event.setFormat(message);
		event.setMessage(chatMessage);
                if (localChat) {
			double range = user.getOptionDouble(this.optionChatRange, worldName, chatRange);
			event.getRecipients().clear();
			event.getRecipients().addAll(this.getLocalRecipients(player, message, range));
		}
    }
//==============================================================================     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event){
         Player player = event.getPlayer();
         PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		if (user == null) {
			return;
		}
         joinMessage = this.translateColorCodes(joinMessage);
         joinMessage = joinMessage.replace("%prefix",user.getPrefix()).replace("%player", user.getName());
         jmtp = jmtp.replace("%prefix",user.getPrefix()).replace("%player", user.getName());
         this.translateColorCodes(jmtp);
         event.setJoinMessage(joinMessage);
         event.getPlayer().sendMessage(jmtp);
     }
//==============================================================================     
     @EventHandler
     public void onPlayerQiut(PlayerQuitEvent event){
          Player player = event.getPlayer();
         PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		if (user == null) {
			return;
		}
         quitMessage = this.translateColorCodes(quitMessage);     
         quitMessage = quitMessage.replace("%prefix",user.getPrefix()).replace("%player", user.getName());
         event.setQuitMessage(quitMessage);
         Bukkit.getServer().broadcastMessage(quitMessage);
     }
//==============================================================================
      @EventHandler(priority = EventPriority.NORMAL)
      public void onEntityDeath(EntityDeathEvent event) {
	 if (deathMessage){
         if (event instanceof PlayerDeathEvent) {
      PlayerDeathEvent deathEvent = (PlayerDeathEvent) event;
      deathEvent.setDeathMessage(null);
         }
      }
}
//==============================================================================      
        protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");
//==============================================================================        
     	protected String translateColorCodes(String string) {
		if (string == null) {
			return "";
		}

		String newstring = string;
		newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
		return newstring;
	}
//==============================================================================
        protected String translateColorCodes(String string, PermissionUser user, String worldName) {
		if (string == null) {
			return "";
		}

		String newstring = string;
		if (user.has("ruChat.chatcolor", worldName)) {
			newstring = chatColorPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has("ruChat.chatmagic", worldName)) {
			newstring = chatMagicPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has("ruChat.chatbold", worldName)) {
			newstring = chatBoldPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has("ruChat.chatstrike", worldName)) {
			newstring = chatStrikethroughPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has("ruChat.chatunderline", worldName)) {
			newstring = chatUnderlinePattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		if (user.has("ruChat.chatitalic", worldName)) {
			newstring = chatItalicPattern.matcher(newstring).replaceAll("\u00A7$1");
		}
		newstring = chatResetPattern.matcher(newstring).replaceAll("\u00A7$1");
		return newstring;
	}
//============================================================================== 
        protected List<Player> getLocalRecipients(Player sender, String message, double range) {
		Location playerLocation = sender.getLocation();
		List<Player> recipients = new LinkedList<Player>();
		double squaredDistance = Math.pow(range, 2);
		for (Player recipient : Bukkit.getServer().getOnlinePlayers()) {
			if (!recipient.getWorld().equals(sender.getWorld())) {
				continue;
			}
			if (playerLocation.distanceSquared(recipient.getLocation()) > squaredDistance) {
				continue;
			}
			recipients.add(recipient);
		}
		return recipients;
	}
//==============================================================================
        protected String replacePlayerPlaceholders(Player player, String format) {
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		String worldName = player.getWorld().getName();
		return format.replace("%prefix", this.translateColorCodes(user.getPrefix(worldName))).replace("%suffix", this.translateColorCodes(user.getSuffix(worldName))).replace("%player", player.getDisplayName());
	}
//==============================================================================
        	protected void updateDisplayName(Player player) {
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
		if (user == null) {
			return;
		}

		String worldName = player.getWorld().getName();
		player.setDisplayName(this.translateColorCodes(this.replacePlayerPlaceholders(player, user.getOption(this.optionDisplayname, worldName, this.displayNameFormat))));
	}
//==============================================================================        
}
