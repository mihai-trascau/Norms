package cartagoEnvironment;

import java.io.File;
import java.util.*;

import cartago.*;

import env.Map;
import env.PathMap;
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
		agentState.put(name, AgentState.IDLE_LOADING);
		actionInThisRound.put(name,false);
		registeredAgents++;
		defineObsProperty("pos", name, initPos.getX(), initPos.getY(), tick);
		
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
						Position load_unload = new Position(next.getX(), next.getY(), next.getTime()+1);
						Position plan = new Position(next.getX(), next.getY(), next.getTime()+2);
						if (!myMap.isValid(load_unload) || !myMap.isValid(plan))
							return null;
						path.add(load_unload);
						path.add(plan);
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
		System.out.println("("+name+") "+"planing");
		Position currentPos = agentPosition.get(name);
		System.out.println("pos "+name+" "+currentPos.getX()+" "+currentPos.getY()+" "+tick);
		removeObsPropertyByTemplate("pos", name, currentPos.getX(), currentPos.getY(), tick);
		
		PathMap myMap = new PathMap(map);
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			//if (pos.getTime() > -myMap.getPosition(pos.getX(), pos.getY()))
			myMap.addPositionOccupiedAt(pos.getX(), pos.getY(), -pos.getTime());
		}
		
		Position packet = new Position(x,y,Integer.MAX_VALUE);
		Vector<Position> neighbours = getNeighbours(packet, myMap);
		
		if (neighbours != null) {
			Vector<Position> pathVector = findPath(currentPos, neighbours.get(0), myMap);
			if(pathVector != null) {
				for (Position pos: pathVector)
					defineObsProperty("pos", name, pos.getX(), pos.getY(), pos.getTime());
				if (agentState.get(name) == AgentState.IDLE_LOADING || agentState.get(name) == AgentState.UNLOADING)
					removeObsPropertyByTemplate("packet", x, y);
				else if (agentState.get(name) == AgentState.IDLE_UNLOADING || agentState.get(name) == AgentState.LOADING)
					removeObsPropertyByTemplate("truck", x, y);
				agentState.put(name, AgentState.PLANNING);
				actionInThisRound.put(name,true);
				System.out.println("("+name+") "+pathVector);
				return;
			}
		}
		System.out.println("("+name+") "+"plan_path: PATH NOT FOUND!");
		defineObsProperty("pos", name, agentPosition.get(name).getX(), agentPosition.get(name).getY(), tick+1);
		if (agentState.get(name) == AgentState.IDLE_LOADING || agentState.get(name) == AgentState.UNLOADING)
			agentState.put(name, AgentState.IDLE_LOADING);
		else if (agentState.get(name) == AgentState.IDLE_UNLOADING || agentState.get(name) == AgentState.LOADING)
			agentState.put(name, AgentState.IDLE_UNLOADING);
		actionInThisRound.put(name,true);
	}
	
	@OPERATION (guard="synchronize")
	void move_to_packet(String name, int x, int y, Object[] path) {
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			if (pos.getTime() == tick) {
				agentPosition.put(name, pos);
				System.out.println("("+name+") current_pos: "+pos);
				removeObsPropertyByTemplate("pos", name, pos.getX(), pos.getY(), pos.getTime());
				agentState.put(name, AgentState.MOVING);
				actionInThisRound.put(name,true);
				return;
			}
		}
		System.out.println("("+name+") "+"move_to_packet: NEXT POS ILEGAL!");
		agentState.put(name, AgentState.MOVING);
		actionInThisRound.put(name,true);
	}
	
	@OPERATION (guard="synchronize")
	void move_to_truck(String name, int x, int y, Object[] path) {
		for (int i=0; i<path.length; i++) {
			Position pos = new Position((String)path[i]);
			if (pos.getTime() == tick) {
				agentPosition.put(name, pos);
				System.out.println("("+name+") current_pos: "+pos);
				removeObsPropertyByTemplate("pos", name, pos.getX(), pos.getY(), pos.getTime());
				agentState.put(name, AgentState.CARRYING);
				actionInThisRound.put(name,true);
				return;
			}
		}
		System.out.println("("+name+") "+"move_to_truck: NEXT POS ILEGAL!");
		agentState.put(name, AgentState.CARRYING);
		actionInThisRound.put(name,true);
	}
	
	@OPERATION
	void update_pos(String name, OpFeedbackParam<Integer> x, OpFeedbackParam<Integer> y) {
		Position pos = agentPosition.get(name);
		x.set(pos.getX());
		y.set(pos.getY());
	}
	
	@OPERATION (guard="synchronize")
	void load_packet(String name, int x, int y, Object[] path) {
		System.out.println("("+name+") "+"loading "+x+" "+y);
		Position currentPos = agentPosition.get(name);
		removeObsPropertyByTemplate("pos", name, currentPos.getX(), currentPos.getY(), tick);
		map.setPosition(x, y, 1);
		agentState.put(name, AgentState.LOADING);
		actionInThisRound.put(name,true);
	}
	
	@OPERATION (guard="synchronize")
	void unload_packet(String name, int x, int y, Object[] path) {
		System.out.println("("+name+") "+"unloading "+x+" "+y);
		Position currentPos = agentPosition.get(name);
		removeObsPropertyByTemplate("pos", name, currentPos.getX(), currentPos.getY(), tick);
		map.setPosition(x, y, 4);
		agentState.put(name, AgentState.UNLOADING);
		actionInThisRound.put(name,true);
	}
	
	@OPERATION (guard="syncBeginNormCheck")
	void check_norm_begin(String name) {
		System.out.println("(( "+name+" )) BEGIN at "+tick+":	"+actionInThisRound);
	}
	
	@OPERATION (guard="syncEndNormCheck")
	void check_norm_end(String name) {
		System.out.println("(( "+name+" )) END at "+tick+":	"+actionInThisRound);
	}
	
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
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gui.drawMap(agentPosition, agentState);
			tick++;
		}
		if (actionInThisRound.get(name) == false)
			return true;
		return false;
	}
}