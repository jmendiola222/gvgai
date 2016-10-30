package controllers.singlePlayer.mendiola;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Agent extends AbstractPlayer{

	/**
	 * Observation grid.
	 */
	protected ArrayList<Observation> grid[][];

	/**
	 * block size
	 */
	protected int block_size;

	protected Knowledge knoledge;

	private int ignoreX = 0;
	private Scanner scanner;
	Random randomGenerator = new Random();

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		grid = so.getObservationGrid();
		block_size = so.getBlockSize();
		knoledge = new Knowledge();
		scanner = new Scanner(System.in);

	}

	private void userInteract(){
		if(this.ignoreX > 0)
		{
			ignoreX--;
			return;
		}
		//  prompt for
		System.out.print("Enter cmd: ");
		String cmd = scanner.next();
		switch (cmd){
			case "1":
				this.ignoreX = 10;
				break;
			case "2":
				this.ignoreX = 100;
				break;
			case "3":
				this.ignoreX = 1000;
				break;
			case "k":
				Printer.printKnowledge(this.knoledge, true);
				break;
			case "f":
				Printer.printKnowledge(this.knoledge, false);
				break;
			default:
				break;
		}
	}

	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		Scenario scenario = new Scenario(stateObs.copy().getObservationGrid());

		List<Theory> theories =  knoledge.getMatchingTheories(scenario, 1);

		userInteract();

		float demand = 1F;
		Theory theory = null;

		while(theory == null){
			theory = pickOneWorthIt(theories, demand);
			if(theory == null){
				theory = buildNewTheory(scenario, stateObs, theories);
				if(theory != null)
					knoledge.addTheory(theory);
			}
			demand *= 0.75;
		}

		Printer.printTheory(theory);
		executeTheory(stateObs, theory);
		theory.k++;
		return theory.getAction();
	}

	private Theory pickOneWorthIt(List<Theory> theories, float threshold){
		if (theories.size() == 0 || theories.get(0).getUtility() <= threshold) return null;

		//As it's ordered by relevance, we weight the first ones
		int index = (int) (Math.pow(randomGenerator.nextDouble(), 2) * theories.size());
		return theories.get(index);
	}

	private Theory buildNewTheory(Scenario scenario, StateObservation stateObs, List<Theory> theories){
		Theory theory = new Theory(scenario);

		ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();

		for(Theory t : theories){
			if(t.match > 0) break;
			int idx = 0;
			while (idx < actions.size()) {
				if (actions.get(idx) == t.getAction()) {
					actions.remove(idx);
					break;
				}
				idx++;
			}
		}

		if(actions.size() == 0)
			return null;

		int index = randomGenerator.nextInt(actions.size());

		Types.ACTIONS action = actions.get(index);
		theory.setAction(action);

		return theory;
	}

	private void executeTheory(StateObservation stateObs, Theory theory){

		StateObservation stCopy = stateObs.copy();
		stCopy.advance(theory.getAction());

		evaluateTheory(theory, stCopy);
		theory.p++;
	}

	private void evaluateTheory(Theory theory, StateObservation resultSO){
		if(resultSO.isGameOver()){
			theory.setUtility(0);
			return;
		}
		float compare = theory.getScenario().compare(new Scenario(resultSO.getObservationGrid()));
		if(compare == 0){
			theory.setUtility(0);
		}
		else{
			theory.setUtility(0.5F);
		}
	}

	private void printStateObs(StateObservation stateObs){
		ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();

		grid = stateObs.getObservationGrid();

		//printDebug(fixedPositions,"fix");
		//printDebug(movingPositions,"mov");

		Printer.printGrid(grid);
		System.out.println();
	}



	/**
	 * Prints the number of different types of sprites available in the "positions" array.
	 * Between brackets, the number of observations of each type.
	 * @param positions array with observations.
	 * @param str identifier to print
	 */
	private void printDebug(ArrayList<Observation>[] positions, String str)
	{
		if(positions != null){
			System.out.print(str + ":" + positions.length + "{");
			for (int i = 0; i < positions.length; i++) {
				ArrayList<Observation> obs = positions[i];
				for (int j = 0; j < obs.size(); j++) {
					Observation observation = obs.get(j);
					printObservation(observation);
				}
			}
			System.out.print("]; ");
		}else System.out.print(str + ";");
	}

	private void printObservation(Observation obs) {
		System.out.println(obs.itype + " (" + obs.position.x / 40 + ", " + obs.position.y / 40 + ") ");
	}
}
