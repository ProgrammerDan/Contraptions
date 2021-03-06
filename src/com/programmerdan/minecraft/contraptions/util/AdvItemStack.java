package com.programmerdan.minecraft.contraptions.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Flyweight wrapper for Bukkit ItemStack allowing for lightweight resource and item
 * representation. Allows for virtual resources to be realized and for real resources
 * to be virtualized.
 *  
 * @author ProgrammerDan
 * @since 1.0.0 September 2015
 */
public class AdvItemStack implements Cloneable, ConfigurationSerializable{
	private double size;
	private ItemStack type;
	private AdvancedMeta meta;
	
	public AdvItemStack(ItemStack type, double size, AdvancedMeta meta) {
		this.type = type;
		this.size = size;
		this.meta = meta;
	}
	
	public AdvItemStack(ItemStack type, double size) {
		this(type, size, null);
	}
	
	public AdvItemStack(ItemStack type) {
		this(type, 1.0, null);
	}
	
	public double getSize() {
		return size;
	}
	
	@Override
	public AdvItemStack clone() {
		return new AdvItemStack(this.type, this.size, (AdvancedMeta) this.meta.clone());
	}
	
	/**
	 * Turns this advanced Item Stack into a list of Item Stacks.
	 * 
	 * @return a List of ItemStacks of the correct type, with any metadata applied.
	 */
	public List<ItemStack> realize() {
		List<ItemStack> result = new LinkedList<ItemStack>();
		
		int ism = type.getMaxStackSize();
		ItemStack is = type.clone();
		if (getMeta() != null) {
			is = getMeta().apply(is);
		}
		double csize = size;
		while (csize > 0) {
			if (csize < ism) {
				is.setAmount((int) Math.floor(csize));
				result.add(is);
			} else {
				is.setAmount(ism);
				result.add(is);
			}
			csize -= ism;
			is = is.clone();
		}
		
		return result;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
	public ItemStack getType() {
		return this.type;
	}
	
	public void setType(ItemStack type) {
		this.type = type;
	}
	
	public AdvancedMeta getMeta() {
		return this.meta;
	}
	
	public void setMeta(AdvancedMeta meta) {
		this.meta = meta;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		
		if (getType() != null) {
			result.put("type", getType());
		}
		
		if (getMeta() != null) {
			result.put("meta", getMeta());
		}
		
		result.put("size", getSize());
		
		return result;
	}
	
	public static AdvItemStack deserialize(Map<String, Object> serial) {
		ItemStack is = null;
		if (serial.containsKey("type")) {
			is = (ItemStack) serial.get("type");
		}
		AdvancedMeta am = null;
		if (serial.containsKey("meta")) {
			am = (AdvancedMeta) serial.get("meta");
		}
		double sz = 1.0d;
		if (serial.containsKey("size")) {
			sz = ( (Number) serial.get("size") ).doubleValue();
		}
		
		AdvItemStack result = new AdvItemStack(is, sz, am);
		
		return result;
	}
}
