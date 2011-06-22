!start.

+!start: true <-
	?get_map(MapID);
	.my_name(MyNameTerm);
	register(MyNameTerm);
	get_initial_position(MyNameTerm,X,Y);
	+current_pos(X,Y);
	focus(MapID);
	+idle;
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
	
+!select_packet: idle <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	check_norm_begin(MyName);
	?packet(PX,PY);
	.println("packet: ",PX," ",PY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T),Path);
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
	if (Path \== []) {
		?current_pos(CX,CY);
		.println("current ",CX," ",CY);
		move_to_packet(MyName,CX,CY,Path);
		-current_pos(CX,CY);
		update_pos(MyName,NX,NY);
		+current_pos(NX,NY);
		!go_to_packet;
	}
	else {
		-moving;
		+loading;
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
	?truck(TX,TY);
	.println("truck: ",TX," ",TY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T),Path);
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
		!select_truck;
	}.
	

+!go_to_truck: carrying <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),Path);
	if (Path \== []) {
		?current_pos(CX,CY);
		.println("current ",CX," ",CY);
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
