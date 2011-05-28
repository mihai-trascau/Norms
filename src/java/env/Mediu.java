package env;

// Environment code for project HelloWorld

import jason.asSyntax.*;
import jason.environment.*;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.*;

public class Mediu extends TimeSteppedEnvironment
{
	
	private Map map;
	private HashMap<String,Position> agentPositions;
	private int nbAgents;
	
	Random moveGenerator;
	
	private Logger logger = Logger.getLogger("HelloWorld."+Mediu.class.getName());

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args)
    {
        super.init(new String[] {"3000"});
        setOverActionsPolicy(OverActionsPolicy.queue);
        
        map = new Map(new File("res//map.in"));
        map.readMap();
        map.printMap();
        
        agentPositions = new HashMap<String, Position>();
        
        moveGenerator = new Random();
        
        nbAgents = 0;
    }

    @Override
    public boolean executeAction(String agName, Structure action)
    {
    	String type = action.getFunctor();
    	if(type.equals("register"))
    	{
    		Position initPos = map.getInitialPosition();
    		agentPositions.put(agName, initPos);
    		addPercept(agName,Literal.parseLiteral("pos("+initPos.getX()+","+initPos.getY()+")"));
    		this.setNbAgs(nbAgents++);
    	}
    	
    	if(type.equals("unregister"))
    	{
    		this.setNbAgs(nbAgents--);
    		if(nbAgents == 0) stop();
    	}
    	
    	// AICI E O MARE MIZERIE
    	if(type.equals("next"))
    	{
    		int dir = moveGenerator.nextInt(4);
    		Position agentPos = agentPositions.get(agName);
    		while(!map.isValidMove(agentPos,Position.DIRECTION.values()[dir]))
    			dir = moveGenerator.nextInt(4);
    		
    		Position nextPos = agentPos.getNextPosition(Position.DIRECTION.values()[dir]);
    		agentPositions.put(agName, nextPos);
    		removePercept(agName, Literal.parseLiteral("pos("+agentPos.getX()+","+agentPos.getY()+")"));
    		addPercept(agName, Literal.parseLiteral("pos("+nextPos.getX()+","+nextPos.getY()+")"));
    		
    		if(agName.equals("scout1"))
    			logger.info(">>	"+agName+" "+this.getStep());
    		else
    			logger.info(">>		"+agName+" "+this.getStep());
    	}
    	
        return true;
    }
    
    
    private long sum = 0;
    protected void stepFinished(int step, long time, boolean timeout)
    {
    	long mean = (step > 0 ? sum / step : 0);
    	logger.info("step "+step+" finished in "+time+" ms. mean = "+mean);
    	sum += time;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop()
    {
        super.stop();
    }
}