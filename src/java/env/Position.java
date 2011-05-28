package env;

public class Position
{
	private int x;
	private int y;
	
	public enum DIRECTION
	{
		NORTH,
		SOUTH,
		EAST,
		WEST
	}
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Position() {
		this(0,0);
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void setPosition(Position newPosition)
	{
		this.x = newPosition.getX();
		this.y = newPosition.getY();
	}
	
	public void update(Position.DIRECTION dir)
	{
		switch(dir)
		{
		case NORTH:
			this.x--;
			break;
		case SOUTH:
			this.x++;
			break;
		case EAST:
			this.y++;
			break;
		case WEST:
			this.y--;
			break;
		}
	}
	
	public Position getNextPosition(Position.DIRECTION dir)
	{
		Position newPos = new Position(x,y);
		
		switch(dir)
		{
		case NORTH:
			newPos.setX(x-1);
			break;
		case SOUTH:
			newPos.setX(x+1);
			break;
		case EAST:
			newPos.setY(y+1);
			break;
		case WEST:
			newPos.setY(y-1);
			break;
		}
		
		return newPos;
	}
	
	public boolean equals(Position p)
	{
		if(this.x != p.getX() || this.y != p.getY())
			return false;
		return true;
	}
}
