package cartagoEnvironment;

import java.io.File;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;

public class MapArtifactBase extends Artifact {
	
	private Map map;
	private Hashtable<String,Position> agentPosition;
	private int registeredAgents;
	private Vector<Integer> actionInThisRound;
	private int tick;
	private String currentNormChecker;
	
	void init() {
		map = new Map(new File("res/map.in"));
		agentPosition = new Hashtable<String,Position>();
		registeredAgents = 0;
		
		map.readMap();
		map.printMap();
		
		actionInThisRound = new Vector<Integer>();
		tick = 1;
	}	
	
	@OPERATION
	void register(String name) {
		//Position initPos = map.getInitialPosition();
		Position initPos = map.getInitialPositions().get(registeredAgents);
		Position finalPos = map.getFinalPositions().get(registeredAgents);
		agentPosition.put(name, initPos);
		registeredAgents++;
		defineObsProperty("current_pos", name, initPos.getX(), initPos.getY());
		defineObsProperty("go_to", name, finalPos.getX(), finalPos.getY());
		actionInThisRound.add(0);
	}
	
	//adjacency list of a cell
	Vector<Position> getNeighbours(Position p, Map myMap) {
		Vector<Position> neighbours = new Vector<Position>();
		int x = p.getX();
		int y = p.getY();
		int t = p.getTime();
		Position neighbour = new Position(x, y-1, t+1);
		if (myMap.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x, y+1, t+1);
		if (myMap.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x-1, y, t+1);
		if (myMap.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x+1, y, t+1);
		if (myMap.isValid(neighbour))
			neighbours.add(neighbour);
		return neighbours;
	}
	
	//BF traversal
	Vector<Position> findPath(Position source, Position destination, Map myMap) {
		source.setTime(tick);
		Vector<Position> path = new Vector<Position>();
		if (source.equals(destination)) {
			path.add(source);
			return path;
		}
		LinkedList<Position> queue = new LinkedList<Position>();
		Vector<Position> visited = new Vector<Position>();
		Hashtable<Position, Position> parents = new Hashtable<Position, Position>();
		queue.add(source);
		visited.add(source);
		while (!queue.isEmpty()) {
			Position current = queue.removeFirst();
			for (Position next: getNeighbours(current, myMap))
			{
				if (!visited.contains(next)) {
					visited.add(next);
					queue.add(next);
					parents.put(next, current);
					//destination reached
					if (next.equals(destination)) {
						Position p = next;
						do {
							path.add(0, p);
							p = parents.get(p);
						} while (!p.equals(source));
						path.add(0, p);
						//for (int i=0; i<path.size(); i++)
						//	path.get(i).setTime(i);
						return path;
					}
				}
			}
		}
		return null;
	}
	
	@OPERATION
	void plan_path(String name, int x, int y, Object[] path) {
		System.out.println("planing "+name);
		Map myMap = new Map(map);
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			if (pos.getTime() > -myMap.getPosition(pos.getX(), pos.getY()))
				myMap.setPosition(pos.getX(), pos.getY(), -pos.getTime());
		}
		myMap.printMap();
		Vector<Position> pathVector = findPath(agentPosition.get(name), new Position(x,y), myMap);
		if(pathVector != null)
			for (Position pos: pathVector)
				defineObsProperty("pos", name, pos.getX(), pos.getY(), pos.getTime());
		else
			defineObsProperty("idle", name, agentPosition.get(name).getX(), agentPosition.get(name).getY(), tick);
	}
	
	@OPERATION (guard="syncBeginNormCheck")
	void check_norm_begin(String name) {}
	
	@OPERATION (guard="syncEndNormCheck")
	void check_norm_end(String name) {}
	
	@GUARD
	boolean syncBeginNormCheck(String name) {
		if (currentNormChecker == null)
		{
			currentNormChecker = name;
			return true;
		}
		return false;
	}
	
	@GUARD
	boolean syncEndNormCheck(String name) {
		if (currentNormChecker.equals(name))
		{
			currentNormChecker = null;
			return true;
		}
		return false;
	}
}