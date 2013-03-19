package game.logic;

import game.cli.CLInterface;
import game.gui.GUInterface;
import game.ui.EagleEvent;
import game.ui.FightEvent;
import game.ui.GameEvent;
import game.ui.GameOptions;
import game.ui.GameOutput;
import game.ui.ResultEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import maze_objects.*;

public class Game {

	//Exit states
	public static final int EXIT_OPEN = 1;
	public static final int EXIT_CLOSED = 0;

	/*** Private Attributes ***/

	//State Attributes
	private int dragonType;
	private int exitState;
	private int numberOfDragons;
	private int remainingDragons;

	//Game elements
	private Maze maze;
	private Sword sword;
	private Hero hero;
	private Eagle eagle;
	private ArrayList<Dragon> dragons;
	private LinkedList<GameEvent> events = new LinkedList<GameEvent>();

	/*** Private Methods ***/

	//Game Initializers

	//Creates a hero object on a random valid position in the maze
	private Hero spawnHero() {
		Random random = new Random();
		int hero_row = 0;
		int hero_column = 0;

		do {
			hero_row = random.nextInt(maze.getRows());
			hero_column = random.nextInt(maze.getColumns());
		} while (!validHeroSpawn(hero_row, hero_column));

		Hero h = new Hero(hero_row, hero_column);
		return h;
	}

	//Creates a sword object on a random valid position in the maze
	private Sword spawnSword() {
		Random random = new Random();
		int sword_row = 0;
		int sword_column = 0;

		do {
			sword_row = random.nextInt(maze.getRows());
			sword_column = random.nextInt(maze.getColumns());
		} while (!validSwordSpawn(sword_row, sword_column));

		Sword sd = new Sword(sword_row, sword_column);
		return sd;
	}

	//Creates a dragon object on a random valid position in the maze
	private Dragon spawnDragon() {
		Random random = new Random();
		int dragon_row = 0;
		int dragon_column = 0;

		do {
			dragon_row = random.nextInt(maze.getRows());
			dragon_column = random.nextInt(maze.getColumns());
		} while (!validDragonSpawn(dragon_row, dragon_column));

		Dragon dragon = new Dragon(dragon_row, dragon_column, dragonType);
		return dragon;
	}

	private boolean validHeroSpawn(int hero_row, int hero_column) {
		return maze.checkIfEmpty(hero_row, hero_column);
	}

	private boolean validSwordSpawn(int sword_row, int sword_column) {
		return maze.checkIfEmpty(sword_row, sword_column) 
				&& !nextToHeroOrOnTop(sword_row, sword_column)
				&& !isOnAliveDragon(sword_row, sword_column);
	}

	private boolean validDragonSpawn(int dragon_row, int dragon_column) {
		return maze.checkIfEmpty(dragon_row, dragon_column)
				&& !nextToHeroOrOnTop(dragon_row, dragon_column)
				&& !nextToAliveDragons(dragon_row, dragon_column);
	}

	private Eagle spawnEagle(int row, int column) { 
		Eagle eagle = new Eagle(row, column, true);
		return eagle;
	}

	private ArrayList<Dragon> spawnDragons() {
		ArrayList<Dragon> dragons = new ArrayList<Dragon>();
		for(int i = 0; i < numberOfDragons; i++)
			dragons.add(spawnDragon());

		return dragons;
	}

	//Returns true if the position given is the same as the eagle's and the eagle is alive
	private boolean isOnLivingEagle(int row, int column) {
		return(row == eagle.getRow()
				&& column == eagle.getColumn()
				&& eagle.getState() != Eagle.DEAD);
	}

	//True if the object is adjacent to the hero (horizontally, vertically or on top), false if not
	private boolean nextToHeroOrOnTop(int row, int column) {
		return((row == hero.getRow() + 1 && column == hero.getColumn()) ||
				(row == hero.getRow() - 1  && column == hero.getColumn()) ||
				(column == hero.getColumn() + 1 && row == hero.getRow()) ||
				(column == hero.getColumn() - 1 && row == hero.getRow()) ||
				(column == hero.getColumn() && row == hero.getRow()));
	}

	//Processes encounters between the hero and a dragon
	private boolean checkDragonEncounters(boolean goOn) {

		for(Dragon dragon : dragons)
			if ( dragonFightCanHappen(dragon) ) {
				if( wonAgainstDragon(dragon) ) {
					FightEvent wonFight = new FightEvent("wonFight");
					events.add(wonFight);
				}
				else {
					goOn = false;
					FightEvent lostFight = new FightEvent("lostFight");
					events.add(lostFight);
					return goOn;
				}
			}

		return goOn;
	}

	private boolean dragonFightCanHappen(Dragon dragon) {
		return hero.getState() != Hero.DEAD 
				&& nextToAliveDragon(hero.getRow(), hero.getColumn(), dragon) 
				&& !dragonAsleepHeroUnarmed(dragon);
	}

	private boolean dragonAsleepHeroUnarmed(Dragon dragon) {
		return (hero.getState() != Hero.ARMED 
				&& dragon.getState() == Dragon.ASLEEP);
	}

	private void checkEagleEncounters() {
		for(Dragon dragon : dragons)
			if(eagle.getState() != Eagle.DEAD && nextToAliveDragon(eagle.getRow(), eagle.getColumn(), dragon)) {
				eagle.killEagle();
				EagleEvent eagleKilled = new EagleEvent("killed");
				events.add(eagleKilled);
			}
	}

	//Executes the dragons' turn
	private boolean moveDragons(boolean goOn) {
		for(Dragon d : dragons)
			if(dragonNotStaticOrDead(d)) {
				d.moveDragon(this);
			}
		return goOn;
	}

	private boolean dragonNotStaticOrDead(Dragon dragon) {
		return dragon.getType() != Dragon.STATIC
				&& (dragon.getState() == Dragon.ALIVE || dragon.getState() == Dragon.ASLEEP);
	}

	private void tryToSendEagle() {
		if(eagle.getState() == Eagle.DEAD) {
			EagleEvent ed = new EagleEvent("cantSendDead");
			events.add(ed);
		}
		else if(!eagle.isWithHero()) {
			EagleEvent or = new EagleEvent("cantSendOnRoute");
			events.add(or);
		}
		else if(sword.isTaken()) {
			EagleEvent ns = new EagleEvent("noSword");
			events.add(ns);
		}
		else
			eagle.takeOff(hero.getRow(), hero.getColumn(), sword); // launches eagle
	}

	private void updateEagle() {
		if (eagle.getState() != Eagle.DEAD && eagle.isWithHero()){
			eagle.moveWithHero(hero.getRow(), hero.getColumn());
		}
		else if(eagle.getState() != Eagle.DEAD) {
			eagle.moveEagle();
			if(eagle.isOnGroundWithSword()) {
				EagleEvent gs = new EagleEvent("gotSword");
				events.add(gs);
			}
			if(eagle.isWaitingForHero()) {
				EagleEvent wh = new EagleEvent("isWaiting");
				events.add(wh);
			}

			if(eagle.isWaitingForHero() && isOnLivingEagle(hero.getRow(), hero.getColumn())) {
				eagle.returnToHero();
				sword.takeSword();
				hero.armHero(hero.getRow(), hero.getColumn() );
				EagleEvent er = new EagleEvent("eagleReturned");
				events.add(er);
			}

			if((eagle.isOnGroundWithSword() || eagle.isWaitingForHero()))
				checkEagleEncounters();

		}
	}

	private void checkHeroState() { //Checks if hero died or exited the maze, creating the necessary events
		switch(hero.getState()) {
		case Hero.EXITED_MAZE:
			ResultEvent won = new ResultEvent(2);
			events.add(won);
			break;
		case Hero.DEAD:
			ResultEvent lost = new ResultEvent(1);
			events.add(lost); 
			break;
		}
	}

	private void checkEnemyState() { //Checks if there are any enemies left
		if(remainingDragons == 0) {
			exitState = EXIT_OPEN;
			ResultEvent exitOpen = new ResultEvent(0);
			events.add(exitOpen);
		}
	}

	/*** Public Methods ***/

	//Main
	public static void main(String[] args) {
		//CLInterface cli = new CLInterface();
		//cli.startGame();
		
		GUInterface gui = new GUInterface();
		gui.startGame();
	}

	//Constructors
	public Game(GameOptions options) {
		int rows = options.rows, columns = options.columns;

		dragonType = options.dragonType;

		// A maze director is in charge of selecting a
		// building pattern and to order its construction
		MazeDirector director = new MazeDirector();

		if(options.randomMaze) {
			if(rows < 5 || columns < 5) {
				GameOutput.printMazeSizeError();
				MazeBuilder predefined = new PredefinedMaze();
				director.setMazeBuilder(predefined);
			} else {
				MazeBuilder randomMaze = new RandomMaze();
				director.setMazeBuilder(randomMaze);
			}
		} else {
			MazeBuilder predefined = new PredefinedMaze();
			director.setMazeBuilder(predefined);
		}

		director.constructMaze(rows, columns);
		maze = director.getMaze();

		//Multiple dragon options
		if(options.multipleDragons)
			numberOfDragons = (maze.getRows() + maze.getColumns()) / 10;
		else
			numberOfDragons = 1;

		remainingDragons = numberOfDragons;

		if(options.randomSpawns) {
			hero = spawnHero();
			dragons = spawnDragons();
			sword = spawnSword();
			eagle = spawnEagle(hero.getRow(), hero.getColumn());
		}
		else {
			hero = new Hero(options.heroRow, options.heroColumn);
			dragons = options.dragons;
			numberOfDragons = dragons.size();
			remainingDragons = numberOfDragons;
			sword = new Sword(options.swordRow, options.swordColumn);
			eagle = spawnEagle(hero.getRow(), hero.getColumn());
		}
	}

	//General Methods
	public int getDragonState(int i) {
		return dragons.get(i).getState();
	}

	public int getExitState() {
		return exitState;
	}

	public Maze getMaze() {
		return maze;
	}

	public Hero getHero() {
		return hero;
	}

	public Dragon getDragon(int i) {
		return dragons.get(i);
	}

	public ArrayList<Dragon> getDragons() {
		return dragons;
	}

	public int getNumberOfDragons() {
		return numberOfDragons;
	}

	public int getRemainingDragons() {
		return remainingDragons;
	}

	public Sword getSword() {
		return sword;
	}

	public void addEvent(GameEvent ev) {
		events.add(ev);
	}

	public LinkedList<GameEvent> getEvents() {
		return events;
	}

	public Eagle getEagle(){
		return eagle;
	}

	//Game Methods

	public boolean checkIfSword(int row, int column) { //Checks if an untaken sword is in that place
		return(row == sword.getRow() && column == sword.getColumn() && !sword.isTaken());
	}

	public boolean checkIfEagle(int row, int column) {
		return(row == eagle.getRow() && column == eagle.getColumn() && !eagle.isWithHero() );
	}

	public boolean checkIfOnAliveDragon(int row, int column) {
		for(Dragon dragon : dragons)
			if(row == dragon.getRow() && column == dragon.getColumn()
			&& (dragon.getState() == Dragon.ALIVE || dragon.getState() == Dragon.ASLEEP))
				return true;

		return false;
	}
	
	public boolean checkIfOnSleepingDragon(int row, int column) {
		for(Dragon dragon : dragons)
			if(row == dragon.getRow() && column == dragon.getColumn()
			&& dragon.getState() == Dragon.ASLEEP)
				return true;

		return false;
	}
	
	public boolean checkIfOnAwakeDragon(int row, int column) {
		for(Dragon dragon : dragons)
			if(row == dragon.getRow() && column == dragon.getColumn()
			&& dragon.getState() == Dragon.ALIVE)
				return true;

		return false;
	}

	public boolean wonAgainstDragon(Dragon dragon) { //True if the hero killed the dragon (was carrying sword), false if the hero died
		if(hero.getState() == Hero.ARMED) {
			dragon.setState(Dragon.DEAD);
			remainingDragons--;
			return true;
		}
		else if(hero.getState() == Hero.IN_GAME && dragon.getState() == Dragon.ALIVE) {
			hero.setState(Hero.DEAD);
			return false;
		}

		return false;
	}

	//True if the hero is adjacent to the dragon (horizontally, vertically or on top), false if not
	public boolean nextToAliveDragons(int row, int column) {

		if(dragons == null || dragons.isEmpty())
			return false;

		for(Dragon dragon : dragons) {
			if(nextToAliveDragon(row, column, dragon))
				return true;
		}

		return false;
	}

	public boolean nextToAliveDragon(int row, int column, Dragon dragon) {
		return(((dragon.getRow() == row + 1 && dragon.getColumn() == column)
				|| (dragon.getRow() == row - 1  && dragon.getColumn() == column)
				|| (dragon.getColumn() == column + 1 && dragon.getRow() == row)
				|| (dragon.getColumn() == column - 1 && dragon.getRow() == row)
				|| (dragon.getColumn() == column && dragon.getRow() == row))
				&& (dragon.getState() == Dragon.ALIVE || dragon.getState() == Dragon.ASLEEP));
	}

	public boolean isOnAliveDragon(int row, int column) {

		if(dragons == null || dragons.isEmpty())
			return false;

		for(Dragon dragon : dragons) {
			if((dragon.getColumn() == column && dragon.getRow() == row)
					&& (dragon.getState() == Dragon.ALIVE || dragon.getState() == Dragon.ASLEEP))
				return true;
		}

		return false;
	}

	public boolean heroTurn(char input) {
		boolean goOn = true;

		switch (input) {
		case 's':
			goOn = hero.moveHero(1, 0, this); //tries to move hero the number or rows or columns given
			break;
		case 'w':
			goOn = hero.moveHero(-1, 0, this);
			break;
		case 'a':
			goOn = hero.moveHero(0, -1, this);
			break;
		case 'd':
			goOn = hero.moveHero(0, 1, this);
			break;
		case ' ':
			goOn = hero.moveHero(0,  0,  this);
			break;
		case 'e':
			tryToSendEagle();
			break;
		case 'z': //z shuts down game
			goOn = false;
			break;
		default:
			break;
		}

		updateEagle();

		return goOn;
	}

	public boolean dragonTurn(boolean goOn) {
		goOn = checkDragonEncounters(goOn);
		goOn = moveDragons(goOn);
		return goOn;

	}

	public boolean checkState(boolean goOn) {
		goOn = checkDragonEncounters(goOn);
		checkEnemyState();
		checkHeroState();
		return goOn;
	}
}
