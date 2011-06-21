!start.

+!start: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	register(MyNameTerm) [artifact_id(MapID)];
	focus(MapID);
	.wait(500);
	!work.
	
+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).

+!work: true <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	check_norm_begin(MyName);
	?go_to(MyName,DX,DY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T),L);
	.println(L);
	plan_path(MyName,DX,DY,L);
	check_norm_end(MyName).