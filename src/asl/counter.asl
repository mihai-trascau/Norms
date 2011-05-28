!create_and_use.

+!create_and_use : true
<-
	println("ONLINE");
	!setupTool(Id);
	inc;
	inc [artifact_id(Id)].

+!setupTool(C): true
<-
	makeArtifact("c0","cartagoEnvironment.Counter",[],C).