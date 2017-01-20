package defendBot;

import battlecode.common.*;

public class Tank extends Bot {

	public Tank(RobotController r) throws GameActionException {
		super(r);
	}

	public void takeTurn() throws Exception {
		rc.disintegrate();
		return;
	}
}