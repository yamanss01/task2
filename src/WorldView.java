import java.util.Optional;

import processing.core.PApplet;
import processing.core.PImage;

public final class WorldView implements WorldViewInterface{
	public PApplet screen;
	public WorldModel world;
	public int tileWidth;
	public int tileHeight;
	public Viewport viewport;

	public WorldView() {
	}

	public WorldView(int numRows, int numCols, PApplet screen, WorldModel world, int tileWidth, int tileHeight) {
		this.screen = screen;
		this.world = world;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.viewport = new Viewport(numRows, numCols);
	}

	public void shiftView(WorldView view, int colDelta, int rowDelta) {
		int newCol = Functions.clamp(view.viewport.col + colDelta, 0, view.world.numCols - view.viewport.numCols);
		int newRow = Functions.clamp(view.viewport.row + rowDelta, 0, view.world.numRows - view.viewport.numRows);

		Viewport.shift(view.viewport, newCol, newRow);
	}

	public void drawViewport(WorldView view) {
		drawBackground(view);
		drawEntities(view);
	}

	public void drawBackground(WorldView view) {
		for (int row = 0; row < view.viewport.numRows; row++) {
			for (int col = 0; col < view.viewport.numCols; col++) {
				Point worldPoint = Point.viewportToWorld(view.viewport, col, row);
				Optional<PImage> image = Background.getBackgroundImage(view.world, worldPoint);
				if (image.isPresent()) {
					view.screen.image(image.get(), col * view.tileWidth, row * view.tileHeight);
				}
			}
		}
	}

	public void drawEntities(WorldView view) {
		for (Entity entity : view.world.entities) {
			Point pos = entity.position;

			if (Viewport.contains(view.viewport, pos)) {
				Point viewPoint = worldToViewport(view.viewport, pos.x, pos.y);
				view.screen.image(ImageStore.getCurrentImage(entity), viewPoint.x * view.tileWidth,
						viewPoint.y * view.tileHeight);
			}
		}
	}

	public Point worldToViewport(Viewport viewport, int col, int row) {
		return new Point(col - viewport.col, row - viewport.row);
	}
}
