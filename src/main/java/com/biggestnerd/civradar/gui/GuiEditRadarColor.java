package com.biggestnerd.civradar.gui;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.biggestnerd.civradar.CivRadar;
import com.biggestnerd.civradar.Config;

public class GuiEditRadarColor extends GuiScreen {

	private GuiScreen parent;
	private Minecraft mc;
	private Config config;
	private GuiSlider redSlider;
	private GuiSlider greenSlider;
	private GuiSlider blueSlider;
	
	public GuiEditRadarColor(GuiScreen parent) {
		this.parent = parent;
		this.mc = Minecraft.getMinecraft();
		this.config = CivRadar.instance.getConfig();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(redSlider = new GuiSlider(0, this.width / 2 - 100, this.height / 4 - 16, 1.0F, 0.0F, "Red", config.getRadarColor().getRed() / 255.0F));
		this.buttonList.add(greenSlider = new GuiSlider(0, this.width / 2 - 100, this.height / 4 + 8, 1.0F, 0.0F, "Green", config.getRadarColor().getGreen() / 255.0F));
		this.buttonList.add(blueSlider = new GuiSlider(0, this.width / 2 - 100, this.height / 4 + 32, 1.0F, 0.0F, "Blue", config.getRadarColor().getBlue() / 255.0F));
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 4 + 56, "Done"));
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.enabled) {
			if(button.id == 1) {
				config.setColor(Color.RED);
			}
			if(button.id == 2) {
				config.setColor(Color.GREEN);
			}
			if(button.id == 3) {
				config.setColor(Color.BLUE);
			}
			if(button.id == 4) {
				config.setColor(new Color(0.0F, 0.5F, 0.5F));
			}
			if(button.id == 100) {
				mc.displayGuiScreen(parent);
			}
		}
	}

	@Override
	public void updateScreen() {
		redSlider.updateDisplayString();
		greenSlider.updateDisplayString();
		blueSlider.updateDisplayString();
		float red = redSlider.getCurrentValue();
		float green = greenSlider.getCurrentValue();
		float blue = blueSlider.getCurrentValue();
		config.setColor(red, green, blue);
		CivRadar.instance.saveConfig();
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawCenteredString(this.fontRenderer, "Edit Radar Color", this.width / 2, this.height / 4 - 40, Color.WHITE.getRGB());
		super.drawScreen(i, j, k);
	}
}
