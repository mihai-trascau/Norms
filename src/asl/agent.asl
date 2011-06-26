//norm(true,false,not pos(9,8)).
norm(1,my_name(MyName) & pos(MyName,X,Y,T) & pos(Name,X,Y,T) & Name \== MyName,false,
.findall(1,pos(MyName,_,_,_),P1) & .findall(pos(Name,X1,Y1,T1),pos(Name,X1,Y1,T1),P2) & .length(P1,N1) & .length(P2,N2) & 
(N1 < N2 | (N1==N2 & MyName < Name))).

pos(agent1,2,3,1).
pos(agent1,2,4,2).
pos(agent1,2,5,3).
pos(agent1,2,6,4).

pos(agent2,1,4,1).
pos(agent2,2,4,2).
pos(agent2,3,4,3).
pos(agent2,3,5,4).

my_name(agent2).

!start2.

+!start2: true <-
	?norm(ID,A,E,C);
	if (A & not E & not C) {
		.println("Incalca norma");
	}
	else {
		.println("Satisface norma");
	}.

+?check_norms: true <-
	.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
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
		.fail;
	}.

+norm_string(Activation,Expiration,Content): true <-
	.term2string(ActivationTerm,Activation);
	.term2string(ExpirationTerm,Expiration);
	.term2string(ContentTerm,Content);
	+norm(ActivationTerm,ExpirationTerm,ContentTerm);
	-norm_string(Activation,Expiration,Content).

+?valid_neighbours(pos(X,Y),Res): true <-
	.findall(norm(A,E,C),norm(A,E,C),Norms);
	?neighbours(pos(X,Y),Neighbours);
	for (.member(N,Neighbours)) {
		+N;
		for (.member(norm(A,E,C),Norms)) {
			if (not C) {
				+bad_neighbour(N);
			}
		}
		if (not bad_neighbour(N)) {
			+valid_neighbour(N);
		}
		else {
			-bad_neighbour(N);
		}
		-N;
	}
	.findall(P,valid_neighbour(P),Res);
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

+?path(Dest,Path): true <-
	?current_pos(SX,SY);
	?tick(Tick);
	+queue(0,SX,SY,Tick);
	+visited(SX,SY);
	while (.findall(queue(I,X,Y,T),queue(I,X,Y,T),Queue) & .length(Queue,Len) & Len>0) {
		.min(Queue,queue(I1,CX,CY,T));
		-queue(I1,CX,CY,T);
		?valid_neighbours(pos(CX,CY),Neighbours);
		for (.member(pos(NX,NY),Neighbours)) {
			if (not visited(NX,NY)) {
				.findall(queue(I,X,Y,T),queue(I,X,Y,T),Queue2);
				.max(Queue,queue(I2,_,_,_));
				+queue(I2+1,NX,NY,T+1);
				+visited(NX,NY);
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
	.findall(pos(X,Y,T),path(pos(X,Y,T)),Path);
	.findall(queue(I,X,Y,T),queue(I,X,Y,T),L1);
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
	.length(Path,Len);
	.println(Len," ",Path).
	
/* Find the MAP_ID */
+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).
	