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
	
	private int queuedActions;	
	
	void init() {
		map = new Map(new File("res/map.in"));
		agentPosition = new Vector<Position>();
		registeredAgents = 0;
		queuedActions = 0;
		
		map.readMap();
		map.printMap();
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
	}
	
	
	@OPERATION//(guard="actionSync")
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
				agentPosition.set(agentID, nextPos);
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
}
