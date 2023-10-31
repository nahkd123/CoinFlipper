package io.github.gronnmann.coinflipper.animations;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.gronnmann.coinflipper.CoinFlipper;
import io.github.gronnmann.coinflipper.GamesManager;
import io.github.gronnmann.coinflipper.customizable.ConfigVar;
import io.github.gronnmann.coinflipper.customizable.Message;
import io.github.gronnmann.coinflipper.hook.HookManager;
import io.github.gronnmann.utils.coinflipper.Debug;
import io.github.gronnmann.utils.coinflipper.GeneralUtils;
import io.github.gronnmann.utils.coinflipper.PacketUtils;
import io.github.gronnmann.utils.coinflipper.PacketUtils.TitleType;
import io.github.nahkd123.comm.coinflipperpatch.Account;

public class AnimationRunnable extends BukkitRunnable {
	private Account s1, s2, winner;
	private String winMoneyFormatted;
	private int phase, winFrame;
	private double winMoney;

	PersonalizedAnimation animation;

	public AnimationRunnable(Account s1, Account s2, Account winner, double winMoney, String animationS, String inventoryName) {
		this.s1 = s1;
		this.s2 = s2;
		this.winner = winner;
		this.phase = 0;
		this.winMoney = winMoney;
		this.winMoneyFormatted = GeneralUtils.getFormattedNumbers(winMoney);
		this.winFrame = ConfigVar.ANIMATIONS_ENABLED.getBoolean() ? ConfigVar.FRAME_WINNER_CHOSEN.getInt() : 1;

		Animation anim = AnimationsManager.getManager().getAnimation(animationS);
		animation = new PersonalizedAnimation(anim, winner, s1, s2, inventoryName);
	}

	public void run() {
		phase++;

		Player p1 = s1.toPlayer().orElse(null); // TODO
		Player p2 = s2.toPlayer().orElse(null);

		showAnimationFor(p1);
		if (!ConfigVar.ANIMATION_ONLY_CHALLENGER.getBoolean()) showAnimationFor(p2);

		if (phase == winFrame) {
			// Revert "Give money -- MOVED TO START OF SPIN INCASE OF SERVER CRASH/TURNOFF"
			// Commit hash = 5ffecd9dad25b8c3e70a2876cafb8e56db81b7e6
			// A guy commissioned me to move the give money back at the end of spin
			// TODO Give all money when onDisable() invoked
			CoinFlipper.getEcomony().depositPlayer(winner.toOfflinePlayer(), winMoney);

			// Sound
			try {
				p1.playSound(p1.getLocation(), Sound.valueOf(ConfigVar.SOUND_WINNER_CHOSEN.getString().toUpperCase()), 1F, 1F);
			} catch (Exception e) {
				// TODO MY GOD WHY?
			}
			try {
				p2.playSound(p2.getLocation(), Sound.valueOf(ConfigVar.SOUND_WINNER_CHOSEN.getString().toUpperCase()), 1F, 1F);
			} catch (Exception e) {
			}

			Account loser = s1.equals(winner)? s2 : s1;

			// Message winner/loser
			winner.toPlayer().ifPresent(win -> {
				String winMsg = Message.BET_WON.getMessage().replace("%MONEY%", winMoneyFormatted + "").replace("%WINNER%", winner.getName()).replace("%LOSER%", loser.getName());
				win.sendMessage(winMsg);

				PacketUtils.sendTitle(win, Message.BET_TITLE_VICTORY.getMessage(), TitleType.TITLE, 20, 60, 20);
				PacketUtils.sendTitle(win, winMsg, TitleType.SUBTITLE, 20, 60, 20);
			});

			//Player los = Bukkit.getPlayer(loser);
			loser.toPlayer().ifPresent(los -> {
				String losMsg = Message.BET_LOST.getMessage().replace("%MONEY%", winMoneyFormatted + "").replace("%WINNER%", winner.getName()).replace("%LOSER%", loser.getName());
				los.sendMessage(losMsg);

				PacketUtils.sendTitle(los, Message.BET_TITLE_LOSS.getMessage(), TitleType.TITLE, 20, 60, 20);
				PacketUtils.sendTitle(los, losMsg, TitleType.SUBTITLE, 20, 60, 20);
			});

			// Possibly broadcast win
			if (!(ConfigVar.VALUE_NEEDED_TO_BROADCAST.getValue() == null)
					&& winMoney >= ConfigVar.VALUE_NEEDED_TO_BROADCAST.getDouble()
					&& ConfigVar.VALUE_NEEDED_TO_BROADCAST.getDouble() != -1) {

				Debug.print("Normal broadcast.");
				Bukkit.broadcastMessage(
						Message.HIGH_GAME_BROADCAST.getMessage()
							.replace("%MONEY%", winMoneyFormatted + "")
							.replace("%WINNER%", winner.getName())
							.replace("%LOSER%", loser.getName()));

			}
		}

		// Sound click
		if (phase < winFrame && ConfigVar.ANIMATIONS_ENABLED.getBoolean()) {
			try {
				p1.playSound(p1.getLocation(), Sound.valueOf(ConfigVar.SOUND_WHILE_CHOOSING.getString().toUpperCase()), 1F, 1F);
			} catch (Exception e) {
			}
			try {
				p2.playSound(p2.getLocation(), Sound.valueOf(ConfigVar.SOUND_WHILE_CHOOSING.getString().toUpperCase()), 1F, 1F);
			} catch (Exception e) {
			}
		}

		if (phase == (winFrame + 20)) {
			GamesManager.getManager().setSpinning(s1.getUuid(), false);
			GamesManager.getManager().setSpinning(s2.getUuid(), false);

			if (p1 != null && ConfigVar.GUI_AUTO_CLOSE.getBoolean()) p1.closeInventory();
			if (p2 != null && ConfigVar.GUI_AUTO_CLOSE.getBoolean()) p2.closeInventory();
		}
	}

	private void showAnimationFor(Player p1) {
		if (p1 != null && ConfigVar.ANIMATIONS_ENABLED.getBoolean()) {
			if (!HookManager.getManager().isTagged(p1)) {
				p1.openInventory(animation.getFrame(phase));
			} else {
				if (phase == 1) {
					p1.sendMessage(Message.BET_START_COMBAT.getMessage());

					PacketUtils.sendTitle(p1, Message.BET_TITLE_COMBAT.getMessage(), TitleType.TITLE, 20, 40, 20);
					PacketUtils.sendTitle(p1, Message.BET_START_COMBAT.getMessage(), TitleType.SUBTITLE, 20, 40, 20);
				}
			}
		}
	}
}
