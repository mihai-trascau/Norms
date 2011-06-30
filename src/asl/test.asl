norm(0, step(N) & N > 0 & my_name(MyName) & loaded_packet(MyName,Type,_,_) & (Type == 1 | Type == 2), false, my_name(MyName) & truck(MyName,3,_,_)).
norm(1, step(N) & N > 0 & my_name(MyName) & loaded_packet(MyName,Type,_,_) & Type \== 1 & Type \== 2, false, my_name(MyName) & not truck(MyName,3,_,_)).
norm(2, step(N) & N > 0 & my_name(MyName) & loaded_packet(MyName,Type,_,_) & (Type == 3 | Type == 5), false, my_name(MyName) & truck(MyName,2,_,_)).
norm(3, step(N) & N > 0 & my_name(MyName) & loaded_packet(MyName,Type,_,_) & Type \== 3 & Type \== 5, false, my_name(MyName) & not truck(MyName,2,_,_)).

norm(4, replanning & pos(Ag,X,Y,T), false, not pos(X,Y,T)).
norm(5, my_name(MyName) & pos(MyName,X,Y,T) & pos(Name,X,Y,T) & Name \== MyName, 
		false,
		.findall(1,pos(MyName,_,_,_),P1) & .findall(2,pos(Name,_,_,_),P2) & 
		.length(P1,N1) & .length(P2,N2) & (N1 < N2 | (N1==N2 & MyName < Name))).

!init.


+!init: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	+my_name(MyName);
	+packet_selection;
	+step(0);
	focus(MapID);
	register(MyNameTerm) [artifact_id(MapID)].

+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).


/* PLANNING */
+tick(N) : N >= 1 & packet_selection <-
	-+step(N);
	.println("Tick ",N," [SELECTING PACKET]");
	?my_name(MyName);
	?current_pos(MyName,SX,SY);
	?step(N);
	sync_start(MyName);
	.findall(packet(math.abs(SX-PX)+math.abs(SY-PY),Type,PX,PY),packet(Type,PX,PY),Packets);
	if(Packets \== []) {
		.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
		+norms_infringed([]);
		+bad_packet_inf_norm([]);
		for (.member(packet(D,T,X,Y),Packets)) {
			+packet(MyName,T,X,Y);
			for (.member(norm(ID,A,E,C),Norms)) {
					if (A & not E & not C) {
						+bad_packet;
						?bad_packet_inf_norm(PacketInfNormList);
						.concat(PacketInfNormList,[ID],PINL);
						-+bad_packet_inf_norm(PINL);
					}
			}
			
			?norms_infringed(NormList);
			?bad_packet_inf_norm(PacketINL);
			if (PacketINL \== []) {
				.concat(NormList,[PacketINL],InfNormList);
				-+norms_infringed(InfNormList);
			}
			-+bad_packet_inf_norm([]);
			
			if (not bad_packet) {
				+selected_packet(packet(D,T,X,Y));
			}
			else {
				-bad_packet;
			}
			-packet(MyName,T,X,Y);
		}
		.findall(packet(D,T,X,Y),selected_packet(packet(D,T,X,Y)),L);
		if (L \== []) {
			.min(L,packet(D,T,X,Y));
			for (.member(P,L)) {
				-selected_packet(P);
			}
			.println("Selected packet: ",T," ",X," ",Y);
			-norms_infringed(_);
			
			?neighbours(pos(X,Y),Neighbours);
			if (Neighbours \== []) {
				register_packet(MyName,T,X,Y);
				.member(pos(NX,NY),Neighbours);
				?find_path(pos(NX,NY),Path);
				if (Path == []) {
					unregister_packet(MyName,T,X,Y);
					.println("No path found to ",pos(NX,NY));
					sync_end(MyName);
					report_action(MyName,0);
					do_action(MyName);
					stay(MyName,SX,SY);
				}
				else {
					-packet_selection;
					+moving;
					publish_path(MyName,Path);
					sync_end(MyName);
					report_action(MyName,1);
					do_action(MyName);
					planned(MyName,SX,SY,N);
				}
			}
			else {
				.println("No valid neighbours found for selected packet");
				sync_end(MyName);
				report_action(MyName,0);
				do_action(MyName);
				stay(MyName,SX,SY);
			}
		}
		else {
			.println("No packet selected due to norms");
			?norms_infringed(NormList);
			.send(facilitator,tell,norms_infringed(MyName,N,true,NormList));
			-norms_infringed(NormList);
			sync_end(MyName);
			report_action(MyName,0);
			do_action(MyName);
			stay(MyName,SX,SY);
		}
	}
	else {
		.println("No packets remaining, returning to depot");
		-packet_selection;
		+select_depot;
		sync_end(MyName);
		report_action(MyName,0);
		do_action(MyName);
		stay(MyName,SX,SY);
	}.

+tick(N) : N > 1 & moving <-
	-+step(N);
	.println("Tick ",N," [MOVING]");
	?my_name(MyName);
	?current_pos(MyName,CX,CY);
	!check_norms(NormsOK);
	if (not NormsOK) {
		.println("Norm checking failed while moving! Selecting a different packet!");
		-moving;
		+packet_selection;
		//scoruri pt pozitii
		if (pos(MyName,X1,Y1,T1) & pos(Name,X1,Y1,T1) & Name \== MyName) {
			if (pos(MyName,X2,Y2,T1+1)) {
				?direction(pos(X1,Y1),pos(X2,Y2),Dir);
				!update_score(X1,Y1,Dir,-10);
			}
		}
		//<<
		.findall(pos(PX,PY,PT),pos(MyName,PX,PY,PT),PathToRemove);
		unpublish_path(MyName,PathToRemove,CX,CY,N);
		?packet(MyName,T,X,Y);
		unregister_packet(MyName,T,X,Y);
		report_action(MyName,0);
		do_action(MyName);
		stay(MyName,CX,CY);
	} 
	else {
		.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),L);
		.length(L,Len);
		?pos(MyName,X,Y,N);
		//>>
		?direction(pos(CX,CY),pos(X,Y),Dir);
		!update_score(CX,CY,Dir,5);
		//<<
		if (Len <= 4) {
			-moving;
			+loading;
		}
		report_action(MyName,2);
		do_action(MyName);
		!check_norms(NormsOKAgain);
		if (not NormsOKAgain) {
			.println("move - changed mind");
			-moving;
			+packet_selection;
			.findall(pos(PX,PY,PT),pos(MyName,PX,PY,PT),PathToRemove);
			unpublish_path(MyName,PathToRemove,CX,CY,N);
			?packet(MyName,PT,PX,PY);
			unregister_packet(MyName,PT,PX,PY);
			stay(MyName,CX,CY);
		}
		else {
			move(MyName,CX,CY,X,Y,N);
		}
	}.

+tick(N) : N > 1 & loading <-
	-+step(N);
	.println("Tick ",N," [LOADING]");
	?my_name(MyName);
	?current_pos(MyName,CX,CY);
	!check_norms(NormsOK);
	if(not NormsOK) {
		.println("Norm checking failed while loading! Selecting a different packet!");
		-loading;
		+packet_selection;
		?packet(MyName,T,X,Y);
		unregister_packet(MyName,T,X,Y);
		report_action(MyName,0);
		do_action(MyName);
		stay(MyName,CX,CY);
	}
	else {
		-loading;
		+truck_selection;
		?packet(MyName,T,PX,PY);
		report_action(MyName,1);
		do_action(MyName);
		load(MyName,CX,CY,N,T,PX,PY);
	}.

+tick(N) : N > 1 & truck_selection <-
	-+step(N);
	.println("Tick ",N," [SELECTING TRUCK]");
	?my_name(MyName);
	?current_pos(MyName,SX,SY);
	sync_start(MyName);
	.findall(truck(math.abs(SX-PX)+math.abs(SY-PY),Type,PX,PY),truck(Type,PX,PY),Trucks);
	if (Trucks \== []) {
		.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
		+norms_infringed([]);
		+bad_truck_inf_norm([]);
		for (.member(truck(D,T,X,Y),Trucks)) {
			+truck(MyName,T,X,Y);
			for (.member(norm(ID,A,E,C),Norms)) {
					if (A & not E & not C) {
						+bad_truck;
						?bad_truck_inf_norm(TruckInfNormList);
						.concat(TruckInfNormList,[ID],TINL);
						-+bad_truck_inf_norm(TINL);
					}
			}
			
			?norms_infringed(NormList);
			?bad_truck_inf_norm(TruckINL);
			if (TruckINL \== []) {
				.concat(NormList,[TruckINL],InfNormList);
				-+norms_infringed(InfNormList);
			}
			-+bad_truck_inf_norm([]);
			
			if (not bad_truck) {
				+selected_truck(truck(D,T,X,Y));
			}
			else {
				-bad_truck;
			}
			-truck(MyName,T,X,Y);
		}
		.findall(truck(D,T,X,Y),selected_truck(truck(D,T,X,Y)),L);
		if (L \== []) {
			.min(L,truck(D,T,X,Y));
			for (.member(P,L)) {
				-selected_truck(P);
			}
			.println("Selected truck: ",T," ",X," ",Y);
			?norms_infringed(NormList);
			.send(facilitator,tell,norms_infringed(MyName,N,false,NormList));
			-norms_infringed(_);
			
			?neighbours(pos(X,Y),Neighbours);
			if (Neighbours \== []) {
				register_truck(MyName,T,X,Y);
				.member(pos(NX,NY),Neighbours);
				?find_path(pos(NX,NY),Path);
				if (Path == []) {
					.println("No path found to ",pos(NX,NY));
					unregister_truck(MyName,T,X,Y);
					sync_end(MyName);
					report_action(MyName,0);
					do_action(MyName);
					stay(MyName,SX,SY);
				}
				else {
					-truck_selection;
					+carrying;
					publish_path(MyName,Path);
					sync_end(MyName);
					report_action(MyName,1);
					do_action(MyName);
					planned(MyName,SX,SY,N);
				}
			}
			else {
				.println("No valid neighbours found for selected truck");
				sync_end(MyName);
				report_action(MyName,0);
				do_action(MyName);
				stay(MyName,SX,SY);
			}
		}
		else {
			.println("No truck selected due to norms");
			?norms_infringed(NormList);
			.send(facilitator,tell,norms_infringed(MyName,N,true,NormList));
			-norms_infringed(NormList);
			sync_end(MyName);
			report_action(MyName,0);
			do_action(MyName);
			stay(MyName,SX,SY);
		}
	}
	else {
		.println("!!! No trucks remaining !!!");
		sync_end(MyName);
	}.

+tick(N) : N > 1 & carrying <-
	-+step(N);
	.println("Tick ",N," [CARRYING]");
	?my_name(MyName);
	?current_pos(MyName,CX,CY);
	!check_norms(NormsOK);
	if (not NormsOK) {
		.println("Norm checking failed while carrying! Selecting a different truck!");
		-carrying;
		+truck_selection;
		//>>
		if (pos(MyName,X1,Y1,T1) & pos(Name,X1,Y1,T1) & Name \== MyName) {
			if (pos(MyName,X2,Y2,T1+1)) {
				?direction(pos(X1,Y1),pos(X2,Y2),Dir);
				!update_score(X1,Y1,Dir,-10);
			}
		}
		//<<
		.findall(pos(PX,PY,PT),pos(MyName,PX,PY,PT),PathToRemove);
		unpublish_path(MyName,PathToRemove,CX,CY,N);
		?truck(MyName,T,X,Y);
		unregister_truck(MyName,T,X,Y);
		report_action(MyName,0);
		do_action(MyName);
		stay(MyName,CX,CY);
	} 
	else {
		.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),L);
		.length(L,Len);
		?pos(MyName,X,Y,N);
		//>>
		?direction(pos(CX,CY),pos(X,Y),Dir);
		!update_score(CX,CY,Dir,5);
		//<<
		if (Len <= 4) {
			-carrying;
			+unloading;
		}
		report_action(MyName,2);
		do_action(MyName);
		!check_norms(NormsOKAgain);
		if (not NormsOKAgain) {
			.println("carry - changed mind");
			-carrying;
			+truck_selection;
			.findall(pos(PX,PY,PT),pos(MyName,PX,PY,PT),PathToRemove);
			unpublish_path(MyName,PathToRemove,CX,CY,N);
			?truck(MyName,TT,TX,TY);
			unregister_truck(MyName,TT,TX,TY);
			stay(MyName,CX,CY);
		}
		else {
			carry(MyName,CX,CY,X,Y,N);
		}
	}.

+tick(N) : N > 1 & unloading <-
	-+step(N);
	.println("Tick ",N," [UNLOADING]");
	?my_name(MyName);
	?current_pos(MyName,CX,CY);
	!check_norms(NormsOK);
	if(not NormsOK) {
		.println("Norm checking failed while unloading! Selecting a different truck!");
		-unloading;
		+packet_selection;
		?truck(MyName,T,X,Y);
		unregister_truck(MyName,T,X,Y);
		report_action(MyName,0);
		do_action(MyName);
		stay(MyName,CX,CY);
	}
	else {
		-unloading;
		+packet_selection;
		?loaded_packet(MyName,LPT,LPX,LPY);
		?truck(MyName,TT,TX,TY);
		report_action(MyName,1);
		do_action(MyName);
		unload(MyName,CX,CY,N,LPT,LPX,LPY,TT,TX,TY);
	}.

+tick(N) : N > 1 & select_depot <-
	-+step(N);
	.println("Tick ",N," [RETURN TO DEPOT]");
	?my_name(MyName);
	?current_pos(MyName,SX,SY);
	?step(N);
	sync_start(MyName);
	?depot(DX,DY);
	?find_path(pos(DX,DY),Path);
	if (Path == []) {
		.println("No path found to ",pos(DX,DY));
		.send(facilitator,tell,norms_infringed(MyName,N,false,NormList));
		sync_end(MyName);
		report_action(MyName,0);
		do_action(MyName);
		stay(MyName,SX,SY);
	}
	else {
		register_depot(MyName,DX,DY);
		-select_depot;
		+move_to_depot;
		!send_positions_score;
		publish_path(MyName,Path);
		sync_end(MyName);
		report_action(MyName,1);
		do_action(MyName);
		planned(MyName,SX,SY,N);
	}.

+tick(N) : N > 1 & move_to_depot <-
	-+step(N);
	.println("Tick ",N," [MOVING TO DEPOT]");
	?my_name(MyName);
	?current_pos(MyName,CX,CY);
	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),L);
	.length(L,Len);
	?pos(MyName,X,Y,N);
	if (Len > 3) {
	.findall(current_pos(Name,CPOSX,CPOSY),current_pos(Name,CPOSX,CPOSY),Cposes);
	move(MyName,CX,CY,X,Y,N);
	}
	else {
		-move_to_depot;
		+idle;
		report_action(MyName,1);
		do_action(MyName);
		idle(MyName,CX,CY);
	}.

+tick(N) : N > 1 & idle <-
	-+step(N);
	.println("Tick ",N," [IDLING]");
	?my_name(MyName);
	?current_pos(MyName,CX,CY);
	report_action(MyName,1);
	do_action(MyName);
	idle(MyName,CX,CY).


/* PATHFINDING */
+?valid_neighbours(pos(X,Y,T),Res): true <-
	.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
	?neighbours(pos(X,Y),Neighbours);
	for (.member(pos(PX,PY),Neighbours)) {
		+pos(PX,PY,T);
		for (.member(norm(ID,A,E,C),Norms)) {
			if (A & not E & not C) {
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
	?current_pos(MyName,SX,SY);
	+queue(0,SX,SY,Step);
	+visited(SX,SY);
	while (.findall(queue(I,X,Y,T),queue(I,X,Y,T),Queue) & .length(Queue,Len) & Len>0) {
		.min(Queue,queue(I1,CX,CY,CT));
		-queue(I1,CX,CY,CT);
		?valid_neighbours(pos(CX,CY,CT+1),Neighbours);
		for (.member(pos(NX,NY,NT),Neighbours)) {
			if (not visited(NX,NY)) {
				.findall(queue(I,X,Y,T),queue(I,X,Y,T),Queue2);
				if (Queue2 == []) {
					I2 = 1;
				}
				else {
					.max(Queue2,queue(I2,_,_,_));
				}
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
	!check_norms(NormsOK);
	//>>
	if (pos(MyName,X1,Y1,T1) & pos(Name,X1,Y1,T1) & Name \== MyName) {
		if (pos(MyName,X2,Y2,T1+1)) {
			?direction(pos(X1,Y1),pos(X2,Y2),Dir);
			!update_score(X1,Y1,Dir,-10);
		}
	}
	//<<
	for (.member(pos(FX,FY,FT),FirstPath)) {
		-pos(MyName,FX,FY,FT);
	}
	if (not NormsOK & not replanning) {
		+replanning;
		.println("Replanning");
		?find_path(Dest,AlternativePath);
		Path = AlternativePath;
	}
	else {
		-replaning;
		Path = FirstPath;
	}.


/* NORM CHECKING */
+!check_norms(Res): true <-
	?step(N);
	.findall(norm(ID,A,E,C),norm(ID,A,E,C),Norms);
	?my_name(MyName);
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
		?norms_infringed(NormList);
		-norms_infringed(NormList);
		Res = false;
		.send(facilitator,tell,norms_infringed(MyName,N,false,NormList));
	}
	else {
		Res = true;
		?norms_infringed(NormList);
		.send(facilitator,tell,norms_infringed(MyName,N,false,NormList));
		-norms_infringed(_);
	}.
	
+?direction(pos(X,Y),pos(X-1,Y),north).
+?direction(pos(X,Y),pos(X+1,Y),south).
+?direction(pos(X,Y),pos(X,Y-1),west).
+?direction(pos(X,Y),pos(X,Y+1),east).
+?direction(pos(X,Y),pos(X,Y),still).

+!update_score(X,Y,D,V): true <-
	if (score(X,Y,D,Score)) {
		-score(X,Y,D,Score);
		+score(X,Y,D,Score+V);
	}
	else {
		+score(X,Y,D,V);
	}.

+!send_positions_score: true <-
	?my_name(MyName);
	.findall(score(X,Y,Dir,Score),score(X,Y,Dir,Score),Scores);
	.send(facilitator,tell,scores(MyName,Scores)).