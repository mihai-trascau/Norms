package cartagoEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import cartago.*;

import env.Map;
import env.Position;
import env.GUI;

public class MapArtifact extends Artifact {
	
	private Map map;
	private int registeredAgents;
	private Hashtable<String,Position> agentPosition;
	private Hashtable<String,AgentState> agentState;
	private Hashtable<String,Boolean> actionInThisRound;
	private Hashtable<String,Vector<Integer>> infringedNorms;
	private Hashtable<String,Integer> reportedActions;
	private int tick;
	private String currentAgent;
	private GUI gui;
	
	void init() throws IOException {
		
		// Initialize map
		map = new Map(new File("res/map4.in"));
		map.readMap();
		map.printMap();
		
		agentPosition = new Hashtable<String, Position>();
		agentState = new Hashtable<String, AgentState>();
		actionInThisRound = new Hashtable<String, Boolean>();
		infringedNorms = new Hashtable<String, Vector<Integer>>();
		reportedActions = new Hashtable<String, Integer>();
		
		System.out.println(map.getHeigth()+" "+map.getWidth());
		
		gui = new GUI(map);
		
		registeredAgents = 0;
		tick = 1;
		
		for (int i=0; i<map.getHeigth(); i++)
			for (int j=0; j<map.getWidth(); j++) 
				defineObsProperty("map", i, j, map.getPosition(i, j));
		
		Vector<Vector<Position>> packets = map.getPackets();
		if (packets != null)
			for (int i=0; i<packets.size(); i++)
				if (packets.get(i) != null)
					for (Position p: packets.get(i))
						defineObsProperty("packet", i+1, p.getX(), p.getY());
		Vector<Vector<Position>> trucks = map.getTrucks();
		if (trucks != null)
			for (int i=0; i<trucks.size(); i++)
				if (trucks.get(i) != null)
					for (Position p: trucks.get(i))
						defineObsProperty("truck", i+1, p.getX(), p.getY());
		
		Vector<Position> depot = map.getDepot();
		for (Position pos: depot)
			defineObsProperty("depot", pos.getX(), pos.getY());
		
		//-----------------
		
		Scanner scanner = new Scanner(new File("res/policy3.in"));
		Hashtable<Position,Integer> policy = new Hashtable<Position, Integer>();
		int policyMap[][] = new int[10][19];
		for (int i=0; i<10; i++)
			for (int j=0; j<19; j++)
				policyMap[i][j] = -1;
		for (int i=0; i<173; i++) {
			int x=0,y=0,dir=0;
			x = scanner.nextInt();
			y = scanner.nextInt();
			dir = scanner.nextInt();
			policy.put(new Position(x,y), dir);
			policyMap[x][y] = dir;
			scanner.nextInt();
		}
		System.out.println(policy);
		gui.drawPolicy(null, policyMap);
		
		//-----------------
	}
	
	@OPERATION
	void register(String name) {
		Position initPos = map.getInitialPosition();
		agentPosition.put(name, initPos);
		registeredAgents++;
		
		defineObsProperty("current_pos", name, initPos.getX(), initPos.getY());
		defineObsProperty("pos", name, initPos.getX(), initPos.getY(), tick);
		defineObsProperty("pos", name, initPos.getX(), initPos.getY(), tick-1);
		
		if(registeredAgents == 5)
			signal("tick",tick);
		
		agentState.put(name, AgentState.IDLE_LOADING);
		actionInThisRound.put(name,false);
		gui.drawMap(agentPosition,agentState);
	}
	
	void registerAction(String name) {
		if(actionInThisRound.get(name) == false) {
			actionInThisRound.put(name,true);
			reportedActions.put(name,Integer.MAX_VALUE);
		}
		if(!actionInThisRound.contains(false)) {
			for(String agentName : actionInThisRound.keySet())
				actionInThisRound.put(agentName, false);
			reportedActions.clear();
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			gui.drawMap(agentPosition, agentState);
			tick++;
			signal("tick",tick);
		}
	}

	@OPERATION
	void report_action(String name, int action) {
		reportedActions.put(name, action);
	}
	
	@OPERATION (guard="barrier_action")
	void do_action(String name) {}
	
	@GUARD
	boolean barrier_action(String name) {
		if (reportedActions.size() < registeredAgents)
			return false;
		if (reportedActions.get(name) == Collections.min(reportedActions.values()))
			return true;
		return false;
	}
	
	@OPERATION
	void stay(String name, int x, int y) {
		removeObsPropertyByTemplate("pos", name, x, y, tick-1);
		defineObsProperty("pos", name, x, y, tick+1);
		agentState.put(name, AgentState.PLANNING);
		registerAction(name);
	}
	
	@OPERATION
	void idle(String name, int x, int y) {
		removeObsPropertyByTemplate("pos", name, x, y, tick-1);
		defineObsProperty("pos", name, x, y, tick+1);
		agentState.put(name, AgentState.IDLE_LOADING);
		registerAction(name);
	}
	
	@OPERATION
	void move(String name, int prev_x, int prev_y, int x, int y, int t) {
		removeObsPropertyByTemplate("pos", name, prev_x, prev_y, t-1);
		getObsPropertyByTemplate("current_pos", name, prev_x, prev_y).updateValues(name, x, y);
		agentState.put(name, AgentState.MOVING);
		agentPosition.put(name, new Position(x, y, t));
		registerAction(name);
	}
	
	@OPERATION
	void carry(String name, int prev_x, int prev_y, int x, int y, int t) {
		removeObsPropertyByTemplate("pos", name, prev_x, prev_y, t-1);
		getObsPropertyByTemplate("current_pos", name, prev_x, prev_y).updateValues(name, x, y);
		agentState.put(name, AgentState.CARRYING);
		agentPosition.put(name, new Position(x, y, t));
		registerAction(name);
	}
	
	@OPERATION
	void load(String name, int x, int y, int t, int type, int px, int py) {
		removeObsPropertyByTemplate("pos",name,x,y,t-1);
		removeObsPropertyByTemplate("packet",name,type,px,py);
		defineObsProperty("loaded_packet", name,type,px,py);
		agentState.put(name, AgentState.LOADING);
		map.setPosition(px, py, 1);
		registerAction(name);
	}
	
	@OPERATION
	void unload(String name, int x, int y, int t, int ptype, int px, int py, int ttype, int tx, int ty) {
		removeObsPropertyByTemplate("pos",name,x,y,t-1);
		removeObsPropertyByTemplate("loaded_packet",name,ptype,px,py);
		removeObsPropertyByTemplate("truck",name,ttype,tx,ty);
		agentState.put(name, AgentState.PLANNING);
		map.setPosition(tx, ty, 40+ptype);
		registerAction(name);
	}
	
	@OPERATION
	void publish_path(String name, Object[] path) {
		for (int i = 0; i < path.length; i++) {
			Position pos = new Position((String)path[i]);
			defineObsProperty("pos",name,pos.getX(),pos.getY(),pos.getTime());
		}
	}
	
	@OPERATION
	void unpublish_path(String name, Object[] path, int x, int y, int time) {
		for (int i = 0; i < path.length; i++) {
			Position pos = new Position((String)path[i]);
			removeObsPropertyByTemplate("pos",name,pos.getX(),pos.getY(),pos.getTime());
		}
		defineObsProperty("pos", name, x, y, time-1);
		defineObsProperty("pos", name, x, y, time);
	}
	
	@OPERATION
	void planned(String name, int x, int y, int t) {
		removeObsPropertyByTemplate("pos",name,x,y,t-1);
		agentState.put(name, AgentState.PLANNING);
		registerAction(name);
	}

	@OPERATION
	void register_packet(String name, int type, int x, int y) {
		removeObsPropertyByTemplate("packet", type, x, y);
		defineObsProperty("packet", name, type, x, y);
	}
	
	@OPERATION
	void unregister_packet(String name, int type, int x, int y) {
		removeObsPropertyByTemplate("packet", name, type, x, y);
		defineObsProperty("packet", type, x, y);
	}
	
	@OPERATION
	void register_truck(String name, int type, int x, int y) {
		removeObsPropertyByTemplate("truck", type, x, y);
		defineObsProperty("truck", name, type, x, y);
	}
	
	@OPERATION
	void unregister_truck(String name, int type, int x, int y) {
		removeObsPropertyByTemplate("truck", name, type, x, y);
		defineObsProperty("truck", type, x, y);
	}
	
	@OPERATION
	void register_depot(String name, int x, int y) {
		removeObsPropertyByTemplate("depot", x, y);
		defineObsProperty("depot", name, x, y);
	}
	
	/*@OPERATION
	void report_infringed_norms(Object[] norms) {
		for (int i=0; i<norms.length; i++)
		{
			int norm = ((Byte)norms[i]).intValue();
			if (infringedNorms.get(norm) == null)
				infringedNorms.put(norm, 1);
			else
				infringedNorms.put(norm, infringedNorms.get(norm)+1);
		}
		System.out.println("Infringed norms: "+infringedNorms);
	}*/
	
	@OPERATION
	void report_infringed_norms(String name, Object[] norms) {
		for (int i=0; i<norms.length; i++)
		{
			int norm = ((Byte)norms[i]).intValue();
			if (infringedNorms.get(name) == null) {
				Vector<Integer> normIDs = new Vector<Integer>();
				normIDs.add(norm);
				infringedNorms.put(name, normIDs);
			}
			else
				infringedNorms.get(name).add(norm);
		}
		System.out.println("Infringed norms: "+infringedNorms);
	}
	
	/// TODO - DE RESCRIS INIT NORMS
	@OPERATION
	public void initNorms() {
		int normID = 0;
		
		normID++;
		int[] normIDList = new int[normID];
		for (int i=0; i<normID; i++)
			normIDList[i] = i;
		defineObsProperty("norm_id_list", normIDList);
	}
	
	@OPERATION (guard="syncBegin")
	void sync_start(String name) {
		System.out.println("SYNC BEGIN "+name);
	}
	
	@OPERATION (guard="syncEnd")
	void sync_end(String name) {
		System.out.println("SYNC END "+name);
	}
	
	@GUARD
	boolean syncBegin(String name) {
		if(actionInThisRound.get(name) == true)
			return false;
		if (currentAgent == null) {
			currentAgent = name;
			return true;
		}
		return false;
	}
	
	@GUARD
	boolean syncEnd(String name) {
		if (currentAgent.equals(name)) {
			currentAgent = null;
			return true;
		}
		return false;
	}
}
