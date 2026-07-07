package com.ematrix25.immersivemobscale.scale;

import com.ematrix25.immersivemobscale.Main;
import com.ematrix25.immersivemobscale.scale.model.EntityScaleData;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Registers persistent entity scale data attachment.
 */
public class EntityScaleAttachment {
	public static final String SCALE_DATA_ID = Main.MOD_ID + ":scale_data";
	public static final AttachmentType<EntityScaleData> SCALE_DATA = AttachmentRegistry
			.create(Identifier.parse(SCALE_DATA_ID), builder -> builder.persistent(EntityScaleData.CODEC));

	/**
	 * Prevents instantiation with a private constructor.
	 */
	private EntityScaleAttachment() {
	}

	/**
	 * Forces registration of attachment types.
	 */
	public static void initialize() {
	}
}
