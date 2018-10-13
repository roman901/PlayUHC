package org.uwtech.epix.playuhc.listeners;

import org.uwtech.epix.playuhc.PlayUhc;
import org.uwtech.epix.playuhc.customitems.*;
import org.uwtech.epix.playuhc.exceptions.UhcPlayerDoesntExistException;
import org.uwtech.epix.playuhc.exceptions.UhcPlayerNotOnlineException;
import org.uwtech.epix.playuhc.exceptions.UhcTeamException;
import org.uwtech.epix.playuhc.game.GameManager;
import org.uwtech.epix.playuhc.game.GameState;
import org.uwtech.epix.playuhc.languages.Lang;
import org.uwtech.epix.playuhc.players.PlayerState;
import org.uwtech.epix.playuhc.players.UhcPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemsListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRightClickItem(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		GameManager gm = GameManager.getGameManager();
		UhcPlayer uhcPlayer;
		try {
			uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
			ItemStack hand = player.getInventory().getItemInMainHand();

			if (gm.getGameState().equals(GameState.WAITING)
				&& UhcItems.isLobbyItem(hand)
				&& uhcPlayer.getState().equals(PlayerState.WAITING)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR 
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
			   ) {
					event.setCancelled(true);
					UhcItems.openTeamInventory(player);
				}
			
			if ( ((gm.getGameState().equals(GameState.WAITING) &&	uhcPlayer.getState().equals(PlayerState.WAITING))
				  || (gm.getGameState().equals(GameState.PLAYING) && uhcPlayer.getState().equals(PlayerState.PLAYING)))
					&& UhcItems.isCraftBookItem(hand)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR 
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				   ) {
						event.setCancelled(true);
						CraftsManager.openCraftBookInventory(player);
					}
			
			if (gm.getGameState().equals(GameState.WAITING)
					&& UhcItems.isLobbyTeamItem(hand)
					&& uhcPlayer.getState().equals(PlayerState.WAITING)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR 
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				   ) {
						event.setCancelled(true);
						Player itemPlayer = Bukkit.getPlayer(hand.getItemMeta().getDisplayName());
						if(itemPlayer != null){
							try {
								UhcPlayer uhcPlayerRequest = gm.getPlayersManager().getUhcPlayer(itemPlayer);
								uhcPlayer.getTeam().join(uhcPlayerRequest);
							} catch (UhcPlayerNotOnlineException | UhcTeamException e) {
								player.sendMessage(ChatColor.RED+e.getMessage());
							}
						}else{
							player.sendMessage(ChatColor.RED+ Lang.TEAM_PLAYER_JOIN_NOT_ONLINE);
						}
						
						player.getInventory().remove(hand);
					}
			
			if ( (gm.getGameState().equals(GameState.PLAYING) || gm.getGameState().equals(GameState.DEATHMATCH))
					&& UhcItems.isRegenHeadItem(hand)
					&& uhcPlayer.getState().equals(PlayerState.PLAYING)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR 
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				   ) {
						event.setCancelled(true);
						uhcPlayer.getTeam().regenTeam();
						player.getInventory().remove(hand);
					}
			
			if ((gm.getGameState().equals(GameState.PLAYING) || gm.getGameState().equals(GameState.DEATHMATCH))
					&& UhcItems.isCompassPlayingItem(hand)
					&& uhcPlayer.getState().equals(PlayerState.PLAYING)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR 
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				   ) {
						event.setCancelled(true);
						uhcPlayer.compassPlayingNextTeammate();
					}
			
			if (gm.getGameState().equals(GameState.WAITING)
					&& UhcItems.isKitSelectionItem(hand)
					&& uhcPlayer.getState().equals(PlayerState.WAITING)
					&& (event.getAction() == Action.RIGHT_CLICK_AIR 
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				   ) {
						event.setCancelled(true);
						KitsManager.openKitSelectionInventory(player);
					}
			
			
		} catch (UhcPlayerDoesntExistException e1) {
			// Nothing
		}
		
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClickInInventory(InventoryClickEvent event){
		
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		GameManager gm = GameManager.getGameManager();
		
		// Click on a player head to join a team
		if(event.getInventory().getName().equals(ChatColor.GREEN+ Lang.DISPLAY_MESSAGE_PREFIX+" "+ChatColor.DARK_GREEN+ Lang.ITEMS_KIT_INVENTORY)){
			if(KitsManager.isKitItem(item)){
				event.setCancelled(true);
				try {
					UhcPlayer uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
					Kit kit = KitsManager.getKitByName(item.getItemMeta().getDisplayName());
					if(!gm.getConfiguration().getEnableKitsPermissions() || (gm.getConfiguration().getEnableKitsPermissions() && player.hasPermission("playuhc.kit."+kit.getKey()))){
						uhcPlayer.setKit(kit);
						uhcPlayer.sendMessage(ChatColor.GREEN+ Lang.ITEMS_KIT_SELECTED.replace("%kit%", kit.getName()));
					}else{
						uhcPlayer.sendMessage(ChatColor.RED+ Lang.ITEMS_KIT_NO_PERMISSION);
					}
					player.closeInventory();
				} catch (UhcPlayerDoesntExistException e1) {
					// Nothing
				}
				
			}
			
		}
		
		if(event.getInventory().getName().equals(ChatColor.GREEN+ Lang.DISPLAY_MESSAGE_PREFIX+" "+ChatColor.DARK_GREEN+ Lang.TEAM_INVENTORY)){
			// Click on a player head to join a team
			if(UhcItems.isLobbyTeamItem(item)){
				event.setCancelled(true);
				
				Player itemPlayer = Bukkit.getPlayer(item.getItemMeta().getDisplayName());
				if(itemPlayer == player){
					player.sendMessage(ChatColor.RED+ Lang.TEAM_CANNOT_JOIN_OWN_TEAM);
				}else if(itemPlayer != null){
					UhcPlayer leader;
					try {
						leader = gm.getPlayersManager().getUhcPlayer(itemPlayer);
						leader.getTeam().askJoin(gm.getPlayersManager().getUhcPlayer(player), leader);
					} catch (UhcPlayerDoesntExistException | UhcTeamException e) {
						player.sendMessage(ChatColor.RED+e.getMessage());
					}
					
				}else{
					player.sendMessage(ChatColor.RED+ Lang.TEAM_LEADER_JOIN_NOT_ONLINE);
				}
				
				player.closeInventory();
			}
		
			// Click on the barrier to leave a team
			if(UhcItems.isLobbyLeaveTeamItem(event.getCurrentItem())){
				event.setCancelled(true);
				
				if(!gm.getConfiguration().getPreventPlayerFromLeavingTeam()){
					UhcPlayer uhcPlayer;
					try {
						uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
						uhcPlayer.getTeam().leave(uhcPlayer);
					} catch (UhcPlayerDoesntExistException e1) {
						// Nothing
					} catch (UhcTeamException e) {
						player.sendMessage(e.getMessage());
					}
					player.closeInventory();
				}
			}
			
			// Click on the item to change ready state
			if(UhcItems.isLobbyReadyTeamItem(event.getCurrentItem())){
				event.setCancelled(true);
				
				if(!gm.getConfiguration().getTeamAlwaysReady()){
					UhcPlayer uhcPlayer;
					try {
						uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
						uhcPlayer.getTeam().changeReadyState(uhcPlayer);
					} catch (UhcPlayerDoesntExistException e1) {
						// Nothing
					} catch (UhcTeamException e) {
						player.sendMessage(e.getMessage());
					}
					player.closeInventory();
				}
				
			}
			
		}

		if(event.getInventory().getName().equals(ChatColor.GREEN+ Lang.DISPLAY_MESSAGE_PREFIX+" "+ChatColor.DARK_GREEN+ Lang.ITEMS_CRAFT_BOOK_INVENTORY)){
			event.setCancelled(true);
			
			if(CraftsManager.isCraftItem(item)){
				player.closeInventory();
				Craft craft = CraftsManager.getCraftByDisplayName(item.getItemMeta().getDisplayName());
				if(!gm.getConfiguration().getEnableCraftsPermissions() || (gm.getConfiguration().getEnableCraftsPermissions() && player.hasPermission("playuhc.craft."+craft.getName()))){
					CraftsManager.openCraftInventory(player,craft);
				}else{
					player.sendMessage(ChatColor.RED+ Lang.ITEMS_CRAFT_NO_PERMISSION.replace("%craft%",craft.getName()));
				}
			}
			
			if(CraftsManager.isCraftBookBackItem(item)){
				event.setCancelled(true);
				player.closeInventory();
				CraftsManager.openCraftBookInventory(player);
			}
			
		}
		
		
		if(!gm.getGameState().equals(GameState.PLAYING) && !gm.getGameState().equals(GameState.DEATHMATCH)){
			// Click in its own inventory while not playing
			event.setCancelled(true);
		}
		
		// Ban level 2 potions
		if(event.getInventory().getType().equals(InventoryType.BREWING) && gm.getConfiguration().getBanLevelTwoPotions()){
			
			final BrewerInventory inv = (BrewerInventory) event.getInventory();
			final HumanEntity human = event.getWhoClicked();
			Bukkit.getScheduler().runTaskLater(PlayUhc.getPlugin(), new CheckBrewingStandAfterClick(inv.getHolder(),human),1);

		}
		
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperEvent(InventoryMoveItemEvent event) {
		Inventory inv = event.getDestination();
		if(inv.getType().equals(InventoryType.BREWING) && GameManager.getGameManager().getConfiguration().getBanLevelTwoPotions() && inv.getHolder() instanceof BrewingStand){
			Bukkit.getScheduler().runTaskLater(PlayUhc.getPlugin(), new CheckBrewingStandAfterClick((BrewingStand) inv.getHolder(),null),1);
		}
		
	}
	
	class CheckBrewingStandAfterClick implements Runnable {
        private BrewingStand stand;
        private HumanEntity human;

        public CheckBrewingStandAfterClick(BrewingStand stand, HumanEntity human) { 
        	this.stand = stand;
        	this.human = human;
        }
        
        public void run() {
        	ItemStack ingredient = stand.getInventory().getIngredient();
			if(ingredient != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)){
				if(human != null)
					human.sendMessage(ChatColor.RED+ Lang.ITEMS_POTION_BANNED);
				
				stand.getLocation().getWorld().dropItemNaturally(stand.getLocation(), ingredient.clone());
				stand.getInventory().setIngredient(new ItemStack(Material.AIR));
				
			}
        	
			
        }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		GameManager gm = GameManager.getGameManager();
		UhcPlayer uhcPlayer;
		try {
			uhcPlayer = gm.getPlayersManager().getUhcPlayer(player);
			ItemStack playerRequestItem = new ItemStack(event.getItemDrop().getItemStack());
			
			// Deny drop of Lobby Item
			if (gm.getGameState().equals(GameState.WAITING)
					&& UhcItems.isLobbyItem(event.getItemDrop().getItemStack())
					&& uhcPlayer.getState().equals(PlayerState.WAITING)
				   ){
				event.setCancelled(true);
			}
			
			// Deny drop of Lobby Item
			if (gm.getGameState().equals(GameState.WAITING)
					&& UhcItems.isKitSelectionItem(event.getItemDrop().getItemStack())
					&& uhcPlayer.getState().equals(PlayerState.WAITING)
				   ){
				event.setCancelled(true);
			}
			
			if (gm.getGameState().equals(GameState.WAITING)
					&& UhcItems.isLobbyTeamItem(playerRequestItem)
					&& uhcPlayer.getState().equals(PlayerState.WAITING)
				   ){
				Player itemPlayer = Bukkit.getPlayer(playerRequestItem.getItemMeta().getDisplayName());
				if(itemPlayer != null){
					UhcPlayer uhcPlayerRequest = gm.getPlayersManager().getUhcPlayer(itemPlayer);
					uhcPlayer.getTeam().denyJoin(uhcPlayerRequest);
				}else{
					player.sendMessage(ChatColor.RED+ Lang.TEAM_PLAYER_NOT_ONLINE.replace("%player%", playerRequestItem.getItemMeta().getDisplayName()));
				}
				
				event.getItemDrop().remove();

			}
		} catch (UhcPlayerDoesntExistException e) {
		    // Nothing
		}
		
			
	}
}