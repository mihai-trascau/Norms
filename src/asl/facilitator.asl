!start.

+!start: true <-
	!setupMap(MapID).


+!setupMap(MapID): true <-
	makeArtifact("map","cartagoEnvironment.MapArtifact",[],MapID);
	focus(MapID);
	.println("map initialised").

+norms_infringed(Name,Tick,Critical,NormList) : true <-
	if (NormList \== []) {
		if (Critical == false) {
			//.println("===> ",Name," sent a report: ",NormList);
		}
		else {
			.println("===> ",Name," sent a CRASH REPORT: ",NormList);
			.member(List1,NormList);
			+common_subset(List1);
			for (.member(List,NormList)) {
				?common_subset(CommonSubset);
				.intersection(CommonSubset,List,CS);
				-+common_subset(CS);
			}
			?common_subset(FCS);
			.println("Norms to be removed: ",FCS);
			-common_subset(FCS);
			for (.member(NormID,NormList)) {
				if(not norm_crash_count(NormID,_)) {
					+norm_crash_count(NormID,1);
				}
				else {
					?norm_crash_count(NormID,Counter);
					-norm_crash_count(NormID,Counter);
					+norm_crash_count(NormID,Counter+1);
				}
			}
			.findall(norm_crash_count(NID,C),norm_crash_count(NID,C),NCCList);
			.println("NORM CRASH COUNTER ",NCCList);
		}
	}
	-norms_infringed(Name,Tick,Critical,NormList);.

+scores(Name,ScoreList): true <-
	for (.member(score(X,Y,Dir,Score),ScoreList)) {
		if (score(X,Y,Dir,Old)) {
			-score(X,Y,Dir,Old);
			+score(X,Y,Dir,Old+Score);
		}
		else {
			+score(X,Y,Dir,Score);
		}
	}.

+tick(N) : true <-
	-+step(N).