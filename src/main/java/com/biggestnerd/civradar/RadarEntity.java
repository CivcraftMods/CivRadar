package com.biggestnerd.civradar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RadarEntity {

	private static final Logger logger = LogManager.getLogger();
	private static final String[] HORSE_VARIANTS = {"white", "creamy", "chestnut", "brown", "black", "gray", "darkbrown"};

	private final String className;
	private boolean enabled = true;

	public RadarEntity(Class entityClass) {
		className = entityClass.getName();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Class<? extends Entity> getEntityClass() {
		try {
			return (Class<? extends Entity>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return Entity.class;
	}

	public String getName() {
		return className;
	}

	public String getEntityName() {
		String[] className = this.className.split("\\.");
		return className[className.length - 1].substring(6);
	}

	public static ResourceLocation getResourceJM(Entity entity) {
		try {
			FMLClientHandler instance = FMLClientHandler.instance();
			Minecraft client = instance.getClient();
			RenderManager renderManager = client.getRenderManager();
			Render render = renderManager.getEntityRenderObject(entity);
			ResourceLocation original;

			if (render instanceof RenderHorse) {
				EntityHorse horseEntity = (EntityHorse) entity;
				int horseVariant = 0xff & horseEntity.getHorseVariant();

				original = new ResourceLocation("minecraft", "textures/entity/horse/horse_" + HORSE_VARIANTS[horseVariant] + ".png");
			} else if(render instanceof RenderLlama) {
				original = new ResourceLocation("minecraft", "textures/entity/llama/llama.png");
			} else if(render instanceof RenderParrot) {
				original = new ResourceLocation("minecraft", "textures/entity/parrot/parrot.png");
			} else if(render instanceof RenderShulker) {
				original = new ResourceLocation("minecraft", "textures/entity/shulker/shulker.png");
			} else {
				original = RadarHelper.getEntityTexture(render, entity);
			}

			return new ResourceLocation(original.getResourceDomain(), original.getResourcePath().replace("textures/entity/", "icons/"));

		} catch (Throwable e) {
			logger.error("Can't get entityTexture for " + entity.getName(), e);
			return null;
		}
	}
}
