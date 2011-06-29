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

+tick(N) : true <-
	-+step(N).