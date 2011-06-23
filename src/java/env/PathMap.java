package env;

import java.util.Vector;

public class PathMap extends Map {
	private Vector<Integer>[][] pathMap;
	
	@SuppressWarnings("unchecked")
	public PathMap(Map sourceMap) {
		super(sourceMap);
		
		pathMap = new Vector[height][width];
		for(int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				pathMap[i][j] = new Vector<Integer>();
				pathMap[i][j].add(map[i][j]);
			}
	}
	
	public void addPositionOccupiedAt(int x, int y, int t) {
		pathMap[x][y].add(t);
	}
	
	public boolean isValid(Position p)
	{
		if (p.getX() >= 0 && p.getX() < height && p.getY() >= 0 && p.getY() < width)
			if (pathMap[p.getX()][p.getY()].firstElement() == 0)
			{
				for(int i = 1; i < pathMap[p.getX()][p.getY()].size(); i++)
					if(-p.getTime() == pathMap[p.getX()][p.getY()].get(i))
						return false;
				return true;
			}
		return false;
	}
}
