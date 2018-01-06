package com.biggestnerd.civradar.gui;

import java.awt.Color;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import com.biggestnerd.civradar.CivRadar;
import com.biggestnerd.civradar.Waypoint;
import com.biggestnerd.civradar.WaypointSave;

public class GuiEditWaypoint extends GuiScreen {

	private GuiScreen parent;
	private Minecraft mc;
	private WaypointSave waypoints;
	private GuiTextField waypointNameField;
	private GuiTextField waypointXField;
	private GuiTextField waypointYField;
	private GuiTextField waypointZField;
	private GuiButton saveButton;
	private GuiSlider redSlider;
	private GuiSlider greenSlider;
	private GuiSlider blueSlider;
	private ArrayList<GuiTextField> textFieldList;
	private Waypoint point;
	
	public GuiEditWaypoint(Waypoint point, GuiScreen parent) {
		this.parent = parent;
		mc = Minecraft.getMinecraft();
		waypoints = CivRadar.instance.getWaypointSave();
		textFieldList = new ArrayList<GuiTextField>();
		this.point = point;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		textFieldList.add(waypointNameField = new GuiTextField(1, fontRenderer, this.width / 2 - 100, this.height / 4 - 16, 200, 20));
		textFieldList.add(waypointXField = new GuiTextField(2, fontRenderer, this.width / 2 - 100, this.height / 4 + 8, 64, 20));
		textFieldList.add(waypointYField = new GuiTextField(2, fontRenderer, this.width / 2 - 32, this.height / 4 + 8, 64, 20));
		textFieldList.add(waypointZField = new GuiTextField(2, fontRenderer, this.width / 2 + 36, this.height / 4 + 8, 64, 20));
		waypointNameField.setText(point.getName());
		waypointXField.setText(String.valueOf((int)point.getX()));
		waypointYField.setText(String.valueOf((int)point.getY()));
		waypointZField.setText(String.valueOf((int)point.getZ()));
		this.buttonList.add(redSlider = new GuiSlider(0, this.width / 2 - 100, this.height / 4 + 32, 1.0F, 0.0F, "Red", point.getRed()));
		this.buttonList.add(greenSlider = new GuiSlider(0, this.width / 2 - 100, this.height / 4 + 56, 1.0F, 0.0F, "Green", point.getGreen()));
		this.buttonList.add(blueSlider = new GuiSlider(0, this.width / 2 - 100, this.height / 4 + 80, 1.0F, 0.0F, "Blue", point.getBlue()));
		this.buttonList.add(saveButton = new GuiButton(100, this.width / 2 - 100, this.height / 4 + 152, "Save"));
		waypointNameField.setFocused(true);
	}

	@Override
	public void updateScreen() {
		redSlider.updateDisplayString();
		greenSlider.updateDisplayString();
		blueSlider.updateDisplayString();
		boolean validName = waypointNameField.getText().length() > 0;
		boolean validCoords = true;
		try {
			int x = Integer.parseInt(waypointXField.getText());
			int y = Integer.parseInt(waypointYField.getText());
			int z = Integer.parseInt(waypointZField.getText());
		} catch (Exception e) {
			validCoords = false;
		}
		if(!validName || !validCoords) {
			saveButton.enabled = false;
		} else {
			saveButton.enabled = true;
		}
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void mouseClicked(int x, int y, int mouseButton) {
		try {
			super.mouseClicked(x, y, mouseButton);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(GuiTextField field : textFieldList) {
			field.mouseClicked(x, y, mouseButton);
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if(button.enabled) {
			if(button.id == 100) {
				try {
					Color color = new Color(redSlider.getCurrentValue(), greenSlider.getCurrentValue(), blueSlider.getCurrentValue());
					CivRadar.instance.getWaypointSave().removeWaypoint(point);
					CivRadar.instance.getWaypointSave().addWaypoint(new Waypoint(Integer.parseInt(waypointXField.getText().trim()), Integer.parseInt(waypointYField.getText().trim()),
						Integer.parseInt(waypointZField.getText().trim()), waypointNameField.getText().trim(), color, true));
					CivRadar.instance.saveWaypoints();
					mc.displayGuiScreen(parent);
				} catch (Exception e) {
					e.printStackTrace();
					mc.displayGuiScreen(parent);
				}
			}
		}
	}

	@Override
	public void keyTyped(char keyChar, int keyCode) {
		for(int i = 0; i < textFieldList.size(); i++) {
			if(textFieldList.get(i).isFocused()) {
				textFieldList.get(i).textboxKeyTyped(keyChar, keyCode);
			}
		}
		if(keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(parent);
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		drawCenteredString(this.fontRenderer, "Edit Waypoint", this.width / 2, this.height / 4 - 40, Color.WHITE.getRGB());
		for(GuiTextField field : textFieldList) {
			field.drawTextBox();
		}
		Gui.drawRect(this.width / 2 - 100, this.height / 4 + 109, this.width / 2 + 100, this.height / 4 + 141, Color.BLACK.getRGB());
		Gui.drawRect(this.width / 2 - 99, this.height / 4 + 110, this.width / 2 + 99, this.height / 4 + 140, new Color(redSlider.getCurrentValue(), greenSlider.getCurrentValue(), blueSlider.getCurrentValue()).getRGB());
		super.drawScreen(i, j, k);
	}
}
