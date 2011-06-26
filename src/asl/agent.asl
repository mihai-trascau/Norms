
norm(1, step(N) & N>0, N>100, not pos(9,8,_)).
norm(2, replanning & pos(Ag,X,Y,T), false, not pos(X,Y,T)).
norm(3, my_name(MyName) & pos(MyName,X,Y,T) & pos(Name,X,Y,T) & Name \== MyName, 
		false,
		.findall(1,pos(MyName,_,_,_),P1) & .findall(2,pos(Name,_,_,_),P2) & 
		.length(P1,N1) & .length(P2,N2) & (N1 < N2 | (N1==N2 & MyName < Name))).


pos("agent2",4,16,3).

my_name("agent1").

!start.

+!start3: true <-
	?check_norms(NormsOK);
	if (not NormsOK) {
		.println("Incalca norma");
	}
	else {
		.println("Satisface norma");
	}.

+!start2: true <-
	?norm(ID,A,E,C);
	if (A & not E & not C) {
		.println("Incalca norma");
	}
	else {
		.println("Satisface norma");
	}.

+?check_norms(Res): true <-
	.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
	+norms_infringed([]);
	for (.member(norm(ID,A,E,C),Norms)) {
		if (A & not E & not C) {
			+conflicting_norm;
			?norms_infringed(NormList);
			.concat(NormList,[ID],InfNormList);
			-+norms_infringed(InfNormList);
		}
	}
	if (conflicting_norm) {
		-conflicting_norm;
		Res = false;
	}
	else {
		Res = true;
		-norms_infringed(_);
	}.

+norm_string(Activation,Expiration,Content): true <-
	.term2string(ActivationTerm,Activation);
	.term2string(ExpirationTerm,Expiration);
	.term2string(ContentTerm,Content);
	+norm(ActivationTerm,ExpirationTerm,ContentTerm);
	-norm_string(Activation,Expiration,Content).

+?valid_neighbours(pos(X,Y,T),Res): true <-
	.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
	?neighbours(pos(X,Y),Neighbours);
	for (.member(pos(PX,PY),Neighbours)) {
		+pos(PX,PY,T);
		for (.member(norm(ID,A,E,C),Norms)) {
			if (A & not E & not C) {
				.println(pos(PX,PY,T)," because of ",ID);
				+bad_neighbour(pos(PX,PY,T));
			}
		}
		if (not bad_neighbour(pos(PX,PY,T))) {
			+valid_neighbour(pos(PX,PY,T));
		}
		else {
			-bad_neighbour(pos(PX,PY,T));
		}
		-pos(PX,PY,T);
	}
	.findall(P,valid_neighbour(P),Res);
	//.println(Res);
	.findall(valid_neighbour(P),valid_neighbour(P),L);
	for (.member(VN,L)) {-VN;}.

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

+?find_path(Dest,Path): true <-
	?step(Step);
	?my_name(MyName);
	?current_pos(Name,SX,SY);
	+queue(0,SX,SY,Step);
	+visited(SX,SY);
	while (.findall(queue(I,X,Y,T),queue(I,X,Y,T),Queue) & .length(Queue,Len) & Len>0) {
		.min(Queue,queue(I1,CX,CY,CT));
		-queue(I1,CX,CY,CT);
		?valid_neighbours(pos(CX,CY,CT+1),Neighbours);
		for (.member(pos(NX,NY,NT),Neighbours)) {
			if (not visited(NX,NY)) {
				.findall(queue(I,X,Y,T),queue(I,X,Y,T),Queue2);
				.max(Queue,queue(I2,_,_,_));
				+queue(I2+1,NX,NY,NT);
				+visited(NX,NY);
				+parent(pos(NX,NY,NT),pos(CX,CY,CT));
				if (pos(NX,NY)==Dest) {
					+path(pos(NX,NY,NT+2));
					+path(pos(NX,NY,NT+1));
					P = pos(NX,NY,NT);
					+current(P);
					while (current(Pos) & Pos \== pos(SX,SY,Step)) {
						+path(Pos);
						?parent(Pos,Parent);
						-+current(Parent);
					}
				}
			}
		}
	}
	.findall(pos(X,Y,T),path(pos(X,Y,T)),FirstPath);
	.findall(queue(I,X,Y,T),queue(I,X,Y,T),L1);
	for (.member(Q,L1)) {-Q;}
	.findall(visited(X,Y),visited(X,Y),L2);
	for (.member(V,L2)) {-V;}
	.findall(parent(X,Y),parent(X,Y),L3);
	for (.member(P,L3)) {-P;}
	.findall(path(X),path(X),L4);
	for (.member(P,L4)) {-P;}
	-current(_);
	for (.member(pos(FX,FY,FT),FirstPath)) {
		+pos(MyName,FX,FY,FT);
	}
	?check_norms(NormsOK);
	for (.member(pos(FX,FY,FT),FirstPath)) {
		-pos(MyName,FX,FY,FT);
	}
	if (not NormsOK & not replanning) {
		.println("Replaning");
		+replanning;
		?find_path(Dest,AlternativePath);
		Path = AlternativePath;
	}
	else {
		-replaning;
		Path = FirstPath;
	}.
	

	
+!start: true <-
	//.eval(E,not true);
	//.println(E);
	?get_map(MapID);
	.my_name(MyNameTerm);
	register(MyNameTerm) [artifact_id(MapID)];
	focus(MapID);
	+current_pos("agent1",3,17);
	+step(1);
	?find_path(pos(9,6),Path);
	.length(Path,Len);
	.println(Len," ",Path).
	
/* Find the MAP_ID */
+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).
	