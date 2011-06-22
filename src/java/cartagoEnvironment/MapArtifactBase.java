package cartagoEnvironment;

import java.io.File;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;
import env.GUI;

public class MapArtifactBase extends Artifact {
	
	private Map map;
	private int registeredAgents;
	private Hashtable<String,Position> agentPosition;
	private Hashtable<String,AgentState> agentState;
	private Hashtable<String,Boolean> actionInThisRound;
	private int tick;
	private String currentNormChecker;
	private GUI gui;
	
	void init() {
		map = new Map(new File("res/map3.in"));
		agentPosition = new Hashtable<String,Position>();
		agentState = new Hashtable<String, AgentState>();
		registeredAgents = 0;
		
		map.readMap();
		map.printMap();
		
		gui = new GUI(map);
		
		Vector<Position> packets = map.getPackets();
		if (packets != null)
			for (Position p: packets)
				defineObsProperty("packet", p.getX(), p.getY());
		Vector<Position> trucks = map.getTrucks();
		if (trucks != null)
			for (Position p: trucks)
				defineObsProperty("truck", p.getX(), p.getY());
		
		actionInThisRound = new Hashtable<String,Boolean>();
		tick = 0;
	}	
	
	@OPERATION
	void register(String name) {
		//Position initPos = map.getInitialPosition();
		//Position finalPos = map.getFinalPositions().get(registeredAgents);
		
		Position initPos = map.getInitialPositions().get(registeredAgents);
		agentPosition.put(name, initPos);
		agentState.put(name, AgentState.IDLE);
		actionInThisRound.put(name,false);
		registeredAgents++;
		
		gui.drawMap(agentPosition,agentState);
		
		//defineObsProperty("current_pos", name, initPos.getX(), initPos.getY());
		//defineObsProperty("go_to", name, finalPos.getX(), finalPos.getY());
	}
	
	@OPERATION
	void get_initial_position(String name, OpFeedbackParam<Integer> x, OpFeedbackParam<Integer> y) {
		Position pos = agentPosition.get(name);
		x.set(pos.getX());
		y.set(pos.getY());
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
						//path.add(0, p);
						return path;
					}
				}
			}
		}
		return null;
	}
	
	@OPERATION (guard="synchronize")
	void plan_path(String name, int x, int y, Object[] path) {
		System.out.println("("+name+") "+"planing "+name);
		Map myMap = new Map(map);
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			if (pos.getTime() > -myMap.getPosition(pos.getX(), pos.getY()))
				myMap.setPosition(pos.getX(), pos.getY(), -pos.getTime());
		}
		
		Position packet = new Position(x,y,Integer.MAX_VALUE);
		Vector<Position> neighbours = getNeighbours(packet, myMap);
		
		if (neighbours != null) {
			Vector<Position> pathVector = findPath(agentPosition.get(name), neighbours.get(0), myMap);
			if(pathVector != null) {
				for (Position pos: pathVector)
					defineObsProperty("pos", name, pos.getX(), pos.getY(), pos.getTime());
				removeObsPropertyByTemplate("packet", x, y);
				actionInThisRound.put(name,true);
				agentState.put(name, AgentState.PLANNING);
				return;
			}
		}
		System.out.println("plan_path: path not found!");
		defineObsProperty("idle", name, agentPosition.get(name).getX(), agentPosition.get(name).getY(), tick);
		actionInThisRound.put(name,true);
		agentState.put(name, AgentState.IDLE);
	}
	
	@OPERATION (guard="synchronize")
	void move(String name, int x, int y, Object[] path) {
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			if (pos.getTime() == tick) {
				agentPosition.put(name, pos);
				removeObsPropertyByTemplate("pos", name, pos.getX(), pos.getY(), pos.getTime());
				actionInThisRound.put(name,true);
				agentState.put(name, AgentState.MOVING);
				return;
			}
		}
		System.out.println("move: next pos ilegal!");
		agentState.put(name, AgentState.MOVING);
		actionInThisRound.put(name,true);
	}
	
	@OPERATION
	void update_pos(String name, OpFeedbackParam<Integer> x, OpFeedbackParam<Integer> y) {
		Position pos = agentPosition.get(name);
		x.set(pos.getX());
		y.set(pos.getY());
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
	
	@GUARD
	boolean synchronize(String name, int x, int y, Object[] path) {
		if (!actionInThisRound.contains(false)) {
			for (String str: actionInThisRound.keySet())
				actionInThisRound.put(str, false);
			gui.drawMap(agentPosition, agentState);
			tick++;
		}
		if (actionInThisRound.get(name) == false)
			return true;
		return false;
	}
}