package controllers.singlePlayer.mendiola;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

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
	private SokobanWorld world;

	private double maxDist;

	private Theory lastChangeStateTheory;
	private double prevWorldBoxDist;
	private int noChangesCounter = 0;

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

		double h = so.getWorldDimension().getHeight() - 80.0;
		double w = so.getWorldDimension().getWidth() - 80.0;
		this.maxDist = Math.sqrt(h*h + w*w);
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
			case "f": //full
				Printer.printKnowledge(this.knoledge, true, true);
				break;
			case "k": //knowledge
				Printer.printKnowledge(this.knoledge, false, false);
			case "s": //scenario
				Printer.printKnowledge(this.knoledge, false, false);
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

		world = new SokobanWorld(stateObs);

		Scenario scenario = world.getCurrentScenario();

		List<Theory> theories =  knoledge.getMatchingTheories(scenario, 1);

		userInteract();

		double demand = 1F;
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

		executeTheory(stateObs, theory);
		theory.k++;

		if(noChangesCounter == 100){
			//TODO make it stop
		}

		try {
			Thread.sleep(200);
		}catch (Exception ex){}

		//Printer.printTheory(theory);
		return theory.getAction();
	}

	private Theory pickOneWorthIt(List<Theory> theories, double threshold){
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
		if(resultSO.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
			theory.setUtility(0);
			return;
		} else if (resultSO.getGameWinner() == Types.WINNER.PLAYER_WINS) {
			theory.setUtility(1);
			return;
		}

		double compare = theory.getScenario().compare(new Scenario(resultSO.getObservationGrid()));
		if(compare == 0)
			theory.setUtility(0);
		else {
			if(this.prevWorldBoxDist == world.boxDistance)
				this.noChangesCounter++;
			else {
				this.noChangesCounter = 0;
				this.lastChangeStateTheory = theory;
				this.prevWorldBoxDist = world.boxDistance;
			}

			double currentProx = calculateStateProximity(world);
			double newProx = calculateStateProximity(new SokobanWorld(resultSO));
			theory.setUtility(0.5 + (currentProx - newProx));
		}
	}

	private double calculateStateProximity(SokobanWorld world){

		ArrayList<Observation> boxes = world.getBoxes();
		double boxAcum = 0;
		double playerAcum = 0;
		Vector2d myPosition = world.getMyPlayerPosition();
		ArrayList<Observation> holes = world.getHoles();
		for(Observation box : boxes){
			for(Observation hole : holes)
				boxAcum += box.position.dist(hole.position);
			playerAcum += box.position.dist(myPosition);
		}
		world.boxDistance = (boxAcum / (double)(boxes.size() * holes.size())) / maxDist;
		double playerDist = (playerAcum / (double)(boxes.size())) / maxDist;
		return world.boxDistance * 10 + playerDist;
	}
}
