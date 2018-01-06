package com.biggestnerd.civradar.gui;

import java.awt.Color;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import com.biggestnerd.civradar.CivRadar;
import com.biggestnerd.civradar.Config;

public class GuiRepositionRadar extends GuiScreen {
	
	private GuiScreen parentScreen;
	private Config config;
	
	public GuiRepositionRadar(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
		config = CivRadar.instance.getConfig();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(1, this.width / 2 - 101, 90, 100, 20, "Snap top left"));
		this.buttonList.add(new GuiButton(2, this.width / 2 + 1, 90, 100, 20, "Snap top right"));
		this.buttonList.add(new GuiButton(3, this.width / 2 - 101, 112, 100, 20, "Snap bottom left"));
		this.buttonList.add(new GuiButton(4, this.width / 2 + 1, 112, 100, 20, "Snap bottom right"));
		this.buttonList.add(new GuiButton(5, this.width / 2 - 100, 134, "Done"));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		CivRadar.instance.saveConfig();
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(!button.enabled) {
			return;
		}
		switch (button.id) {
			case 1:
				config.setRadarX(0);
				config.setRadarY(0);
				break;
			case 2:
				config.setRadarX(1);
				config.setRadarY(0);
				break;
			case 3:
				config.setRadarX(0);
				config.setRadarY(1);
				break;
			case 4:
				config.setRadarX(1);
				config.setRadarY(1);
				break;
			case 5:
				mc.displayGuiScreen(parentScreen);
				break;
		}
		CivRadar.instance.saveConfig();
	}

	@Override
	public void updateScreen() {
		ScaledResolution res = new ScaledResolution(mc);
		float xSpeed = 1.f / res.getScaledWidth();
		float ySpeed = 1.f / res.getScaledHeight();

		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			config.setRadarX(config.getRadarX() - xSpeed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			config.setRadarX(config.getRadarX() + xSpeed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			config.setRadarY(config.getRadarY() - ySpeed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			config.setRadarY(config.getRadarY() + ySpeed);
		}
		CivRadar.instance.saveConfig();
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawCenteredString(mc.fontRenderer, "Use arrow keys to reposition radar", this.width / 2, 80, Color.WHITE.getRGB());
		super.drawScreen(i, j, k);
	}

}
