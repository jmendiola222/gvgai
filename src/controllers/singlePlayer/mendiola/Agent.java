package controllers.singlePlayer.mendiola;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer{

	/**
	 * Observation grid.
	 */
	protected ArrayList<Observation> grid[][];

	/**
	 * block size
	 */
	protected int block_size;

	protected Knoledge knoledge;

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		grid = so.getObservationGrid();
		block_size = so.getBlockSize();
		knoledge = new Knoledge();
	}


	/**
	 * Picks an action. This function is called every game step to request an
	 * action from the player.
	 * @param stateObs Observation of the current state.
	 * @param elapsedTimer Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		printStateObs(stateObs);

		Scenario scenario = new Scenario(stateObs.getObservationGrid());

		Theory theory = getTheoryToRun(scenario);
		if(theory == null || isWorthIt(theory)) {
			theory = buildNewTheory(scenario, stateObs);
			executeTheory(stateObs, theory);
			knoledge.addTheory(theory);
		}

		System.out.println(theory);
		theory.k++;
		return theory.getAction();
	}

	private boolean isWorthIt(Theory theory){
		return theory.getUtility() > 0.15;
	}

	private Theory getTheoryToRun(Scenario scenario){
		Theory theory = knoledge.existMatchingTheory(scenario);
		if(theory == null){
			theory = knoledge.getMostSimilarTheory(scenario);
		}
		if(theory == null || theory.match > 50){
			return null;
		}
		return theory;
	}

	private Theory buildNewTheory(Scenario scenario, StateObservation stateObs){
		Theory theory = new Theory(scenario);
		Random randomGenerator = new Random();

		ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
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

		printGrid(grid);
		System.out.println();
	}

	private void printGrid(ArrayList<Observation>[][] grid)
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

	private String toPrintObs(ArrayList<Observation> observations) {
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

	/**
	 * Gets the player the control to draw something on the screen.
	 * It can be used for debug purposes.
	 * @param g Graphics device to draw to.
	 */
	public void draw(Graphics2D g)
	{
		int half_block = (int) (block_size*0.5);
		for(int j = 0; j < grid[0].length; ++j)
		{
			for(int i = 0; i < grid.length; ++i)
			{
				if(grid[i][j].size() > 0)
				{
					Observation firstObs = grid[i][j].get(0); //grid[i][j].size()-1
					//Three interesting options:
					int print = firstObs.category; //firstObs.itype; //firstObs.obsID;
					g.drawString(print + "", i*block_size+half_block,j*block_size+half_block);
				}
			}
		}
	}
}
