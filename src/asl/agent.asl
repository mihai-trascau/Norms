!start.

+?valid(pos(X,Y)):true <-
	?map(X,Y,V);
	V == 0.

+?neighbours(pos(X,Y),Res): true <-
	+neighbours_list([]);
	?neighbours_list(L1);
	if (map(X-1,Y,V) & V==0) {
		.concat(L1,[pos(X-1,Y)],LL1);
		-+neighbours_list(LL1);
	}
	?neighbours_list(L2);
	if (map(X+1,Y,V) & V==0) {
		.concat(L2,[pos(X+1,Y)],LL2);
		-+neighbours_list(LL2);
	}
	?neighbours_list(L3);
	if (map(X,Y-1,V) & V==0) {
		.concat(L3,[pos(X,Y-1)],LL3);
		-+neighbours_list(LL3);
	}
	?neighbours_list(L4);
	if (map(X,Y+1,V) & V==0) {
		.concat(L4,[pos(X,Y+1)],LL4);
		-+neighbours_list(LL4);
	}
	?neighbours_list(L5);
	-neighbours_list(L5);
	Res = L5.

+?path(Dest,Path): true <-
	?current_pos(SX,SY);
	?tick(Tick);
	+queue(SX,SY,Tick);
	+visited(SX,SY);
	while (.findall(queue(X,Y,T),queue(X,Y,T),Queue) & .length(Queue,Len) & Len>0) {
		?queue(CX,CY,T);
		-queue(CX,CY,T);
		?neighbours(pos(CX,CY),Neighbours);
		for (.member(pos(NX,NY),Neighbours)) {
			if (not visited(NX,NY)) {
				+visited(NX,NY);
				+queue(NX,NY,T+1);
				+parent(pos(NX,NY,T+1),pos(CX,CY,T));
				if (pos(NX,NY)==Dest) {
					P = pos(NX,NY,T+1);
					+current(P);
					while (current(Pos) & Pos \== pos(SX,SY,Tick)) {
						+path(Pos);
						?parent(Pos,Parent);
						-+current(Parent);
					}
				}
			}
		}
	}
	.findall(path(X,Y,T),path(pos(X,Y,T)),Path);
	.findall(queue(X,Y,T),queue(X,Y,T),L1);
	for (.member(Q,L1)) {-Q;}
	.findall(visited(X,Y),visited(X,Y),L2);
	for (.member(V,L2)) {-V;}
	.findall(parent(X,Y),parent(X,Y),L3);
	for (.member(P,L3)) {-P;}
	.findall(path(X),path(X),L4);
	for (.member(P,L4)) {-P;}
	-current(_).
	
+!start: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	register(MyNameTerm) [artifact_id(MapID)];
	focus(MapID);
	+current_pos(3,17);
	+tick(1);
	?path(pos(9,6),Path);
	.println(Path).

+!start2: true <-
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
	