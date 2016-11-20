package controllers.singlePlayer.mendiola;

import controllers.singlePlayer.mendiola.helpers.*;
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

	protected Knowledge knowledge;

	private Scanner scanner;
	Random randomGenerator = new Random();
	private SokobanWorld world;

	private double maxDist;

	private List<Theory> lastChangeStateTheorys = new LinkedList<Theory>();
	private int noChangesCounter = 0;
	private Plan planInExecution;
	private int theoryIndex;
	private List<Types.ACTIONS> forceActions = new LinkedList<>();
	private int gameTick;

	/**
	 * Public constructor with state observation and time due.
	 * @param so state observation of the current game.
	 * @param elapsedTimer Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
	{
		grid = so.getObservationGrid();
		block_size = so.getBlockSize();
		knowledge = Knowledge.getKnowledge();
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
				Printer.printKnowledge(knowledge, true, true);
				break;
			case "k": //knowledge
				Printer.printKnowledge(knowledge, false, true);
			case "s": //scenario
				Printer.printKnowledge(knowledge, false, false);
				break;
			case "q":
				throw new QuiteGame();
			case "p":
				int x = Integer.parseInt(cmd.substring(1,2));
				int y = Integer.parseInt(cmd.substring(2,3));
				Printer.printKnowledgeByPlayerPos(knowledge, new Vector2d(40.0 + x * 40.0, 40.0 + y * 40.0));
				break;
			case "u":
				this.forceActions.add(Types.ACTIONS.ACTION_UP);
				break;
			case "d":
				this.forceActions.add(Types.ACTIONS.ACTION_DOWN);
				break;
			case "l":
				this.forceActions.add(Types.ACTIONS.ACTION_LEFT);
				break;
			case "r":
				this.forceActions.add(Types.ACTIONS.ACTION_RIGHT);
				break;
			case "v":
				UserCmd.visual = !UserCmd.visual;
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

		this.gameTick = stateObs.getGameTick();
		userInteract();
		/*try {
			Thread.sleep(100);
		}catch (Exception ex){}*/
		if(forceActions.size() > 0){
			Types.ACTIONS action = forceActions.get(0);
			forceActions.remove(0);
			return action;
		}

		world = new SokobanWorld(stateObs);

		Theory theory = getTheory(knowledge, world);
		executeTheory(stateObs, theory);
		if(theory.id == 0)
			knowledge.addTheory(theory);

		int noChangesCounterTolerance = 50 * ((int)stateObs.getWorldDimension().getHeight()/40 - 5);
		if(noChangesCounter == Math.max(25, noChangesCounterTolerance)){
			//penalize theories that led me to stall
			tainTheories(this.lastChangeStateTheorys, -1);
			//TODO make it stop better
			throw new QuiteGame();
		}

		//Printer.printTheory(theory, false);
		return theory.getAction();
	}

	public Theory getTheory(Knowledge knowledge, SokobanWorld world){

		Theory theory = null;
		if(planInExecution != null)
			theory = planInExecution.getTheory(theoryIndex++);

		if(theory == null) {
			this.theoryIndex = 0;
			this.planInExecution = null;
			if(knowledge.theories.size() > Consts.MIN_KNOWLEDGE_TO_PLAN) {
				planInExecution = tryToBuildPlan(knowledge, world);
			}
			if(planInExecution == null) {

				Scenario scenario = world.getCurrentScenario();
				List<Theory> theories = knowledge.getMatchingTheories(scenario, 3, false);

				double demand = 0.75;

				while (theory == null) {

					// If there is not an exact match, but close, mutate one
					if (theories.size() > 0 && theories.get(0).match != 0 && theories.get(0).getRelevance() > demand) {
						Scenario mutateScenario = Scenario.mutate(theories.get(0).scenario, scenario);
						Theory t = new Theory(mutateScenario);
						t.setAction(theories.get(0).getAction());
						theory = knowledge.exists(t);
						if(theory == null) theory = t;
					}

					theory = pickOneWorthIt(theories, demand);

					if (theory == null) {
						theory = buildNewTheory(scenario, world, theories);
						if (theory == null)
							demand = 0;
					} else {
						if (theory.getUtility() < 0.01) {
							System.out.println("!!! Picking a BAD theory" + theory);
						}
					}
					demand *= 0.5;
				}
				planInExecution = new Plan(theory);
			}
			theory = planInExecution.getTheory(theoryIndex++);
		}
		return theory;
	}

	public double getEntropy() {
		double entropy = 2 * (noChangesCounter / 200.0); //higher val = 0.5
		// the higher the gameTick, higher the entropy
		entropy += this.gameTick / 4000.0;
		return entropy;
	}

	public double demandUtility() {
		// starts in 1
		double result = 1;
		// the higher steps without changing nothing, higher the entropy
		double entropy = getEntropy();
		result += (randomGenerator.nextInt(10)) * Math.pow(1 + entropy,2);
		return result;
	}

	public Plan tryToBuildPlan(Knowledge knowledge, SokobanWorld world){
		double demand = demandUtility();
		List<Theory> theories = knowledge.getHighUtilityTheories(demand);
		//TODO sort by proximity

		List<Theory> roadMap;
		Scenario origin = world.getCurrentScenario();

		for(int i = 0; i < theories.size(); i++) {
			Theory goal = theories.get(i);
			roadMap = knowledge.tryGetMinPath(origin, goal.getScenario());
			if(roadMap != null ) {
				return new Plan(roadMap);
			}
		}
		return null;
	}

	private void tainTheories(List<Theory> theories, int tainFactor){
		double sucessDelta = 2;
		//System.out.println("Taining " + tainFactor);
		for(int i = theories.size() - 1; i >= 0; i--){
			Theory theory = theories.get(i);
			//Printer.printTheory(theory, true);
			theory.sucessFactor = theory.sucessFactor + (tainFactor * (theory.sucessFactor / Math.pow(sucessDelta++,2)));
			//System.out.println(" -> " + theory.getUtility());
		}
	}

	private Theory pickOneWorthIt(List<Theory> theories, double threshold){
		if (theories.size() == 0 || theories.get(0).getRelevance() <= threshold) return null;

		double accumUtil = 0;
		double base = getEntropy() / 100.0; //Very low probability for zero Utility

		//As it's ordered by relevance, we weight the first ones
		int size = Math.min(theories.size(), 5);
		for(int i = 0; i < size; i ++){
			accumUtil += base + Math.max(theories.get(i).getRelevance(), 0);
		}
		double[] norm = new double[size];
		for(int i = 0; i < size; i ++){
			norm[i] = (base + Math.max(theories.get(i).getRelevance(), 0)) / accumUtil;
		}

		double rnd = randomGenerator.nextDouble();
		for(int i = 0; i < size; i++) {
			rnd -= norm[i];
			if(rnd <= 0) return theories.get(i);
		}
		return theories.get(0);
	}

	private Theory buildNewTheory(Scenario scenario, SokobanWorld world, List<Theory> theories){
		Theory theory = new Theory(scenario);

		ArrayList<Types.ACTIONS> actions = world.stateObservation.getAvailableActions();

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

		evaluateTheory(theory, new SokobanWorld(stCopy));
		theory.k++;
	}

	private void evaluateTheory(Theory theory, SokobanWorld resultWorld){
		if(resultWorld.stateObservation.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
			theory.setUtility(0);
			return;
		}

		Vector2d playerPos = resultWorld.normalizePos(resultWorld.getMyPlayerPosition());
		Scenario resultScenario = new Scenario(resultWorld.stateObservation.getObservationGrid(), playerPos);

		if (theory.prediction == null) {
			theory.prediction = resultScenario;
			theory.p = 1;
		} else {
			CompareResult predictionCompare = theory.prediction.compare(resultScenario, null);
			if (predictionCompare.value() == 0) {
				theory.p++;
			} else {
				buildMutantTheories(theory, resultScenario);
			}
		}

		CompareResult compare = theory.getScenario().compare(resultScenario, null);
		if(compare.value() == 0) {
			theory.setUtility(0);
		} else {

			if (resultWorld.stateObservation.getGameWinner() == Types.WINNER.PLAYER_WINS) {
				theory.setUtility(5);
				addLastChangeStateTheory(theory);
				tainTheories(this.lastChangeStateTheorys, 1);
			} else {

				CompareResult compareResult = calculateStateUtility(world);
				CompareResult newCompareResult = calculateStateUtility(resultWorld);

				//Has moved boxes
				if (Math.abs(compareResult.scenarioDist - newCompareResult.scenarioDist) > 0) {
					addLastChangeStateTheory(theory);
				} else {
					this.noChangesCounter++;
				}

				theory.setUtility(0.5 + (compareResult.value() - newCompareResult.value()));
			}
		}
	}

	private void buildMutantTheories(Theory origin, Scenario resultScenario){
		Theory exact = new Theory(origin.getScenario());
		exact.k = 1;
		exact.p = 1;
		exact.prediction = resultScenario;
		exact.setAction(origin.getAction());
		exact.setUtility(origin.getUtility());
		this.knowledge.addTheory(exact);

		Theory mutant = new Theory(origin.getScenario());
		mutant.k = 1;
		mutant.p = 1;
		mutant.prediction = Scenario.mutate(origin.prediction, resultScenario);
		mutant.setAction(exact.getAction());
		mutant.setUtility(origin.getUtility());
		this.knowledge.addTheory(mutant);
	}

	private void addLastChangeStateTheory(Theory theory){
		this.noChangesCounter = 0;
		if (this.lastChangeStateTheorys.size() > 4)
			this.lastChangeStateTheorys.remove(0);
		this.lastChangeStateTheorys.add(theory);
	}

	private CompareResult calculateStateUtility(SokobanWorld world){

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
		return new CompareResult(boxDist, playerDist);
	}
}
