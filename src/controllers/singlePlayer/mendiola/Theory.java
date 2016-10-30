package controllers.singlePlayer.mendiola;

import ontology.Types;

/**
 * Created by julian on 23/10/16.
 */
public class Theory {

    public long id;

    private Scenario scenario;

    private Types.ACTIONS action;
    private float utility;
    public int k; //used
    public int p; //success

    public float match;

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

    public float getUtility() {
        return utility;
    }

    public void setUtility(float utility) {
        this.utility = utility;
    }

    public String toString(){
        return "Theory [" + this.id + "]: " + this.action;
    }

}
