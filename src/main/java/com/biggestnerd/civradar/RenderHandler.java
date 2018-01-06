package com.biggestnerd.civradar;

import com.biggestnerd.civradar.Config.NameLocation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.biggestnerd.civradar.RadarEntity.getResourceJM;
import static com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN;

public class RenderHandler extends Gui {

	private Config config = CivRadar.instance.getConfig();
	private Minecraft mc = Minecraft.getMinecraft();
	private int pingTicks = 0;
	private List<Entity> entityList;
	private ArrayList<String> inRangePlayers = new ArrayList<String>();
	private float iconScale;
	private float radarScale;

	@SubscribeEvent
	public void renderRadar(RenderGameOverlayEvent event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
			return;
		}
		if (config.isEnabled()) {
			drawRadar();
		}
	}

	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START && mc.world != null) {
			pingTicks -= 1;
			entityList = mc.world.loadedEntityList;
			ArrayList<String> newInRangePlayers = new ArrayList<String>();
			for (Entity e : entityList) {
				if (e instanceof EntityOtherPlayerMP) {
					newInRangePlayers.add(e.getName());
				}
			}
			ArrayList<String> updatedInRangePlayers = (ArrayList<String>) newInRangePlayers.clone();
			newInRangePlayers.removeAll(inRangePlayers);
			inRangePlayers = updatedInRangePlayers;

			for (String name : newInRangePlayers) {
				float playerPitch = .5f + 1.5f * new Random(name.hashCode()).nextFloat(); // unique for each player, but always the same
				mc.player.playSound(new SoundEvent(new ResourceLocation("block.note.pling")), config.getPingVolume(), playerPitch);
				pingTicks = 20;
			}
		}
	}

	@SubscribeEvent
	public void renderWaypoints(RenderWorldLastEvent event) {
		if (CivRadar.instance.getWaypointSave() == null) {
			return;
		}
		if (config.isRenderWaypoints()) {
			for (Waypoint point : CivRadar.instance.getWaypointSave().getWaypoints()) {
				if (point.getDimension() == mc.world.provider.getDimension() && point.isEnabled()) {
					renderWaypoint(point, event);
				}
			}
		}
	}

	private void drawRadar() {
		Color radarColor = config.getRadarColor();

		ScaledResolution res = new ScaledResolution(mc);

		int radarDisplayDiameter = (int) ((res.getScaledHeight() - 2) * config.getRadarSize());
		int radarDisplayRadius = radarDisplayDiameter / 2;
		radarScale = (float) radarDisplayRadius / config.getRadarDistance();
		iconScale = config.getIconScale() / 8;

		// top/bottom and left/right (0 or 1 x and y) means touching the window frame
		int windowInnerWidth = res.getScaledWidth() - radarDisplayDiameter;
		int windowInnerHeight = res.getScaledHeight() - radarDisplayDiameter;

		int radarDisplayX = radarDisplayRadius + 1 + (int) (config.getRadarX() * (windowInnerWidth - 2));
		int radarDisplayY = radarDisplayRadius + 1 + (int) (config.getRadarY() * (windowInnerHeight - 2));

		GlStateManager.pushMatrix();
		GlStateManager.translate(radarDisplayX, radarDisplayY, 0);

		if (config.isRenderCoordinates()) {
			String coords = "(" + (int) Math.floor(mc.player.posX) + "," + (int) Math.floor(mc.player.posY) + "," + (int) Math.floor(mc.player.posZ) + ")";
			int  stringX = -(mc.fontRenderer.getStringWidth(coords) / 2);
			mc.fontRenderer.drawStringWithShadow(coords, stringX, radarDisplayRadius, 0xe0e0e0);
		}

		GlStateManager.rotate(-mc.player.rotationYaw, 0.0F, 0.0F, 1.0F);

		// background
		drawCircle(0, 0, radarDisplayRadius, radarColor, true);
		GlStateManager.glLineWidth(2.0f);

		// border
		drawCircle(0, 0, radarDisplayRadius, radarColor, false);
		GlStateManager.glLineWidth(1.0f);

		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();

		// eight concentric lines
		GlStateManager.glBegin(GL11.GL_LINES);
		final float cos45 = 0.7071f;
		float diagonalInner = cos45 * radarScale;
		float diagonalOuter = cos45 * radarDisplayRadius;
		GlStateManager.glVertex3f(0, -radarDisplayRadius, 0f);
		GlStateManager.glVertex3f(0, -radarScale, 0f);
		GlStateManager.glVertex3f(0, radarScale, 0f);
		GlStateManager.glVertex3f(0, radarDisplayRadius, 0f);

		GlStateManager.glVertex3f(-radarDisplayRadius, 0, 0f);
		GlStateManager.glVertex3f(-radarScale, 0, 0f);
		GlStateManager.glVertex3f(radarScale, 0, 0f);
		GlStateManager.glVertex3f(radarDisplayRadius, 0, 0f);

		GlStateManager.glVertex3f(-diagonalOuter, -diagonalOuter, 0f);
		GlStateManager.glVertex3f(-diagonalInner, -diagonalInner, 0f);
		GlStateManager.glVertex3f(diagonalInner, diagonalInner, 0f);
		GlStateManager.glVertex3f(diagonalOuter, diagonalOuter, 0f);

		GlStateManager.glVertex3f(-diagonalOuter, diagonalOuter, 0f);
		GlStateManager.glVertex3f(-diagonalInner, diagonalInner, 0f);
		GlStateManager.glVertex3f(diagonalInner, -diagonalInner, 0f);
		GlStateManager.glVertex3f(diagonalOuter, -diagonalOuter, 0f);

		GlStateManager.glEnd();

		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();

		drawCircle(0, 0, radarScale, radarColor, false);

		GlStateManager.pushMatrix();
		GlStateManager.scale(radarScale, radarScale, radarScale);
		drawRadarIcons();

		GlStateManager.popMatrix();

		// player location
		GlStateManager.rotate(mc.player.rotationYaw, 0.0F, 0.0F, 1.0F);
		drawTriangle(0, 0, Color.WHITE);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	private void drawCircle(int x, int y, double radius, Color c, boolean filled) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, filled ? config.getRadarOpacity() : config.getRadarOpacity() + 0.5F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(filled ? GL11.GL_TRIANGLE_FAN : GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		for (int i = 0; i <= 360; i++) {
			double x2 = Math.sin(i * Math.PI / 180.0D) * radius;
			double y2 = Math.cos(i * Math.PI / 180.0D) * radius;
			buffer.pos(x + x2, y + y2, 0.0D).endVertex();
		}
		tessellator.draw();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	private void drawTriangle(int x, int y, Color c) {
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, config.getRadarOpacity() + 0.5F);
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
		buffer.pos(x, y + 3, 0.0D).endVertex();
		buffer.pos(x + 3, y - 3, 0.0D).endVertex();
		buffer.pos(x - 3, y - 3, 0.0D).endVertex();
		tessellator.draw();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.rotate(-180.0F, 0.0F, 0.0F, 1.0F);
	}

	private void drawRadarIcons() {
		if (entityList == null) {
			return;
		}
		for (Entity e : entityList) {
			if (mc.player.getDistanceSq(e.posX, mc.player.posY, e.posZ) > config.getRadarDistance() * config.getRadarDistance())
				continue; // outside radar range
			if (e == mc.player)
				continue;

			double playerPosX = mc.player.posX;
			double playerPosZ = mc.player.posZ;
			double entityPosX = e.posX;
			double entityPosZ = e.posZ;
			double displayPosX = playerPosX - entityPosX;
			double displayPosZ = playerPosZ - entityPosZ;
			if (e instanceof EntityItem) {
				EntityItem item = (EntityItem) e;
				if (config.isRender(EntityItem.class)) {
					renderItemIcon(displayPosX, displayPosZ, item.getItem());
				}
			} else if (e instanceof EntityOtherPlayerMP) {
				if (config.isRender(EntityPlayer.class)) {
					EntityOtherPlayerMP eop = (EntityOtherPlayerMP) e;
					try {
						renderPlayerHeadIcon(displayPosX, displayPosZ, eop);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else if (e instanceof EntityBoat) {
				if (config.isRender(EntityBoat.class)) {
					ItemStack boat = new ItemStack(Items.BOAT);
					renderItemIcon(displayPosX, displayPosZ, boat);
				}
			} else if (e instanceof EntityMinecart) {
				if (config.isRender(EntityMinecart.class)) {
					ItemStack cart = new ItemStack(Items.MINECART);
					renderItemIcon(displayPosX, displayPosZ, cart);
				}
			} else if (config.isRender(e.getClass())) {
				ResourceLocation resource = getResourceJM(e);
				if (resource != null) {
					renderIcon(displayPosX, displayPosZ, resource);
				}
			}
		}
	}

	private void renderItemIcon(double x, double y, ItemStack item) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.rotate(mc.player.rotationYaw, 0.0F, 0.0F, 1.0F);
		GlStateManager.scale(iconScale, iconScale, iconScale);

		mc.getRenderItem().renderItemIntoGUI(item, -8, -8);
		GlStateManager.disableLighting();

		GlStateManager.popMatrix();
	}

	private void renderPlayerHeadIcon(double x, double y, EntityOtherPlayerMP player) throws Exception {
		GlStateManager.color(1.0F, 1.0F, 1.0F, config.getIconOpacity());
		GlStateManager.enableBlend();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.rotate(mc.player.rotationYaw, 0.0F, 0.0F, 1.0F);

		GlStateManager.pushMatrix();
		GlStateManager.scale(iconScale, iconScale, iconScale);

		try {
			GameProfile gameProfile = player.getGameProfile();
			Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> texMap = mc.getSkinManager().loadSkinFromCache(gameProfile);
			if (texMap.containsKey(SKIN)) {
				MinecraftProfileTexture profileTexture = texMap.get(SKIN);
				this.mc.getTextureManager().bindTexture(mc.getSkinManager().loadSkin(profileTexture, SKIN));
				Gui.drawScaledCustomSizeModalRect(-8, -8, 8, 8, 8, 8, 16, 16, 64, 64);
				if (player.isWearing(EnumPlayerModelParts.HAT)) {
					Gui.drawScaledCustomSizeModalRect(-8, -8, 40, 8, 8, 8, 16, 16, 64, 64);
				}
			} else {
				this.mc.getTextureManager().bindTexture(new ResourceLocation("civRadar/entity/player.png"));
				Gui.drawScaledCustomSizeModalRect(-8, -8, 0, 0, 8, 8, 16, 16, 8, 8);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		if (config.isPlayerNames()) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(1 / radarScale, 1 / radarScale, 1 / radarScale);

			String playerName = player.getName();
			if (config.isExtraPlayerInfo()) {
				playerName += " (" + (int) mc.player.getDistanceToEntity(player) + "m)(Y" + (int) player.posY + ")";
			}
			int yOffset = -4 + (int) ((config.getIconScale() * radarScale + 8) * (config.getNameLocation() == NameLocation.below ? 1 : -1));
			drawCenteredString(mc.fontRenderer, playerName, 0, yOffset, Color.WHITE.getRGB());

			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

	private void renderIcon(double x, double y, ResourceLocation resource) {
		mc.getTextureManager().bindTexture(resource);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, config.getIconOpacity());
		GlStateManager.enableBlend();

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, 0);
		GL11.glRotatef(mc.player.rotationYaw, 0.0F, 0.0F, 1.0F);
		GlStateManager.scale(iconScale, iconScale, iconScale);
		drawModalRectWithCustomSizedTexture(-8, -8, 0, 0, 16, 16, 16, 16);

		GL11.glPopMatrix();

		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
	}

	private void renderWaypoint(Waypoint point, RenderWorldLastEvent event) {
		// TODO update rendering
		// when refactoring the rendering code, I did not test this, so it's probably not being displayed correctly
		String name = point.getName();
		Color c = point.getColor();
		float partialTickTime = event.getPartialTicks();
		double distance = point.getDistance(mc);
		int maxView = mc.gameSettings.renderDistanceChunks * 22;
		if (distance <= config.getMaxWaypointDistance() || config.getMaxWaypointDistance() < 0) {
			FontRenderer fr = mc.fontRenderer;
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder vb = tess.getBuffer();
			RenderManager rm = mc.getRenderManager();

			float playerX = (float) (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTickTime);
			float playerY = (float) (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTickTime);
			float playerZ = (float) (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTickTime);

			float displayX = (float) point.getX() - playerX;
			float displayY = (float) point.getY() + 1.3f - playerY;
			float displayZ = (float) point.getZ() - playerZ;

			// z is what would typically be y here btw
			// The ever so lovely Laura [REDACTED] is a math godess
			// While she did not provide the math used here, she is still very
			// good at math
			// Also she was very helpful in me figuring this math out for myself
			// I hope this makes her happy
			if (distance > maxView) {
				float slope = displayZ / displayX;
				displayX = Math.abs((float) Math.sqrt(Math.pow(maxView, 2) / (1 + Math.pow(slope, 2)))) * (point.getX() < 0 ? -1 : 1);
				displayZ = slope * displayX;
			}

			float scale = 0.45F * (float) (Math.max(10.0D, Math.min(distance, maxView)) / 120);// old scaling math: (float) (Math.max(2, distance /5) * 0.0185f);

			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glPushMatrix();
			GL11.glTranslatef(displayX, displayY, displayZ);
			GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-scale, -scale, scale);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			name += " (" + (int) distance + "m)";
			int width = fr.getStringWidth(name);
			int height = 10;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			int stringMiddle = width / 2;
			vb.color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, config.getWaypointOpcaity());
			vb.pos(-stringMiddle - 1, -1, 0.0D);
			vb.pos(-stringMiddle - 1, 1 + height, 0.0D);
			vb.pos(stringMiddle + 1, 1 + height, 0.0D);
			vb.pos(stringMiddle + 1, -1, 0.0D);
			tess.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			fr.drawString(name, -width / 2, 1, Color.WHITE.getRGB());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
	}
}
