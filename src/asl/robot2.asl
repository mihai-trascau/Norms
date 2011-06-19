goTo(0,2,6).
goTo(1,2,7).

!start.

+!start: true <-
	?getMap(MAP_ID);
	register(MyID) [artifact_id(MAP_ID)];
	+myID(MyID);
	println("Registered with ID:",MyID);
	focus(MAP_ID);
	.wait(500);
	!work(MyID).
	
/* Find the MAP_ID */
+?getMap(MAP_ID): true <-
	lookupArtifact("map", MAP_ID).

-?getMap(MAP_ID): true <-
	.wait(10);
	?getMap(MAP_ID).

+push_norm(NormID, Activation, Expiration, Content, Source) : true <-
	.add_plan(Activation, Source);
	if (Expiration \== "") {
		.add_plan(Expiration, Source);
	}
	.add_plan(Content, Source);
	.println("added norm ",NormID).	
	
+!work(MyID): true <-
	?goTo(MyID,X,Y);
	planPath(MyID,X,Y);
	!check_all_norms(MyID).
	
+!check_all_norms(MyID) : true <-
	?norm_id_list(L);
	for (.member(NormID,L)) {
		!check_norm(NormID, MyID);
	}
	.println("checked all").	
		
+!check_norm(NormID, MyID) : true <-
	!norm_activation(NormID,MyID);
	!norm_content(NormID,MyID).
	
-!check_norm(NormID, MyID) : true <-
	true.
	