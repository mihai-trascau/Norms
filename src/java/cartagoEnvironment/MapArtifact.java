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
		
		System.out.println(findPath(new Position(0,0), new Position(4,7)));
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
	
	void registerAction(int agentID)
	{
		actionInThisRound.set(agentID, 1);
	}
	
	@GUARD
	boolean synchronize(int agentID, int dir)
	{
		if (!actionInThisRound.contains(0))
		{
			for (int i=0; i<actionInThisRound.size(); i++)
				actionInThisRound.set(i, 0);
			tick++;
		}
		if (actionInThisRound.get(agentID) == 0)
			return true;
		return false;
	}
	
	public Vector<Position> getNeighbours(Position p)
	{
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
	
	public Vector<Position> findPath(Position source, Position destination)
	{
		LinkedList<Position> queue = new LinkedList<Position>();
		HashSet<Position> visited = new HashSet<Position>();
		Hashtable<Position, Position> parents = new Hashtable<Position, Position>();
		queue.add(source);
		while (!queue.isEmpty())
		{
			Position first = queue.removeFirst();
			for (Position next: getNeighbours(first))
				if (!visited.contains(next))
				{
					visited.add(next);
					queue.add(next);
					parents.put(next, first);
					//destination reached
					if (next.equals(destination))
					{
						Vector<Position> path = new Vector<Position>();
						Position p = next;
						do
						{
							path.add(0, p);
							p = parents.get(p);
						} while (!p.equals(source));
						path.add(0, p);
						return path;
					}
				}
		}
		return null;
	}
	
	@OPERATION
	public void plan(int agentID, int x, int y, OpFeedbackParam<Position[]> path)
	{
		Vector<Position> pathVector = findPath(agentPosition.get(agentID), new Position(x,y));
		Position[] pathArray = new Position[pathVector.size()];
		for (int i=0; i<pathVector.size(); i++)
			pathArray[i] = pathVector.get(i);
		path.set(pathArray);
		
		/*int i=0;
		for (Position p: findPath(agentPosition.get(agentID), new Position(x,y)))
		{
			defineObsProperty("position", agentID, p.getX(), p.getY(), i);
			i++;
		}*/
	}
}
