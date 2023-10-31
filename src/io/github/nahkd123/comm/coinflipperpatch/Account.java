package io.github.nahkd123.comm.coinflipperpatch;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

/**
 * <p>
 * Stores both player's UUID and player's name.
 * </p>
 */
public class Account {
	private static final String PREFIX = "@modern:";
	private UUID uuid;
	private String name;

	public Account(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public Account(Player player) {
		this.uuid = player.getUniqueId();
		this.name = player.getName();
	}

	public Account(HumanEntity e) {
		this.uuid = e.getUniqueId();
		this.name = e.getName();
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public Optional<Player> toPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid));
	}

	public OfflinePlayer toOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		return Objects.equals(name, other.name) && Objects.equals(uuid, other.uuid);
	}

	@Override
	public String toString() {
		return PREFIX + uuid + ":" + name;
	}

	public static Account fromString(String s) {
		// Compatibility with legacy config
		if (!s.startsWith(PREFIX)) return new Account(Bukkit.getPlayer(s));

		String[] ss = s.split("\\:", 3);
		return new Account(UUID.fromString(ss[1]), ss[2]);
	}
}
