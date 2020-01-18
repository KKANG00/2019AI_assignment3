import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

//individual object
class Individual{
	
	//arraylist to save the location(row) of queens at each column
	ArrayList<Integer> state = new ArrayList<Integer>();
	
	//the number of attackable queens
	int attackable = 100;
	
	//set functions
	void setstate(ArrayList<Integer> state) {
		this.state = state;
	}	
	void setvalue(int attackable) {
		this.attackable = attackable;
	}

}

//population object (consisted by individuals) 
class Population {
	ArrayList<Individual> population = new ArrayList<Individual>();
}

//generation object (consisted by population) 
class Generation {
	//number of generation
	int generation_number = 1;
	
	Population thisgeneration = new Population();
	
	void setpopulation(Population population) {
		this.thisgeneration = population;
	}
	
	void setgeneration_number(int generation_number) {
		this.generation_number = generation_number;
	}
}

public class nqueens {

	//population size
	static final int POPULATION_SIZE = 1000;
	//parent size (50% of population)
	static final int PARENTS_SIZE = 500;
	//crossover size (50% of population)
	static final int CROSSOVER_SIZE = 500;
	//mutation size (60% of parents)
	static final int MUTATION_SIZE = 300;
	
	//n value
	static int input;
		
	//convert to string form
	static String convert(ArrayList<Integer> state) {
		String result="";
		
		for(int i=0;i<state.size();i++)
			result += state.get(i)+" ";
		
		return result;
	}
	
	//calculate attackable value
	static int attackablevalue(Individual individual) {
		ArrayList<Integer> state = individual.state;
		
		int count = 0;
		int size = state.size();
		//calculate the number of queens which are attackable for any of 3 side
		for(int i=0;i<size;i++) {
			for(int j=i+1;j<size;j++) {
				//row check, diagonal check
				if(state.get(i)==state.get(j) || Math.abs(state.get(i)-state.get(j))==Math.abs(i-j))
					count++;
			}
		}
			
		return count;
	}
	
	//make random state by locating every queen at each column randomly
	static Individual randomIndividual() {
		ArrayList<Integer> state = new ArrayList<Integer>();
		Random rand = new Random();
		
		for(int i=0;i<input;i++) {
			state.add(rand.nextInt(input));
		}
		
		Individual randomIndividual = new Individual();
		
		randomIndividual.setstate(state);
		randomIndividual.setvalue(attackablevalue(randomIndividual));
		
		return randomIndividual;
	}
	
	//do crossover
	static Individual crossover(Individual parent1, Individual parent2) {
		
		Individual newindividual = new Individual();
		
		ArrayList<Integer> newstate = new ArrayList<Integer>();
		
		//parent2 is better, append parent1's genes at the end of parent2's
		if(parent1.attackable > parent2.attackable) {
			newstate.add(parent1.state.get(0));
			for(int i=1;i<input-1;i++) {
				newstate.add(parent2.state.get(i));
			}
			newstate.add(parent1.state.get(input-1));
		}
		//parent1 is better or same, append parent2's genes at the end of parent1's
		else {
			newstate.add(parent2.state.get(0));
			for(int i=1;i<input-1;i++) {
				newstate.add(parent1.state.get(i));
			}
			newstate.add(parent2.state.get(input-1));
		}
		
		newindividual.setstate(newstate);
		newindividual.setvalue(attackablevalue(newindividual));
		
		return newindividual;
	}	
	
	//do mutation by replacing one duplicate gene
	static Individual mutation(Individual parent) {
		
		Individual mutation = new Individual();
		Random r = new Random();
		
		ArrayList<Integer> newstate = (ArrayList<Integer>) parent.state.clone();
		
		//which gene to change
		int changepoint = -1;
		
		//check duplication
		for(int i=0;i<input;i++) {
			int a = newstate.get(i);
			for(int j=i+1;j<input;j++) {
				int b = newstate.get(j);
				if(a==b) {
					changepoint = j;
					i=input;
					break;
				}
			}
		}
		
		//there is no duplication, change one gene randomly
		if(changepoint==-1) {
			int randomgene = r.nextInt(input);
			changepoint = r.nextInt(input);
			
			newstate.set(changepoint, randomgene);
		}
		//for duplicate gene change to another gene that is not duplicated
		else {			
			for(int k=0;k<input;k++) {
				if(!newstate.contains(k)) {
					newstate.set(changepoint, k);
					break;
				}
			}
		}
		
		mutation.setstate(newstate);
		mutation.setvalue(attackablevalue(mutation));
		
		return mutation;
	}
	
	//select individuals to be parents in next generation
	static ArrayList<Individual> selection(Generation G) {
		
		ArrayList<Individual> parents = new ArrayList<Individual>();
		//numbers that already selected
		ArrayList<Integer> selected = new ArrayList<Integer>();
		Random rand = new Random();
		
		//use tournament method with k=10 
		int tournament = 10;
		//number of most fit individual
		int winner = 0; 
		int population_size = POPULATION_SIZE;
		
		for(int j=0;j<PARENTS_SIZE;j++) {
			int fittest = 1000; 
			for(int i=0;i<tournament;i++) {
				int random = rand.nextInt(population_size);
				//choose random index that is not be selected yet
				while(selected.contains(random)) random = rand.nextInt(population_size);
				//find best individual
				if(G.thisgeneration.population.get(random).attackable < fittest) {
					fittest = G.thisgeneration.population.get(random).attackable;
					winner= random;
				}
			}
			
			parents.add(G.thisgeneration.population.get(winner));
			population_size--;
			selected.add(winner);
		}
		
		return parents;
	}
	
	//generation fitness measure
	static String fitness (Generation G) {
		String result = "";
		
		//add all individuals' 100-attackable values
		int countall = 0;
		//find the answer or not
		String find = "false";
		//answer index
		int answer = -1;
		
		//add all individuals' 100-attackable values
		for(int i=0;i<POPULATION_SIZE;i++) {
			countall += 100-G.thisgeneration.population.get(i).attackable;
			if(G.thisgeneration.population.get(i).attackable == 0) {
				find = "true";
				answer = i;
			}
		}
		
		//average of 100-attackable value
		int fitness = countall/POPULATION_SIZE;
		//measurement result string
		result = fitness+","+find+","+answer;
		
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		//start
		double start = System.currentTimeMillis();
		
		input = Integer.parseInt(args[0]);
		Random rand = new Random();
		int generation_number = 1; 
		int answer_index = -1;
		boolean find = false;
		
		Generation G = new Generation();
		
		//initial generation (random individuals)
		for(int i=0;i<POPULATION_SIZE;i++) {
			G.thisgeneration.population.add(randomIndividual());
		} G.setgeneration_number(generation_number);
		
		while(!find&&input>3) {
			
			//0. fitness measure
			String fitnessresult = fitness(G);
			
			String[] result = fitnessresult.split(",");
			if(result[1].equals("true")) {
				find = true;
				answer_index = Integer.parseInt(result[2]);
				break;
			}
			
			Generation newG = new Generation();
			newG.setgeneration_number(generation_number++);
			
			//make new generation
			//1. selection
			ArrayList<Individual> parents = selection(G);
			//2. mutation
			for(int i=0;i<MUTATION_SIZE;i++) {
				Individual mutation = mutation(parents.get(i));
				parents.set(i, mutation);
			}
			//3. add parents in next generation
			for(int i=0;i<PARENTS_SIZE;i++) {
				newG.thisgeneration.population.add(parents.get(i));
			}
			//4. crossover
			for(int i=0;i<CROSSOVER_SIZE;i++) {
				int random1 = rand.nextInt(PARENTS_SIZE);
				int random2 = rand.nextInt(PARENTS_SIZE);
				Individual newindividual = crossover(parents.get(random1), parents.get(random2));
				newG.thisgeneration.population.add(newindividual);
			}
			
			G = newG;
		}
		
		//end time
		double end = System.currentTimeMillis();
		double time = (end-start)/(double)1000.0;

		//string to be written in the file
		String searchtime = "Total Elapsed time: "+time;
		
		//for file
		String result = "";
		if(input<=3) {
			result = "No solution";
			searchtime = "";
		}
		else result = convert(G.thisgeneration.population.get(answer_index).state);
				
		//set path and filename and write to the text file
		String path = args[1];
		String filename = "result"+input+".txt";

		BufferedWriter file;
		file = new BufferedWriter(new FileWriter(new File(path, filename)));
						
		try {
			
			file.write(">Genetic Algorithm");
			file.newLine();
			file.write(result);
			file.newLine();
			file.write(searchtime);
			file.close();
					
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
}
