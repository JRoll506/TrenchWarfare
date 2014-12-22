package com.rbruno.TrenchWarfare;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {

	PluginDescriptionFile pdf = this.getDescription();
	public static int gameState = 0;
	private static boolean pregameCountdown = false;;
	public static int tick = 20;
	public static Plugin plugin;
	public static Game game;
	FileConfiguration config = getConfig();
	static TrenchConfig trenchConfig;
	static Location spawn;

	public static String[] classes = { "Gunner", "Scout" };

	public static HashMap<Player, String> classMap = new HashMap<Player, String>();
	
	public static ArrayList<Player> parkour = new ArrayList<Player>();

	@Override
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		plugin = this;
		trenchConfig = new TrenchConfig();
		spawn = trenchConfig.getSpawn();
		getLogger().info("TrenchWarfare" + "[" + pdf.getVersion() + "]" + " is enabled");
		getLogger().info("Plugin made by " + pdf.getAuthors());
		lobby();
		getServer().getPluginManager().registerEvents(new listeners(), this);
	}

	private void lobby() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				tick();
			}
		}, 0L, 20L);
	}

	protected void tick() {
		if (gameState == 0) {
			if (pregameCountdown) {
				if (tick == 0) {
					gameState = 1;
					gameStart();
					tick = trenchConfig.getGameClock() * 60;
					pregameCountdown = false;
				} else {
					if (tick <= 5 || tick % 10 == 0) {
						broadcast(tick + " seconds till game start!", true);
					}
					tick--;
				}
			} else {
				if (getServer().getOnlinePlayers().length >= trenchConfig.getMinPlayer()) {
					tick = trenchConfig.getPregameCountdown();
					pregameCountdown = true;
				}
			}
		}
		if (gameState == 1) {
			String second = Main.tick % 60 + "";
			if (Main.tick % 60 <= 9) second = "0" + second;
			String time = (Main.tick - (Main.tick % 60)) / 60 + ":" + second;
			game.objective.setDisplayName(ChatColor.YELLOW + "Time: " + ChatColor.WHITE + time);
			if (tick % 60 == 0) {
				broadcast(tick / 60 + " minutes left in game!", true);
			}

			tick--;
			if (tick <= 5 && tick != 0) {
				broadcast(tick + " seconds left in game!", true);
			}
			if (tick == 0) {
				endGame(false);
			}
		}

	}

	public static void endGame(boolean flag) {
		parkour.clear();
		game.objective.setDisplayName(ChatColor.BOLD + "Pre-Game");
		if (!(flag)) {
			broadcast("The game has ended!", true);
			if (game.blueScore < game.redScore) {
				broadcast("Red has won the game!", true);
			} else {
				broadcast("Blue has won the game!", true);
			}
			broadcast("Score: " + ChatColor.BLUE + game.blueScore + " " + ChatColor.RED + game.redScore, true);
		}

		gameState = 0;
		Player[] players = Main.plugin.getServer().getOnlinePlayers();
		if (!(players.length == 0)) {
			for (int i = 0; i < players.length; i++) {
				players[i].getInventory().setArmorContents(null);
				players[i].removePotionEffect(PotionEffectType.SPEED);
				players[i].teleport(spawn);
				players[i].getInventory().clear();
			}
			tick = trenchConfig.getPregameCountdown();
			pregameCountdown = false;
		}
	}

	private void gameStart() {
		parkour.clear();
		game = new Game();
		game.pickTeams();
		game.giveItems(getServer().getOnlinePlayers());
		game.tpPlayers();
		game.setScoreBoard();
		broadcast("The war has begun!", true);
	}

	public static void messagePlayer(Player player, String string) {
		String message = plugin.getConfig().getString("messagePrefix").replace("&", "�") + string;
		player.sendMessage(message);
	}

	public static void broadcast(String string, Boolean Value) {
		if (Value == true) {
			string = plugin.getConfig().getString("messagePrefix").replace("&", "�") + string;
		}
		Player[] onlinePlayers = (Bukkit.getOnlinePlayers());
		for (int i = 0; i < onlinePlayers.length; i++) {
			onlinePlayers[i].sendMessage(string);
		}
	}

	public static Plugin getInstance() {
		return plugin;
	}

	public static void addPlayer(Player player) {
		game.addPlayer(player);

	}

}
