/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.StringHelper;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.util.ChatUtils;

@SearchTags({"mass tpa"})
@DontSaveState
public final class MassTpaHack extends Hack
	implements UpdateListener, ChatInputListener
{
	private final TextFieldSetting commandSetup = new TextFieldSetting(
			"Type of Teleport Command",
			"Write down the tpa command statement you want > tpo, tpahere, etc..",
			"tpa"
	);

	private final Random random = new Random();
	private final ArrayList<String> players = new ArrayList<>();
	
	private int index;
	private int timer;
	
	public MassTpaHack()
	{
		super("MassTPA");
		setCategory(Category.CHAT);
		addSetting(commandSetup);
	}
	
	@Override
	public void onEnable()
	{
		index = 0;
		timer = -1;
		
		players.clear();
		String playerName = MC.getSession().getUsername();
		
		for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
		{
			String name = info.getProfile().getName();
			name = StringHelper.stripTextFormat(name);
			
			if(name.equalsIgnoreCase(playerName))
				continue;
			
			players.add(name);
		}

		Collections.shuffle(players, random);
		
		EVENTS.add(ChatInputListener.class, this);
		EVENTS.add(UpdateListener.class, this);
		
		if(players.isEmpty())
		{
			errorComment("Couldn't find any players.");
		}

		if (!commandSetup.getValue().contains("tp"))
		{
			errorComment("Commands other than tp are not available.");
		}
	}

	private void errorComment(String comment) {
		ChatUtils.error(comment);
		setEnabled(false);
	}

	@Override
	public void onDisable()
	{
		EVENTS.remove(ChatInputListener.class, this);
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(timer > -1)
		{
			timer--;
			return;
		}
		
		if(index >= players.size())
		{
			setEnabled(false);
			return;
		}

		String commandLabel = this.commandSetup.getValue() + " ";
 		MC.getNetworkHandler().sendChatCommand(commandLabel + players.get(index));

		index++;
		timer = 20;
	}

	@Override
	public void onReceivedMessage(ChatInputEvent event)
	{
		String message = event.getComponent().getString().toLowerCase();
		if(message.startsWith("\u00a7c[\u00a76wurst\u00a7c]"))
			return;
		
		if(message.contains("/help") || message.contains("permission"))
		{
			event.cancel();
			sendChatting("This server doesn't have " + this.commandSetup.getValue() + ".");
			setEnabled(false);
			
		}else if(message.contains("accepted") && message.contains("request")
			|| message.contains("akzeptiert") && message.contains("anfrage"))
		{
			event.cancel();
			sendChatting("Someone accepted. Stopping.");
			setEnabled(false);
		}
	}

	private void sendChatting(String comment) {
		ChatUtils.error(comment);
	}
}
