package com.meekdev.openheads;

public class FFTUtils {

    public static double detectPitch(short[] samples, int sampleRate) {
        if (samples.length < 2048) return 0.0;

        int fftSize = 2048;
        double[] complexReal = new double[fftSize];
        double[] complexImag = new double[fftSize];

        for (int i = 0; i < fftSize && i < samples.length; i++) {
            complexReal[i] = samples[i] / 32768.0 * hamming(i, fftSize);
            complexImag[i] = 0.0;
        }

        fft(complexReal, complexImag);

        double[] magnitudes = new double[fftSize / 2];
        for (int i = 0; i < fftSize / 2; i++) {
            magnitudes[i] = Math.sqrt(complexReal[i] * complexReal[i] + complexImag[i] * complexImag[i]);
        }

        int minBin = (int) (80.0 * fftSize / sampleRate);
        int maxBin = (int) (1000.0 * fftSize / sampleRate);

        int peakBin = minBin;
        double peakMagnitude = magnitudes[minBin];

        for (int i = minBin + 1; i < maxBin && i < magnitudes.length; i++) {
            if (magnitudes[i] > peakMagnitude) {
                peakMagnitude = magnitudes[i];
                peakBin = i;
            }
        }

        if (peakMagnitude < 0.01) return 0.0;

        return (double) peakBin * sampleRate / fftSize;
    }

    private static double hamming(int i, int size) {
        return 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (size - 1));
    }

    private static void fft(double[] real, double[] imag) {
        int n = real.length;
        if (n <= 1) return;

        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException("FFT size must be power of 2");
        }

        int bits = Integer.numberOfTrailingZeros(n);
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - bits);
            if (j > i) {
                double tempReal = real[i];
                double tempImag = imag[i];
                real[i] = real[j];
                imag[i] = imag[j];
                real[j] = tempReal;
                imag[j] = tempImag;
            }
        }

        for (int len = 2; len <= n; len *= 2) {
            double angle = -2.0 * Math.PI / len;
            double wlenReal = Math.cos(angle);
            double wlenImag = Math.sin(angle);

            for (int i = 0; i < n; i += len) {
                double wReal = 1.0;
                double wImag = 0.0;

                for (int j = 0; j < len / 2; j++) {
                    double uReal = real[i + j];
                    double uImag = imag[i + j];
                    double vReal = real[i + j + len / 2] * wReal - imag[i + j + len / 2] * wImag;
                    double vImag = real[i + j + len / 2] * wImag + imag[i + j + len / 2] * wReal;

                    real[i + j] = uReal + vReal;
                    imag[i + j] = uImag + vImag;
                    real[i + j + len / 2] = uReal - vReal;
                    imag[i + j + len / 2] = uImag - vImag;

                    double wTempReal = wReal * wlenReal - wImag * wlenImag;
                    wImag = wReal * wlenImag + wImag * wlenReal;
                    wReal = wTempReal;
                }
            }
        }
    }
}