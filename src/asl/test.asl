norm(t>0,t<100,not pack(packet(2,13))).
!init.


+!init: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	focus(MapID);
	register(MyNameTerm) [artifact_id(MapID)].

+tick(N) : true <-
	-+step(N);
	!start.

+!start: true <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?current_pos(MyName,SX,SY);
	.println("Planning path from ",SX," ",SY);
	?path(pos(9,0),Path);
	+idle;
	.println("Path: ",Path);
	!select_packet.

+!select_packet : idle <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?current_pos(MyName,SX,SY);
	.findall(packet(math.abs(SX-PX)+math.abs(SY-PY),PX,PY),packet(PX,PY),Packets);
	.sort(Packets,SortedPackets);
	.println("Sorted packets: ",SortedPackets);
	.findall(norm(A,E,C),norm(A,E,C),Norms);
	for (.member(packet(D,X,Y),SortedPackets)) {
		+pack(packet(X,Y));
		for (.member(norm(A,E,C),Norms)) {
			if (not C) {
				+bad_packet;
			}
		}
		if (not bad_packet) {
			+selected_packet(packet(D,X,Y));
		}
		else {
			-bad_packet;
		}
		-pack(packet(X,Y));
	}
	.findall(packet(D,X,Y),selected_packet(packet(D,X,Y)),L);
	.min(L,SelectedPacket);
	for (.member(P,L)) {
		-selected_packet(P);
	}
	.println("Selected packet: ",SelectedPacket).

+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).

/* PATHFIDING */

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
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?current_pos(MyName,SX,SY);
	?step(Tick);
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