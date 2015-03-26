package vg.civcraft.mc.contraptions.gadgets;

import vg.civcraft.mc.contraptions.ContraptionsPlugin;
import vg.civcraft.mc.contraptions.contraptions.Contraption;
import vg.civcraft.mc.contraptions.utility.Resource;
import vg.civcraft.mc.contraptions.utility.InventoryHelpers;
import java.util.Set;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONObject;

/**
 * A gadget which will converts between ItemStacks and resources
 *
 * This can be used for things such as powering Contraptions via the items they
 * contain within them
 *
 * It can be imported from a JSON object in the following format
 * <pre>
 * {
 *   "inputs":
 *     [{
 *         "material": "MATERIAL_NAME",
 *         "amount": 1,
 *         "durability": 0,
 *         "name": "DISPLAY_NAME",
 *         "lore": "LORE"
 *       },...
 *       }],
 *   "conversion": 1
 * }
 * </pre>
 */
public class ConversionGadget {

    //Converted ItemStacks
    Set<ItemStack> itemStacks;
    //The Exchange rate between ItemStacks and Resources
    double conversion;

    /**
     * Creates a ConversionGadget
     *
     * @param itemStacks The ItemStacks consumed
     * @param conversion The amount of resource generated by the ItemStacks
     */
    public ConversionGadget(Set<ItemStack> itemStacks, double conversion) {
        this.itemStacks = itemStacks;
        this.conversion = conversion;
    }

    /**
     * Given an inventory checks if there are enough ItemStacks to generate
     * amount of resource
     *
     * @param amount    The amount of resource to generate
     * @param inventory The inventory to pull ItemStacks from
     * @return Check if there are enough ItemStacks to generate amount
     */
    public boolean canConvertToResource(double amount, Inventory inventory) {
        double amountAvailible = InventoryHelpers.amountAvailable(inventory, itemStacks);
        return amountAvailible * conversion >= amount;
    }

    /**
     * Consumes ItemSets to generate a resource
     *
     * @param amount    Amount of resource to produce
     * @param inventory The inventory from which to draw ItemStacks
     * @param resource  The resource to generate
     * @return If there were enough ItemStacks to generate amount
     */
    public boolean convertToResource(double amount, Inventory inventory, Resource resource) {
        int numberOfSets = (int) Math.ceil(amount / conversion);
        if (InventoryHelpers.removeMultiple(inventory, itemStacks, numberOfSets)) {
            resource.change(numberOfSets * conversion);
            return true;
        }
        return false;
    }

    /**
     * Consumes a resource to generate ItemStacks
     *
     * @param resource  Resource to consume
     * @param inventory Inventory to place ItemStacks in
     * @param amount    Amount of resource to consume
     */
    public void convertToItemStacks(Resource resource, Inventory inventory, double amount) {
        int numberOfSets = (int) Math.ceil(amount / conversion);
        InventoryHelpers.putMultiple(inventory, itemStacks, numberOfSets);
        resource.change(numberOfSets * conversion);

    }

    /**
     * Gets a ConvertToItemStackRunnable
     *
     * @param contraption Associated contraption
     * @param resource    Associated resource
     * @return A BukkitTask associated with a runnable of this ConversionGadget
     */
    public BukkitTask getConvertToItemStacksRunnable(Contraption contraption, Resource resource) {
        return (new ConvertToItemStacksRunnable(contraption, resource)).runTaskTimerAsynchronously(ContraptionsPlugin.getContraptionPlugin(), 1000, 1000);
    }

    public class ConvertToItemStacksRunnable extends BukkitRunnable {

        Contraption contraption;
        Resource resource;

        /**
         * The ConversionGadget runnable associated with converting Resources to
         * ItemStacks
         *
         * @param contraption Contraption associated with runnable
         * @param resource    Resource associated with runnable
         */
        public ConvertToItemStacksRunnable(Contraption contraption, Resource resource) {
            this.contraption = contraption;
            this.resource = resource;
        }

        /**
         * Schedules the task and grows the resource to the delay
         *
         * @param plugin The Contraptions Plugin
         * @param delay  The delay until the task is executed in ticks
         * @param period The period in ticks with which the task is executed
         * 
         * @return A Task associated with this runnable
         */
        @Override
        public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
            run();
            return super.runTaskTimer(plugin, delay, period);
        }

        /**
         * Grows the resource
         */
        @Override
        public void run() {
            convertToItemStacks(resource, contraption.getInventory(), resource.get());
        }
    }

    /**
     * Imports a ConversionGadget from a JSONObject
     *
     * @param jsonObject The JSONObject containing the information
     * @return A ConversionGadget with the properties contained in the
     *         JSONObject
     */
    public static ConversionGadget fromJSON(JSONObject jsonObject) {
        Set<ItemStack> itemStacks = InventoryHelpers.fromJSON(jsonObject.getJSONArray("inputs"));
        double conversion = jsonObject.getDouble("conversion");
        return new ConversionGadget(itemStacks, conversion);
    }
}
