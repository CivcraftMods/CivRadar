package com.biggestnerd.civradar;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import com.biggestnerd.civradar.gui.GuiAddWaypoint;
import com.biggestnerd.civradar.gui.GuiRadarOptions;

@Mod(modid=CivRadar.MODID, name=CivRadar.MODNAME, version=CivRadar.VERSION)
public class CivRadar {
	public final static String MODID = "civradar";
	public final static String MODNAME = "CivRadar";
	public final static String VERSION = "@VERSION@";
	private RenderHandler renderHandler;
	private Config radarConfig;
	private File configFile;
	private KeyBinding radarOptions = new KeyBinding("CivRadar Settings", Keyboard.KEY_R, "CivRadar");
	private KeyBinding addWaypoint = new KeyBinding("Add Waypoint", Keyboard.KEY_P, "CivRadar");
	Minecraft mc;
	public static CivRadar instance;
	private WaypointSave currentWaypoints;
	private File saveFile;
	public static String currentServer = "";
	public static File waypointDir;
	private File radarDir;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		mc = Minecraft.getMinecraft();
		instance = this;
		File oldConfig = new File(event.getModConfigurationDirectory(), "civRadar.json");
		radarDir = new File(mc.mcDataDir, "/civradar/");
		if(!radarDir.isDirectory()) {
			radarDir.mkdir();
		}
		configFile = new File(radarDir, "config.json");
		if(oldConfig.exists()) {
			try {
				FileUtils.copyFile(oldConfig, configFile);
				oldConfig.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!configFile.isFile()) {
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
			radarConfig = new Config();
			radarConfig.save(configFile);
		} else {
			radarConfig = Config.load(configFile);
			if(radarConfig == null) {
				radarConfig = new Config();
			}
			radarConfig.save(configFile);
		}
		renderHandler = new RenderHandler();
		
		waypointDir = new File(radarDir, "/waypoints/");
		if(!waypointDir.isDirectory()) {
			waypointDir.mkdir();
		}
		currentWaypoints = new WaypointSave();
		currentWaypoints.convertZanWayPoint();
		
		MinecraftForge.EVENT_BUS.register(renderHandler);
    	MinecraftForge.EVENT_BUS.register(this);
		
		ClientRegistry.registerKeyBinding(radarOptions);
		ClientRegistry.registerKeyBinding(addWaypoint);
	}
	
	@SubscribeEvent
	public void keyPress(KeyInputEvent event) {
		if(radarOptions.isPressed()) {
			mc.displayGuiScreen(new GuiRadarOptions(mc.currentScreen));
		}
		if(addWaypoint.isPressed()) {
			mc.displayGuiScreen(new GuiAddWaypoint(mc.currentScreen));
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(mc.world != null) {
			if(mc.isSingleplayer()) {
				String worldName = mc.getIntegratedServer().getWorldName();
				if(worldName == null) {
					return;
				}
				if(!currentServer.equals(worldName)) {
					currentServer = worldName;
					loadWaypoints(new File(waypointDir, worldName + ".points"));
				}
			} else if (mc.getCurrentServerData() != null) {
				if(!currentServer.equals(mc.getCurrentServerData().serverIP)) {
					currentServer = mc.getCurrentServerData().serverIP;
					loadWaypoints(new File(waypointDir, currentServer + ".points"));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onDisconnect(ClientDisconnectionFromServerEvent event) {
		currentServer = "";
	}
	
	public void loadWaypoints(File saveFile) {
		if(!saveFile.isFile()) {
			try {
				saveFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
			currentWaypoints.save(saveFile);
		} else {
			currentWaypoints = WaypointSave.load(saveFile);
			currentWaypoints.save(saveFile);
			this.saveFile = saveFile;
		}
	}
	
	public Config getConfig() {
		return radarConfig;
	}
	
	public void saveConfig() {
		radarConfig.save(configFile);
	}
	
	public WaypointSave getWaypointSave() {
		return currentWaypoints;
	}
	
	public void saveWaypoints() {
		currentWaypoints.save(saveFile);
	}
}
