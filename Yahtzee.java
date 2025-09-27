import java.awt.Color;
import java.awt.Font;
import java.awt.*;

import acm.graphics.GLabel;
import acm.io.*;
import acm.program.*;
import acm.util.*;

/** ADDED:
 * Line 175 - 186 Multiple yahtzee scores.
 * Line 180 - 205 Bonus message on bonus yahtzees
 * Line 100 - 136 Prints Apropriate Message for winner on canvas
 */

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		setGlobalVariables();
		getPlayerNames(dialog);
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

/** initializes player names */
	private void getPlayerNames(IODialog dialog) {
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		
	}

/** initializing instance variables */
	private void setGlobalVariables() {
		playerNames = new String[nPlayers];
		topScore = new int[nPlayers];
		bottomScore = new int[nPlayers];
		bonus = new int[nPlayers];
		categoriesWithScore = new boolean[nPlayers][N_SCORING_CATEGORIES];
		yahtzeeAmount = new int[nPlayers];
	}

/** displays scores */
	private void displayScores() {
		for(int i = 1; i <= nPlayers; i++){
			displayScoreForOnePlayer(i);
		}
		
	}

/** displays score for one player */
	private void displayScoreForOnePlayer(int indexOfPlayer) {
		updateTopScores(indexOfPlayer);
		updateBottomScores(indexOfPlayer);
	}

/** updates Bottom Scores */
	private void updateBottomScores(int indexOfPlayer) {
		display.updateScorecard(16,indexOfPlayer,bottomScore[indexOfPlayer - 1]);
		display.updateScorecard(17,indexOfPlayer,topScore[indexOfPlayer - 1] + bottomScore[indexOfPlayer - 1] + bonus[indexOfPlayer - 1]);
		
	}

/** updates top scores */
	private void updateTopScores(int indexOfPlayer) {
		display.updateScorecard(7,indexOfPlayer,topScore[indexOfPlayer - 1]);
		display.updateScorecard(8,indexOfPlayer,checkForBonus(indexOfPlayer));
		
	}

/** checks for bonus score and adds it */
	private int checkForBonus(int indexOfPlayer) {
		int score = 0;
		if(topScore[indexOfPlayer - 1] >= 63){
			score = 35;
			bonus[indexOfPlayer - 1] = 35;
		}
		return score;
	}

/** plays the game (plays all 13 rounds and displays end scores) */
	private void playGame() {
		for(int j = 1; j <= 1; j++){
			playRound();
		}
		displayScores();
		findWinner();
	}
	
/** finds winner and prints appropriate message */
	private void findWinner() {
		String winnersMessage = findWinnersName();
		printWinMessage(winnersMessage);
		display.printMessage(winnersMessage);
	}
	
/** prints winners message */
	private void printWinMessage(String winnersMessage) {
		GLabel winMessage = new GLabel(winnersMessage,APPLICATION_WIDTH - 210 ,APPLICATION_HEIGHT / 2 - 15);
		winMessage.setFont(new Font("SansSerif",10, 28));
		winMessage.setColor(Color.BLUE);
		add(winMessage);
	}

/** finds winners name and returns winners message with winners name */
	private String findWinnersName() {
		int winnerIndex = 0;
		for(int i = 1; i < nPlayers; i++){
			winnerIndex = getHigherScore(winnerIndex, i);
		}
		for(int i = 0; i < nPlayers; i++){
			if((topScore[i] + bottomScore[i] + bonus[i] == topScore[winnerIndex] + bottomScore[winnerIndex] + bonus[winnerIndex]) && i != winnerIndex){
				return "DRAW";
			}
		}
		return playerNames[winnerIndex] + " You Win";
	}

/** gets Higher score between 2 players */
	private int getHigherScore(int winnerIndex, int i) {
		if(topScore[i] + bottomScore[i] + bonus[i] > topScore[winnerIndex] + bottomScore[winnerIndex] + bonus[winnerIndex]){
			return i;
		}
		return winnerIndex;
	
	}

/** plays a round (plays 1 turn for each player) */
	private void playRound() {
		for(int i = 1; i <= nPlayers; i++){
			turn(i);
		}
		
	}

/** plays a turn (rolls dice, updates category and prints appropriate message) */
	private void turn(int indexOfPlayer) {
		int[] diceNumbers = firstRoll(indexOfPlayer);
		diceNumbers = reRollTwice(diceNumbers);
		display.printMessage("please select a category");
		updateCategory(indexOfPlayer, diceNumbers);
		
		
	}

/** updates category based on dice Numbers */
	private void updateCategory(int indexOfPlayer, int[] diceNumbers) {
		while(true){
			int category = display.waitForPlayerToSelectCategory();
			boolean categoryCompleted = checkCategory(category, diceNumbers);
			checkForMultipleYahtzee(indexOfPlayer, diceNumbers);
			if(categoriesWithScore[indexOfPlayer - 1][getCategory(category)] == false){
				updateScore(category, indexOfPlayer, categoryCompleted, diceNumbers);
				break;
			}
			else{
				display.printMessage("please choose another category");
			}
		}
		
	}

/** checks if there are more yahtzees */
	private void checkForMultipleYahtzee(int indexOfPlayer , int[] diceNumbers) {
		if(checkForNumberNTimes(5, diceNumbers) && yahtzeeAmount[indexOfPlayer - 1] >= 1){
			addMultipleYahtzeeScore(indexOfPlayer);
			bonusMessage(indexOfPlayer);
		}
	}

/** adds score for multiplay yahtzzes */
	private void addMultipleYahtzeeScore(int indexOfPlayer) {
		display.updateScorecard(14,indexOfPlayer, yahtzeeAmount[indexOfPlayer - 1] * 100 + 50);
		yahtzeeAmount[indexOfPlayer - 1] += 1;
	}
	
/** adds message on bonus yahtzees */
	private void bonusMessage(int indexOfPlayer) {
		GLabel bonusYahtzeeText = new GLabel("+100 Points",APPLICATION_WIDTH - 185 ,APPLICATION_HEIGHT / 2 - 15);
		bonusYahtzeeText.setFont(new Font("SansSerif",10, 28));
		bonusYahtzeeText.setColor(Color.RED);
		popUpMessageFor2Seconds(bonusYahtzeeText);
	}

/** adds a message and removes it afte 2 seconds */
	private void popUpMessageFor2Seconds(GLabel bonusYahtzeeText) {
		add(bonusYahtzeeText);
		pause(1500);
		remove(bonusYahtzeeText);
		
	}


/** checks if category is completed */
	private boolean checkCategory(int category, int[] diceNumbers) {
		if((category >= 1 && category <= 6) || category == 15){
			return true;
		}
		if(category == 9){
			return checkForNumberNTimes(3, diceNumbers);
		}
		if(category == 10){
			return checkForNumberNTimes(4, diceNumbers);
		}
		if(category == 14){
			return checkForNumberNTimes(5, diceNumbers);
		}
		if(category == 11){
			return checkForFullHouse(diceNumbers);
		}
		if(category == 12){
			return checkSmallStraight(diceNumbers);
		}
		else{
			return checkBigStraight(diceNumbers);
		}
	}

/** checks dice numbers for a specific number */
	private boolean checkForNum(int num, int[] diceNumbers){
		for(int i = 0; i < 5; i++){
			if(diceNumbers[i] == num){
				return true;
			}
			
		}
		return false;
	}
	
/** checks dice numbers for small straight */
	private boolean checkSmallStraight(int[] diceNumbers) {
		if(checkForNum(4, diceNumbers) && checkForNum(3, diceNumbers)){
			if(checkForNum(1, diceNumbers) && checkForNum(2, diceNumbers)){
				return true;
			}
			if(checkForNum(5, diceNumbers) && checkForNum(6, diceNumbers)){
				return true;
			}
			if(checkForNum(2, diceNumbers) && checkForNum(5, diceNumbers)){
				return true;
			}
		}
		return false;
	}
	
/** checks dice numbers for big straight */
	private boolean checkBigStraight(int[] diceNumbers) {
		if(checkExactDuplicantsForInt(2, 1, diceNumbers) && checkExactDuplicantsForInt(3, 1, diceNumbers) && 
				checkExactDuplicantsForInt(4, 1, diceNumbers) && checkExactDuplicantsForInt(5, 1, diceNumbers)){
			if(checkExactDuplicantsForInt(1, 1, diceNumbers)){
				return true;
			}
			if(checkExactDuplicantsForInt(6, 1, diceNumbers)){
				return true;
			}
			
		}
		return false;
	}
	
/** checks dice numbers for full house */
	private boolean checkForFullHouse(int[] diceNumbers) {
		boolean isFullHouse = false;
		if(checkForNumberNTimes(3, diceNumbers)){
			isFullHouse = checkForNumberExactlyNTimes(2, diceNumbers);
		}
		return isFullHouse;
	}

/** checks if there are exactly [amountOfTimes] duplicants  in dice numbers */
	private boolean checkForNumberExactlyNTimes(int amountOfTimes, int[] diceNumbers) {
		for(int i = 1; i <= 6; i++){
			boolean isTrue = checkExactDuplicantsForInt(i, amountOfTimes, diceNumbers);
			if(isTrue == true){
				return true;
			}
		}
		return false;
	}

/** checks if there are exactly [amountOfTimes] duplicants of a specific number in dice numbers */
	private boolean checkExactDuplicantsForInt(int diceNumber, int amountOfTimes, int[] diceNumbers) {
		int amount = 0;
		amount = amountOfNumberInDiceNumbers(diceNumber, amount, diceNumbers);
		return isEqual(amount, amountOfTimes);
	}

/** checks if 2 numbers are equal */
	private boolean isEqual(int amount, int amountOfTimes) {
		if(amount == amountOfTimes){
			return true;
		}
		else{
			return false;
		}
	
	}

/** checks if there are more duplicants than [amountOfDuplicants] in dice numbers */
	private boolean checkForNumberNTimes(int amountOfDuplicants, int[] diceNumbers) {
		for(int i = 1; i <= 6; i++){
			boolean isTrue = checkDuplicantsForInt(i, amountOfDuplicants, diceNumbers);
			if(isTrue == true){
				return true;
			}
		}
		return false;
		
	}

/** checks if there are more duplicants than [amountOfDuplicants] of a specific number in dice numbers */
	private boolean checkDuplicantsForInt(int diceNumber, int amountOfDuplicants, int[] diceNumbers) {
		int amount = 0;
		amount = amountOfNumberInDiceNumbers(diceNumber, amount, diceNumbers);
		return compare2Numbers(amount, amountOfDuplicants);
		
	}

/** compares 2 numbers, returns true if first one is bigger */
	private boolean compare2Numbers(int amount, int amountOfDuplicants) {
		if(amount >= amountOfDuplicants){
			return true;
		}
		else{
			return false;
		}
		
	}

/** counts the number of times a specific number was in dice numbers */
	private int amountOfNumberInDiceNumbers(int diceNumber, int amount, int[] diceNumbers) {
		for(int i = 0; i < N_DICE; i++){
			if(diceNumber == diceNumbers[i]){
				amount++;
			}
		}
		return amount;
	}

/** checks for a specific number in dice numbers and returns the amount of score for one of the top categories*/
	private int checkForNumbersInDice(int i, int[] diceNumbers) {
		int sum = 0;
		for(int j = 0; j < N_DICE; j++){
			if(diceNumbers[j] == i){
				sum += i;
			}
		}
		return sum;
		
	}

/** updates score on the score board after player chooses category */
	private void updateScore(int category, int indexOfPlayer, boolean categoryCompleted, int[] diceNumbers) {
		if(categoryCompleted == true){
			display.updateScorecard(category,indexOfPlayer,scoreCalculator(category, indexOfPlayer, diceNumbers));
		}
		else{
			display.updateScorecard(category,indexOfPlayer,0);
		}
		categoriesWithScore[indexOfPlayer - 1][getCategory(category)] = true;
		
	}

/** rolls twice */
	private int[] reRollTwice(int[] diceNumbers) {
		diceNumbers = roll(diceNumbers);
		diceNumbers = roll(diceNumbers);
		return diceNumbers;
	}

/** rolls the dice with ability to select unwanted dice */
	private int[] roll(int[] diceNumbers) {
		display.printMessage("please select the dice u want to reroll");
		display.waitForPlayerToSelectDice();
		diceNumbers = formNewDice(diceNumbers);
		display.displayDice(diceNumbers);
		return diceNumbers;
	}

/** rolls the dice first time */
	private int[] firstRoll(int indexOfPlayer) {
		display.printMessage("please roll the dice " + playerNames[indexOfPlayer - 1]);
		display.waitForPlayerToClickRoll(indexOfPlayer);
		int[] diceNumbers = createDiceNumbers();
		display.displayDice(diceNumbers);
		return diceNumbers;
	}

/** gets category number*/
	private int getCategory(int category) {
		if(category >= 9 && category <= 15){
			return category - 3;
		}
		else{
			return category - 1;
		}
	}

/** calculates score depending on category */
	private int scoreCalculator(int category,int indexOfPlayer, int[] diceNumbers) {
		int score = 0;
		if(category == 11){
			score = 25;
			bottomScore[indexOfPlayer - 1] += 25;
		}
		else if(category == 12){
			score = 30;
			bottomScore[indexOfPlayer - 1] += 30;
		}
		else if(category == 13){
			score = 40;
			bottomScore[indexOfPlayer - 1] += 40;
		}
		else if(category == 14){
			score = 50;
			bottomScore[indexOfPlayer - 1] += 50;
			yahtzeeAmount[indexOfPlayer - 1] = 1;
		}
		else if(category == 15 || category == 9 || category == 10){
			score = addScore(diceNumbers);
			bottomScore[indexOfPlayer - 1] += score;
		}
		else{
			score = checkForTopCategories(category, indexOfPlayer, diceNumbers);
		}
		
		return score;
	}

/** checks for top categories and sees which one is complated */
	private int checkForTopCategories(int category, int indexOfPlayer, int[] diceNumbers) {
		int score = 0;
		for(int i = 1; i <=6; i++){
			if(category == i){
				score = checkForNumbersInDice(i, diceNumbers);
				topScore[indexOfPlayer - 1] += score;
			}
		}
		return score;
	}

/** adds score depending on dice numbers */
	private int addScore(int[] diceNumbers) {
		int score = 0;
		for(int i = 0; i < N_DICE; i++){
			score += diceNumbers[i];
		}
		return score;
	}

/** forms new dice */
	private int[] formNewDice(int[] diceNumbers) {
		for(int i = 0; i < N_DICE; i++){
			if(display.isDieSelected(i)){
				diceNumbers[i] = rgen.nextInt(1, 6);
			}

		}
		return diceNumbers;
	}

/** creates dice numbers */
	private int[] createDiceNumbers() {
		int[] diceNumbers = new int[N_DICE];
		for(int i = 0; i < N_DICE; i++){
			diceNumbers[i] = rgen.nextInt(1, 6);
		}
		return diceNumbers;
	}

/* Private instance variables */
	private int nPlayers = 1;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[] topScore = new int[nPlayers];
	private int[] bottomScore = new int[nPlayers];
	private int[] bonus = new int[nPlayers];
	private boolean[][] categoriesWithScore = new boolean[nPlayers][N_SCORING_CATEGORIES];
	private int[] yahtzeeAmount = new int[nPlayers];
}
