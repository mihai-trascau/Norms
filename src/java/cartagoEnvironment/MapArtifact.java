package cartagoEnvironment;

import java.io.File;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;

public class MapArtifact extends Artifact {
	
	private Map map;
	private Hashtable<String,Position> agentPosition;
	private int registeredAgents;
	private Vector<Integer> actionInThisRound;
	private int tick;
	private int currentPlanerID;
	
	void init() {
		map = new Map(new File("res/map.in"));
		agentPosition = new Hashtable<String,Position>();
		registeredAgents = 0;
		
		map.readMap();
		map.printMap();
		
		actionInThisRound = new Vector<Integer>();
		tick = 1;
	}	
	
	/**
	 * Registers the agent on the map, giving it an ID number and creating a new observable
	 * property stating the agent's current position.
	 * @param agentID - Feedback parameter indicating the id received by the agent upon
	 * registering on the map.
	 */
	@OPERATION
	void register(String name) {
		Position initPos = map.getInitialPosition();
		agentPosition.put(name, initPos);
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
				"+!norm_activation("+normID+",Results) : true <-" +
						".my_name(MyNameTerm);" +
						".term2string(MyNameTerm,MyName);" +
						".findall(pos(X,Y,T),pos(MyName,X,Y,T),L1);" +
						".findall(pos(X,Y,T),(pos(Name,X,Y,T) & not Name==MyName),L2);" +
						".intersection(L1,L2,L);" +
						".length(L,N);" +
						" N > 0;" +
						".member(pos(X,Y,T),L);" +
						".findall(Name,(pos(Name,X,Y,T) & not Name==MyName),ConflictAgents);" +
						" Results = ConflictAgents.",
				"",
				"+!norm_content("+normID+",Conflicts) : true <-" +
						".println(\"apply norm \","+normID+");" +
						".my_name(MyNameTerm);" +
						".term2string(MyNameTerm,MyName);" +
						".member(Conflict,Conflicts);" +
						".findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),L1);" +
						".findall(pos(X,Y,T),pos(Conflict,X,Y,T),L2);" +
						".length(L1,N1);" +
						".length(L2,N2);" +
						".println(\"lengths: \",N1,N2);" +
						" if (N2 < N1) {" +
						"	.println(\"case 1\");" +
						"	dropOldPlan(L1);" +
						"	?goTo(MyName,DX,DY);" +
						"	replanPath(MyName,DX,DY,L2);" +
						"}" +
						"else {" +
						"	.println(\"case 2\");" +
						"	term2string(ConflictTerm,Conflict);" +
						"	send(ConflictTerm,tell,replan(555555555555555));" +
						"}" +
						".println(ConflictTerm).",
				"facilitator"
				);
		normID++;
		int[] normIDList = new int[normID];
		for (int i=0; i<normID; i++)
			normIDList[i] = i;
		defineObsProperty("norm_id_list", normIDList);
	}
	
	@OPERATION
	void replanPath(String name, int x, int y, Object[] path) {
		System.out.println("replaning "+name);
		Map myMap = new Map(map);
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			myMap.setPosition(pos.getX(), pos.getY(), -pos.getTime());
		}
		planPath(name, x, y);
	}
	
	@OPERATION
	void dropOldPlan(Object[] path) {
		System.out.println(path[0]);
	}
	
	//adjacency list of a cell
	Vector<Position> getNeighbours(Position p) {
		Vector<Position> neighbours = new Vector<Position>();
		int x = p.getX();
		int y = p.getY();
		int t = p.getTime();
		Position neighbour = new Position(x, y-1, t+1);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x, y+1, t+1);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x-1, y, t+1);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		neighbour = new Position(x+1, y, t+1);
		if (map.isValid(neighbour))
			neighbours.add(neighbour);
		return neighbours;
	}
	
	//BF traversal
	Vector<Position> findPath(Position source, Position destination) {
		source.setTime(tick);
		Vector<Position> path = new Vector<Position>();
		if (source.equals(destination)) {
			path.add(source);
			return path;
		}
		LinkedList<Position> queue = new LinkedList<Position>();
		HashSet<Position> visited = new HashSet<Position>();
		Hashtable<Position, Position> parents = new Hashtable<Position, Position>();
		queue.add(source);
		while (!queue.isEmpty()) {
			Position current = queue.removeFirst();
			for (Position next: getNeighbours(current))
				if (!visited.contains(next)) {
					next.setTime(current.getTime()+1);
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
		return null;
	}
	
	@OPERATION //(guard="syncPlan")
	void planPath(String name, int x, int y) {
		Vector<Position> pathVector = findPath(agentPosition.get(name), new Position(x,y));
		for (Position pos: pathVector)
			defineObsProperty("pos", name, pos.getX(), pos.getY(), pos.getTime());
	}
	
	/*boolean syncPlanGeneral(int agentID) {
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
	}*/
}
