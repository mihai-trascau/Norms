package cartagoEnvironment;

import java.io.File;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;
import env.Position.DIRECTION;

public class MapArtifact extends Artifact {
	
	private Map map;
	private Vector<Position> agentPosition;
	private int registeredAgents;
	private Vector<Integer> actionInThisRound;	//AT
	private int tick;							//AT
	
	private int queuedActions;
	
	void init() {
		map = new Map(new File("res/map.in"));
		agentPosition = new Vector<Position>();
		registeredAgents = 0;
		queuedActions = 0;
		
		map.readMap();
		map.printMap();
		
		actionInThisRound = new Vector<Integer>();	//AT
		tick = 0;									//AT
		
		/*Position[] path1 = {new Position(0,2,0), new Position(1,2,1), new Position(1,3,2), new Position(1,4,3), 
				new Position(2,4,4), new Position(3,4,5)};
		Position[] path2 = {new Position(1,1,0), new Position(1,2,1), new Position(1,3,2), new Position(1,4,3),
				new Position(1,5,4)};
		Position[] path3 = {new Position(4,4,8), new Position(3,4,9), new Position(2,4,10)};
		this.defineObsProperty("path1", path1, 0);
		this.defineObsProperty("path2", path2, 0);
		this.defineObsProperty("path3", path3, 0);*/
	}	
	
	/**
	 * Registers the agent on the map, giving it an ID number and creating a new observable
	 * property stating the agent's current position.
	 * @param agentID - Feedback parameter indicating the id received by the agent upon
	 * registering on the map.
	 */
	@OPERATION
	void register(OpFeedbackParam<Integer> agentID) {
		Position initPos = map.getInitialPosition();
		agentPosition.add(initPos);
		defineObsProperty("pos",registeredAgents,initPos.getX(),initPos.getY());
		agentID.set(registeredAgents);
		registeredAgents++;
		
		actionInThisRound.add(0);
	}
	
	@OPERATION (guard="synchronize")
	void move(int agentID, int dir) {
		Position nextPos = agentPosition.get(agentID).getNextPosition(DIRECTION.values()[dir]);
		if(!map.isValid(nextPos))
			failed("Invalid move","invalid_move");
		else
		{
			ObsProperty prop = getObsPropertyByTemplate(
					"pos",
					agentID,
					agentPosition.get(agentID).getX(),
					agentPosition.get(agentID).getY());
			if(prop == null)
					System.err.println("NULL PROPERTY");
			else
			{
				prop.updateValues(agentID,nextPos.getX(),nextPos.getY());
				registerAction(agentID);
				agentPosition.set(agentID, nextPos);
				System.out.println(agentID + ": " + tick + " " + nextPos.toString());
			}
		}
	}
	
	void registerAction(int agentID) {
		actionInThisRound.set(agentID, 1);
	}
	
	@GUARD
	boolean synchronize(int agentID, int dir) {
		if (!actionInThisRound.contains(0)) {
			for (int i=0; i<actionInThisRound.size(); i++)
				actionInThisRound.set(i, 0);
			tick++;
		}
		if (actionInThisRound.get(agentID) == 0)
			return true;
		return false;
	}
	
	Vector<Position> getNeighbours(Position p) {
		Vector<Position> neighbours = new Vector<Position>();
		int x = p.getX();
		int y = p.getY();
		Position neighbour = new Position(x, y-1);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x, y+1);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x-1, y);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x+1, y);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		return neighbours;
	}
	
	Vector<Position> findPath(Position source, Position destination) {
		LinkedList<Position> queue = new LinkedList<Position>();
		HashSet<Position> visited = new HashSet<Position>();
		Hashtable<Position, Position> parents = new Hashtable<Position, Position>();
		queue.add(source);
		while (!queue.isEmpty()) {
			Position first = queue.removeFirst();
			for (Position next: getNeighbours(first))
				if (!visited.contains(next)) {
					visited.add(next);
					queue.add(next);
					parents.put(next, first);
					//destination reached
					if (next.equals(destination)) {
						Vector<Position> path = new Vector<Position>();
						Position p = next;
						do {
							path.add(0, p);
							p = parents.get(p);
						} while (!p.equals(source));
						path.add(0, p);
						for (int i=0; i<path.size(); i++)
							path.get(i).setTime(i);
						return path;
					}
				}
		}
		return null;
	}
	
	@OPERATION
	void plan(int agentID, int x, int y) {
		Vector<Position> pathVector = findPath(agentPosition.get(agentID), new Position(x,y));
		Position[] pathArray = new Position[pathVector.size()];
		for (int i=0; i<pathVector.size(); i++)
			pathArray[i] = pathVector.get(i);
		defineObsProperty("path", agentID, pathArray, 0);
		
		/*Vector<Position> pathVector = findPath(agentPosition.get(agentID), new Position(x,y));
		String[] pathArray = new String[pathVector.size()];
		for (int i=0; i<pathVector.size(); i++)
			pathArray[i] = pathVector.get(i).toString() + ":" + i;
		defineObsProperty("path", agentID, pathArray, 0);*/
	}
	
	@OPERATION
	void replan(int agentID, Object[] myPathObj, Object[] paths) {
		Vector<Position> myPath = new Vector<Position>();
		for (int i=0; i<myPathObj.length; i++)
			myPath.add((Position)myPathObj[i]);
		
		//for each path in list of paths
		for (int pathNo=0; pathNo<paths.length; pathNo++) {
			Object[] pathObj = (Object[])paths[pathNo];
			Vector<Position> path = new Vector<Position>();
			for (int i=0; i<pathObj.length; i++)
				path.add((Position)pathObj[i]);
			
			for (int i=0; i<myPath.size(); i++) {
				if (path.contains(myPath.get(i))) {
					Position index = findOverlaping(myPath, path, i, path.indexOf(myPath.get(i)));
					for (i=index.getX(); i<=index.getY(); i++)
						myPath.add(i, myPath.get(i-1).resetTime(1));
					for (i=index.getY()+1; i<myPath.size(); i++)
						myPath.set(i, myPath.get(i).resetTime(index.getY()-index.getX()+1));
					System.out.println("partial path: "+myPath);
					continue;
				}
			}
		}
		System.out.println("   final path: "+myPath);
		Position[] pathArray = new Position[myPath.size()];
		for (int i=0; i<myPath.size(); i++)
			pathArray[i] = myPath.get(i);
		//defineObsProperty("path", agentID, pathArray, 1);
		ObsProperty prop = getObsPropertyByTemplate("path", agentID);
		prop.updateValues(agentID, pathArray, 1);
	}
	
	Position findOverlaping(Vector<Position> myPath, Vector<Position> path, int index1, int index2) {
		int down = index1;
		int up = index1;
		TreeSet<Integer> index = new TreeSet<Integer>();
		index.add(index2);
		while (true) {
			if (down-1 < 0)
				break;
			Position pos = myPath.get(down-1);
			if (index.first()-1>=0 && pos.like(path.get(index.first()-1))) {
				index.add(index.first()-1);
				down--;
			}
			else if (index.last()+1<path.size() && pos.like(path.get(index.last()+1))) {
				index.add(index.last()+1);
				down--;
			}
			else
				break;
		}
		while (true) {
			if (up+1 >= myPath.size())
				break;
			Position pos = myPath.get(up+1);
			if (index.first()-1>=0 && pos.like(path.get(index.first()-1))) {
				index.add(index.first()-1);
				up++;
			}
			else if (index.last()+1<path.size() && pos.like(path.get(index.last()+1))) {
				index.add(index.last()+1);
				up++;
			}
			else
				break;
		}
		return new Position(down, up+1);
	}
}
