package com.biggestnerd.civradar.gui;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.biggestnerd.civradar.CivRadar;
import com.biggestnerd.civradar.Config;

public class GuiPlayerOptions extends GuiScreen {

	private GuiScreen parent;
	private Minecraft mc;
	private Config config;
	private GuiButton playerNamesButton;
	private GuiButton playerInfoButton;
	private GuiButton playerNamePosButton;
	private GuiSlider pingVolumeSlider;
	
	public GuiPlayerOptions(GuiScreen parent) {
		this.parent = parent;
		mc = Minecraft.getMinecraft();
		config = CivRadar.instance.getConfig();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(playerNamesButton = new GuiButton(0, this.width / 2 - 100, this.height / 4 - 20, "Player Names: " + (config.isPlayerNames() ? "Enabled" : "Disabled")));
		this.buttonList.add(playerInfoButton = new GuiButton(1, this.width / 2 - 100, this.height / 4 + 2, "Position Info: " + (config.isExtraPlayerInfo() ? "Enabled" : "Disabled")));
		this.buttonList.add(playerNamePosButton = new GuiButton(3, this.width / 2 - 100, this.height / 4 + 24, "Player Name Location: " + config.getNameLocation().toString()));
		this.buttonList.add(pingVolumeSlider = new GuiSlider(4, this.width / 2 - 100, this.height / 4 + 46, 1.0F, 0.0F, "Ping Volume", config.getPingVolume()));
		this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 68, "Done"));
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.enabled) {
			if(button.id == 0) {
				config.setPlayerNames(!config.isPlayerNames());
			}
			if(button.id == 1) {
				config.setExtraPlayerInfo(!config.isExtraPlayerInfo());
			}
			if(button.id == 2) {
				mc.displayGuiScreen(parent);
			}
			if(button.id == 3) {
				config.switchNameLocation();
			}
			CivRadar.instance.saveConfig();
		}
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void updateScreen() {
		playerNamesButton.displayString = "Player Names: " + (config.isPlayerNames() ? "Enabled" : "Disabled");
		playerInfoButton.displayString = "Position Info: " + (config.isExtraPlayerInfo() ? "Enabled" : "Disabled");
		playerNamePosButton.displayString = "Player Name Location: " + config.getNameLocation().toString();
		if(pingVolumeSlider.getCurrentValue() == 0.0F) {
			pingVolumeSlider.setDisplayString("off");
		} else {
			pingVolumeSlider.updateDisplayString();
		}
		config.setPingVolume(pingVolumeSlider.getCurrentValue());
		CivRadar.instance.saveConfig();
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		mc.fontRenderer.drawString("Player names:", this.width / 2 - 50, this.height / 4 - 20, Color.WHITE.getRGB());
		drawCenteredString(this.fontRenderer, "Player Options", this.width / 2, this.height / 4 - 40, Color.WHITE.getRGB());
		super.drawScreen(i, j, k);
	}
}
