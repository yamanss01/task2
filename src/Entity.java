import java.util.List;
import java.util.Optional;

import processing.core.PImage;

/**
 * An entity that exists in the world. See EntityKind for the different kinds of
 * entities that exist.
 */
public class Entity{
	public EntityKind kind;
	public String id;
	public Point position;
	public List<PImage> images;
	public int imageIndex;
	public int resourceLimit;
	public int resourceCount;
	public int actionPeriod;
	public int animationPeriod;
	public int health;
	public int healthLimit;

	public Entity(EntityKind kind, String id, Point position, List<PImage> images, int resourceLimit, int resourceCount,
			int actionPeriod, int animationPeriod, int health, int healthLimit) {
		this.kind = kind;
		this.id = id;
		this.position = position;
		this.images = images;
		this.imageIndex = 0;
		this.resourceLimit = resourceLimit;
		this.resourceCount = resourceCount;
		this.actionPeriod = actionPeriod;
		this.animationPeriod = animationPeriod;
		this.health = health;
		this.healthLimit = healthLimit;
	}

	public static void moveEntity(WorldModel world, Entity entity, Point pos) {
		Point oldPos = entity.position;
		if (Functions.withinBounds(world, pos) && !pos.equals(oldPos)) {
			Functions.setOccupancyCell(world, oldPos, null);
			removeEntityAt(world, pos);
			Functions.setOccupancyCell(world, pos, entity);
			entity.position = pos;
		}
	}

	public static void removeEntity(WorldModel world, Entity entity) {
		removeEntityAt(world, entity.position);
	}

	private static void removeEntityAt(WorldModel world, Point pos) {
		if (Functions.withinBounds(world, pos) && Functions.getOccupancyCell(world, pos) != null) {
			Entity entity = Functions.getOccupancyCell(world, pos);

			/*
			 * This moves the entity just outside of the grid for debugging purposes.
			 */
			entity.position = new Point(-1, -1);
			world.entities.remove(entity);
			Functions.setOccupancyCell(world, pos, null);
		}
	}

	public static Optional<Entity> getOccupant(WorldModel world, Point pos) {
		if (Functions.isOccupied(world, pos)) {
			return Optional.of(Functions.getOccupancyCell(world, pos));
		} else {
			return Optional.empty();
		}
	}

}
