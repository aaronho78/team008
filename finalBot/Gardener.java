package team008.finalBot;

import battlecode.common.*;

public class Gardener extends Bot {
	static int numTreesBuilt = 0;
	static int numLumberjacksBuilt = 0;
	static int numScoutsBuilt = 0;
	static int numSoldiersBuilt = 0;
	static int numTanksBuilt = 0;
	public Gardener(RobotController r){
		super(r);
		//anything else gardener specific
	}
	
	public void takeTurn() throws GameActionException{
//		// Listen for home archon's location
//        int xPos = rc.readBroadcast(0);
//        int yPos = rc.readBroadcast(1);
//        MapLocation archonLoc = new MapLocation(xPos,yPos);
//
//        // Generate a random direction
//        Direction dir = randomDirection();
//
//        // Randomly attempt to build a soldier or lumberjack in this direction
//        if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
//            rc.buildRobot(RobotType.SOLDIER, dir);
//        } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
//            rc.buildRobot(RobotType.LUMBERJACK, dir);
//        }
//
//        // Move randomly
//        tryMove(randomDirection());
		waterLowestHealthTree();
		buildSomething();
	}
	public void waterLowestHealthTree() throws GameActionException {
		TreeInfo[] treesToWater = rc.senseNearbyTrees();
		TreeInfo treeToHeal = Util.leastHealth(treesToWater, true);
		if(treeToHeal != null){
			rc.water(treeToHeal.getID());
		}
	}
	public void buildSomething() throws GameActionException {
//		if(numTreesBuilt < 2){
//			if(rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST){
//				plantATree();
//			}
//		}
//		else if (numScoutsBuilt < 1){
//			if(rc.getTeamBullets() >= 80){
//				numScoutsBuilt++;
//				buildRobot(RobotType.SCOUT);
//			}
//		}
//		else if (numLumberjacksBuilt < 1){
//			if(rc.getTeamBullets() >= 100){
//				numLumberjacksBuilt++;
//				buildRobot(RobotType.LUMBERJACK);
//			}
//		}
		if(numTreesBuilt < 3){
			if(rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST){
				plantATree();
			}
		}
		if(rc.getTeamBullets() >= 100){
				numSoldiersBuilt++;
				buildRobot(RobotType.SOLDIER);
			}
		
		
	}

	public void buildRobot(RobotType type) throws GameActionException{

		Direction dir = new Direction(0);
		for(int i = 36; i --> 0;){
		    if (rc.canBuildRobot(type, dir)) {
		        rc.buildRobot(type, dir);
		        break;
		    }
		    else{
		    	dir = dir.rotateLeftDegrees(10);
		    }
		}
	}
	
	public void plantATree() throws GameActionException{

		Direction dir = new Direction(0);
		for(int i = 3; i --> 0;){
		    if (rc.canPlantTree(dir)) {
		        rc.plantTree(dir);
		        numTreesBuilt++;
		        break;
		    }
		    else{
		    	dir = dir.rotateLeftDegrees(120);
		    }
		}
	}
}