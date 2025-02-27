/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	private ClientWorld world;
	@Shadow
	private DynamicRegistryManager.Immutable registryManager;

	@Inject(method = "onPlayerRespawn", at = @At(value = "NEW", target = "net/minecraft/client/world/ClientWorld"))
	private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.world != null) {
			for (Entity entity : this.world.getEntities()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.world);
			}

			for (WorldChunk chunk : ((LoadedChunksCache) this.world).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.world);
				}
			}
		}
	}

	/**
	 * An explanation why we unload entities during onGameJoin:
	 * Proxies such as Waterfall may send another Game Join packet if entity meta rewrite is disabled, so we will cover ourselves.
	 * Velocity by default will send a Game Join packet when the player changes servers, which will create a new client world.
	 * Also anyone can send another GameJoinPacket at any time, so we need to watch out.
	 */
	@Inject(method = "onGameJoin", at = @At(value = "NEW", target = "net/minecraft/client/world/ClientWorld"))
	private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.world != null) {
			for (Entity entity : world.getEntities()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.world);
			}

			for (WorldChunk chunk : ((LoadedChunksCache) this.world).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.world);
				}
			}
		}
	}

	// Called when the client disconnects from a server.
	@Inject(method = "clearWorld", at = @At("HEAD"))
	private void onClearWorld(CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.world != null) {
			for (Entity entity : this.world.getEntities()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.world);
			}

			for (WorldChunk chunk : ((LoadedChunksCache) this.world).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.world);
				}
			}
		}
	}

	@Inject(
			method = "onSynchronizeTags",
			at = @At(
					value = "INVOKE",
					target = "java/util/Map.forEach(Ljava/util/function/BiConsumer;)V",
					shift = At.Shift.AFTER, by = 1
			)
	)
	private void hookOnSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
		CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(registryManager, true);
	}
}
