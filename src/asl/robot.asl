!start.

/*	Initialization
 *		- acquire MAP_ID (MapArtifact created by the facilitator agent)
 *		- focus the map artifact
 *		- register on the map
 */
+!start: true
<-	?getMap(MAP_ID);
	register(ID) [artifact_id(MAP_ID)];
	+myID(ID);
	println("Registered with ID:",ID);
	focus(MAP_ID);
	!nextMove(MAP_ID,ID).
	

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
	println("position	",X,",",Y);
	move(ID,math.floor(math.random(4))) [artifact_id(MAP_ID)];
	?pos(ID,Xnew,Ynew);
	println("	==>	",Xnew,",",Ynew);
	!nextMove(MAP_ID,ID).

-!nextMove(MAP_ID,ID): true
<-	!!nextMove(MAP_ID,ID).