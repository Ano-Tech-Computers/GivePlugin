package com.floyd.bukkit.give;

import java.io.*;

import org.bukkit.Server;
import org.bukkit.entity.Player;
//import org.bukkit.Server;
//import org.bukkit.event.Event.Priority;
//import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
//import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
//import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
//import org.bukkit.material.*;
import java.util.logging.Logger;
import java.util.*;
import org.bukkit.command.*;

//import com.nijikokun.bukkit.Permissions.Permissions;

/**
* GivePlugin plugin for Bukkit
*
* @author FloydATC
*/
public class GivePlugin extends JavaPlugin implements Listener {
    //public static Permissions Permissions = null;
    private final HashMap<String, String> items = new HashMap<String, String>();

	public static final Logger logger = Logger.getLogger("Minecraft.GivePlugin");
    
//    public GivePlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
//        super(pluginLoader, instance, desc, folder, plugin, cLoader);
//        // TODO: Place any custom initialization code here
//
//        // NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
//    }

    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled
    	
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
    	PluginDescriptionFile pdfFile = this.getDescription();
		logger.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
    }

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

    	//setupPermissions();
    	loadItems();
    	
        // Register our events
        PluginManager pm = getServer().getPluginManager();
  //      pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvents((Listener) this, this);

    	
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
		logger.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }

    private void loadItems() {
    	String fname = "plugins/GivePlugin/items.txt";
    	try {
        	BufferedReader input =  new BufferedReader(new FileReader(fname));
    		String line = null;
    		while (( line = input.readLine()) != null) {
    			line = line.trim();
    			if (!line.matches("^#.*") && !line.matches("")) {
    				String[] parts = line.split("=", 2);
    				items.put(parts[0], parts[1]);
    			}
    		}
    		input.close();
    	}
    	catch (FileNotFoundException e) {
    		logger.warning( "Error reading " + e.getLocalizedMessage() + ", item names will not be usable" );
    		logger.warning( "Expected file format is:" );
    		logger.warning( "name=id" );
    		logger.warning( "name=id:data" );
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args ) {
    	String cmdname = cmd.getName().toLowerCase();
        Player player = null;
        String pname = "(Console)";
        if (sender instanceof Player) {
        	player = (Player)sender;
        	pname = player.getName();
        }

        if (cmdname.equalsIgnoreCase("give")) {
        	Gift gift = null;
        	Integer amount = 1;
        	Player recepient = player;
        	if (args.length == 0 || args.length > 3) {
        		respond(player, "§7[§6Give§7]§b Syntax:");
        		respond(player, "§7[§6Give§7]§b /give <item>[:<data>[:<durability>]] [<count> [<player>]]");
        		return true;
        	}
        	if (player != null) {
        		if (! player.hasPermission("giveplugin.give")) {
        			getLogger().warning(pname+": Permission denied");
					respond(player, "§7[§6Give§7]§c Permission denied");
            		return true;
        		}
        	}
        	
        	// Get item type (may include data and/or durability)
        	if (args.length >= 1) {
        		try {
					gift = new Gift(this, args[0]);
				} catch (InvalidGiftException e) {
        			getLogger().warning(pname+": "+e.getMessage());
					respond(player, "§7[§6Give§7]§c "+e.getMessage());
					return true;
				}
        		
        	}
        	
        	// Get amount, if any
        	if (args.length >= 2) {
        		try {
        			amount = Integer.parseInt(args[1]);
        		}
        		catch (Exception e) {
        			getLogger().warning(pname+": Invalid amount '"+args[1]+"'");
        			respond(player, "§7[§6Give§7]§c Invalid amount '"+args[1]+"'");
        			return true;
        		}
        	}
        	
        	// Get recepient, if any
        	if (args.length == 3) {
        		recepient = getServer().getPlayer(args[2]);
        		if (recepient == null) {
        			getLogger().warning(pname+": Player is not online: '"+args[2]+"'");
        			respond(player, "§7[§6Give§7]§c Player is not online: '"+args[2]+"'");
        			return true;
        		}
        	}
        	
        	// Validate recepient
        	if (recepient == null) {
        		respond(player, "§7[§6Give§7]§c Must specify a player when used from console");
        		return true;
        	}
        	
        	if (give(gift, amount, recepient)) {
    			getLogger().info(pname+" gave "+amount+" "+gift.getName()+" to "+recepient.getName());
        		respond(player, "§7[§6Give§7]§b "+amount+" "+gift.getName()+" given to "+(recepient==player ? "you" : recepient.getName()));
            	return true;
        	}
        }
        
    	return false;
    }

	public boolean give(Gift gift, Integer amount, Player recepient) {
		
		ItemStack stack = new ItemStack(gift.getMaterial(), amount, gift.getDurability(), gift.getData());
//		stack.setAmount(amount);
//		stack.setData(gift.getMaterialData());
//		getLogger().info("MaterialData="+gift.getMaterialData());
//		stack.setDurability(gift.getDurability());
		
		PlayerInventory inventory = recepient.getInventory();
		inventory.addItem(stack);
		recepient.sendMessage("§7[§6Give§7]§b Enjoy your gift!");
		
		return true;
	}

	private Integer getItemID(String input) {
		String[] parts = input.split(":");
		return Integer.parseInt(parts[0]);
	}
	
	public String lookupItem(String name) {
		// Use items.txt for backwards compatibility, then Bukkit built-in names
		String id = items.get(name);
		if (id == null) {
			Integer type = getItemID(name);
			id = type.toString(); 
		}
		return id;
	}
	
    private void respond(Player player, String message) {
    	if (player == null) {
    		Server server = getServer();
        	ConsoleCommandSender console = server.getConsoleSender();
        	console.sendMessage(message);
    	} else {
    		player.sendMessage(message);
    	}
    }
}

