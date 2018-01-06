package com.biggestnerd.civradar.gui;

import java.awt.Color;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.biggestnerd.civradar.CivRadar;
import com.biggestnerd.civradar.Config;

public class GuiRadarOptions extends GuiScreen {

	private GuiScreen parentScreen;
	private GuiSlider opacitySlider;
	private GuiSlider scaleSlider;
	private GuiButton coordToggle;
	private GuiButton radarButton;
	private GuiSlider radarDistanceSlider;
	private GuiSlider iconScaleSlider;

	public GuiRadarOptions(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		this.buttonList.clear();
		int y = this.height / 4 - 16;
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, y, 100, 20, "Reposition Radar"));
		this.buttonList.add(new GuiButton(4, this.width / 2 + 1, y, 100, 20, "Edit Radar Color"));
		y += 24;
		this.buttonList.add(opacitySlider = new GuiSlider(3, this.width / 2 -100, y, 1, 0, "Radar Opacity", CivRadar.instance.getConfig().getRadarOpacity()));
		y += 24;
		this.buttonList.add(scaleSlider = new GuiSlider(6, this.width / 2 - 100, y, 1, .1f, "Radar Size", CivRadar.instance.getConfig().getRadarSize()));
		y += 24;
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, y, 100, 20, "Icon Settings"));
		this.buttonList.add(new GuiButton(5, this.width / 2 + 1, y, 100, 20, "Edit Player Options"));
		y += 24;
		int radarDistance = CivRadar.instance.getConfig().getRadarDistance();
		this.buttonList.add(radarDistanceSlider = new GuiSlider(10, this.width / 2 - 100, y, 7, 3, "Radar Range", (float) (Math.log(radarDistance) / Math.log(2))));
		y += 24;
		this.buttonList.add(iconScaleSlider = new GuiSlider(11, this.width / 2 - 100, y, radarDistance, .5f, "Icon Scale", CivRadar.instance.getConfig().getIconScale()));
		y += 24;
		this.buttonList.add(coordToggle = new GuiButton(7, this.width / 2 - 100, y, 100, 20, "Coordinates: "));
		this.buttonList.add(new GuiButton(8, this.width / 2 + 1, y, 100, 20, "Waypoint Shizz"));
		y += 24;
		this.buttonList.add(radarButton = new GuiButton(9, this.width / 2 - 100, y, 100, 20, "Radar: "));
		this.buttonList.add(new GuiButton(100, this.width / 2 + 1, y, 100, 20, "Done"));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		CivRadar.instance.saveConfig();
	}

	@Override
	public void actionPerformed(GuiButton guiButton) {
		if(!guiButton.enabled)
			return;
		int id = guiButton.id;
		if(id == 0) {
			mc.displayGuiScreen(new GuiRepositionRadar(this));
		}
		if(id == 1) {
			mc.displayGuiScreen(new GuiEntitySettings(this));
		}
		if(id == 4) {
			mc.displayGuiScreen(new GuiEditRadarColor(this));
		}
		if(id == 5) {
			mc.displayGuiScreen(new GuiPlayerOptions(this));
		}
		if(id == 7) {
			CivRadar.instance.getConfig().setRenderCoordinates(!CivRadar.instance.getConfig().isRenderCoordinates());
			CivRadar.instance.saveConfig();
		}
		if(id == 8) {
			mc.displayGuiScreen(new GuiWaypointOptions(this));
		}
		if(id == 9) {
			CivRadar.instance.getConfig().setEnabled(!CivRadar.instance.getConfig().isEnabled());
			CivRadar.instance.saveConfig();
		}
		if(id == 100) {
			mc.displayGuiScreen(parentScreen);
		}
	}

	public void updateScreen() {
		Config config = CivRadar.instance.getConfig();
		config.setRadarOpacity(opacitySlider.getCurrentValue());
		config.setRadarSize(scaleSlider.getCurrentValue());
		config.setRadarDistance((int) Math.pow(2, radarDistanceSlider.getCurrentValue()));
		config.setIconScale(iconScaleSlider.getCurrentValue());
		CivRadar.instance.saveConfig();

		iconScaleSlider.maxValue = config.getRadarDistance();

		coordToggle.displayString = "Coordinates: " + (CivRadar.instance.getConfig().isRenderCoordinates() ? "On" : "Off");

		radarButton.displayString = "Radar: " + (CivRadar.instance.getConfig().isEnabled() ? "On" : "Off");
		opacitySlider.updateDisplayString();
		scaleSlider.updateDisplayString();
		iconScaleSlider.updateDisplayString();
		radarDistanceSlider.setDisplayString("" + config.getRadarDistance());
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		drawCenteredString(this.fontRenderer, "CivRadar Options", this.width / 2, this.height / 4 - 40, Color.WHITE.getRGB());
		super.drawScreen(i, j, k);
	}
}
