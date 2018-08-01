package com.gmail.trubatpig.CombatLog;

import org.bukkit.entity.Player;

public class CombatTag 
{
	private final Player player;
	private int tickDuration;
	private Player targetPlayer;
	public CombatTag(Player player, Player targetPlayer, int tickDuration)
	{
		this.player = player;
		this.tickDuration = tickDuration;
		this.targetPlayer = targetPlayer;
	}
	public Player getPlayer()
	{
		return this.player;
	}
	public int getTickDuration()
	{
		return this.tickDuration;
	}
	public Player getTargetPlayer()
	{
		return this.targetPlayer;
	}
	public void setTargetPlayer(Player targetPlayer)
	{
		this.targetPlayer = targetPlayer;
	}
	public void setTickDuration(int ticks)
	{
		this.tickDuration = ticks;
	}
	public void addTickDuration(int ticks)
	{
		this.tickDuration += ticks;
	}
	public void subtractTickDuration(int ticks)
	{
		if(this.tickDuration - ticks >= 0)
		{
			this.tickDuration -= ticks;
		}
		else
		{
			return;
		}
	}
}
