package com.meekdev.openheads.client;

import com.meekdev.openheads.VoiceChatIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OpenheadsClient implements ClientModInitializer {

    public static final Map<Integer, Float> HEAD_ROTATIONS = new HashMap<>();
    private static final Map<Integer, Float> CURRENT_SCALES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.world == null) return;

        client.world.getPlayers().forEach(player -> {
            int id = player.getId();
            VoiceChatIntegration.AudioData audioData = VoiceChatIntegration.getAudioData(player.getUuid());

            float targetSquish = 0.0f;
            if (audioData != null) {
                targetSquish = normalizeVolume(audioData.volume) * 35.0f;
            }

            float currentSquish = CURRENT_SCALES.getOrDefault(id, 0.0f);
            float lerpSpeed = targetSquish > currentSquish ? 0.5f : 0.35f;
            float newSquish = lerp(currentSquish, targetSquish, lerpSpeed);

            if (Math.abs(newSquish) > 0.5f) {
                CURRENT_SCALES.put(id, newSquish);
                HEAD_ROTATIONS.put(id, newSquish);
            } else {
                CURRENT_SCALES.remove(id);
                HEAD_ROTATIONS.remove(id);
            }
        });

        Iterator<Map.Entry<Integer, Float>> iterator = CURRENT_SCALES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Float> entry = iterator.next();
            if (!isPlayerInWorld(client, entry.getKey())) {
                iterator.remove();
                HEAD_ROTATIONS.remove(entry.getKey());
            }
        }
    }

    private boolean isPlayerInWorld(MinecraftClient client, int playerId) {
        if (client.world == null) return false;
        return client.world.getPlayers().stream().anyMatch(p -> p.getId() == playerId);
    }

    private float normalizeVolume(double volumeDb) {
        double minDb = -50.0;
        double maxDb = -5.0;
        double clamped = Math.max(minDb, Math.min(maxDb, volumeDb));
        return (float) ((clamped - minDb) / (maxDb - minDb));
    }

    private float lerp(float start, float end, float alpha) {
        return start + (end - start) * alpha;
    }
}