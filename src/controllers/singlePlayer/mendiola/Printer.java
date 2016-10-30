package controllers.singlePlayer.mendiola;

import core.game.Observation;

import java.util.ArrayList;

/**
 * Created by julian on 30/10/16.
 */
public class Printer {

    public static void printKnowledge(Knowledge knowledge, boolean withScenarios) {
        System.out.println("/******** KNOWLEDGE START ********/");
        for (Theory t : knowledge.theories) {
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

        System.out.println("----------------------------");
        for (int j = 0; j < size; j++) {
            System.out.print("[");
            for (int i = 0; i < grid.length; i++) {
                ArrayList<Observation> cell = grid[i][j];
                System.out.print("" + toPrintObs(cell) + ",");
            }
            System.out.println("]");
        }
    }

    public static String toPrintObs(ArrayList<Observation> observations) {
        String result = "";
        for (int j = 0; j < 2; j++) {
            if(observations.size() > j) {
                Observation observation = observations.get(j);
                switch(observation.itype){
                    case 2: //is always
                        break;
                    case 0: //is wall
                        result += '+';
                        break;
                    case 1: //is player
                        result += 'X';
                        break;
                    case 3: //is hole
                        result += 'O';
                        break;
                    case 4: //is box
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
