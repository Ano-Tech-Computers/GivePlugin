package com.floyd.bukkit.give;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

public class Gift {
	
	private Material m;
	private Byte data = 0;
	private Short durability = 0;
	private String description = "";
	private Plugin plugin = null;

	public Gift(Plugin p, String desc) throws InvalidGiftException {
		// Check server objet
		plugin = p;
		if (plugin == null || !(plugin instanceof Plugin)) {
			throw new InvalidGiftException("Need a valid Plugin object");
		}
		
		// Check for description
		description = desc;
		if (description.equals("")) {
			throw new InvalidGiftException("Empty description");
		}
		String[] args = description.split(":");
		
		// Extract item type
		if (args.length >= 1) {
			
			// Try to match material name
			m = findMaterial(args[0]);
			if (m == null) {
				throw new InvalidGiftException("Material '"+args[0]+"' is invalid");
			}
			
			
			
		}

		// Extract item data
		if (args.length >= 2) {
			if (!args[1].equals("")) {
				
				Boolean valid = false;
				
				// Try to match a color name
				try {
					DyeColor dc = DyeColor.valueOf(args[1].toUpperCase());
					data = dc.getData();
					valid = true;
				}
				catch (Exception e) {
					valid = false;
				}
				
				// Check for certain known strings
				if (args[1].startsWith("st")) { // stone
					data = 0; 
					valid = true;
				}
				if (args[1].startsWith("sa")) { // sandstone
					data = 1; 
					valid = true;
				}
				if (args[1].startsWith("wo")) { // wood
					data = 2; 
					valid = true;
				}
				if (args[1].startsWith("co")) { // cobblestone
					data = 3; 
					valid = true;
				}
				if (args[1].startsWith("br")) { // brick
					data = 4; 
					valid = true;
				}
				if (args[1].startsWith("sm")) { // smoothstone
					data = 5; 
					valid = true;
				}
				
				// Try to convert to byte
				if (valid == false) {
					try {
						data = Byte.parseByte(args[1]);
						valid = true;
					}
					catch (Exception e) {
						valid = false;
					}
				}
				
				// This is no good...
				if (valid == false) { 
					throw new InvalidGiftException("Material data '"+args[1]+"' is invalid");
				}

			}
		}
		
		// Extract item durability
		if (args.length >= 3) {
			if (!args[2].equals("")) {
				// Try to parse as number
				try {
					durability = Short.parseShort(args[2]);
				}
				catch (Exception e) {
					throw new InvalidGiftException("Material durability '"+args[2]+"' is invalid");
				}
			}
		}

		// More than three parts? 
		if (args.length >= 4) {
			throw new InvalidGiftException("Expected material:data:durability, got "+description);
		}
		
		// Debug
		plugin.getLogger().info("Gift: Material="+m.name()+" Data="+data+" Durability="+durability);
	}
	
	public Material getMaterial() {
		return m;
	}
	
	public Short getDurability() {
		return durability;
	}
	
	public MaterialData getMaterialData() {
		return new MaterialData(m, data);
	}
	
	public Byte getData() {
		return data;
	}
	
	public String getName() {
		return m.name().toLowerCase()+(data==0 ? "" : ":"+data);
	}
	
	// Improve upon Material.matchMaterial() by trying different variations
	private Material findMaterial(String query) {
		query = query.toLowerCase();
		for (Material m: Material.values()) {
			String name = m.name().toLowerCase();
			// Straight match
			if (query.equals(name)) { return m; }
			// Try the material ID as a string
			if (query.matches(Integer.toString(m.getId()))) { return m; }
			// Try without underscores
			if (name.contains("_")) {
				name = name.replaceAll("_", "");
				if (query.equals(name)) { return m; }
			}
			// Try partial match
			if (name.startsWith(query)) { return m; }
		}
		return null;
	}

}
