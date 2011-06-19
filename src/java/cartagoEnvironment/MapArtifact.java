package cartagoEnvironment;

import java.io.File;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;

public class MapArtifact extends Artifact {
	
	private Map map;
	private Vector<Position> agentPosition;
	private int registeredAgents;
	private Vector<Integer> actionInThisRound;
	private int tick;
	private int currentPlanerID;
	
	void init() {
		map = new Map(new File("res/map.in"));
		agentPosition = new Vector<Position>();
		registeredAgents = 0;
		
		map.readMap();
		map.printMap();
		
		actionInThisRound = new Vector<Integer>();
		tick = 0;
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
		defineObsProperty("pos",registeredAgents,initPos.getX(),initPos.getY(),0);
		agentID.set(registeredAgents);
		registeredAgents++;
		
		actionInThisRound.add(0);
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
	
	@OPERATION
	public void initNorms() {
		int normID = 0;
		defineObsProperty("push_norm",
				normID,
				"+!norm_activation("+normID+",AgID) : true <-" +
						".println(\"check norm \","+normID+");" +
						".findall(pos(X,Y,T),pos(AgID,X,Y,T),L1);" +
						".findall(pos(X,Y,T),(pos(ID,X,Y,T) & not ID==AgID),L2);" +
						".intersection(L1,L2,L);" +
						".length(L,N);" +
						"N > 0;" +
						".",
				"",
				"+!norm_content("+normID+",AgID) : true <-" +
						"replanPath(AgID).",
				"facilitator"
				);
		normID++;
		/*defineObsProperty("push_norm",
				normID,
				"+!norm_activation("+normID+") : true <- .println(\"check \","+normID+").",
				"",
				"",
				"facilitator"
				);
		normID++;*/
		int[] normIDList = new int[normID];
		for (int i=0; i<normID; i++)
			normIDList[i] = i;
		defineObsProperty("norm_id_list", normIDList);
	}
	
	@OPERATION
	void replanPath(int agentID)
	{
		System.out.println("replaning "+agentID);
	}
	
	//adjacency list of a cell
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
	
	//BFS traversal
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
	
	boolean syncPlanGeneral(int agentID) {
		if (currentPlanerID == -1)
		{
			currentPlanerID = agentID;
			return true;
		}
		if (currentPlanerID == agentID)
			return true;
		return false;
	}
	
	@GUARD
	boolean syncPlan(int agentID, int x, int y) {
		return syncPlanGeneral(agentID);
	}
	
	@GUARD
	boolean syncCommitPath(int agentID, Object[] myPathObj) {
		boolean result = syncPlanGeneral(agentID);
		currentPlanerID = -1;
		return result;
	}
	
	@GUARD
	boolean syncReplan(int agentID, Object[] myPathObj, Object[] paths) {
		boolean result = syncPlanGeneral(agentID);
		currentPlanerID = -1;
		return result;
	}
	
	@OPERATION //(guard="syncPlan")
	void planPath(int agentID, int x, int y) {
		Vector<Position> pathVector = findPath(agentPosition.get(agentID), new Position(x,y));
		for (Position pos: pathVector)
			defineObsProperty("pos", agentID, pos.getX(), pos.getY(), pos.getTime());
	}	
}
