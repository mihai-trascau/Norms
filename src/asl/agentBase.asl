!start.

+!start: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	register(MyNameTerm);
	get_initial_position(MyNameTerm,X,Y);
	+current_pos(X,Y);
	focus(MapID);
	+idle;
	T = 4;
	.term2string(R,"T>5");
	.println(R);
	.eval(E,R);
	.println(E);
	.wait(500);
	!work.
	
+?get_map(MapID): true <-
	lookupArtifact("map", MapID).

-?get_map(MapID): true <-
	.wait(10);
	?get_map(MapID).

+!work: true <-
	!select_packet;
	!go_to_packet;
	!load_packet;
	!select_truck;
	!go_to_truck;
	!unload_packet;
	!work.

+?choose_packet(X,Y): true <-
	.findall(packet(PX,PY),packet(PX,PY),L);
	.length(L,Len);
	.random(N1);
	N2 = N1*100;
	N3 = N2 mod Len;
	.nth(N3,L,packet(PX,PY));
	X = PX;
	Y = PY.

-?choose_packet(X,Y): true <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?base(BX,BY);
	set_done(MyName);
	+done;
	X = BX;
	Y = BY.

+?choose_truck(X,Y): true <-
	.findall(truck(PX,PY),truck(PX,PY),L);
	.length(L,Len);
	.random(N1);
	N2 = N1*100;
	N3 = N2 mod Len;
	.nth(N3,L,truck(PX,PY));
	X = PX;
	Y = PY.
	
+!select_packet: idle <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	check_norm_begin(MyName);
	?choose_packet(PX,PY);
	.println("selected packet: ",PX," ",PY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T) & MyName \== Name,Path);
	plan_path(MyName,PX,PY,Path);
	check_norm_end(MyName);
	?current_pos(CX,CY);
	.findall(T,pos(MyName,CX,CY,T),L);
	if (L == []) {
		+my_packet(PX,PY);
		-idle;
		+moving;
	}
	else {
		.println("IDLE");
		!select_packet;
	}.

+!go_to_packet: moving <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),Path);
	.length(Path,Len);
	if (Len > 2) {
		?current_pos(CX,CY);
		//.println("current ",CX," ",CY);
		move_to_packet(MyName,CX,CY,Path);
		-current_pos(CX,CY);
		update_pos(MyName,NX,NY);
		+current_pos(NX,NY);
		!go_to_packet;
	}
	else {
		.findall(done,done,D);
		if (D == []) {
			-moving;
			+loading;
		}
		else {
			!stay;
		}
	}.

+!load_packet: loading <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?my_packet(PX,PY);
	load_packet(MyName,PX,PY,[]);
	-my_packet(PX,PY);
	-loading;
	+planning.

+!select_truck: planning <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	check_norm_begin(MyName);
	?choose_truck(TX,TY);
	.println("selected truck: ",TX," ",TY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T) & MyName \== Name,Path);
	plan_path(MyName,TX,TY,Path);
	check_norm_end(MyName);
	?current_pos(CX,CY);
	.findall(T,pos(MyName,CX,CY,T),L);
	if (L == []) {
		+my_truck(TX,TY);
		-planning;
		+carrying;
	}
	else {
		.println("IDLE");
		!select_truck;
	}.
	

+!go_to_truck: carrying <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),Path);
	.length(Path,Len);
	if (Len > 2) {
		?current_pos(CX,CY);
		move_to_truck(MyName,CX,CY,Path);
		-current_pos(CX,CY);
		update_pos(MyName,NX,NY);
		+current_pos(NX,NY);
		!go_to_truck;
	}
	else {
		-carrying;
		+unloading;
	}.

+!unload_packet: unloading <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	?my_truck(TX,TY);
	unload_packet(MyName,TX,TY,[]);
	-my_truck(TX,TY);
	-unloading;
	+idle.

+!stay: true <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	stay(MyName,0,0,[]);
	!stay.
	