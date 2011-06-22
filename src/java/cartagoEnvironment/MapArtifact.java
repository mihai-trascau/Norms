package cartagoEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;
import env.GUI;

public class MapArtifact extends Artifact {
	
	private Map map;
	private Hashtable<String,Position> agentPosition;
	private int registeredAgents;
	private Vector<Integer> actionInThisRound;
	private int tick;
	private String currentNormChecker;
	private GUI gui;
	
	void init() throws IOException {
		map = new Map(new File("res/testmap.in"));
		agentPosition = new Hashtable<String,Position>();
		registeredAgents = 0;
		
		map.readMap();
		map.printMap();
		
		gui = new GUI(map);
		
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
		//Position initPos = map.getInitialPosition();
		Position initPos = map.getInitialPositions().get(registeredAgents);
		Position finalPos = map.getFinalPositions().get(registeredAgents);
		agentPosition.put(name, initPos);
		registeredAgents++;
		defineObsProperty("current_pos", name, initPos.getX(), initPos.getY());
		defineObsProperty("go_to", name, finalPos.getX(), finalPos.getY());
		
		gui.drawMap(agentPosition,null);
		
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
		String[] norm_content = new String[2];
		norm_content[0] = 
			"+!norm_content("+normID+",Conflicts) : true <-" +
			"	.println(\"inconsitency detected with norm \","+normID+");" +
			"	.my_name(MyNameTerm);" +
			"	.term2string(MyNameTerm,MyName);" +
			"	.member(Conflict,Conflicts);" +
			"	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),L1);" +
			"	.findall(pos(Conflict,X,Y,T),pos(Conflict,X,Y,T),L2);" +
			"	.length(L1,N1);" +
			"	.length(L2,N2);" +
			" 	if (N2 < N1) {" +
			"		.println(\"path replan caused by norm "+normID+"\");" +
			"		drop_path_plan(L1);" +
			"		?go_to(MyName,DX,DY);" +
			"		replan_path(MyName,DX,DY,L2);" +
			"	}" +
			"	else {" +
			"		.term2string(ConflictTerm,Conflict);" +
			"		if (N2 == N1) {" +
			"			if (MyName < Conflict) {" +
			"				.send(ConflictTerm,achieve,path_conflict(MyName,L1));" +
			"				.println(\"sent path conflict message (name based) to \",Conflict);" +
			"			}" +
			"			else {" +
			"				.println(\"must replan path due to \",Conflict);" +
			"				drop_path_plan(L1);" +
			"				?go_to(MyName,DX,DY);" +
			"				replan_path(MyName,DX,DY,L2);" +
			"			}" +
			"		}" +
			"		else {" +
			"			.send(ConflictTerm,achieve,path_conflict(MyName,L1));" +
			"			.println(\"sent path conflict message to \",Conflict);" +
			"		}" +
			"	}" +
			"	.println(\"plan now consistent with norm "+normID+"\").";
		norm_content[1] = 
			"+!path_conflict(RequesterName,RequesterPath) : true <-" +
			"	.my_name(MyNameTerm);" +
			"	 check_norm_begin(MyNameTerm);" +
			"	.println(\"path conflict message received from \",RequesterName);" +
			"	.term2string(MyNameTerm,MyName);" +
			"	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),L1);" +
			"	drop_path_plan(L1);" +
			"	?go_to(MyName,DX,DY);" +
			"	replan_path(MyName,DX,DY,RequesterPath);" +
			"	.println(\"resolved path conflict with \",RequesterName);" +
			"	 check_norm_end(MyNameTerm).";
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
						"Results = ConflictAgents.",
				"",
				norm_content,
				"facilitator"
				);
		normID++;
		int[] normIDList = new int[normID];
		for (int i=0; i<normID; i++)
			normIDList[i] = i;
		defineObsProperty("norm_id_list", normIDList);
	}
	
	@OPERATION
	void replan_path(String name, int x, int y, Object[] path) {
		System.out.println("replaning "+name);
		Map myMap = new Map(map);
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			myMap.setPosition(pos.getX(), pos.getY(), -pos.getTime());
		}
		Vector<Position> pathVector = findPath(agentPosition.get(name), new Position(x,y), myMap);
		if(pathVector != null)
			for (Position pos: pathVector)
				defineObsProperty("pos", name, pos.getX(), pos.getY(), pos.getTime());
		else
			defineObsProperty("idle", name, agentPosition.get(name).getX(), agentPosition.get(name).getY(), tick);
	}
	
	@OPERATION
	void drop_path_plan(Object[] path) {
		for (int i=0; i<path.length; i++) {
			String pos = (String)path[i];
			String[] splitPos = pos.substring(pos.indexOf('(')+1,pos.indexOf(')')).split(",");
			//removeObsProperty("pos");
			String name = splitPos[0].replace("\"", "");
			int x = Integer.parseInt(splitPos[1]);
			int y = Integer.parseInt(splitPos[2]);
			int t = Integer.parseInt(splitPos[3]);
			if(this.getObsPropertyByTemplate("pos", name, x, y, t) != null)
				removeObsPropertyByTemplate("pos", name, x, y, t);
			else
				System.out.println("path plan already deleted");
		}
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
	
	@OPERATION //(guard="syncPlan")
	void plan_path(String name, int x, int y) {
		Vector<Position> pathVector = findPath(agentPosition.get(name), new Position(x,y), map);
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
