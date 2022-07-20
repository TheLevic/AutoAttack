package com.thelevic.autoattack;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.mob.MobEntity;


import net.minecraft.item.ItemStack;

import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import org.lwjgl.glfw.GLFW;

public class AutoAttack implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	private static boolean isActive = false;
	public static final String MOD_ID = "autoattack";

	private static final KeyBinding keyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding("Toggle Auto Attack", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_COMMA,"Auto Attack"));


	public AutoAttack(){
		AutoAttack INSTANCE = this;
	}

	private void toggleActive(){
		if (keyBind.wasPressed()){
			isActive = !isActive;
		}
	}

	private boolean isCooldownFinished(MinecraftClient mc){
		try{
			assert mc.player != null;
			return mc.player.getAttackCooldownProgress(0) == 1.0F;
		} catch (Exception e){
			return false;
		}
	}

	private int checkDurability(MinecraftClient mc){
		ItemStack itemStack = null;
		if (mc.player != null) {
			itemStack = mc.player.getMainHandStack();
		}
		int damage = 0;
		if (itemStack != null) {
			damage = itemStack.getDamage();
		}
		int maxDamage = 0;
		if (itemStack != null) {
			maxDamage = itemStack.getMaxDamage();
		}
		return maxDamage - damage;
	}



	private void attackMob(MinecraftClient mc){
		HitResult rayTrace = mc.crosshairTarget;
		if (isActive){
			if (rayTrace instanceof EntityHitResult && mc.interactionManager != null  && ((EntityHitResult) rayTrace).getEntity() instanceof MobEntity){
				if (isCooldownFinished(mc)){
					if (checkDurability(mc) <= 20){
						isActive = false;
					}
					mc.interactionManager.attackEntity(mc.player,((EntityHitResult) rayTrace).getEntity());

				}

			}
		}

	}
	private void clickTickEvent(MinecraftClient mc){
		toggleActive();
		attackMob(mc);
	}
	@Override
	public void onInitialize() {
		ClientTickEvents.END_CLIENT_TICK.register(this::clickTickEvent);
	}
}
