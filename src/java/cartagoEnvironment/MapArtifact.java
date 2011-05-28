package cartagoEnvironment;

import java.io.File;
import java.util.Vector;

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
	
	@GUARD
	boolean actionSync(int agentID, int dir)
	{
		System.out.println("[MAP_ARTIFACT]:	"+agentID+" -> q="+queuedActions);
		if(queuedActions < registeredAgents)
		{
			queuedActions++;
			System.out.println("[MAP_ARTIFACT]:	"+agentID+" -> q="+queuedActions);
			return false;
		}
		else
		{
			queuedActions = 0;
			System.out.println("[MAP_ARTIFACT]:	"+agentID+" -> executing");
			return true;
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
}
