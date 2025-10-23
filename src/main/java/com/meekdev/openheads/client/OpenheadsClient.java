package com.meekdev.openheads.client;

import com.meekdev.openheads.VoiceChatIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OpenheadsClient implements ClientModInitializer {

    public static final Map<Integer, SquishData> HEAD_SQUISH = new HashMap<>();

    public static class SquishData {
        public float xScale;
        public float yScale;
        public float zScale;

        public SquishData(float x, float y, float z) {
            this.xScale = x;
            this.yScale = y;
            this.zScale = z;
        }
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.world == null) return;

        client.world.getPlayers().forEach(player -> {
            int id = player.getId();
            VoiceChatIntegration.AudioData audioData = VoiceChatIntegration.getAudioData(player.getUuid());

            SquishData targetSquish = new SquishData(0.0f, 0.0f, 0.0f);

            if (audioData != null) {
                float volumeIntensity = calculateVolumeIntensity(audioData.volume);
                float pitchFactor = calculatePitchFactor(audioData.pitch);

                float horizontalScale = volumeIntensity * 0.4f * pitchFactor;

                targetSquish.xScale = horizontalScale;
                targetSquish.yScale = -volumeIntensity * 0.3f * (2.0f - pitchFactor);
                targetSquish.zScale = horizontalScale;
            }

            SquishData current = HEAD_SQUISH.getOrDefault(id, new SquishData(0.0f, 0.0f, 0.0f));

            float lerpSpeed = isIncreasing(current, targetSquish) ? 0.6f : 0.4f;

            float newX = lerp(current.xScale, targetSquish.xScale, lerpSpeed);
            float newY = lerp(current.yScale, targetSquish.yScale, lerpSpeed);
            float newZ = lerp(current.zScale, targetSquish.zScale, lerpSpeed);

            if (Math.abs(newX) > 0.01f || Math.abs(newY) > 0.01f || Math.abs(newZ) > 0.01f) {
                HEAD_SQUISH.put(id, new SquishData(newX, newY, newZ));
            } else {
                HEAD_SQUISH.remove(id);
            }
        });

        Iterator<Map.Entry<Integer, SquishData>> iterator = HEAD_SQUISH.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, SquishData> entry = iterator.next();
            if (!isPlayerInWorld(client, entry.getKey())) {
                iterator.remove();
            }
        }
    }

    private boolean isPlayerInWorld(MinecraftClient client, int playerId) {
        if (client.world == null) return false;
        return client.world.getPlayers().stream().anyMatch(p -> p.getId() == playerId);
    }

    private float calculateVolumeIntensity(double volumeDb) {
        double minDb = -50.0;
        double maxDb = -5.0;
        double clamped = Math.max(minDb, Math.min(maxDb, volumeDb));
        float normalized = (float) ((clamped - minDb) / (maxDb - minDb));

        if (normalized < 0.3f) {
            return normalized * 0.5f;
        } else if (normalized < 0.7f) {
            return 0.15f + (normalized - 0.3f) * 1.5f;
        } else {
            return 0.75f + (normalized - 0.7f) * 2.5f;
        }
    }

    private float calculatePitchFactor(double pitch) {
        if (pitch < 100.0) return 1.0f;

        if (pitch < 200.0) {
            return 0.8f + (float)(pitch - 100.0) / 500.0f;
        } else if (pitch < 350.0) {
            return 1.0f + (float)(pitch - 200.0) / 300.0f;
        } else {
            return 1.5f + (float)Math.min(pitch - 350.0, 200.0) / 400.0f;
        }
    }

    private boolean isIncreasing(SquishData current, SquishData target) {
        float currentMag = Math.abs(current.xScale) + Math.abs(current.yScale) + Math.abs(current.zScale);
        float targetMag = Math.abs(target.xScale) + Math.abs(target.yScale) + Math.abs(target.zScale);
        return targetMag > currentMag;
    }

    private float lerp(float start, float end, float alpha) {
        return start + (end - start) * alpha;
    }
}