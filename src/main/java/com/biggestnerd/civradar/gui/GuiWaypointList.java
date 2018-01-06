package com.biggestnerd.civradar.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;

import com.biggestnerd.civradar.CivRadar;
import com.biggestnerd.civradar.Waypoint;

public class GuiWaypointList extends GuiScreen {
	
	private final GuiScreen parent;
	private ArrayList<Waypoint> waypointList;
	private int selected = -1;
	private GuiButton enableButton;
	private GuiButton disableButton;
	private GuiButton editButton;
	private GuiButton deleteButton;
	private WaypointList waypointListContainer;
	
	public GuiWaypointList(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(enableButton = new GuiButton(0, this.width / 2 - 100, this.height - 63, 64, 20, "Enable"));
		this.buttonList.add(disableButton = new GuiButton(1, this.width / 2 - 32, this.height - 63, 64, 20, "Disable"));
		this.buttonList.add(editButton = new GuiButton(2, this.width / 2 + 36, this.height - 63, 64, 20, "Edit"));
		this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height - 42, 64, 20, "Enable All"));
		this.buttonList.add(new GuiButton(4, this.width / 2 - 32, this.height - 42, 64, 20, "Disable All"));
		this.buttonList.add(deleteButton = new GuiButton(5, this.width / 2 + 36, this.height - 42, 64, 20, "Delete"));
		this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height - 21, "Done"));
		this.waypointListContainer = new WaypointList(this.mc);
		this.waypointListContainer.registerScrollButtons(4, 5);
		editButton.enabled = false;
		enableButton.enabled = false;
		disableButton.enabled = false;
		deleteButton.enabled = false;
		this.waypointList = CivRadar.instance.getWaypointSave().getWaypoints();
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.waypointListContainer.handleMouseInput();
	}

	private void enableOrDisableSelectedWaypoint(boolean enabled) {
		Waypoint point = waypointList.get(selected);
		CivRadar.instance.getWaypointSave().setEnabled(point, enabled);
		CivRadar.instance.saveWaypoints();
	}
	
	private void enableOrDisableAllWaypoints(boolean enabled) {
		for(Waypoint point : waypointList) {
			CivRadar.instance.getWaypointSave().setEnabled(point, enabled);
		}
		CivRadar.instance.saveWaypoints();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException	 {
		if(button.enabled) {
			if(button.id == 0) {
				enableOrDisableSelectedWaypoint(true);
			}
			if(button.id == 1) {
				enableOrDisableSelectedWaypoint(false);
			}
			if(button.id == 2) {
				mc.displayGuiScreen(new GuiEditWaypoint(waypointList.get(selected), this));
			}
			if(button.id == 3) {
				enableOrDisableAllWaypoints(true);
			}
			if(button.id == 4) {
				enableOrDisableAllWaypoints(false);
			}
			if(button.id == 5) {
				CivRadar.instance.getWaypointSave().removeWaypoint(waypointList.get(selected));
				CivRadar.instance.saveWaypoints();
			}
			if(button.id == 100) {
				mc.displayGuiScreen(parent);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.waypointListContainer.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, "Waypoint List", this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

	@Override
	public void updateScreen() {
		this.waypointList = CivRadar.instance.getWaypointSave().getWaypoints();

	}
	
	class WaypointList extends GuiSlot {
		
		public WaypointList(Minecraft mc) {
			super(mc, GuiWaypointList.this.width, GuiWaypointList.this.height, 32, GuiWaypointList.this.height - 64, 36);
		}

		@Override
		protected int getSize() {
			return GuiWaypointList.this.waypointList.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			GuiWaypointList.this.selected = slotIndex;
			boolean isValidSlot = slotIndex >= 0 && slotIndex < getSize();
			GuiWaypointList.this.enableButton.enabled = isValidSlot;
			GuiWaypointList.this.disableButton.enabled = isValidSlot;
			GuiWaypointList.this.editButton.enabled = isValidSlot;
			GuiWaypointList.this.deleteButton.enabled = isValidSlot;
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == GuiWaypointList.this.selected;
		}

		@Override
		protected int getContentHeight() {
			return getSize() * 36;
		}

		@Override
		protected void drawBackground() {
			GuiWaypointList.this.drawDefaultBackground();
		}

		@Override
		protected void drawSlot(int entryId, int par2, int par3, int par4, int par5, int par6, float par7) {
			Waypoint point = GuiWaypointList.this.waypointList.get(entryId);
			GuiWaypointList.this.drawString(mc.fontRenderer, point.getName(), par2 + 1, par3 + 1, Color.WHITE.getRGB());
			String dimension = "null";
			if(point.getDimension() == 0) {
				dimension = "overworld";
			} else if(point.getDimension() == -1) {
				dimension = "nether";
			} else if(point.getDimension() == 1) {
				dimension = "nether";
			}
			String coords = "(" + (int)point.getX() + "," + (int)point.getY() + "," + (int)point.getZ() + ") Dimension: " + dimension;
			GuiWaypointList.this.drawString(mc.fontRenderer, coords, par2 + 1, par3 + 13, point.getColor().getRGB());
			GuiWaypointList.this.drawString(mc.fontRenderer, point.isEnabled() ? "Enabled" : "Disabled", par2 + 215 - mc.fontRenderer.getStringWidth("Disabled"), par3 + 1, point.isEnabled() ? Color.GREEN.getRGB() : Color.RED.getRGB());
		}
	}
}
