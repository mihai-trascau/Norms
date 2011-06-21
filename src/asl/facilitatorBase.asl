!start.

+!start: true <-
	!setupMap(MapID).

+!setupMap(MapID): true <-
	makeArtifact("map","cartagoEnvironment.MapArtifactBase",[],MapID);
	println("map initialised").