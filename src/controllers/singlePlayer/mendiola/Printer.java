package controllers.singlePlayer.mendiola;

import core.game.Observation;

import java.util.ArrayList;

/**
 * Created by julian on 30/10/16.
 */
public class Printer {

    public static void printKnowledge(Knowledge knowledge, boolean withScenarios, boolean includeUseless) {
        System.out.println("/******** KNOWLEDGE START ********/");
        for (Theory t : knowledge.theories) {
            if(includeUseless && t.getUtility() == 0d)
                continue;
            printTheory(t);
            if (withScenarios)
                printGrid(t.getScenario().grid);
        }
        System.out.println("/******** KNOWLEDGE END ********/");
    }

    public static void printTheory(Theory theory){
        System.out.println(theory);
    }

    public static void printGrid(ArrayList<Observation>[][] grid)
    {
        int size = grid[0].length;

        for (int j = 0; j < size; j++) {
            System.out.print("[");
            for (int i = 0; i < grid.length; i++) {
                ArrayList<Observation> cell = grid[i][j];
                System.out.print("" + toPrintObs(cell) + ",");
            }
            System.out.println("]");
        }
        System.out.println("----------------------------");
    }

    public static String toPrintObs(ArrayList<Observation> observations) {
        String result = "";
        for (int j = 0; j < 2; j++) {
            if(observations.size() > j) {
                Observation observation = observations.get(j);
                switch(observation.itype){
                    case Consts.OBS_ITYPE_DN: //is always
                        break;
                    case Consts.OBS_ITYPE_WALL: //is wall
                        result += '+';
                        break;
                    case Consts.OBS_ITYPE_PLAYER: //is player
                        result += 'X';
                        break;
                    case Consts.OBS_ITYPE_HOLE: //is hole
                        result += 'O';
                        break;
                    case Consts.OBS_ITYPE_BOX: //is box
                        result += '#';
                        break;
                    default:
                        result += observation.itype;
                        break;
                }
            }else{
                result += ' ';
            }
        }
        return result;
    }
}
