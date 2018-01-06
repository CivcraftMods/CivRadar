package com.biggestnerd.civradar;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WaypointSave {

	private ArrayList<Waypoint> waypoints;
	
	public WaypointSave() {
		waypoints = new ArrayList<Waypoint>();
		// Why this needed?
		//waypoints.add(new Waypoint(0, 0, 0, "0,0", Color.BLACK, false));
	}
	
	public ArrayList<Waypoint> getWaypoints() {
		if(waypoints == null) {
			waypoints = new ArrayList<Waypoint>();
		}
		return waypoints;
	}
	
	public void addWaypoint(Waypoint point) {
		if(waypoints == null) {
			waypoints = new ArrayList<Waypoint>();
		}
		waypoints.add(point);
	}
	
	public void setEnabled(Waypoint point, boolean enabled) {
		for(Waypoint w : waypoints) {
			if(w.equals(point)) {
				w.setEnabled(enabled);
			}
		}
	}
	
	public void removeWaypoint(Waypoint point) {
		for(int i = 0; i < waypoints.size(); i++) {
			if(waypoints.get(i).equals(point)) {
				waypoints.remove(i);
			}
		}
	}
	
	public void save(File file) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			String json = gson.toJson(this);
			
			FileWriter fw = new FileWriter(file);
			fw.write(json);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static WaypointSave load(File file) {
		Gson gson = new Gson();
		try {
			return (WaypointSave) gson.fromJson(new FileReader(file), WaypointSave.class);
		} catch (Exception e) {
			return new WaypointSave();
		}
	}
	
	public void convertZanWayPoint() {
		File zan = new File(Minecraft.getMinecraft().mcDataDir + "/mods/VoxelMods/voxelMap/");
		if (!zan.exists()){
			System.out.println("Zan folder was not found: " + zan.getAbsolutePath());
			return;
		}
		File directory = CivRadar.waypointDir;
		List<String> previousServers = new ArrayList<String>();
		for (File x: directory.listFiles())
			previousServers.add(x.getName());
		
		for (File x: zan.listFiles()){
			if (x.isDirectory())
				continue;
			if (!x.getName().endsWith(".points"))
				continue;
			try {
				File toSave = new File(directory, "/" + x.getName());
				if (previousServers.contains(x.getName()))
					load(toSave);
				FileReader reader = new FileReader(x);
				BufferedReader r = new BufferedReader(reader);
				String line = "";
				r.readLine(); // first line is garbage
				try {
					while ((line = r.readLine()) != null){
						if (line.equals(""))
							continue;
						String[] parts = line.split(",");
						String name = getRelevantInfo(parts[0]);
						int xx = Integer.parseInt(getRelevantInfo(parts[1]));
						int zz = Integer.parseInt(getRelevantInfo(parts[2]));
						int yy = Integer.parseInt(getRelevantInfo(parts[3]));
						boolean enabled = Boolean.parseBoolean(getRelevantInfo(parts[4]));
						float rr = Float.parseFloat(getRelevantInfo(parts[5]));
						float gg = Float.parseFloat(getRelevantInfo(parts[6]));
						float bb = Float.parseFloat(getRelevantInfo(parts[7]));
						Color c = new Color(rr, gg, bb);
						int world = Integer.parseInt(getRelevantInfo(parts[10]).substring(0, 1));
						Waypoint way = new Waypoint(xx, yy, zz, name, c, enabled);
						way.setDimension(world);
						addWaypoint(way);
					}
				} catch(Exception e){
				}
				if (!toSave.exists())
					toSave.createNewFile();
				save(toSave);
				waypoints.clear();
				
				File bak = new File(x.getAbsolutePath() + ".bak");
				if (bak.exists())
					continue;
				
				r.close();
				reader.close();
				
				if(!x.renameTo(bak))
					System.out.println("bitch");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private String getRelevantInfo(String x){
		return x.split(":")[1];
	}
}
