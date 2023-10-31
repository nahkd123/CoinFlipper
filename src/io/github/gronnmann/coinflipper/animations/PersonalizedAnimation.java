package io.github.gronnmann.coinflipper.animations;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.gronnmann.coinflipper.customizable.ConfigVar;
import io.github.gronnmann.coinflipper.customizable.Message;
import io.github.gronnmann.utils.coinflipper.InventoryUtils;
import io.github.gronnmann.utils.coinflipper.ItemUtils;
import io.github.nahkd123.comm.coinflipperpatch.Account;
import net.md_5.bungee.api.ChatColor;

public class PersonalizedAnimation {
	private String inventoryName;
	private Animation animation;
	private ItemStack p1Skull, p2Skull, winnerSkull;

	public PersonalizedAnimation(Animation animation, Account winner, Account p1, Account p2, String inventoryName) {
		this.inventoryName = inventoryName;
		this.animation = animation;

		p1Skull = ItemUtils.getSkull(p1);
		p2Skull = ItemUtils.getSkull(p2);
		winnerSkull = ItemUtils.getSkull(winner);

		p1Skull = ItemUtils.setName(p1Skull, Message.ANIMATION_ROLL_P1SKULL.getMessage().replace("%PLAYER%", p1.getName()));
		p2Skull = ItemUtils.setName(p2Skull, Message.ANIMATION_ROLL_P2SKULL.getMessage().replace("%PLAYER%", p2.getName()));
		winnerSkull = ItemUtils.setName(winnerSkull, Message.ANIMATION_ROLL_WINNERSKULL.getMessage().replace("%PLAYER%", winner.getName()));
	}

	public Inventory getFrame(int frame) {
		Inventory fram = animation.getFrame(frame);
		fram = InventoryUtils.changeName(fram, inventoryName);

		for (int slot = 0; slot <= 44; slot++) {
			ItemStack item = fram.getItem(slot);
			if (item != null) {
				switch (item.getType()) {
				case WOOD_HOE:
					fram.setItem(slot, p1Skull);
					break;
				case STONE_HOE:
					fram.setItem(slot, p2Skull);
					break;
				case DIAMOND_HOE:
					fram.setItem(slot, winnerSkull);
					break;
				default:
					if (frame < ConfigVar.FRAME_WINNER_CHOSEN.getInt()) {
						if (frame % 2 == 0) {
							fram.setItem(slot, ItemUtils.setName(item,
									ChatColor.YELLOW + Message.ANIMATION_ROLL_ROLLING.getMessage()));
						} else {
							fram.setItem(slot, ItemUtils.setName(item,
									ChatColor.GOLD + Message.ANIMATION_ROLL_ROLLING.getMessage()));
						}

					} else {
						fram.setItem(slot, ItemUtils.setName(item, Message.ANIMATION_ROLL_WINNERCHOSEN.getMessage()));
					}
					break;
				}
			}
		}

		return fram;
	}
}
