package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {
	private int turnsSinceSeenEnemy;
	private boolean containing;
	
    public Soldier(RobotController r) throws GameActionException{
        super(r);
        containing = false;
    }
    
	public void takeTurn() throws Exception{
        //if(debug)System.out.println("In instantiation:"+Clock.getBytecodeNum());
		if(!containing && MapAnalysis.initialEnemyArchonLocations.length - Message.ENEMY_ARCHONS_KILLED.getLength() < 2 && nearbyEnemyRobots.length == 1 && here.distanceTo(nearbyEnemyRobots[0].location) < 5 && nearbyEnemyRobots[0].type == RobotType.ARCHON && nearbyAlliedRobots.length < (MapAnalysis.numSoldier > 4 ? 2 : 1)){
    		containing = true;
    	}
		if(nearbyEnemyRobots.length == 0){
			containing = false;
		}
        if(nearbyEnemyRobots.length > 0 && (containing || !(nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.ARCHON && here.distanceTo(nearbyEnemyRobots[0].location) < 5))){
            notifyFriendsOfEnemies(nearbyEnemyRobots);
            RangedCombat.execute();
            turnsSinceSeenEnemy = 0;
            return;
        }
        turnsSinceSeenEnemy++;
        if(target == null || (rc.getRoundNum() + rc.getID()) % 10 == 0){
            assignNewTarget();
        }
        if (rc.getMoveCount() == 0) {
        	//System.out.println("here");
        	if(nearbyBullets.length > 0 && turnsSinceSeenEnemy < 75 || nearbyBullets.length > 3){
        		Direction dirToMove = here.directionTo(nearbyBullets[0].location);
        		if(target != null)
        			dirToMove = here.directionTo(target);
        		RangedCombat.bulletMove(here.add(dirToMove, type.strideRadius), true);
        	}
        	if (target != null && turnsSinceSeenEnemy > 10 && ((here.distanceTo(target) < 3 || rc.canSenseLocation(target) && rc.isLocationOccupiedByTree(target)))) {
				if(debug)rc.setIndicatorLine(here, target, 0, 255, 0); 
				if(debug)System.out.println("removing");
        		Message.ENEMY_ARMIES.removeLocation(target);
				Message.ISOLATED_ENEMIES.removeLocation(target);
				Message.DISTRESS_SIGNALS.removeLocation(target);
				assignNewTarget();
			}
        	else if(target != null && here.distanceTo(target) < 5 && nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.ARCHON && here.distanceTo(nearbyEnemyRobots[0].location) < 5 && !containing){
        		Message.ENEMY_ARMIES.removeLocation(target);
				Message.ISOLATED_ENEMIES.removeLocation(target);
				Message.DISTRESS_SIGNALS.removeLocation(target);
				assignNewTarget();
        	}
			else if (target != null) {
				if (debug) {
		        	rc.setIndicatorLine(here, target, 255, 0, 0); 
				}
				goTo(target);
			} 
			else {
				if(nearbyAlliedRobots.length > 0 && here.distanceTo(nearbyAlliedRobots[0].location) > 4)
					tryMoveDirection(here.directionTo(nearbyAlliedRobots[0].location), true, true);
				else{
					tryMoveDirection(here.directionTo(MapAnalysis.center), true, true);
				}
			}
		}
	}
	}
