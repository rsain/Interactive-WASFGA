package jmetal.metaheuristics.wasfga;

import jmetal.core.*;
import jmetal.util.*;

/**
 * Implementation of the preference based algorithm named WASF-GA.
 * 
 * @author Rubén Saborido Infantes
 * 
 *         This algorithm is described in the paper: A.B. Ruiz, R. Saborido, M.
 *         Luque "A Preference-based Evolutionary Algorithm for Multiobjective
 *         Optimization: The Weighting Achievement Scalarizing Function Genetic
 *         Algorithm" Published in Journal of Global Optimization in 2014 
 *         DOI = {10.1007/s10898-014-0214-y}
 */
public class WASFGA extends Algorithm {
	double[][] weights_;
	AchievementScalarizingFunction asf;
	ReferencePoint referencePoint;
	boolean estimatePoints, normalization;
	String folderForOutputFiles;

	/**
	 * Constructor
	 *
	 * @param problem
	 *            Problem to solve
	 */
	public WASFGA(Problem problem) {
		super(problem);
	}

	/**
	 * Runs the WASF-GA algorithm.
	 *
	 * @return A <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int evaluations;
		String weightsDirectory, weightFileName;

		Solution newSolution;

		SolutionSet population;
		SolutionSet offspringPopulation;
		SolutionSet union;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		evaluations = 0;

		// --- INITIALIZING DATA --- \\

		// Read the population size
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		population = new SolutionSet(populationSize);

		// Read the number of evaluations
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

		// Read the output folder
		folderForOutputFiles = (String) this.getInputParameter("folderForOutputFiles");

		// Read the normalization parameter.
		// It indicates if the ASF is normalized or not
		normalization = ((Boolean) getInputParameter("normalization")).booleanValue();

		// Read the estimatePoints parameter.
		estimatePoints = ((Boolean) getInputParameter("estimatePoints")).booleanValue();
		// If true, the nadir and ideal point of the ASF will be estimated using
		// the solutions in the population
		if (estimatePoints) {
			asf = new AchievementScalarizingFunction(problem_.getNumberOfObjectives());

			// If the nadir and ideal point of the problem are not known, they
			// will be estimated using the population
			initializeBounds();
		} else {
			asf = (AchievementScalarizingFunction) getInputParameter("asf");
		}

		// Read the reference point
		referencePoint = ((ReferencePoint) this.getInputParameter("referencePoint"));
		try {
			asf.setReferencePoint(referencePoint);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Read the weights directory parameter
		weightsDirectory = this.getInputParameter("weightsDirectory").toString();
		// the name of the weight file must be "WND_P.dat", where N is the
		// dimension of the problem and P the population size
		weightFileName = weightsDirectory + "/W" + problem_.getNumberOfObjectives() + "D_" + populationSize + ".dat";

		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

		// If the dimension of the problem is equal to 2, the weight vectors are
		// calculated
		if (problem_.getNumberOfObjectives() == 2) {
			this.weights_ = Weights.initUniformWeights2D(0.005, populationSize);
		}
		// If the dimension of the problem is greater than 2, the weight vectors
		// are read from a file
		else {
			this.weights_ = Weights.getWeightsFromFile(weightFileName);
		}

		// The weight vectors are inverted
		this.weights_ = Weights.invertWeights(weights_, true);

		// --- ALGORITHM --- \\

		// Create the initial solutionSet
		for (int i = 0; i < populationSize; i++) {
			newSolution = new Solution(problem_);
			problem_.evaluate(newSolution);
			problem_.evaluateConstraints(newSolution);
			evaluations++;
			population.add(newSolution);

			// If the nadir and ideal points of the problem are not known, they
			// are estimated for the Achievement Scalarizing Function
			if (estimatePoints)
				updateBounds(newSolution);
		}

		// Evolutionary process
		while (evaluations < maxEvaluations) {
			// Create the offSpring solutionSet
			offspringPopulation = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize / 2); i++) {
				if (evaluations < maxEvaluations) {
					// obtain parents
					parents[0] = (Solution) selectionOperator.execute(population);
					parents[1] = (Solution) selectionOperator.execute(population);
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					problem_.evaluate(offSpring[0]);
					problem_.evaluateConstraints(offSpring[0]);
					problem_.evaluate(offSpring[1]);
					problem_.evaluateConstraints(offSpring[1]);
					offspringPopulation.add(offSpring[0]);
					offspringPopulation.add(offSpring[1]);
					evaluations += 2;

					// If the nadir and ideal points of the problem are not
					// known, they are estimated for the Achievement Scalarizing Function
					if (estimatePoints) {
						updateBounds(offSpring[0]);
						updateBounds(offSpring[1]);
					}
				} // end if (evaluations < maxEvaluations)
			} // end for (int i = 0; i < (populationSize / 2); i++)

			// Create the solutionSet union of solutionSet and offSpring
			union = ((SolutionSet) population).union(offspringPopulation);

			// Ranking the union considering the values of the Achievement Scalarizing Function
			RankingASFs ranking = new RankingASFs(union, asf, this.weights_, normalization);

			// Obtain the next front
			int remain = populationSize;
			int index = 0;
			SolutionSet front;
			population.clear();
			front = ranking.getSubfront(index);

			while ((remain > 0) && (remain >= front.size())) {
				// Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					population.add(front.get(k));
				}

				// Decrement remain
				remain = remain - front.size();

				// Obtain the next front
				index++;
				if (remain > 0) {
					front = ranking.getSubfront(index);
				}
			} // while

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) {
				// front contains individuals to insert
				for (int k = 0; k < remain; k++) {
					population.add(front.get(k));
				}

				remain = 0;
			} // if

			// If the nadir and ideal points of the problem are not known, they
			// are estimated for the Achievement Scalarizing Function
			if (estimatePoints) {
				updateLowerBounds(ranking.getSubfront(0));
				updateUpperBounds(ranking.getSubfront(0));
			}
		} // end while (evaluations < maxEvaluations)

		// Return the first non-dominated front
		RankingASFs ranking = new RankingASFs(population, asf, this.weights_, normalization);

		// If the nadir and ideal points of the problem are not known, they are
		// estimated for the Achievement Scalarizing Function
		if (estimatePoints) {
			updateLowerBounds(ranking.getSubfront(0));
			updateUpperBounds(ranking.getSubfront(0));
		}

		// Return as output parameter the required evaluations
	    setOutputParameter("evaluations", evaluations);
	    
		return ranking.getSubfront(0);
	} // execute

	/**
	 * Update lower bounds (ideal point in a minimization problem) of the Achievement Scalarizing Function
	 * considering the values of the given <code>Solution</code>
	 * 
	 * @param individual
	 */
	private void updateLowerBounds(Solution individual) {
		for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
			if (individual.getObjective(n) < this.asf.getIdeal()[n]) {
				this.asf.setIdeal(n, individual.getObjective(n));
			}
		}
	}

	/**
	 * Update upper bounds (nadir point in a minimization problem) of the Achievement Scalarizing Function
	 * considering the values of the given <code>Solution</code>
	 * 
	 * @param individual
	 */
	private void updateUpperBounds(Solution individual) {
		for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
			if (individual.getObjective(n) > this.asf.getNadir()[n]) {
				this.asf.setNadir(n, individual.getObjective(n));
			}
		}
	}

	/**
	 * Update lower and upper bounds (ideal and nadir points in a minimization
	 * problem) of the Achievement Scalarizing Function considering the values of the given
	 * <code>Solution</code>
	 * 
	 * @param individual
	 */
	private void updateBounds(Solution individual) {
		updateLowerBounds(individual);
		updateUpperBounds(individual);
	}

	/**
	 * Update upper bounds (nadir point in a minimization problem) of the Achievement Scalarizing Function
	 * considering the values of the given <code>SolutionSet</code>
	 * 
	 * @param population
	 */
	private void updateUpperBounds(SolutionSet population) {
		initializeUpperBounds(population.get(0));

		for (int i = 1; i < population.size(); i++) {
			updateUpperBounds(population.get(i));
		}
	}

	/**
	 * Update lower bounds (ideal point in a minimization problem) of the Achievement Scalarizing Function
	 * considering the values of the given <code>SolutionSet</code>
	 * 
	 * @param population
	 */
	private void updateLowerBounds(SolutionSet population) {
		initializeLowerBounds(population.get(0));

		for (int i = 1; i < population.size(); i++) {
			updateLowerBounds(population.get(i));
		}
	}

	/**
	 * Initialize upper bounds (nadir point in a minimization problem) of the
	 * Achievement Scalarizing Function considering the values of the given <code>Solution</code>
	 * 
	 * @param individual
	 */
	private void initializeUpperBounds(Solution individual) {
		for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
			this.asf.setNadir(n, individual.getObjective(n));
		}
	}

	/**
	 * Initialize lower bounds (ideal point in a minimization problem) of the
	 * Achievement Scalarizing Function considering the values of the given <code>Solution</code>
	 * 
	 * @param individual
	 */
	private void initializeLowerBounds(Solution individual) {
		for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
			this.asf.setIdeal(n, individual.getObjective(n));
		}
	}

	/**
	 * Initialize nadir and ideal points of the Achievement Scalarizing Function to the worst values
	 */
	private void initializeBounds() {
		for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
			this.asf.setIdeal(n, Double.MAX_VALUE);
			this.asf.setNadir(n, Double.MIN_VALUE);
		}
	}
}