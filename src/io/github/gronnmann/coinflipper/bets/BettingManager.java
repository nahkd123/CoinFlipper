package io.github.gronnmann.coinflipper.bets;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.github.gronnmann.coinflipper.CoinFlipper;
import io.github.gronnmann.coinflipper.ConfigManager;
import io.github.gronnmann.coinflipper.animations.Animation;
import io.github.gronnmann.coinflipper.animations.AnimationsManager;
import io.github.gronnmann.coinflipper.customizable.ConfigVar;
import io.github.gronnmann.coinflipper.gui.SelectionScreen;
import io.github.gronnmann.utils.coinflipper.Debug;
import io.github.nahkd123.comm.coinflipperpatch.Account;

public class BettingManager {
	private BettingManager() {
	}

	private static BettingManager manager = new BettingManager();

	public static BettingManager getManager() {
		return manager;
	}

	private FileConfiguration conf;

	private ArrayList<Bet> bets = new ArrayList<Bet>();

	public void load() {
		conf = ConfigManager.getManager().getBets();
		if (conf.getConfigurationSection("bets") == null) return;

		for (String ids : conf.getConfigurationSection("bets").getKeys(false)) {
			int id = Integer.parseInt(ids);
			boolean alreadyExist = false;
			for (Bet b : bets) {
				if (b.getID() == id)
					alreadyExist = true;
			}

			if (alreadyExist)
				continue;

			int booster = conf.getInt("bets." + ids + ".booster");
			String playerString = conf.getString("bets." + ids + ".player");
			double money = conf.getDouble("bets." + ids + ".money");
			int side = conf.getInt("bets." + ids + ".side");
			int time = conf.getInt("bets." + ids + ".time");
			Animation animation = AnimationsManager.getManager().getAnimation(conf.getString("bets." + ids + ".animation"));

			Bet bet = new Bet(Account.fromString(playerString), side, money, id, booster, animation);
			bet.setTimeRemaining(time);

			bets.add(bet);
		}
	}

	public void save() {

		conf.set("bets", null);

		for (Bet b : bets) {
			conf.set("bets." + b.getID() + ".booster", b.getBooster());
			conf.set("bets." + b.getID() + ".player", b.getPlayer().toString());
			conf.set("bets." + b.getID() + ".money", b.getAmount());
			conf.set("bets." + b.getID() + ".side", b.getSide());
			conf.set("bets." + b.getID() + ".time", b.getTimeRemaining());
			conf.set("bets." + b.getID() + ".animation", b.getAnimation().getName());

			Debug.print("Saving bet ID: " + b.getID() + " with owner: " + b.getPlayer());
		}

		ConfigManager.getManager().saveBets();
	}

	public Bet addBet(Player p, int side, double amount) {
		// Booster
		int booster = 0;
		for (int i = 0; i <= 100; i++) {
			if (p.hasPermission("coinflipper.boost." + i)) {
				if (i > booster) {
					booster = i;
				}
			}
		}

		Animation animation = AnimationsManager.getManager().getAnimationToUse(p);

		// Rest
		Bet b = new Bet(new Account(p), side, amount, this.getNextAvaibleID(), booster, animation);
		bets.add(b);
		return b;
	}

	public int getNextAvaibleID() {
		if (bets.isEmpty()) {
			return 1;
		}

		int greatestID = 1;

		for (Bet g : bets) {
			if (g.getID() > greatestID) {
				greatestID = g.getID();
			}
		}

		return greatestID + 1;

	}

	public Bet getBet(int id) {
		for (Bet g : bets) {
			if (g.getID() == id) {
				return g;
			}
		}

		return null;
	}

	public boolean betExists(int id) {
		for (Bet g : bets) {
			if (id == g.getID())
				return true;
		}
		return false;
	}

	public void removeBet(Bet g) {
		bets.remove(g);
	}

	public void removeBet(int id) {
		bets.remove(this.getBet(id));
	}

	public Account challengeBet(Bet b, Player p) {
		int[] chances = this.getChances(p, b);
		bets.remove(b);

		Debug.print("Betowner chances: " + chances[1]);
		Debug.print("Challenger chances: " + chances[0]);

		int totalChances = chances[0] + chances[1];
		Debug.print("Total chances: " + totalChances);

		Random rn = new Random();
		int r = rn.nextInt(totalChances + 1);
		Debug.print("Rolled: " + r + "");

		return r <= chances[1]? b.getPlayer() : new Account(p);
	}

	public int[] getChances(Player p1, Bet b) {

		if (ConfigVar.BOOSTERS_ENABLED.getValue() != null) {
			if (!ConfigVar.BOOSTERS_ENABLED.getBoolean()) {
				return new int[] { 50, 50 };
			}
		}

		int i1 = 50;
		int i2 = 50;

		int booster1 = 0, booster2 = 0;

		for (int i = 0; i <= 100; i++) {
			if (p1.hasPermission("coinflipper.boost." + i)) {
				if (i > booster1) {
					booster1 = i;
				}
			}
		}
		booster2 = b.getBooster();

		if (booster1 == 0 && booster2 != 0) {
			i2 = booster2;
			i1 = 100 - booster2;
		} else if (booster1 != 0 && booster2 == 0) {
			i1 = booster1;
			i2 = 100 - booster1;
		} else if (booster1 != 0 && booster2 != 0) {
			i1 = booster1;
			i2 = booster2;
		}

		int[] returned = { i1, i2 };
		return returned;

	}

	public ArrayList<Bet> getBets() {
		return bets;
	}

	public void clearBets() {
		for (Bet b : bets) CoinFlipper.getEcomony().depositPlayer(b.getPlayer().toOfflinePlayer(), b.getAmount());
		bets.clear();
		SelectionScreen.getInstance().refreshGameManager();
	}

	public boolean isAlreadyThere(Player p) {
		int limit = 1;
		int bets = 0;

		if (ConfigVar.BETS_PER_PLAYER.getValue() != null) {
			limit = ConfigVar.BETS_PER_PLAYER.getInt();
		}

		for (Bet b : BettingManager.getManager().getBets()) {
			if (b.getPlayer().getUuid().equals(p.getUniqueId())) bets++;
		}

		return bets >= limit;
	}
}
