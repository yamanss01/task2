import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import processing.core.PImage;

/**
 * An action that can be taken by an entity
 */
public final class Action implements ActionInterface{
	public ActionKind kind;
	public Entity entity;
	public WorldModel world;
	public ImageStore imageStore;
	public int repeatCount;

	public Action() {
	}

	public Action(ActionKind kind, Entity entity, WorldModel world, ImageStore imageStore, int repeatCount) {
		this.kind = kind;
		this.entity = entity;
		this.world = world;
		this.imageStore = imageStore;
		this.repeatCount = repeatCount;
	}

	 public void executeAction(Action action, EventScheduler scheduler) {
		switch (action.kind) {
		case ACTIVITY:
			executeActivityAction(action, scheduler);
			break;

		case ANIMATION:
			executeAnimationAction(action, scheduler);
			break;
		}
	}

	public void executeAnimationAction(Action action, EventScheduler scheduler) {
		nextImage(action.entity);

		if (action.repeatCount != 1) {
			EventScheduler.scheduleEvent(scheduler, action.entity,
					createAnimationAction(action.entity, Math.max(action.repeatCount - 1, 0)),
					EventScheduler.getAnimationPeriod(action.entity));
		}
	}

	public void executeActivityAction(Action action, EventScheduler scheduler) {
		switch (action.entity.kind) {
		case SAPLING:
			executeSaplingActivity(action.entity, action.world, action.imageStore, scheduler);
			break;

		case TREE:
			executeTreeActivity(action.entity, action.world, action.imageStore, scheduler);
			break;

		case FAIRY:
			executeFairyActivity(action.entity, action.world, action.imageStore, scheduler);
			break;

		case DUDE_NOT_FULL:
			executeDudeNotFullActivity(action.entity, action.world, action.imageStore, scheduler);
			break;

		case DUDE_FULL:
			executeDudeFullActivity(action.entity, action.world, action.imageStore, scheduler);
			break;

		default:
			throw new UnsupportedOperationException(
					String.format("executeActivityAction not supported for %s", action.entity.kind));
		}
	}

	public static Action createAnimationAction(Entity entity, int repeatCount) {
		return new Action(ActionKind.ANIMATION, entity, null, null, repeatCount);
	}

	public static Action createActivityAction(Entity entity, WorldModel world, ImageStore imageStore) {
		return new Action(ActionKind.ACTIVITY, entity, world, imageStore, 0);
	}

	public void executeSaplingActivity(Entity entity, WorldModel world, ImageStore imageStore,
			EventScheduler scheduler) {
		entity.health++;
		if (!transformPlant(entity, world, scheduler, imageStore)) {
			EventScheduler.scheduleEvent(scheduler, entity, createActivityAction(entity, world, imageStore),
					entity.actionPeriod);
		}
	}

	public void executeTreeActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler) {

		if (!transformPlant(entity, world, scheduler, imageStore)) {

			EventScheduler.scheduleEvent(scheduler, entity, createActivityAction(entity, world, imageStore),
					entity.actionPeriod);
		}
	}

	public void executeFairyActivity(Entity entity, WorldModel world, ImageStore imageStore,
			EventScheduler scheduler) {
		Optional<Entity> fairyTarget = Functions.findNearest(world, entity.position,
				new ArrayList<>(Arrays.asList(EntityKind.STUMP)));

		if (fairyTarget.isPresent()) {
			Point tgtPos = fairyTarget.get().position;

			if (moveToFairy(entity, world, fairyTarget.get(), scheduler)) {
				Entity sapling = createSapling("sapling_" + entity.id, tgtPos,
						ImageStore.getImageList(imageStore, Functions.SAPLING_KEY));

				Functions.addEntity(world, sapling);
				EventScheduler.scheduleActions(sapling, scheduler, world, imageStore);
			}
		}

		EventScheduler.scheduleEvent(scheduler, entity, createActivityAction(entity, world, imageStore),
				entity.actionPeriod);
	}

	public void executeDudeNotFullActivity(Entity entity, WorldModel world, ImageStore imageStore,
			EventScheduler scheduler) {
		Optional<Entity> target = Functions.findNearest(world, entity.position,
				new ArrayList<>(Arrays.asList(EntityKind.TREE, EntityKind.SAPLING)));

		if (!target.isPresent() || !moveToNotFull(entity, world, target.get(), scheduler)
				|| !transformNotFull(entity, world, scheduler, imageStore)) {
			EventScheduler.scheduleEvent(scheduler, entity, createActivityAction(entity, world, imageStore),
					entity.actionPeriod);
		}
	}

	public void executeDudeFullActivity(Entity entity, WorldModel world, ImageStore imageStore,
			EventScheduler scheduler) {
		Optional<Entity> fullTarget = Functions.findNearest(world, entity.position,
				new ArrayList<>(Arrays.asList(EntityKind.HOUSE)));

		if (fullTarget.isPresent() && moveToFull(entity, world, fullTarget.get(), scheduler)) {
			transformFull(entity, world, scheduler, imageStore);
		} else {
			EventScheduler.scheduleEvent(scheduler, entity, createActivityAction(entity, world, imageStore),
					entity.actionPeriod);
		}
	}

	public void updateOnTime(EventScheduler scheduler, long time) {
		while (!scheduler.eventQueue.isEmpty() && scheduler.eventQueue.peek().time < time) {
			Event next = scheduler.eventQueue.poll();

			EventScheduler eventScheduler = new EventScheduler();
			eventScheduler.removePendingEvent(scheduler, next);

			executeAction(next.action, scheduler);
		}
	}

	public boolean moveToFairy(Entity fairy, WorldModel world, Entity target, EventScheduler scheduler) {
		if (Functions.adjacent(fairy.position, target.position)) {
			Entity.removeEntity(world, target);
			EventScheduler.unscheduleAllEvents(scheduler, target);
			return true;
		} else {
			Point nextPos = nextPositionFairy(fairy, world, target.position);

			if (!fairy.position.equals(nextPos)) {
				Optional<Entity> occupant = Entity.getOccupant(world, nextPos);
				if (occupant.isPresent()) {
					EventScheduler.unscheduleAllEvents(scheduler, occupant.get());
				}

				Entity.moveEntity(world, fairy, nextPos);
			}
			return false;
		}
	}

	public Point nextPositionFairy(Entity entity, WorldModel world, Point destPos) {
		int horiz = Integer.signum(destPos.x - entity.position.x);
		Point newPos = new Point(entity.position.x + horiz, entity.position.y);

		if (horiz == 0 || Functions.isOccupied(world, newPos)) {
			int vert = Integer.signum(destPos.y - entity.position.y);
			newPos = new Point(entity.position.x, entity.position.y + vert);

			if (vert == 0 || Functions.isOccupied(world, newPos)) {
				newPos = entity.position;
			}
		}

		return newPos;
	}

	public boolean moveToNotFull(Entity dude, WorldModel world, Entity target, EventScheduler scheduler) {
		if (Functions.adjacent(dude.position, target.position)) {
			dude.resourceCount += 1;
			target.health--;
			return true;
		} else {
			Point nextPos = Point.nextPositionDude(dude, world, target.position);

			if (!dude.position.equals(nextPos)) {
				Optional<Entity> occupant = Entity.getOccupant(world, nextPos);
				if (occupant.isPresent()) {
					EventScheduler.unscheduleAllEvents(scheduler, occupant.get());
				}

				Entity.moveEntity(world, dude, nextPos);
			}
			return false;
		}
	}

	public boolean moveToFull(Entity dude, WorldModel world, Entity target, EventScheduler scheduler) {
		if (Functions.adjacent(dude.position, target.position)) {
			return true;
		} else {
			Point nextPos = Point.nextPositionDude(dude, world, target.position);

			if (!dude.position.equals(nextPos)) {
				Optional<Entity> occupant = Entity.getOccupant(world, nextPos);
				if (occupant.isPresent()) {
					EventScheduler.unscheduleAllEvents(scheduler, occupant.get());
				}

				Entity.moveEntity(world, dude, nextPos);
			}
			return false;
		}
	}

	public void nextImage(Entity entity) {
		entity.imageIndex = (entity.imageIndex + 1) % entity.images.size();
	}

	public boolean transformNotFull(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
		if (entity.resourceCount >= entity.resourceLimit) {
			Entity miner = createDudeFull(entity.id, entity.position, entity.actionPeriod, entity.animationPeriod,
					entity.resourceLimit, entity.images);

			Entity.removeEntity(world, entity);
			EventScheduler.unscheduleAllEvents(scheduler, entity);

			Functions.addEntity(world, miner);
			EventScheduler.scheduleActions(miner, scheduler, world, imageStore);

			return true;
		}

		return false;
	}

	public void transformFull(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
		Entity miner = createDudeNotFull(entity.id, entity.position, entity.actionPeriod, entity.animationPeriod,
				entity.resourceLimit, entity.images);

		Entity.removeEntity(world, entity);
		EventScheduler.unscheduleAllEvents(scheduler, entity);

		Functions.addEntity(world, miner);
		EventScheduler.scheduleActions(miner, scheduler, world, imageStore);
	}

	public static Entity createDudeFull(String id, Point position, int actionPeriod, int animationPeriod,
			int resourceLimit, List<PImage> images) {
		return new Entity(EntityKind.DUDE_FULL, id, position, images, resourceLimit, 0, actionPeriod, animationPeriod,
				0, 0);
	}

	public static Entity createDudeNotFull(String id, Point position, int actionPeriod, int animationPeriod,
			int resourceLimit, List<PImage> images) {
		return new Entity(EntityKind.DUDE_NOT_FULL, id, position, images, resourceLimit, 0, actionPeriod,
				animationPeriod, 0, 0);
	}

	public boolean transformPlant(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
		if (entity.kind == EntityKind.TREE) {
			return transformTree(entity, world, scheduler, imageStore);
		} else if (entity.kind == EntityKind.SAPLING) {
			return transformSapling(entity, world, scheduler, imageStore);
		} else {
			throw new UnsupportedOperationException(String.format("transformPlant not supported for %s", entity));
		}
	}

	public boolean transformTree(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
		if (entity.health <= 0) {
			Entity stump = Functions.createStump(entity.id, entity.position,
					ImageStore.getImageList(imageStore, Functions.STUMP_KEY));

			Entity.removeEntity(world, entity);
			EventScheduler.unscheduleAllEvents(scheduler, entity);

			Functions.addEntity(world, stump);
			EventScheduler.scheduleActions(stump, scheduler, world, imageStore);

			return true;
		}

		return false;
	}

	public boolean transformSapling(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
		if (entity.health <= 0) {
			Entity stump = Functions.createStump(entity.id, entity.position,
					ImageStore.getImageList(imageStore, Functions.STUMP_KEY));

			Entity.removeEntity(world, entity);
			EventScheduler.unscheduleAllEvents(scheduler, entity);

			Functions.addEntity(world, stump);
			EventScheduler.scheduleActions(stump, scheduler, world, imageStore);

			return true;
		} else if (entity.health >= entity.healthLimit) {
			Entity tree = Functions.createTree("tree_" + entity.id, entity.position,
					Functions.getNumFromRange(Functions.TREE_ACTION_MAX, Functions.TREE_ACTION_MIN),
					Functions.getNumFromRange(Functions.TREE_ANIMATION_MAX, Functions.TREE_ANIMATION_MIN),
					Functions.getNumFromRange(Functions.TREE_HEALTH_MAX, Functions.TREE_HEALTH_MIN),
					ImageStore.getImageList(imageStore, Functions.TREE_KEY));

			Entity.removeEntity(world, entity);
			EventScheduler.unscheduleAllEvents(scheduler, entity);

			Functions.addEntity(world, tree);
			EventScheduler.scheduleActions(tree, scheduler, world, imageStore);

			return true;
		}

		return false;
	}

	public Entity createSapling(String id, Point position, List<PImage> images) {
		return new Entity(EntityKind.SAPLING, id, position, images, 0, 0, Functions.SAPLING_ACTION_ANIMATION_PERIOD,
				Functions.SAPLING_ACTION_ANIMATION_PERIOD, 0, Functions.SAPLING_HEALTH_LIMIT);
	}

}
