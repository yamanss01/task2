import java.util.List;
import java.util.Optional;

import processing.core.PImage;

/**
 * Represents a background for the 2D world.
 */
public final class Background {
	public String id;
	public List<PImage> images;
	public int imageIndex;

	public Background(String id, List<PImage> images) {
		this.id = id;
		this.images = images;
	}

	public static Background getBackgroundCell(WorldModel world, Point pos) {
		return world.background[pos.y][pos.x];
	}

	public static Optional<PImage> getBackgroundImage(WorldModel world, Point pos) {
		if (Functions.withinBounds(world, pos)) {
			return Optional.of(ImageStore.getCurrentImage(getBackgroundCell(world, pos)));
		} else {
			return Optional.empty();
		}
	}

	public static void setBackground(WorldModel world, Point pos, Background background) {
		if (Functions.withinBounds(world, pos)) {
			setBackgroundCell(world, pos, background);
		}
	}

	public static void setBackgroundCell(WorldModel world, Point pos, Background background) {
		world.background[pos.y][pos.x] = background;
	}
	
}
