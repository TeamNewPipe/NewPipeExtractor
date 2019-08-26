package org.schabi.newpipe.extractor.stream;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class StreamFrames {

	private final List<Frameset> frames;

	public StreamFrames(String baseUrl, List<String> params) {
		frames = new ArrayList<>(params.size());
		for (int i = 0; i < params.size(); i++) {
			String param = params.get(i);
			final String[] parts = param.split("#");
			frames.add(new Frameset(
					baseUrl.replace("$L", String.valueOf(i)).replace("$N", parts[6]) + "&sigh=" + parts[7],
					Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2]),
					Integer.parseInt(parts[3]),
					Integer.parseInt(parts[4])
			));
		}
	}

	public int getVariantsCount() {
		return frames.size();
	}

	public Frameset getVariant(int index) {
		return frames.get(index);
	}

	@Nullable
	public Frameset getDefaultVariant() {
		for (final Frameset f : frames) {
			if (f.getUrl().contains("default.jpg")) {
				return f;
			}
		}
		return null;
	}

	public static class Frameset {

		private String url;
		private int frameWidth;
		private int frameHeight;
		private int totalCount;
		private int framesPerPageX;
		private int framesPerPageY;

		private Frameset(String url, int frameWidth, int frameHeight, int totalCount, int framesPerPageX, int framesPerPageY) {
			this.url = url;
			this.totalCount = totalCount;
			this.frameWidth = frameWidth;
			this.frameHeight = frameHeight;
			this.framesPerPageX = framesPerPageX;
			this.framesPerPageY = framesPerPageY;
		}

		public String getUrl() {
			return url;
		}

		public String getUrl(int page) {
			return url.replace("$M", String.valueOf(page));
		}

		public int getTotalPages() {
			if (!url.contains("$M")) {
				return 0;
			}
			return (int) Math.ceil(totalCount / (double) (framesPerPageX * framesPerPageY));
		}

		/**
		 * @return total count of frames
		 */
		public int getTotalCount() {
			return totalCount;
		}

		/**
		 * @return maximum frames count by x
		 */
		public int getFramesPerPageX() {
			return framesPerPageX;
		}

		/**
		 * @return maximum frames count by y
		 */
		public int getFramesPerPageY() {
			return framesPerPageY;
		}

		/**
		 * @return width of a one frame, in pixels
		 */
		public int getFrameWidth() {
			return frameWidth;
		}

		/**
		 * @return height of a one frame, in pixels
		 */
		public int getFrameHeight() {
			return frameHeight;
		}
	}
}
