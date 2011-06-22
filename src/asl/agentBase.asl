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
	!unload_packet.
	
+!select_packet: idle <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	check_norm_begin(MyName);
	?packet(PX,PY);
	.println("packet: ",PX," ",PY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T),Path);
	plan_path(MyName,PX,PY,Path);
	+my_packet(PX,PY);
	-idle;
	+moving;
	check_norm_end(MyName).

+!go_to_packet: moving <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	.findall(pos(MyName,X,Y,T),pos(MyName,X,Y,T),Path);
	if (Path \== []) {
		?current_pos(CX,CY);
		.println("current ",CX," ",CY);
		move(MyName,CX,CY,Path);
		-current_pos(CX,CY);
		update_pos(MyName,NX,NY);
		+current_pos(NX,NY);
		!go_to_packet;
	}
	else {
		-moving;
		+loading;
	}.

//+!load_packet: loading <-
//	?my_packet(PX,PY);
	

+!work2: true <-
	.my_name(MyNameTerm);
	.term2string(MyNameTerm,MyName);
	check_norm_begin(MyName);
	?go_to(MyName,DX,DY);
	.findall(pos(Name,X,Y,T),pos(Name,X,Y,T),L);
	plan_path(MyName,DX,DY,L);
	check_norm_end(MyName).