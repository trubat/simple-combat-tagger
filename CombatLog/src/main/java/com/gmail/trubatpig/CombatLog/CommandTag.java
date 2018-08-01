package com.gmail.trubatpig.CombatLog;

import org.bukkit.entity.Player;

public class CommandTag
{
	private final Player player;
	private int duration;
	public CommandTag(Player player, int duration)
	{
		this.player = player;
		this.duration = duration;
	}
	public int getTickDuration()
	{
		return this.duration;
	}
	public void subtractTickDuration(int ticks)
	{
		this.duration -= ticks;
	}
	public Player getPlayer()
	{
		return this.player;
	}
}
