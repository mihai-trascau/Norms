!start.

+!start: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	register(MyNameTerm) [artifact_id(MapID)];
	focus(MapID);
	.wait(500);
	!work.
	
/* Find the MAP_ID */
+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).

+push_norm(NormID, Activation, Expiration, Content, Source) : true <-
	.add_plan(Activation, Source);
	if (Expiration \== "") {
		.add_plan(Expiration,Source);
	}
	.add_plan(Content, Source);
	.println("added norm ",NormID," to norm base").	
	
+!work: true <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?go_to(MyName,X,Y);
	plan_path(MyName,X,Y);
	!check_all_norms.
	
+!check_all_norms : true <-
	?norm_id_list(L);
	.my_name(MyNameTerm);
	check_norm_begin(MyNameTerm);
	for (.member(NormID,L)) {
		!check_norm(NormID);
	}
	.println("consistent with all norms");
	check_norm_end(MyNameTerm).
		
+!check_norm(NormID) : true <-
	.println("check consistency with norm ",NormID);
	!norm_activation(NormID,Conflicts);
	!norm_content(NormID,Conflicts).
	
-!check_norm(NormID) : true <-
	.println("nu s-a activat norma ",NormID).
	