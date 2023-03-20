import java.util.List;
import java.util.Map;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PImage;

public interface ImageStoreInterface {

	void loadImages(Scanner in, ImageStore imageStore, PApplet screen);

	void processImageLine(Map<String, List<PImage>> images, String line, PApplet screen);

	List<PImage> getImages(Map<String, List<PImage>> images, String key);
	
	void setAlpha(PImage img, int maskColor, int alpha);
}
