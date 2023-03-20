/**
 * A simple class representing a location in 2D space.
 */
public final class Point
{
    public final int x;
    public final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean equals(Object other) {
        return other instanceof Point && ((Point)other).x == this.x
                && ((Point)other).y == this.y;
    }

    public int hashCode() {
        int result = 17;
        result = result * 31 + x;
        result = result * 31 + y;
        return result;
    }
    
    public static Point nextPositionDude(
            Entity entity, WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.x - entity.position.x);
        Point newPos = new Point(entity.position.x + horiz, entity.position.y);

        if (horiz == 0 || Functions.isOccupied(world, newPos) && Functions.getOccupancyCell(world, newPos).kind != EntityKind.STUMP) {
            int vert = Integer.signum(destPos.y - entity.position.y);
            newPos = new Point(entity.position.x, entity.position.y + vert);

            if (vert == 0 || Functions.isOccupied(world, newPos) &&  Functions.getOccupancyCell(world, newPos).kind != EntityKind.STUMP) {
                newPos = entity.position;
            }
        }

        return newPos;
    }
    
    public static Point viewportToWorld(Viewport viewport, int col, int row) {
        return new Point(col + viewport.col, row + viewport.row);
    }

    
}
