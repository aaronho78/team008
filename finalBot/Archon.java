package team008.finalBot;

import battlecode.common.*;



public class Archon extends Bot {

	public static int numGardenersCreated = 0;
	public static Direction lastDirection = new Direction(0);
	public Archon(RobotController r){
		super(r);
		//anything else archon specific
	}
	
	public void takeTurn() throws Exception{
		if(rc.getRoundNum() % 20 == 0){
		     lastDirection = lastDirection.rotateLeftDegrees(100);
			}
	    // Generate a random direction
	    Direction dir = Util.randomDirection();
	    if(rc.getRoundNum() + 5 > GameConstants.GAME_DEFAULT_ROUNDS || rc.getTeamVictoryPoints() + rc.getTeamBullets()/10 > 1000){
			rc.donate(((int)(rc.getTeamBullets()/10))*10);
		}
	    else if(rc.getTeamBullets() > 120 || rc.getRoundNum() < 400 && rc.getTeamBullets() > 100){
	    	hireGardener();
		}
	    // Randomly attempt to build a gardener in this direction
	    //if (rc.canHireGardener(dir) && Math.random() < .01 && false) {
	    //    rc.hireGardener(dir);
	    //}


	    // Move randomly
//		if(rc.senseBroadcastingRobotLocations().length > 0){
//	    goTo(rc.senseBroadcastingRobotLocations()[0]);
//		}
//		else{

	    RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
	    RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemy);
	    if(nearbyRobots.length > 0){
	    	if(enemyRobots.length> 0){
	    		lastDirection = Util.closestRobot(enemyRobots, here).location.directionTo(here);
	    	}
	    	else{
	    		lastDirection = Util.closestRobot(nearbyRobots, here).location.directionTo(here);
	    	}
	    }
	    tryMoveDirection(lastDirection);
//		}
	    // Broadcast archon's location for other robots on the team to know
	    /*
	    MapLocation myLocation = rc.getLocation();
	    rc.broadcast(0,(int)myLocation.x);
	    rc.broadcast(1,(int)myLocation.y);*/
	}
	

	public void hireGardener() throws GameActionException{
		Direction dir = Util.randomDirection();
		for(int i = 15; i --> 0;){
		    if (rc.canHireGardener(dir)) {
		        rc.hireGardener(dir);
		        numGardenersCreated++;
		        break;
		    }
		    else{
		    	dir = dir.rotateLeftDegrees(24);
		    }
		}
	}
}