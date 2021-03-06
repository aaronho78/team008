package team008.finalBot;

import battlecode.common.*;

public class Lumberjack extends Bot {

    float DAMAGE_THEM_MOD;
    float TREE_DAMAGE_MOD;

    public Lumberjack(RobotController r) throws GameActionException{
        super(r);
        myRandomDirection = Util.randomDirection();
        debug = false;
        DAMAGE_THEM_MOD = 2f; // tested and determined to be better than 2.5
        TREE_DAMAGE_MOD = .2f;
    }

    public static boolean attacked = false;
    public static boolean moved = false;
    public static int treesWithinRange;
    public static Direction myRandomDirection;
//    public TreeInfo closestNeutralWithUnit;
    static MapLocation clearAroundLoc;
    public Message[] messagesToTry = {Message.DISTRESS_SIGNALS, Message.TREES_WITH_UNITS, Message.CLEAR_TREES_PLEASE, Message.ENEMY_TREES, Message.ENEMY_ARCHONS};
    public int[] howFarToGoForMessage = {     15,                        10,                       15,                         10,                  10};
//    public boolean[] checkEnemiesToRemove = { true,                     false,                    false,               true};

    public void takeTurn() throws Exception{
    	treesWithinRange = rc.senseNearbyTrees(here, 2, Team.NEUTRAL).length;
        attacked = false;
        moved = false;
        if(rc.getRoundNum() % 23 == 0){
            myRandomDirection = Util.randomDirection();
        }

//        closestNeutralWithUnit = Util.closestTree(nearbyNeutralTrees, rc.getLocation(), true, 50, true);
//        if(debug && closestNeutralWithUnit != null) rc.setIndicatorLine(here, closestNeutralWithUnit.getLocation(), 0, 0, 255);

        if(nearbyEnemyRobots.length > 0) {
            //Notify allies of enemies
            if ((rc.getRoundNum() + rc.getID()) % 5 == 0 || target == null) {
                notifyFriendsOfEnemies(nearbyEnemyRobots);
            }
        }
        if(nearbyEnemyRobots.length > 0 // pickier micro condition because LJs suck at fighting
                && (nearbyEnemyRobots[0].type == RobotType.GARDENER
                    || nearbyEnemyRobots[0].type == RobotType.ARCHON
                    || nearbyEnemyRobots[0].type == RobotType.LUMBERJACK && here.distanceTo(nearbyEnemyRobots[0].location) < 6
                    || nearbyEnemyRobots[0].type == RobotType.SCOUT && here.distanceTo(nearbyEnemyRobots[0].location) < 5
                    || nearbyEnemyRobots[0].type == RobotType.SOLDIER && here.distanceTo(nearbyEnemyRobots[0].location) < 4
                    || nearbyEnemyRobots[0].type == RobotType.TANK && here.distanceTo(nearbyEnemyRobots[0].location) < 5)){
            doMicro();
        } else {
        	updateTarget(1);
        }
        if(target != null && !moved && treesWithinRange == 0){
            if(clearAroundLoc == null
                    || here.distanceTo(clearAroundLoc) > 5
                    || nearbyNeutralTrees.length > 0 && clearAroundLoc.distanceTo(nearbyNeutralTrees[0].location) > 6 + nearbyNeutralTrees[0].radius) {
                goTo(target);
                moved = true;
            }
            // else go for trees will take care of movement
        }

//        int start = Clock.getBytecodeNum();
        goForTrees(); // moves towards them and chops
//        if(debug) System.out.println(" going for trees took " + (Clock.getBytecodeNum() - start));

        if (!moved) { // no trees around // just random ish
            MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(enemy);
            if ((((roundNum + rc.getID()) / 20) % (enemyArchonLocs.length + 1)) == 0) {
                tryMoveDirection(myRandomDirection, true, true);
            } else {
                tryMoveDirection(
                        here.directionTo(
                                enemyArchonLocs[(((roundNum + rc.getID()) / 20) % (enemyArchonLocs.length + 1)) -1]),
                        true, true);
            }
            moved = true;
        }
        if(debug && target != null) { rc.setIndicatorLine(here, target, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0); };
    }

    public void updateTarget(int howDesperate) throws GameActionException {
    	/*if(target != null && roundNum + rc.getID() % 10 == 0 && !Message.DISTRESS_SIGNALS.containsLocation(target) && !Message.ENEMY_ARCHONS.containsLocation(target) ){
    		//if(debug)System.out.println("changing");
    		target = null;
    	}*/
//        target = (closestNeutralWithUnit == null ? null : closestNeutralWithUnit.getLocation());
        target = null;
        MapLocation targetD;

        for(int i = 0; i < messagesToTry.length; i++){
            //System.out.println("loops " + i);
            targetD = messagesToTry[i].getClosestLocation(here);
            if (targetD != null && here.distanceTo(targetD) < howFarToGoForMessage[i]*howDesperate && (target == null || (here.distanceTo(targetD) < here.distanceTo(target) && here.distanceTo(targetD) < 7))) {
                //if(debug)System.out.println("targetD = " + targetD);
                target = targetD;
                if (messagesToTry[i] == Message.CLEAR_TREES_PLEASE ){//&& treesWithinRange == 0){
                    clearAroundLoc = target;
                }
            }
        }

        if (clearAroundLoc != null && here.distanceTo(clearAroundLoc) < 5
                && (Util.numBodiesTouchingRadius(nearbyNeutralTrees, clearAroundLoc, 5, 18) == 0) &&
                Message.CLEAR_TREES_PLEASE.removeLocation(clearAroundLoc)) {
            target = null;
            clearAroundLoc = null;
        }
        if (target != null && rc.getLocation().distanceTo(target) < 3) {
            if (nearbyEnemyRobots.length == 0 &&
                    Message.ENEMY_ARCHONS.removeLocation(target)) {
                //if(debug)System.out.println("thinking about removing");
                target = null;
            } else if (nearbyEnemyTrees.length == 0 &&
                    Message.ENEMY_TREES.removeLocation(target)) {
                target = null;
            } else if (here.distanceTo(target) < 1.5 &&
                    Message.TREES_WITH_UNITS.removeLocation(target)) {
                target = null;
            } else if (nearbyEnemyRobots.length == 0 &&
                    Message.DISTRESS_SIGNALS.removeLocation(target)) {
                target = null;
            }
        }

        // may have to be less strict
        if(target == null && howDesperate == 1) {
            updateTarget(2);
        } else if(target == null && howDesperate == 2){
            updateTarget(10);
        }
    }

    public float DIST_TO_ME_MOD = .3f;
    public float DIST_TO_CA_MOD = 1f;
    public float CONTAINS_UNIT_MOD = 10f;
    public float IN_THE_WAY_MOD = 1f;
    public float ENEMY_TREE_MOD = 3f;
    public float HEALTH_PCT_MOD = 2f;
    public float scoreTree(TreeInfo tree){
        return  -DIST_TO_ME_MOD * rc.getLocation().distanceTo(tree.getLocation())
                -DIST_TO_CA_MOD * (clearAroundLoc != null? clearAroundLoc.distanceTo(tree.getLocation()) : 0)
                +                 (tree.containedRobot == null ? 0 : CONTAINS_UNIT_MOD )
                -IN_THE_WAY_MOD * (target == null? 0 : rc.getLocation().distanceTo(tree.getLocation()) + tree.getLocation().distanceTo(target) - rc.getLocation().distanceTo(target))
                +                 (tree.getTeam() == enemy? ENEMY_TREE_MOD : 0)
                -HEALTH_PCT_MOD * (tree.health / tree.maxHealth)
                ;
    }

    public int WHEN_TO_STOP_SCORING_TREES_AND_MOVE = 5000;
    public void goForTrees() throws GameActionException {
//        int s = Clock.getBytecodeNum();
//        System.out.println("getting closest " + nearbyNeutralTrees.length + " neutral took " + (Clock.getBytecodeNum() - s));
//        if(debug) rc.setIndicatorDot(rc.getLocation(), 0, 255,0);

//        System.out.println("TSA pre-check");
        if(moved && attacked) return;
//        System.out.println("TSA post-check" + Clock.getBytecodesLeft());

        TreeInfo moveTo = null;
        TreeInfo attackMe = null;
        float bestMoveScore = -99999f;
        float bestAttackScore = -999999f;
        float score;
        for(int i = 0; i < nearbyTrees.length && Clock.getBytecodesLeft() > WHEN_TO_STOP_SCORING_TREES_AND_MOVE; i++){
//            if(debug) System.out.print("loopy");
            if(nearbyTrees[i].getTeam() == us) continue;
            score = scoreTree(nearbyTrees[i]);
//            if(debug) System.out.println("loc: " + nearbyTrees[i].location.x + " " + nearbyTrees[i].location.y + " score: " + score);
            if(!moved && score > bestMoveScore){
                bestMoveScore = score;
                moveTo = nearbyTrees[i];
            }
            if(!attacked && score > bestAttackScore && rc.getLocation().distanceTo(nearbyTrees[i].location) < GameConstants.LUMBERJACK_STRIKE_RADIUS + nearbyTrees[i].radius){
                bestAttackScore = score;
                attackMe = nearbyTrees[i];
            }
        }

        if(debug){
            if(attackMe != null){
                rc.setIndicatorLine(rc.getLocation(), attackMe.location, 255, 255, 255);
                rc.setIndicatorDot(attackMe.location, 255, 255, 255);
            }
            if(moveTo != null) rc.setIndicatorLine(rc.getLocation(), moveTo.location, 0, 0, 0);
        }

        if(attackMe != null){
            if(attackMe.health > 5 && Util.numBodiesTouchingRadius(nearbyAlliedRobots, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS) == 0
                    && Util.numBodiesTouchingRadius(nearbyNeutralTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS, 5)
                    + Util.numBodiesTouchingRadius(nearbyEnemyTrees, rc.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS, 5) > 2){
                rc.strike();
                attacked = true;
            } else {
                rc.chop(attackMe.ID);
                attacked = true;
            }
        }
        if(moveTo != null){
            if(moveTo == attackMe){
                tryMoveDirection(here.directionTo(moveTo.location), false, false);
                if(debug) rc.setIndicatorDot(here.add(calculatedMove), 0, 0, 255);
                if(calculatedMove != null && here.distanceTo(moveTo.getLocation()) + TOLERANCE > here.add(calculatedMove).distanceTo(moveTo.getLocation())){
                    rc.move(calculatedMove, type.strideRadius);
                }
            } else if(treesWithinRange == 0){
                goTo(moveTo.location);
            }
            moved = true;
        }
    }

    public void doMicro() throws GameActionException {
        if(debug) rc.setIndicatorDot(here, 255, 0 , 0);
        calculatedMove = null;
        tryMoveDirection(here.directionTo(nearbyEnemyRobots[0].location), false, true);
        if(calculatedMove != null && here.add(calculatedMove, type.strideRadius).distanceTo(nearbyEnemyRobots[0].location) < here.distanceTo(nearbyEnemyRobots[0].location)){
            rc.move(calculatedMove, type.strideRadius);
            moved = true;
        }
        if(evalForAttacking(rc.getLocation()) > 0) {
            rc.strike();
            attacked = true;
        }
        if(calculatedMove != null && !moved){
            rc.move(calculatedMove, type.strideRadius);
            moved = true;
        }
        if(!attacked && evalForAttacking(rc.getLocation()) > 0) {
            rc.strike();
            attacked = true;
        }
    }

   /* public void doLumberjackMicro() throws Exception{
        // gets called when there are enemies that can be seen
        // don't worry about chopping trees here, that's checked for after. only enemies
        if (debug) { System.out.println("whee micro"); }

        // TODO: add kamikaze function: if about to die anyways, just go for best place to attack for final stand

        float attackScoreHere = evalForAttacking(here);
        float bestMoveScore = evaluateLocation(here) + (attackScoreHere < 0 ? 0 : MOVE_ATTACK_MOD * attackScoreHere) + IMPATIENCE_MOD * turnsWithoutMovingOrAttacking;
        // there should be a way to remove the attack bit here such that
        //     a better location won't be disregarded since we can attack
        //     here and then move there
        if (debug) { System.out.println("here score " + bestMoveScore); }
        MapLocation bestLoc = here;
        float bestLocAttackScore = -999;
        MapLocation currLoc;
        float score, attackScore;
        int startTheta = 0; // if we want to start at something nonzero then change the hardcoded zeroes below
        int currentTheta = 0;
        int dtheta = 36;
        int numLocsEvaled = 0;
        float stridedist = RobotType.LUMBERJACK.strideRadius;

        int startBytecode = Clock.getBytecodeNum();
        while (Clock.getBytecodeNum() + (Clock.getBytecodeNum() - startBytecode)/((numLocsEvaled < 2 ? 1 : numLocsEvaled)) < WHEN_TO_STOP_MICRO){
            // stop when the average time it takes to eval puts us over the WHEN_TO_STOP_MICRO threshold
            currLoc = here.add(Util.radians(currentTheta), stridedist);
            if (rc.canMove(currLoc)) {
                if (debug) { rc.setIndicatorDot(currLoc, 0, 0, (int)(1.0*currentTheta / 360 * 255)); }
                attackScore = evalForAttacking(currLoc);
                score = evaluateLocation(currLoc) + (attackScoreHere < 0 ? 0 : MOVE_ATTACK_MOD * attackScoreHere);
                //                                  if you're not going to attack anyways, it doesn't matter how bad it is
                if (debug) { System.out.println(currentTheta + " " + currLoc.x + " " + currLoc.y + " score " + score); }
                if (score > bestMoveScore) {
                    bestLoc = currLoc;
                    bestLocAttackScore = attackScore;
                    bestMoveScore = score;
                }
                numLocsEvaled += 1;
            }

            currentTheta += dtheta;
            if (currentTheta >= 360){
                // tried every point around a circle, now try closer
                // TODO: make the test points more evenly distributed inside (see circle-packing on wikipedia)
                stridedist /= 2;
                currentTheta = 0;
                dtheta = dtheta * 5 / 3; // it'll get more dense as it gets closer so adjust a little for that
                if (stridedist < .187) { // probably silly to keep checking
                    if (debug) { System.out.print("I've tried everything dammit"); }
                    break;
                }
            }
        }
        if (debug) { System.out.println("tried " + numLocsEvaled + " locs and finished at theta " + currentTheta + " and radius " + stridedist); }

        if ( attackScoreHere > bestLocAttackScore && attackScoreHere > 0){
            // attack first, then move
            if (debug) {rc.setIndicatorDot(here, 255,0,0); }//red dot == SMASH
            rc.strike();
            rc.move(bestLoc);

        } else if (bestLocAttackScore > 0){
            // move first, then attack
            rc.move(bestLoc);
            if (debug) { rc.setIndicatorDot(bestLoc, 255, 0, 0); }//red dot == SMASH
            rc.strike();
        } else if (bestLoc != here){
            // just move
            rc.move(bestLoc);
        }
    }

    public float evaluateLocation(MapLocation loc){
        // 'scores' the location in terms of possible damage accrued (bullets and otherwise) and forward progress,
        //     but NOT attacking damage
        // TODO: take into account other strategery like defending our trees/units, swarming or not, etc

        float distToNearestEnemy = (loc == here ? here.distanceTo(nearbyEnemyRobots[0].getLocation()) : Util.distToClosestBody(nearbyEnemyRobots, loc));
        /*if (distToNearestEnemy < GameConstants.LUMBERJACK_STRIKE_RADIUS + RobotType.LUMBERJACK.strideRadius + 1){
            distToNearestEnemy = 0; // close enough to hit already
        }*//*

        return KNOWN_DAMAGE_MOD * knownDamageToLoc(loc)
                + HYPOTHETICAL_DAMAGE_MOD * hypotheticalDamageToSpot(loc)
                + PROXIMITY_MOD * distToNearestEnemy
                + (target != null ? PROGRESS_MOD * here.distanceTo(target) - loc.distanceTo(target) : 0)
                + (gardenerLoc != null ? GARDENER_PROXIMITY_MOD * (loc.distanceTo(gardenerLoc) < 3.6 ? 3.6f - loc.distanceTo(gardenerLoc) : 0) : 0 )
                // translation: if too close to gardener I'm defending, it's bad. (3.6 isn't random, it's sqrt(3)*3/2)
                ;
    }*/

    public float evalForAttacking(MapLocation loc){
        // how good it is to attack from this spot
        // score <= 0 means it's better not to attack
        float damageToThem = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyEnemyRobots, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToUs = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyAlliedRobots, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToEnemyTrees = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyEnemyTrees, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        float damageToAlliedTrees = RobotType.LUMBERJACK.attackPower * Util.numBodiesTouchingRadius(nearbyAlliedTrees, loc, GameConstants.LUMBERJACK_STRIKE_RADIUS);
        return DAMAGE_THEM_MOD * damageToThem - damageToUs + TREE_DAMAGE_MOD * (damageToEnemyTrees - damageToAlliedTrees);
    }

    public static void goTo(MapLocation dest) throws GameActionException {
        // this method makes us not try to bug if we can chop the thing in the way
        if(debug) System.out.println("goin' to " + dest.toString());
        Direction dir = here.directionTo(dest);
        MapLocation directlyInFront = here.add(dir, type.strideRadius*2);
        MapLocation left = here.add(dir.rotateLeftDegrees(30), type.strideRadius*2);
        MapLocation right = here.add(dir.rotateRightDegrees(30), type.strideRadius*2);
        TreeInfo treeInTheWay = rc.senseTreeAtLocation(directlyInFront);
        if(treeInTheWay == null)
            treeInTheWay = rc.senseTreeAtLocation(left);
        if(treeInTheWay == null)
            treeInTheWay = rc.senseTreeAtLocation(right);
        if(treeInTheWay != null) {
            if(treeInTheWay.team != us){
                tryMoveDirection(here.directionTo(dest), true, false);
                moved = true;
            }
        } else {
            RobotInfo botInTheWay = rc.senseRobotAtLocation(directlyInFront);
            if(botInTheWay == null)
                botInTheWay = rc.senseRobotAtLocation(left);
            if(botInTheWay == null)
                botInTheWay = rc.senseRobotAtLocation(right);
            if(botInTheWay != null){
                if(botInTheWay.team != us){
                    tryMoveDirection(here.directionTo(dest), true, false);
                    moved = true;
                }
            }
        }
        if(!moved){
//            if(clearAroundLoc != null && here.distanceTo(clearAroundLoc) < 10)
//                bugState = BugState.DIRECT;
            Bot.goTo(dest);
            moved = true;
        }
    }
}