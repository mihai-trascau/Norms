package env;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class Map
{
	File mapFile;
	protected int[][] map;
	protected int height;
	protected int width;
	
	private Vector<Position> initialPositions;
	private Vector<Vector<Position>> packets;
	private Vector<Vector<Position>> trucks;
	
	public Map(File mapFile) {
		map = null;
        height = 0;
        width = 0;
        initialPositions = new Vector<Position>();
        packets = new Vector<Vector<Position>>();
		trucks = new Vector<Vector<Position>>();
        this.mapFile = mapFile;
	}
	
	public Map(Map m) {
		height = m.height;
		width = m.width;
		map = new int[height][width];
		for (int i=0; i<height; i++)
			for (int j=0; j<width; j++)
				map[i][j] = m.map[i][j];
		initialPositions = new Vector<Position>();
		packets = new Vector<Vector<Position>>();
		trucks = new Vector<Vector<Position>>();
	}
	
	public boolean isValid(Position p)
	{
		if (p.getX() >= 0 && p.getX() < height && p.getY() >= 0 && p.getY() < width)
			if (map[p.getX()][p.getY()] <= 0 && -p.getTime() < map[p.getX()][p.getY()])
				return true;
		return false;
	}
	
	public boolean isValidMove(Position p, Position.DIRECTION dir)
	{
		return isValid(p.getNextPosition(dir));
	}
	
	public void readMap()
	{
		try {
			Scanner scanner = new Scanner(mapFile);
			height = scanner.nextInt();
			width = scanner.nextInt();
			map = new int[height][width];
			
			for(int i = 0; i < height; i++)
				for (int j = 0; j < width; j++)
				{
					if(scanner.hasNextInt()) {
						map[i][j] = scanner.nextInt();
						if (map[i][j] >= 30) {
							int type = map[i][j] % 10;
							if (trucks.size() < type) {
								trucks.setSize(type);
								trucks.set(type-1, new Vector<Position>());
							}
							trucks.get(type-1).add(new Position(i,j));
						}
						else if (map[i][j] >= 20) {
							int type = map[i][j] % 10;
							if (packets.size() < type) {
								packets.setSize(type);
								packets.set(type-1, new Vector<Position>());
							}
							packets.get(type-1).add(new Position(i,j));
						}
							/*if (map[i][j] == 2)
							packets.add(new Position(i,j));
						if (map[i][j] == 3)
							trucks.add(new Position(i,j));*/
					}
					else
						throw new IOException();
				}
			
			int x, y;
			while(scanner.hasNextInt())
			{
				x = scanner.nextInt();
				if(scanner.hasNextInt())
					y = scanner.nextInt();
				else
					throw new IOException();
				initialPositions.add(new Position(x,y));
				
				/*x = scanner.nextInt();
				if(scanner.hasNextInt())
					y = scanner.nextInt();
				else
					throw new IOException();
				finalPositions.add(new Position(x,y));*/
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Unable to load map file !!!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("[ERROR] Unable to read map file !!!");
			e.printStackTrace();
		}
	}

    public void printMap()
    {
    	if(map != null)
    	{
    		if(height != 0 && width != 0)
    		{
    			for (int i = 0; i < height; i++)
    			{
    				String line = "";
	    			for (int j = 0; j < width; j++)
		    			line += map[i][j]+" ";
	    			System.out.println(line);
    			}
    		}
    	}
    	else
    		System.out.println("MAP NOT LOADED");
    }
    
    public void setPosition(int x, int y, int val) {
    	map[x][y] = val;
    }
    
    public int getPosition(int x, int y) {
    	return map[x][y];
    }
	
	public Position getInitialPosition()
	{
		if(map == null || initialPositions == null || initialPositions.size() == 0)
			return null;
		
		Position initP = null;
		if(initialPositions.size() == 1)
			initP = initialPositions.firstElement();
		else
		{	
			Random rand = new Random();
			int index = rand.nextInt(initialPositions.size()-1);
			initP = initialPositions.elementAt(index);
			initialPositions.remove(index);
		}
		return initP;
	}
	
	public Vector<Position> getInitialPositions() {
		return initialPositions;
	}
	
	public int getHeigth() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}

	public Vector<Vector<Position>> getPackets() {
		return packets;
	}

	public Vector<Vector<Position>> getTrucks() {
		return trucks;
	}
}
