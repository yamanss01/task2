
public interface WorldViewInterface {

	 void shiftView(WorldView view, int colDelta, int rowDelta);
	 
	 void drawViewport(WorldView view);
	 
	 void drawBackground(WorldView view);
	 
	 void drawEntities(WorldView view);
	 
	 Point worldToViewport(Viewport viewport, int col, int row);
}
