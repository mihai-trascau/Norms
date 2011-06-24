!start.

+!start: true <-
	!setupMap(MapID).

+!setupMap(MapID): true <-
	makeArtifact("map","cartagoEnvironment.MapArtifact",[],MapID);
	.println("map initialised").