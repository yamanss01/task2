import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PImage;

public final class ImageStore implements ImageStoreInterface{
	public Map<String, List<PImage>> images;
	public List<PImage> defaultImages;

	private static final int KEYED_RED_IDX = 2;
	private static final int KEYED_GREEN_IDX = 3;
	private static final int KEYED_BLUE_IDX = 4;

	public ImageStore() {
	}

	public ImageStore(PImage defaultImage) {
		this.images = new HashMap<>();
		defaultImages = new LinkedList<>();
		defaultImages.add(defaultImage);
	}

	public static List<PImage> getImageList(ImageStore imageStore, String key) {
		return imageStore.images.getOrDefault(key, imageStore.defaultImages);
	}

	public void loadImages(Scanner in, ImageStore imageStore, PApplet screen) {
		int lineNumber = 0;
		while (in.hasNextLine()) {
			try {
				processImageLine(imageStore.images, in.nextLine(), screen);
			} catch (NumberFormatException e) {
				System.out.println(String.format("Image format error on line %d", lineNumber));
			}
			lineNumber++;
		}
	}

	public void processImageLine(Map<String, List<PImage>> images, String line, PApplet screen) {
		String[] attrs = line.split("\\s");
		if (attrs.length >= 2) {
			String key = attrs[0];
			PImage img = screen.loadImage(attrs[1]);
			if (img != null && img.width != -1) {
				List<PImage> imgs = getImages(images, key);
				imgs.add(img);

				if (attrs.length >= Functions.KEYED_IMAGE_MIN) {
					int r = Integer.parseInt(attrs[KEYED_RED_IDX]);
					int g = Integer.parseInt(attrs[KEYED_GREEN_IDX]);
					int b = Integer.parseInt(attrs[KEYED_BLUE_IDX]);
					setAlpha(img, screen.color(r, g, b), 0);
				}
			}
		}
	}

	public List<PImage> getImages(Map<String, List<PImage>> images, String key) {
		List<PImage> imgs = images.get(key);
		if (imgs == null) {
			imgs = new LinkedList<>();
			images.put(key, imgs);
		}
		return imgs;
	}

	public void setAlpha(PImage img, int maskColor, int alpha) {
		int alphaValue = alpha << 24;
		int nonAlpha = maskColor & Functions.COLOR_MASK;
		img.format = PApplet.ARGB;
		img.loadPixels();
		for (int i = 0; i < img.pixels.length; i++) {
			if ((img.pixels[i] & Functions.COLOR_MASK) == nonAlpha) {
				img.pixels[i] = alphaValue | nonAlpha;
			}
		}
		img.updatePixels();
	}

	public static PImage getCurrentImage(Object entity) {
		if (entity instanceof Background) {
			return ((Background) entity).images.get(((Background) entity).imageIndex);
		} else if (entity instanceof Entity) {
			return ((Entity) entity).images.get(((Entity) entity).imageIndex);
		} else {
			throw new UnsupportedOperationException(String.format("getCurrentImage not supported for %s", entity));
		}
	}
}
