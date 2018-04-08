package org.yanzi.shareserver;

public class Data1 {

	int description;
	int priority;
	int heading;
	int extent;
	int position;
	FullPositionVectorCfg fullpositionvectorcfg;
	class FullPositionVectorCfg {
		int utcTime;
		DDateTimeCfg ddatetimecfg;
		class  DDateTimeCfg {
			int Year;
			int Month;
			int Day;
			int Hour;
			int Min;
			int Sec;
			public DDateTimeCfg(){
				ddatetimecfg = new DDateTimeCfg();
			}
			
		}
	int elevation;
	int heading;
	int speed;
	int posAccuracy;
	int timeConfidence;
	int posConfidence;
	int speedConfidence;
		
		public FullPositionVectorCfg()
		{
			fullpositionvectorcfg = new FullPositionVectorCfg();
		}
	}
	int furtherInfoID;
}
