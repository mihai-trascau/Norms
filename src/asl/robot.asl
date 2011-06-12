goTo(0,0,7).
goTo(1,4,7).

norm([time(5),time(6)],[time(10)],work).

!start.

/*	Initialization
 *		- acquire MAP_ID (MapArtifact created by the facilitator agent)
 *		- focus the map artifact
 *		- register on the map
 */
 
+norm(Act, Exp, Norm): true
<-	?acceptNorm(Act, Exp, Norm);
	!processNorm(Act, Exp, Norm).
	
+?acceptNorm(Act, Exp, Norm): true
<-	true.

+!processNorm(Act, Exp, Norm): true
<-	println("norm: ",Norm).
 
+!start: true
<-	?getMap(MAP_ID);
	register(ID) [artifact_id(MAP_ID)];
	+myID(ID);
	println("Registered with ID:",ID);
	focus(MAP_ID);
	//!nextMove(MAP_ID,ID).
	!action(ID).

/* Find the MAP_ID */
+?getMap(MAP_ID): true
<-	lookupArtifact("map",MAP_ID).

-?getMap(MAP_ID): true
<-	.wait(10);
	?getMap(MAP_ID).

/* Move randomly */
+!nextMove(MAP_ID,ID): pos(ID,1,4)
<-	println("Reached destination").

+!nextMove(MAP_ID,ID):	true
<-	?pos(ID,X,Y);
	//println("position	",X,",",Y);
	move(ID,math.floor(math.random(4))) [artifact_id(MAP_ID)];
	?pos(ID,Xnew,Ynew);
	//println("	==>	",Xnew,",",Ynew);
	!nextMove(MAP_ID,ID).

-!nextMove(MAP_ID,ID): true
<-	!!nextMove(MAP_ID,ID).

+!action(ID): true
<-	?goTo(ID,X,Y);
	plan(ID,X,Y,Path);
	.member(P,Path);
	+path(ID,Path);
	println("pos: ",P);
	!checkViability(ID).
	
+!checkViability(ID): true 
<-	?path(ID,P1);
	?path(ID2,P2);
	.member(X,P1);
	.member(Y,P2);
	X==Y;
	println("T1==T2").