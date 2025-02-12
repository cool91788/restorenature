package io.github.kuohsuanlo.restorenature;


import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

public class RestoreNatureBlockListener implements Listener {
	RestoreNaturePlugin rplugin;
	public RestoreNatureBlockListener(RestoreNaturePlugin plugin){
		rplugin = plugin;
	}
    @EventHandler
    public void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }   
    @EventHandler
    public void onBrewEvent(BrewEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }   
    @EventHandler
    public void onEnchantItemEvent(EnchantItemEvent event) {
        Block block = event.getEnchantBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }      
    
  
    
    @EventHandler
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }     
    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }     
    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }   
    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }    
    @EventHandler
    public void onBlockFromToEvent(BlockFromToEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Block block = event.getBlock();
        rplugin.ChunkUpdater.setWorldsChunkUntouchedTime(block);
    }
    
}