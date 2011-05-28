package cartagoEnvironment;

import cartago.*;

public class Counter extends Artifact {
	
	void init()	{
		defineObsProperty("counter",0);
	}
	
	@OPERATION void inc() {
		ObsProperty prop = getObsProperty("counter");
		prop.updateValue(prop.intValue()+1);
		signal("tick");
	}
}
