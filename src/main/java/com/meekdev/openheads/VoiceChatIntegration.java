package com.meekdev.openheads;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceChatIntegration implements VoicechatPlugin {

    public static final String MOD_ID = "openheads";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static VoicechatApi VOICECHAT_API;

    @Nullable
    public static VoicechatServerApi VOICECHAT_SERVER_API;

    public static class AudioData {
        public double volume;
        public long timestamp;

        public AudioData(double volume, long timestamp) {
            this.volume = volume;
            this.timestamp = timestamp;
        }

        public void update(double volume, long timestamp) {
            this.volume = volume;
            this.timestamp = timestamp;
        }
    }

    public static final Map<UUID, AudioData> TALKING_PLAYERS = new ConcurrentHashMap<>();

    @Nullable
    private OpusDecoder decoder;

    @Override
    public String getPluginId() {
        return MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VOICECHAT_API = api;
        LOGGER.info("VoiceChat API initialized");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        VOICECHAT_SERVER_API = event.getVoicechat();
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        byte[] opusData = event.getPacket().getOpusEncodedData();
        if (opusData.length == 0) return;

        if (decoder == null) {
            decoder = event.getVoicechat().createDecoder();
        }

        decoder.resetState();
        short[] decoded = decoder.decode(opusData);

        double audioLevel = AudioUtils.calculateAudioLevel(decoded);
        UUID uuid = event.getSenderConnection().getPlayer().getUuid();
        long timestamp = System.currentTimeMillis();

        AudioData existing = TALKING_PLAYERS.get(uuid);
        if (existing != null) {
            existing.update(audioLevel, timestamp);
        } else {
            TALKING_PLAYERS.put(uuid, new AudioData(audioLevel, timestamp));
        }
    }

    public static AudioData getAudioData(UUID playerUuid) {
        AudioData data = TALKING_PLAYERS.get(playerUuid);
        if (data == null) return null;

        if (System.currentTimeMillis() - data.timestamp > 150) {
            TALKING_PLAYERS.remove(playerUuid);
            return null;
        }

        return data;
    }
}