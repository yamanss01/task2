import java.util.*;

import processing.core.PImage;

/**
 * This class contains many functions written in a procedural style. You will
 * reduce the size of this class over the next several weeks by refactoring this
 * codebase to follow an OOP style.
 */
public final class Functions {
	public static final Random rand = new Random();

	public static final int COLOR_MASK = 0xffffff;
	public static final int KEYED_IMAGE_MIN = 5;

	public static final int PROPERTY_KEY = 0;

	public static final List<String> PATH_KEYS = new ArrayList<>(Arrays.asList("bridge", "dirt", "dirt_horiz",
			"dirt_vert_left", "dirt_vert_right", "dirt_bot_left_corner", "dirt_bot_right_up", "dirt_vert_left_bot"));

	public static final String SAPLING_KEY = "sapling";
	public static final int SAPLING_HEALTH_LIMIT = 5;
	public static final int SAPLING_ACTION_ANIMATION_PERIOD = 1000; // have to be in sync since grows and gains health
																	// at same time
	private static final int SAPLING_NUM_PROPERTIES = 4;
	private static final int SAPLING_ID = 1;
	private static final int SAPLING_COL = 2;
	private static final int SAPLING_ROW = 3;
	private static final int SAPLING_HEALTH = 4;

	private static final String BGND_KEY = "background";
	private static final int BGND_NUM_PROPERTIES = 4;
	private static final int BGND_ID = 1;
	private static final int BGND_COL = 2;
	private static final int BGND_ROW = 3;

	private static final String OBSTACLE_KEY = "obstacle";
	private static final int OBSTACLE_NUM_PROPERTIES = 5;
	private static final int OBSTACLE_ID = 1;
	private static final int OBSTACLE_COL = 2;
	private static final int OBSTACLE_ROW = 3;
	private static final int OBSTACLE_ANIMATION_PERIOD = 4;

	private static final String DUDE_KEY = "dude";
	private static final int DUDE_NUM_PROPERTIES = 7;
	private static final int DUDE_ID = 1;
	private static final int DUDE_COL = 2;
	private static final int DUDE_ROW = 3;
	private static final int DUDE_LIMIT = 4;
	private static final int DUDE_ACTION_PERIOD = 5;
	private static final int DUDE_ANIMATION_PERIOD = 6;

	private static final String HOUSE_KEY = "house";
	private static final int HOUSE_NUM_PROPERTIES = 4;
	private static final int HOUSE_ID = 1;
	private static final int HOUSE_COL = 2;
	private static final int HOUSE_ROW = 3;

	private static final String FAIRY_KEY = "fairy";
	private static final int FAIRY_NUM_PROPERTIES = 6;
	private static final int FAIRY_ID = 1;
	private static final int FAIRY_COL = 2;
	private static final int FAIRY_ROW = 3;
	private static final int FAIRY_ANIMATION_PERIOD = 4;
	private static final int FAIRY_ACTION_PERIOD = 5;

	public static final String STUMP_KEY = "stump";

	public static final String TREE_KEY = "tree";
	private static final int TREE_NUM_PROPERTIES = 7;
	private static final int TREE_ID = 1;
	private static final int TREE_COL = 2;
	private static final int TREE_ROW = 3;
	private static final int TREE_ANIMATION_PERIOD = 4;
	private static final int TREE_ACTION_PERIOD = 5;
	private static final int TREE_HEALTH = 6;

	public static final int TREE_ANIMATION_MAX = 600;
	public static final int TREE_ANIMATION_MIN = 50;
	public static final int TREE_ACTION_MAX = 1400;
	public static final int TREE_ACTION_MIN = 1000;
	public static final int TREE_HEALTH_MAX = 3;
	public static final int TREE_HEALTH_MIN = 1;

	public static boolean withinBounds(WorldModel world, Point pos) {
		return pos.y >= 0 && pos.y < world.numRows && pos.x >= 0 && pos.x < world.numCols;
	}

	public static boolean isOccupied(WorldModel world, Point pos) {
		return withinBounds(world, pos) && getOccupancyCell(world, pos) != null;
	}

	public static Entity getOccupancyCell(WorldModel world, Point pos) {
		return world.occupancy[pos.y][pos.x];
	}

	public static void setOccupancyCell(WorldModel world, Point pos, Entity entity) {
		world.occupancy[pos.y][pos.x] = entity;
	}

	public static boolean adjacent(Point p1, Point p2) {
		return (p1.x == p2.x && Math.abs(p1.y - p2.y) == 1) || (p1.y == p2.y && Math.abs(p1.x - p2.x) == 1);
	}

	public static int getNumFromRange(int max, int min) {
		Random rand = new Random();
		return min + rand.nextInt(max - min);
	}

	public static void load(Scanner in, WorldModel world, ImageStore imageStore) {
		int lineNumber = 0;
		while (in.hasNextLine()) {
			try {
				if (!processLine(in.nextLine(), world, imageStore)) {
					System.err.println(String.format("invalid entry on line %d", lineNumber));
				}
			} catch (NumberFormatException e) {
				System.err.println(String.format("invalid entry on line %d", lineNumber));
			} catch (IllegalArgumentException e) {
				System.err.println(String.format("issue on line %d: %s", lineNumber, e.getMessage()));
			}
			lineNumber++;
		}
	}

	private static boolean processLine(String line, WorldModel world, ImageStore imageStore) {
		String[] properties = line.split("\\s");
		if (properties.length > 0) {
			switch (properties[PROPERTY_KEY]) {
			case BGND_KEY:
				return parseBackground(properties, world, imageStore);
			case DUDE_KEY:
				return parseDude(properties, world, imageStore);
			case OBSTACLE_KEY:
				return parseObstacle(properties, world, imageStore);
			case FAIRY_KEY:
				return parseFairy(properties, world, imageStore);
			case HOUSE_KEY:
				return parseHouse(properties, world, imageStore);
			case TREE_KEY:
				return parseTree(properties, world, imageStore);
			case SAPLING_KEY:
				return parseSapling(properties, world, imageStore);
			}
		}

		return false;
	}

	private static void tryAddEntity(WorldModel world, Entity entity) {
		if (Functions.isOccupied(world, entity.position)) {
			// arguably the wrong type of exception, but we are not
			// defining our own exceptions yet
			throw new IllegalArgumentException("position occupied");
		}

		addEntity(world, entity);
	}

	public static void addEntity(WorldModel world, Entity entity) {
		if (Functions.withinBounds(world, entity.position)) {
			Functions.setOccupancyCell(world, entity.position, entity);
			world.entities.add(entity);
		}
	}

	private static boolean parseBackground(String[] properties, WorldModel world, ImageStore imageStore) {
		if (properties.length == BGND_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[BGND_COL]), Integer.parseInt(properties[BGND_ROW]));
			String id = properties[BGND_ID];
			Background.setBackground(world, pt, new Background(id, ImageStore.getImageList(imageStore, id)));
		}

		return properties.length == BGND_NUM_PROPERTIES;
	}

	private static boolean parseSapling(String[] properties, WorldModel world, ImageStore imageStore) {
		if (properties.length == SAPLING_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[SAPLING_COL]), Integer.parseInt(properties[SAPLING_ROW]));
			String id = properties[SAPLING_ID];
			int health = Integer.parseInt(properties[SAPLING_HEALTH]);
			Entity entity = new Entity(EntityKind.SAPLING, id, pt, ImageStore.getImageList(imageStore, SAPLING_KEY), 0,
					0, SAPLING_ACTION_ANIMATION_PERIOD, SAPLING_ACTION_ANIMATION_PERIOD, health, SAPLING_HEALTH_LIMIT);
			tryAddEntity(world, entity);
		}

		return properties.length == SAPLING_NUM_PROPERTIES;
	}

	private static boolean parseDude(String[] properties, WorldModel world, ImageStore imageStore)
			throws NumberFormatException {
		if (properties.length == DUDE_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[DUDE_COL]), Integer.parseInt(properties[DUDE_ROW]));
			Entity entity = Action.createDudeNotFull(properties[DUDE_ID], pt,
					Integer.parseInt(properties[DUDE_ACTION_PERIOD]),
					Integer.parseInt(properties[DUDE_ANIMATION_PERIOD]), Integer.parseInt(properties[DUDE_LIMIT]),
					ImageStore.getImageList(imageStore, DUDE_KEY));
			tryAddEntity(world, entity);
		}

		return properties.length == DUDE_NUM_PROPERTIES;
	}

	private static boolean parseFairy(String[] properties, WorldModel world, ImageStore imageStore)
			throws NumberFormatException {
		if (properties.length == FAIRY_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[FAIRY_COL]), Integer.parseInt(properties[FAIRY_ROW]));
			Entity entity = createFairy(properties[FAIRY_ID], pt, Integer.parseInt(properties[FAIRY_ACTION_PERIOD]),
					Integer.parseInt(properties[FAIRY_ANIMATION_PERIOD]),
					ImageStore.getImageList(imageStore, FAIRY_KEY));
			tryAddEntity(world, entity);
		}

		return properties.length == FAIRY_NUM_PROPERTIES;
	}

	private static boolean parseTree(String[] properties, WorldModel world, ImageStore imageStore) {
		if (properties.length == TREE_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[TREE_COL]), Integer.parseInt(properties[TREE_ROW]));
			Entity entity = createTree(properties[TREE_ID], pt, Integer.parseInt(properties[TREE_ACTION_PERIOD]),
					Integer.parseInt(properties[TREE_ANIMATION_PERIOD]), Integer.parseInt(properties[TREE_HEALTH]),
					ImageStore.getImageList(imageStore, TREE_KEY));
			tryAddEntity(world, entity);
		}

		return properties.length == TREE_NUM_PROPERTIES;
	}

	private static boolean parseObstacle(String[] properties, WorldModel world, ImageStore imageStore) {
		if (properties.length == OBSTACLE_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[OBSTACLE_COL]),
					Integer.parseInt(properties[OBSTACLE_ROW]));
			Entity entity = createObstacle(properties[OBSTACLE_ID], pt,
					Integer.parseInt(properties[OBSTACLE_ANIMATION_PERIOD]),
					ImageStore.getImageList(imageStore, OBSTACLE_KEY));
			tryAddEntity(world, entity);
		}

		return properties.length == OBSTACLE_NUM_PROPERTIES;
	}

	private static boolean parseHouse(String[] properties, WorldModel world, ImageStore imageStore) {
		if (properties.length == HOUSE_NUM_PROPERTIES) {
			Point pt = new Point(Integer.parseInt(properties[HOUSE_COL]), Integer.parseInt(properties[HOUSE_ROW]));
			Entity entity = createHouse(properties[HOUSE_ID], pt, ImageStore.getImageList(imageStore, HOUSE_KEY));
			tryAddEntity(world, entity);
		}

		return properties.length == HOUSE_NUM_PROPERTIES;
	}

	private static Entity createFairy(String id, Point position, int actionPeriod, int animationPeriod,
			List<PImage> images) {
		return new Entity(EntityKind.FAIRY, id, position, images, 0, 0, actionPeriod, animationPeriod, 0, 0);
	}

	private static Entity createHouse(String id, Point position, List<PImage> images) {
		return new Entity(EntityKind.HOUSE, id, position, images, 0, 0, 0, 0, 0, 0);
	}

	private static Entity createObstacle(String id, Point position, int animationPeriod, List<PImage> images) {
		return new Entity(EntityKind.OBSTACLE, id, position, images, 0, 0, 0, animationPeriod, 0, 0);
	}

	public static Entity createTree(String id, Point position, int actionPeriod, int animationPeriod, int health,
			List<PImage> images) {
		return new Entity(EntityKind.TREE, id, position, images, 0, 0, actionPeriod, animationPeriod, health, 0);
	}

	public static Entity createStump(String id, Point position, List<PImage> images) {
		return new Entity(EntityKind.STUMP, id, position, images, 0, 0, 0, 0, 0, 0);
	}

	private static Optional<Entity> nearestEntity(List<Entity> entities, Point pos) {
		if (entities.isEmpty()) {
			return Optional.empty();
		} else {
			Entity nearest = entities.get(0);
			int nearestDistance = distanceSquared(nearest.position, pos);

			for (Entity other : entities) {
				int otherDistance = distanceSquared(other.position, pos);

				if (otherDistance < nearestDistance) {
					nearest = other;
					nearestDistance = otherDistance;
				}
			}

			return Optional.of(nearest);
		}
	}

	private static int distanceSquared(Point p1, Point p2) {
		int deltaX = p1.x - p2.x;
		int deltaY = p1.y - p2.y;

		return deltaX * deltaX + deltaY * deltaY;
	}

	public static Optional<Entity> findNearest(WorldModel world, Point pos, List<EntityKind> kinds) {
		List<Entity> ofType = new LinkedList<>();
		for (EntityKind kind : kinds) {
			for (Entity entity : world.entities) {
				if (entity.kind == kind) {
					ofType.add(entity);
				}
			}
		}

		return nearestEntity(ofType, pos);
	}

	public static int clamp(int value, int low, int high) {
		return Math.min(high, Math.max(value, low));
	}

}
