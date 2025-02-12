package io.github.kuohsuanlo.restorenature;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.massivecore.ps.PS;

import io.github.kuohsuanlo.restorenature.util.Lag;
import io.github.kuohsuanlo.restorenature.util.RestoreNatureUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import java.time.Instant;



class RestoreNatureEnqueuer implements Runnable {
	private RestoreNaturePlugin rsplugin;
	
	public ArrayList<MapChunkInfo> maintained_worlds = new ArrayList<MapChunkInfo>();
	
	private Faction faction =null;
	
	private GriefPrevention gp;
	private final String notClaimedOwner = "administrator";
	
	
	public static final int chunk_center_x = 8;
	public static final int chunk_center_y = 64;
	public static final int chunk_center_z = 8;
	public int processCount = 1;
	public int currentCount = 0;
	
	public double lastSecondTPS = 20;
	public int tpsCount = 20;
	public int tpsCurrentCount = 0;
	
	public int currentTimerLoopX = 0;
	
	public int currentChunkReqested = 0;
	public int currentEntityRequested = 0;
	public long last_time=Instant.now().getEpochSecond(); 
	public long now_time = Instant.now().getEpochSecond();
    public RestoreNatureEnqueuer(ArrayList<MapChunkInfo> existing_worlds,RestoreNaturePlugin plugin) {
    	rsplugin= plugin;
	
    	maintained_worlds = existing_worlds;

    }
    
    public void run() {
    	tpsCurrentCount++;
    	if(tpsCurrentCount>=tpsCount){
    		tpsCurrentCount=0;
    		lastSecondTPS = Lag.getTPS();
    	}
    	
    	currentCount++;
    	processCount = calculateProcessCount();
    	if(currentCount>=processCount){
    		currentCount=0;
    		processRequest();
    	}
    	
    	
    	
    	
    }
    private void processRequest(){
    	
    	for(int i=0;i<maintained_worlds.size();i++){
    		MapChunkInfo chunksInfo = maintained_worlds.get(i);
    		
        	int x=chunksInfo.now_min_x;
        	int z=chunksInfo.now_min_z;

			int chunk_x = RestoreNatureUtil.convertArrayIdxToChunkIdx(x);
	    	int chunk_z = RestoreNatureUtil.convertArrayIdxToChunkIdx(z);
	    	
    		if(RestoreNatureUtil.isInRadius(chunk_x, chunk_z, chunksInfo.chunk_radius)) {
    	    	Location ChunkMid = new Location(Bukkit.getServer().getWorld(chunksInfo.world_name),chunk_x*16+8,60,chunk_z*16+8);

    	    	if(!checkLocationClaimed(ChunkMid)){ // Land not claimed
    				if(chunksInfo.chunk_untouchedtime[x][z]>=RestoreNaturePlugin.MAX_SECONDS_UNTOUCHED){
    					currentChunkReqested++;
    					if(rsplugin.ChunkTimeTicker.addFullRestoreTask(ChunkMid)){
    						if(RestoreNaturePlugin.Verbosity>=1)
    							Bukkit.getServer().getConsoleSender().sendMessage(RestoreNaturePlugin.PLUGIN_PREFIX+"addFullRestoreTask : "+ ChunkMid.getWorld().getName()+" "+
    		    			RestoreNatureUtil.convertArrayIdxToChunkIdx(x)+" "+
    		    			RestoreNatureUtil.convertArrayIdxToChunkIdx(z));

    					}
    					else{
    						if(RestoreNaturePlugin.Verbosity>=1)
    							rsplugin.getServer().getConsoleSender().sendMessage(RestoreNaturePlugin.PLUGIN_PREFIX+"Maximum number of tasks in TaskQueue reached. Please increase CHECK_PERIOD_IN_SECONDS" );
    					}
    				}
    				else if(chunksInfo.chunk_untouchedtime[x][z]>=RestoreNaturePlugin.MAX_SECONDS_ENTITYRECOVER){
    					currentEntityRequested++;
    					if(rsplugin.ChunkTimeTicker.addEntityRestoreTask(ChunkMid)){
    						if(RestoreNaturePlugin.Verbosity>=1)
    							Bukkit.getServer().getConsoleSender().sendMessage(RestoreNaturePlugin.PLUGIN_PREFIX+"addEntityRestoreTask : "+ ChunkMid.getWorld().getName()+" "+
    		    			RestoreNatureUtil.convertArrayIdxToChunkIdx(x)+" "+
    		    			RestoreNatureUtil.convertArrayIdxToChunkIdx(z));

    					}
    					else{
    						if(RestoreNaturePlugin.Verbosity>=1)
    							rsplugin.getServer().getConsoleSender().sendMessage(RestoreNaturePlugin.PLUGIN_PREFIX+"Maximum number of tasks in TaskQueue reached. Please increase CHECK_PERIOD_IN_SECONDS" );
    					}
    				}
    			}
        		
    		}
    		else{
    			chunksInfo.now_min_z =chunksInfo.max_z;
    		}
	    	
    		double elapsed = now_time-last_time;
    		
        	chunksInfo.now_min_z +=1;
        	if(chunksInfo.now_min_z >=chunksInfo.max_z){
        		for(int tx=0;tx<chunksInfo.max_x;tx++){
        			for(int tz=0;tz<chunksInfo.max_z;tz++){
        				chunksInfo.chunk_untouchedtime[tx][tz]+=elapsed;
        			}
        		}
        		int lastFullChunkRestored = rsplugin.ChunkTimeTicker.lastFullChunkRestored;
        		int lastEntityChunkRestored = rsplugin.ChunkTimeTicker.lastEntityChunkRestored;
        		int lastEntityRespawn = rsplugin.ChunkTimeTicker.lastEntityRespawn;
        		int lastBannedBlockRemoved = rsplugin.ChunkTimeTicker.lastBannedBlockRemoved;
        		
        		rsplugin.getServer().getConsoleSender().sendMessage(
        				ChatColor.LIGHT_PURPLE+RestoreNaturePlugin.PLUGIN_PREFIX+
        				"progress: "+chunksInfo.now_min_x+"/"+chunksInfo.max_x+" | "+
        				"elapsed: "+elapsed+" sec(s)"+" | "+
        				"Full Enq/Deq: "+
        				currentChunkReqested+"/"+lastFullChunkRestored+" | "+
        				"Entity Enq/Deq: "+
        				currentEntityRequested+"/"+lastEntityChunkRestored+" | "+
        				"Entity respawned: "+lastEntityRespawn+" | "+
        				"Block removed: "+lastBannedBlockRemoved
        				);
        		
        		rsplugin.ChunkTimeTicker.lastFullChunkRestored = 0;
        		rsplugin.ChunkTimeTicker.lastEntityChunkRestored = 0;
        		rsplugin.ChunkTimeTicker.lastEntityRespawn =0;
        		rsplugin.ChunkTimeTicker.lastBannedBlockRemoved =0;
				currentChunkReqested=0;
				currentEntityRequested=0;
        		
        		chunksInfo.now_min_z =0;
        		chunksInfo.now_min_x +=1;
        		
    	    	if(chunksInfo.now_min_x >=chunksInfo.max_x){
            		chunksInfo.now_min_x=0;
            	}
    	    	
    	    	last_time = Instant.now().getEpochSecond();
        	}
        	
        	
	    
        	
    	}
    	now_time = Instant.now().getEpochSecond();
	


  	
    }
	public void setWorldsChunkUntouchedTime(Block touched_block){
		

		for(int i=0;i<maintained_worlds.size();i++){
    		if(maintained_worlds.get(i).world_name.equals(touched_block.getWorld().getName())){
    			if(
					RestoreNatureUtil.convertChunkIdxToArrayIdx( touched_block.getLocation().getChunk().getX())<=maintained_worlds.get(i).max_x  &&
							RestoreNatureUtil.convertChunkIdxToArrayIdx( touched_block.getLocation().getChunk().getZ())<=maintained_worlds.get(i).max_z  
    					){
    				int x = RestoreNatureUtil.convertChunkIdxToArrayIdx( touched_block.getChunk().getX());
        			int z = RestoreNatureUtil.convertChunkIdxToArrayIdx( touched_block.getChunk().getZ());
        			
        			int R = RestoreNaturePlugin.BLOCK_EVENT_EFFECTING_RADIUS-1;
        			for(int r_x=(-1)*R;r_x<=R;r_x++){
            			for(int r_z=(-1)*R;r_z<=R;r_z++){
            				if((x+r_x)>=0  &&  (x+r_x)<=maintained_worlds.get(i).max_x  &&  (z+r_z)>=0  &&  (z+r_z)<=maintained_worlds.get(i).max_z){
            					maintained_worlds.get(i).chunk_untouchedtime[x+r_x][z+r_z] = 0;
            				}
            			}
        			}
    				
    			} 
    			
    		}
    	}
	
		
	}
	private int calculateProcessCount(){
	   	if(lastSecondTPS>=19){
			return 1;
		}
		else if(lastSecondTPS>=18){
			return 4;
		}
		else if(lastSecondTPS>=17){
			return 20;
		}
		else if(lastSecondTPS>=16){
			return 40;
		}
		else if(lastSecondTPS>=15){
			return 160;
		}
		else if(lastSecondTPS>=14){
			return 400;
		}
		else{
			return 1000;
		}
    }
	public boolean checkLocationClaimed(Location location){
    	
    	
    	if(RestoreNaturePlugin.USING_FEATURE_FACTION){
        	faction = BoardColl.get().getFactionAt(PS.valueOf(location));
    	}
    	
    	if(RestoreNaturePlugin.USING_FEATURE_GRIEFPREVENTION){
    		gp = GriefPrevention.instance;
    	}
    	
    	boolean fc_claimed = true;
    	boolean gp_claimed = true;
    	for(int i=0;i<maintained_worlds.size();i++){
    		if(location.getWorld().getName().equals(maintained_worlds.get(i).world_name)){
        		if(maintained_worlds.get(i).factions_name.size()==0){
        			fc_claimed = false;
        		}
        		else{
            		for(int j=0;j<maintained_worlds.get(i).factions_name.size();j++){
                    	if(faction==null){
                    		fc_claimed = false;
                    	}else if(faction.getName().equals(maintained_worlds.get(i).factions_name.get(j))){
                    		fc_claimed = false;
                    		
                    	}
                    	
                    	if(gp==null){
                    		gp_claimed = false;
                    	}
                    	else{
                    		boolean isOthersLand = false;
                    		for(int x=-8;x<8;x++){
                    			for(int z=-8;z<8;z++){
                    				Claim claim = gp.dataStore.getClaimAt(
                    						location.clone().add(x, 0, z), true, null
											);
                    				
                    				//no one's land
                    				if(claim==null){
                    					
                    				}
                    				//someone's land
                    				else{
                    					//System.out.println(claim.getOwnerName());
                        				isOthersLand = isOthersLand  ||  ( !claim.getOwnerName().equals(notClaimedOwner) );
                        				
                    				}
                    				
                    				if(isOthersLand) break;
                    			}
                    			if(isOthersLand) break;
                    		}
                    		gp_claimed = isOthersLand;
                    		//System.out.println(gp_claimed);
                    	}
            			
            		}
        		}         		
        	}
    	}        	
    	
    	
		return gp_claimed || fc_claimed ;

	}

}