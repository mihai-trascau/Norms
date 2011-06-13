goTo(0,2,7).
goTo(1,2,7).
goTo(2,2,7).

norm([time(5),time(6)],[time(10)],work).

!start.

/*	Initialization
 *		- acquire MAP_ID (MapArtifact created by the facilitator agent)
 *		- focus the map artifact
 *		- register on the map
 */

/* 
+norm(Act, Exp, Norm): true
<-	?acceptNorm(Act, Exp, Norm);
	!processNorm(Act, Exp, Norm).
	
+?acceptNorm(Act, Exp, Norm): true
<-	true.

+!processNorm(Act, Exp, Norm): true
<-	println("norm: ",Norm).
*/
 
+!start: true
<-	?getMap(MAP_ID);
	register(ID) [artifact_id(MAP_ID)];
	+myID(ID);
	println("Registered with ID:",ID);
	focus(MAP_ID);
	//!nextMove(MAP_ID,ID).
	!work(ID).

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
	move(ID,math.floor(math.random(4))) [artifact_id(MAP_ID)];
	?pos(ID,Xnew,Ynew);
	!nextMove(MAP_ID,ID).

-!nextMove(MAP_ID,ID): true
<-	!!nextMove(MAP_ID,ID).

+!work(ID): true
<-	?goTo(ID,X,Y);
	plan(ID,X,Y);
	!checkValidity(ID).
	
+!checkValidity(ID): true
<-	.println("checkValidity start");
	.findall(AgentID, (path(AgentID,Path,_) & (AgentID \== ID)), L);
	?path(ID,MyPath,0);
	if (L==[])
	{
		.println("NU EXISTA PATH-URI");
		-path(ID,MyPath,0);
		+path(ID,MyPath,1);
	}
	else
	{
		.println("EXISTA PATH-URI");
		!solveConflicts(ID);
	}
	.println("checkValidity end").

+!solveConflicts(MyID): true
<-	.findall(ID, (path(ID,Path,0) & (ID \== MyID)), L0);
	/*?path1(P1,0);
	?path2(P2,0);
	?path3(P3,0);
	replan(MyID,P1,[P2,P3]);*/
	?path(MyID,MyPath,0);
	if (L0 == [])
	{
		.println("TOATE PATH-URILE SUNT COMMITED");
		.findall(Path, (path(ID,Path,1) & (ID \== MyID)), L1);
		replan(MyId, MyPath, L1);
	}
	else
	{
		.println("EXISTA PATH-URI UNCOMMITED");
		.min(L0,Min);
		if (MyID <= Min)
		{
			//.println("==================================");
			.findall(Path, (path(ID,Path,1) & (ID \== MyID)), L1);
			replan(MyID, MyPath, L1);
		}
		else
		{
			.wait(1000);
			!solveConflicts(MyID);
		}
	}
	.println("solveConflicts end").
	
+?getFirstCommon(Path1,Path2,C): true
<-	.println("getFirstCommon start");
	.min([.length(Path1),.length(Path2)],Len);
	for (.range(I,0,Len-1))
	{
		.nth(I,Path1,X);
		.nth(I,Path2,Y);
		.term2string(X,XT);
		.term2string(Y,YT);
		if (X==Y)
		{
			C = X;
		}
	}
	.println("getFirstCommon stop").