package controllers.singlePlayer.mendiola;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.*;

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

	private Scanner scanner;
	Random randomGenerator = new Random();
	private SokobanWorld world;

	private double maxDist;

	private List<Theory> lastChangeStateTheorys = new LinkedList<Theory>();
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
		knoledge = Knowledge.getKnoledge();
		scanner = new Scanner(System.in);

		double h = so.getWorldDimension().getHeight() - 80.0;
		double w = so.getWorldDimension().getWidth() - 80.0;
		this.maxDist = Math.sqrt(h*h + w*w);
	}

	private void userInteract(){
		if(UserCmd.ignoreX > 0)
		{
			UserCmd.ignoreX--;
			return;
		}
		//  prompt for
		System.out.print("Enter cmd: ");
		String cmd = scanner.next();
		switch (cmd.substring(0, 1)){
			case "i":
				int ignoreExp = Integer.parseInt(cmd.substring(1,2));
				UserCmd.ignoreX = (int)Math.pow(10, ignoreExp);
				break;
			case "f": //full
				Printer.printKnowledge(this.knoledge, true, true);
				break;
			case "k": //knowledge
				Printer.printKnowledge(this.knoledge, false, true);
			case "s": //scenario
				Printer.printKnowledge(this.knoledge, false, false);
				break;
			case "q":
				throw new QuiteGame();
			case "p":
				int x = Integer.parseInt(cmd.substring(1,2));
				int y = Integer.parseInt(cmd.substring(2,3));
				Printer.printKnowledgeByPlayerPos(this.knoledge, new Vector2d(40.0 + x * 40.0, 40.0 + y * 40.0));
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
		/*try {
			Thread.sleep(100);
		}catch (Exception ex){}*/

		double demand = 1F;
		Theory theory = null;

		while(theory == null){
			theory = pickOneWorthIt(theories, demand);

			if(theory == null){
				theory = buildNewTheory(scenario, stateObs, theories);
				if(theory == null)
					demand = 0;
				else
					knoledge.addTheory(theory);
			} else {
				if(theory.getUtility() < 0.01){
					System.out.println("!!! Picking a BAD theory" + theory);
				}
			}
			demand *= 0.5;
		}

		executeTheory(stateObs, theory);
		theory.k++;

		if(noChangesCounter == 100){
			//penalize theories that led me to stall
			tainTheories(this.lastChangeStateTheorys, -1);
			//TODO make it stop better
			throw new QuiteGame();
		}

		//Printer.printTheory(theory, false);
		return theory.getAction();
	}

	private void tainTheories(List<Theory> theories, int tainFactor){
		double sucessDelta = 2;
		//System.out.println("Taining " + tainFactor);
		for(int i = theories.size() - 1; i >= 0; i--){
			Theory theory = theories.get(i);
			//System.out.print(" - Theory [" + theory.id + "] " + theory.getUtility());
			theory.sucessFactor = theory.sucessFactor + (tainFactor * (theory.sucessFactor / Math.pow(sucessDelta++,2)));
			//System.out.println(" -> " + theory.getUtility());
		}
	}

	private Theory pickOneWorthIt(List<Theory> theories, double threshold){
		if (theories.size() == 0 || theories.get(0).getUtility() <= threshold) return null;

		double accumUtil = 0;
		double base = 0; //Very low probability for zero Utility
		//As it's ordered by relevance, we weight the first ones
		int size = Math.min(theories.size(), 5);
		for(int i = 0; i < size; i ++){
			accumUtil += base + Math.max(theories.get(i).getUtility(), 0);
		}
		double[] norm = new double[size];
		for(int i = 0; i < size; i ++){
			norm[i] = (base + Math.max(theories.get(i).getUtility(), 0)) / accumUtil;
		}

		double rnd = randomGenerator.nextDouble();
		for(int i = 0; i < size; i++) {
			rnd -= norm[i];
			if(rnd <= 0) return theories.get(i);
		}
		return theories.get(0);
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
		}

		Scenario resultScenario = new Scenario(resultSO.getObservationGrid());
		if(theory.prediction == null) {
			theory.prediction = resultScenario;
		}
		double compare = theory.getScenario().compare(resultScenario);
		if(compare == 0) {
			theory.setUtility(0);
		} else {

			Utility utility = calculateStateUtility(world);
			Utility newUtility = calculateStateUtility(new SokobanWorld(resultSO));

			//Has moved boxes
			if (Math.abs(utility.boxDist - newUtility.boxDist) > 0) {
				this.noChangesCounter = 0;
				if (this.lastChangeStateTheorys.size() > 4)
					this.lastChangeStateTheorys.remove(0);
				this.lastChangeStateTheorys.add(theory);
			} else {
				this.noChangesCounter++;
			}

			theory.setUtility(0.5 + (utility.value() - newUtility.value()));

			if (resultSO.getGameWinner() == Types.WINNER.PLAYER_WINS) {
				tainTheories(this.lastChangeStateTheorys, 1);
			}
		}
	}

	private Utility calculateStateUtility(SokobanWorld world){

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
		double boxDist = (boxAcum / (double)(boxes.size() * holes.size())) / maxDist;
		double playerDist = (playerAcum / (double)(boxes.size())) / maxDist;
		return new Utility(boxDist, playerDist);
	}

	class Utility {
		public double boxDist;
		public double playerDist;
		public Utility(double boxDist, double playerDist) { this.boxDist = boxDist; this.playerDist = playerDist; }
		public double value() { return boxDist * 10 + playerDist; }
	}
}
