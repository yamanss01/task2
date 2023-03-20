import java.util.List;

import processing.core.PImage;

public interface ActionInterface {

	void executeAction(Action action, EventScheduler scheduler);

	void executeAnimationAction(Action action, EventScheduler scheduler);

	void executeActivityAction(Action action, EventScheduler scheduler);

	void executeSaplingActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler);

	void executeTreeActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler);

	void executeFairyActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler);

	void executeDudeNotFullActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler);

	void executeDudeFullActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler);

	void updateOnTime(EventScheduler scheduler, long time);

	boolean moveToFairy(Entity fairy, WorldModel world, Entity target, EventScheduler scheduler);

	Point nextPositionFairy(Entity entity, WorldModel world, Point destPos);

	boolean moveToNotFull(Entity dude, WorldModel world, Entity target, EventScheduler scheduler);

	boolean moveToFull(Entity dude, WorldModel world, Entity target, EventScheduler scheduler);

	void nextImage(Entity entity);

	boolean transformNotFull(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore);

	void transformFull(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore);

	boolean transformPlant(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore);

	boolean transformTree(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore);
	
	boolean transformSapling(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore);
	
	Entity createSapling(String id, Point position, List<PImage> images);
}
