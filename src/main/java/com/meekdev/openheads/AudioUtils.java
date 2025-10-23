package com.meekdev.openheads;

public class AudioUtils {

    public static double calculateAudioLevel(short[] samples) {
        double rms = 0D;

        for (short sample : samples) {
            double normalized = (double) sample / Short.MAX_VALUE;
            rms += normalized * normalized;
        }

        int sampleCount = samples.length;
        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        if (rms > 0D) {
            return Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        }

        return -127D;
    }

}