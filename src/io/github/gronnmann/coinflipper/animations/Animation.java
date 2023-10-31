package io.github.gronnmann.coinflipper.animations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import io.github.gronnmann.coinflipper.gui.GameInventoryHolder;
import io.github.gronnmann.utils.coinflipper.ItemUtils;

public class Animation {
	private FileConfiguration animationFile;
	private File file;
	private boolean isDefault;

	public Animation(FileConfiguration animationFile, File file) {
		this.animationFile = animationFile;
		this.file = file;
		this.isDefault = false;
	}

	public ArrayList<Inventory> animationInventory = new ArrayList<Inventory>();

	@SuppressWarnings("unchecked")
	public void copy(Animation copyInto) {
		copyInto.animationInventory = (ArrayList<Inventory>) this.animationInventory.clone();
	}

	public void draw() {
		// Create default animation
		if (animationFile.getString("animation") == null) {
			for (int frame = 0; frame <= 50; frame++) {
				Inventory frameInv = Bukkit.createInventory(new GameInventoryHolder(), 45);
				animationInventory.add(frame, frameInv);
			}

			return;
		}

		// Draw animation
		int configuredFrames = animationFile.getConfigurationSection("animation").getKeys(false).size();
		for (int frame = 0; frame < configuredFrames; frame++) {
			Inventory frameInv = Bukkit.createInventory(new GameInventoryHolder(), 45);
			// For every slot
			for (int slot = 0; slot <= 44; slot++) {
				Material forSlot = null;

				try {
					forSlot = Material.valueOf(animationFile.getString("animation." + frame + "." + slot + ".material"));
				} catch (Exception e) {
					forSlot = Material.AIR;
				}

				short data = (short) animationFile.getInt("animation." + frame + "." + slot + ".data");
				ItemStack item = new ItemStack(forSlot, 1, (short) data);

				if (item.getType() != Material.AIR) {
					ItemUtils.setName(item, animationFile.getString("animation." + frame + "." + slot + ".name"));
				}

				frameInv.setItem(slot, item);
			}
			animationInventory.add(frame, frameInv);
		}
	}

	@SuppressWarnings("deprecation")
	public void save() {
		for (int frame = 0; frame < getFramesCount(); frame++) {
			Inventory inv = animationInventory.get(frame);
			for (int slot = 0; slot <= 44; slot++) {
				ItemStack item = inv.getItem(slot);
				if (item != null) {
					animationFile.set("animation." + frame + "." + slot + ".material", item.getType().toString());
					animationFile.set("animation." + frame + "." + slot + ".data", item.getData().getData());
					if (item.getItemMeta() != null) {
						animationFile.set("animation." + frame + "." + slot + ".name", item.getItemMeta().getDisplayName());
					} else {
						animationFile.set("animation." + frame + "." + slot + ".name", " ");
					}
				}

			}
		}

		try {
			animationFile.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Inventory> getFrames() {
		return animationInventory;
	}

	public int getFramesCount() {
		return animationInventory.size();
	}

	public void setFrame(int frame, Inventory window) {
		while (frame >= getFramesCount()) animationInventory.add(Bukkit.createInventory(new GameInventoryHolder(), 45));
		animationInventory.set(frame, window);
	}

	public Inventory getFrame(int frame) {
		while (frame >= getFramesCount()) animationInventory.add(Bukkit.createInventory(new GameInventoryHolder(), 45));
		return animationInventory.get(frame);
	}

	public String getName() {
		String name = file.getName();
		name = name.substring(0, name.length() - 4);
		return name;
	}

	public File getFile() {
		return file;
	}

	public boolean isDefault() {
		return isDefault;
	}

	protected void setDefault(boolean bool) {
		this.isDefault = bool;
	}
}
