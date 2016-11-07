package controllers.singlePlayer.mendiola;

import ontology.Types;

/**
 * Created by julian on 23/10/16.
 */
public class Theory {

    public long id = 0L;

    public Scenario scenario;
    public Scenario prediction;

    private Types.ACTIONS action;
    public double sucessFactor = 1;
    private double utility;
    public int k; //used
    public int p; //success

    public double match;

    public Theory(Scenario scenario){
        this.setScenario(scenario);
    }

    public Scenario getScenario(){
        return scenario;
    }

    public void setAction(Types.ACTIONS action){ this.action = action; }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Types.ACTIONS getAction() {
        return action;
    }

    public double getUtility() {
        return Math.max(sucessFactor * utility, 0);
    }

    public void setUtility(double utility) {
        this.utility = utility;
    }

    public String toString(){
        return "Theory [" + this.id + "]: " + this.action + " | utility: " + this.getUtility() +
                " | p: " + this.p + " | k: " + this.k;
    }
}
