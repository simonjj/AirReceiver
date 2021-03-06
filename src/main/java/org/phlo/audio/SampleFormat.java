package org.phlo.audio;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

public enum SampleFormat {
	UnsignedInteger16(2) {
		@Override public final Signedness getSignedness() {
			return Signedness.Unsigned;
		}
		
		@Override public final SampleAccessor getAccessor(final ByteBuffer buffer) {
			return new SampleAccessor() {
				@Override public float getSample(final int index) {
					return Signedness.Unsigned.shortToNormalizedFloat(buffer.getShort(BytesPerSample * index));
				}

				@Override public void setSample(final int index, final float sample) {
					buffer.putShort(BytesPerSample * index, Signedness.Unsigned.shortFromNormalizedFloat(sample));
				}
			};
		}
	},

	SignedInteger16(2) {
		@Override public final Signedness getSignedness() {
			return Signedness.Signed;
		}

		@Override public final SampleAccessor getAccessor(final ByteBuffer buffer) {
			return new SampleAccessor() {
				@Override public float getSample(final int index) {
					return Signedness.Signed.shortToNormalizedFloat(buffer.getShort(BytesPerSample * index));
				}

				@Override public void setSample(final int index, final float sample) {
					buffer.putShort(BytesPerSample * index, Signedness.Signed.shortFromNormalizedFloat(sample));
				}
			};
		}
	},

	Float32(4) {
		@Override public final Signedness getSignedness() {
			return null;
		}

		@Override public final SampleAccessor getAccessor(final ByteBuffer buffer) {
			return new SampleAccessor() {
				@Override public float getSample(final int index) {
					return buffer.getFloat(BytesPerSample * index);
				}

				@Override public void setSample(final int index, final float sample) {
					buffer.putFloat(BytesPerSample * index, sample);
				}
			};
		}
	};
	
	public static SampleFormat fromAudioFormat(AudioFormat audioFormat) {
		if (
			(AudioFormat.Encoding.PCM_SIGNED.equals(audioFormat.getEncoding())) &&
			(audioFormat.getSampleSizeInBits() == 16)
		) {
			return SignedInteger16;
		}
		else if (
			(AudioFormat.Encoding.PCM_UNSIGNED.equals(audioFormat.getEncoding())) &&
			(audioFormat.getSampleSizeInBits() == 16)
		) {
			return UnsignedInteger16;
		}
		else {
			throw new IllegalArgumentException("Audio format with encoding " + audioFormat.getEncoding() + " and " + audioFormat.getSampleSizeInBits() + " bits per sample is unsupported");
		}
	}

	public final int BytesPerSample;

	private SampleFormat(final int _bytesPerSample) {
		BytesPerSample = _bytesPerSample;
	}
	
	public int getSizeBytes(SampleDimensions dimensions) {
		return dimensions.getTotalSamples() * BytesPerSample;
	}
	
	public SampleDimensions getDimensionsFromChannelsAndByteSize(int channels, int byteSize) {
		return new SampleDimensions(channels, byteSize / (BytesPerSample * channels));
	}
	
	public abstract Signedness getSignedness();

	public abstract SampleAccessor getAccessor(ByteBuffer buffer);
}
