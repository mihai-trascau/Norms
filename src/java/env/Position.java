package env;

public class Position
{
	private int x;
	private int y;
	private int time;
	
	public enum DIRECTION
	{
		NORTH,
		SOUTH,
		EAST,
		WEST
	}
	
	public Position() {
		this(0,0);
	}
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
		this.time = 0;
	}
	
	public Position(int x, int y, int time) {
		this.x = x;
		this.y = y;
		this.time = time;
	}
	
	public Position(String pos) {
		this.x = Integer.parseInt(pos.substring(pos.indexOf('(')+1, pos.indexOf(',')));
		this.y = Integer.parseInt(pos.substring(pos.indexOf(',')+1, pos.lastIndexOf(',')));
		this.time = Integer.parseInt(pos.substring(pos.lastIndexOf(',')+1, pos.indexOf(')')));
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
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
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
	
	@Override
	public boolean equals(Object obj)
	{
		Position p = (Position)obj;
		if (this.x == p.getX() && this.y == p.getY()/* && this.time == p.getTime()*/)
			return true;
		return false;
	}
	
	public boolean like(Position p)
	{
		if (this.x != p.getX() || this.y != p.getY())
			return false;
		return true;
	}
	
	public Position resetTime(int k)
	{
		return new Position(this.x, this.y, this.time+k);
	}
	
	public String toString()
	{
		return "("+x+","+y+"):"+time;
	}
}
