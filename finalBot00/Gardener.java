package battlecode2017.finalBot00;

import battlecode.common.*;

public class Gardener extends Bot {

	public Gardener(RobotController r){
		super(r);
		//anything else gardener specific
	}
	
	public void takeTurn() throws Exception{
		// Listen for home archon's location
        int xPos = rc.readBroadcast(0);
        int yPos = rc.readBroadcast(1);
        MapLocation archonLoc = new MapLocation(xPos,yPos);

        // Generate a random direction
        Direction dir = randomDirection();

        // Randomly attempt to build a soldier or lumberjack in this direction
        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
            rc.buildRobot(RobotType.SOLDIER, dir);
        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
            rc.buildRobot(RobotType.LUMBERJACK, dir);
        }

        // Move randomly
        tryMove(randomDirection());
	}
}