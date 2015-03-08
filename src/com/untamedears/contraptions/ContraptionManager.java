package com.untamedears.contraptions;

import com.untamedears.contraptions.contraptions.Contraption;
import com.untamedears.contraptions.properties.ContraptionProperties;
import com.untamedears.contraptions.properties.FactoryProperties;
import com.untamedears.contraptions.utlity.Response;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ContraptionManager {

    Plugin plugin;
    Map<String, ContraptionProperties> contraptionProperties;
    Map<Location, Contraption> contraptions;

    public ContraptionManager(Plugin plugin) {
        this.plugin = plugin;
        //There has gotta be a better way to do this
    }

    public void init() {
        try {
            contraptionProperties = loadProperties(new File(plugin.getDataFolder(), "config.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the ContraptionProperties from the Config File
     * <p>
     * @param contraptionProperties
     */
    private Map<String, ContraptionProperties> loadProperties(File file) {
        Map<String, ContraptionProperties> newContraptionProperties = new HashMap<String, ContraptionProperties>();
        try {
            JSONTokener tokener = new JSONTokener(new FileReader(file));
            JSONObject jsonObject = new JSONObject(tokener);
            JSONObject factories = jsonObject.getJSONObject("Factory");
            for (String ID : factories.keySet()) {
                newContraptionProperties.put(ID, FactoryProperties.fromConfig(this, ID, factories.getJSONObject(ID)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newContraptionProperties;
    }

    /*
     * Gets a Contraption associated with the given location
     * If no Contraption exists null is returned
     */
    public Contraption getContraption(Location location) {
        return contraptions.get(location);

    }

    /**
     * Gets contraptions located within a square around the given location
     * <p>
     * @param location Central location
     * @param radius   Square radius from which to search
     * @return Set of contraptions in radius
     */
    public Set<Contraption> getContraptions(Location location, int radius) {
        Set<Contraption> contraptions = new HashSet<Contraption>();
        Contraption contraption;
        Location currentLocation = location.clone();

        for (int x = -radius; x <= radius; x++) {
            currentLocation.setX(x);
            for (int z = -radius; z <= radius; z++) {
                currentLocation.setZ(z);
                contraption = getContraption(currentLocation);
                if (contraption != null) {
                    contraptions.add(contraption);
                }
            }
        }
        return contraptions;
    }

    /*
     * Iterates through all ContraptionProperties and attempts to create a factory
     * based at the contraption
     */
    public Response createContraption(Location location) {
        Block block = location.getBlock();
        boolean matchedBlock = false;
        Response response;
        for (ContraptionProperties contraptionProperty : contraptionProperties.values()) {
            if (contraptionProperty.validBlock(block)) {
                matchedBlock = true;
                response = contraptionProperty.createContraption(location);
                if (response.success) {
                    return response;
                }
            }
        }
        if (matchedBlock) {
            return new Response(false, "Block is correct, but other conditions are not");
        } else {
            return new Response(false, "Incorrect block");
        }

    }
    
    public void registerContraption(Contraption cotraption) {
        contraptions.put(cotraption.getLocation(), cotraption);        
    }

    /*
     * Removes the contraption at a given location from being tracked by the manager
     * Returns true if the contraption was removed
     */
    public boolean destroy(Contraption contraption) {
        if (contraptions.containsKey(contraption.getLocation())) {
            contraptions.remove(contraption.getLocation());
            contraption.destroy();
            return true;
        } else {
            return false;
        }
    }

    /*
     * Handels instances of a block breaking
     * If a contraption existed at this block it is destroyed
     */
    public void handelBlockDestruction(Block block) {
        if (contraptions.containsKey(block.getLocation())) {
            destroy(contraptions.get(block.getLocation()));
        }
    }

    public void handelInteraction(PlayerInteractEvent e) {
        //if the player left clicked a block
        if (!e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }
        Player player = e.getPlayer();
        //If the player was holding a item matching the interaction material
        if (!player.getItemInHand().getType().equals(Material.STICK)) {
            return;
        }
        Location location = e.getClickedBlock().getLocation();
        
        Contraption contraption = getContraption(location);
        //If a contraption doesn't exist on location
        if(contraption==null) {
            createContraption(location).conveyTo(player);
        }
        //If contraption exists at location
        else {
            contraption.trigger().conveyTo(player);
        }
        

    }
}
