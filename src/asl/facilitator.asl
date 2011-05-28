!start.

+!start: true
<-	!setupMap(MAP_ID).

+!setupMap(MAP_ID): true
<-	makeArtifact("map","cartagoEnvironment.MapArtifact",[],MAP_ID);
	println("Map initialised").
