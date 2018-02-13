
package jmetal.metaheuristics.iwasfga;

import com.sun.java.swing.plaf.windows.resources.windows;
import java.awt.Dimension;
import jmetal.metaheuristics.wasfga.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import jmetal.metaheuristics.wasfga.*;
import jmetal.core.*;
import jmetal.experiments.settings.iWASFGA_Settings;
import jmetal.problems.ProblemFactory;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.*;

/**
 * Implementation of the preference based algorithm named iWASF-GA.
 * 
 * @author Rubén Saborido Infantes
 * 
 *         It is an interactive version of WASFGA.
 */
public class iWASFGA extends Algorithm {

    double[][] weights_;
    AchievementScalarizingFunction asf;
    ReferencePoint referencePoint;
    boolean allowRepetitions, estimatePoints, normalization;
    String folderForOutputFiles;
    int populationSize, numberOfWeights, generations, evaluations;
    int executedIterations = 0;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public iWASFGA(Problem problem) {
        super(problem);
    } // NSGAII

    /**
	 * Runs the iWASF-GA algorithm.
	 *
	 * @return A <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
	 * @throws JMException
	 * @throws ClassNotFoundException
	 */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        final JFrame window = new JFrame("Evaluating ...");
        final JProgressBar progressBar = new JProgressBar();

        SwingWorker<SolutionSet, Integer> worker = new SwingWorker<SolutionSet, Integer>() {
            @Override
            protected SolutionSet doInBackground() throws Exception {
                progressBar.setStringPainted(true);
                window.setPreferredSize(new Dimension(300, 80));
                window.getContentPane().add(progressBar);
                window.setAlwaysOnTop(true);
                window.setResizable(false);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.pack();
                window.setLocationRelativeTo(null);
                window.setVisible(true);

                String weightsDirectory, weightsFileName;
                QualityIndicator indicators; // QualityIndicator object
                int requiredEvaluations; // Use in the example of use of the        

                SolutionSet population;
                SolutionSet offspringPopulation;
                SolutionSet union;

                Operator mutationOperator;
                Operator crossoverOperator;
                Operator selectionOperator;

                //Read the parameters
                populationSize = ((Integer) getInputParameter("populationSize")).intValue();
                numberOfWeights = ((Integer) getInputParameter("numberOfWeights")).intValue();
                generations = ((Integer) getInputParameter("generations")).intValue();
                indicators = (QualityIndicator) getInputParameter("indicators");
                allowRepetitions = ((Boolean) getInputParameter("allowRepetitions")).booleanValue();
                folderForOutputFiles = (String) getInputParameter("folderForOutputFiles");
                normalization = ((Boolean) getInputParameter("normalization")).booleanValue();
                estimatePoints = ((Boolean) getInputParameter("estimatePoints")).booleanValue();

                if (estimatePoints) {
                    asf = new AchievementScalarizingFunction(problem_.getNumberOfObjectives());
                } else {
                    asf = (AchievementScalarizingFunction) getInputParameter("asf");
                }
                referencePoint = ((ReferencePoint) getInputParameter("referencePoint"));
                try {
                    asf.setReferencePoint(referencePoint);
                } catch (CloneNotSupportedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                weightsDirectory = getInputParameter("weightsDirectory").toString();
                weightsFileName = getInputParameter("weightsFileName").toString();

                //Initialize the variables
                population = new SolutionSet(populationSize);
                evaluations = 0;

                requiredEvaluations = 0;

                //Read the operators
                mutationOperator = operators_.get("mutation");
                crossoverOperator = operators_.get("crossover");
                selectionOperator = operators_.get("selection");

                if (problem_.getNumberOfObjectives() == 2) {
                    weights_ = Weights.initUniformWeights2D(0.01, numberOfWeights);
                } else {
                    weights_ = Weights.getWeightsFromFile(weightsDirectory + File.separator + weightsFileName);
                }

                // Create the initial solutionSet
                if (estimatePoints) {
                    initializeBounds();
                }

                Solution newSolution;
                for (int i = 0; i < populationSize; i++) {
                    newSolution = new Solution(problem_);
                    problem_.evaluate(newSolution);
                    problem_.evaluateConstraints(newSolution);
                    evaluations++;
                    population.add(newSolution);

                    if (estimatePoints) {
                        updateBounds(newSolution);
                    }
                } //for

                // Generations 
                int localGenerations = 0;
                progressBar.setMaximum(generations);
                while (localGenerations < generations) //while (requiredEvaluations == 0 && evaluations < maxEvaluations)
                {
                    // Create the offSpring solutionSet      
                    offspringPopulation = new SolutionSet(populationSize);
                    Solution[] parents = new Solution[2];
                    for (int i = 0; i < (populationSize / 2); i++) {
                        //obtain parents
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

                        if (estimatePoints) {
                            updateBounds(offSpring[0]);
                            updateBounds(offSpring[1]);
                        }
                    } // for                     

                    // Create the solutionSet union of solutionSet and offSpring
                    union = ((SolutionSet) population).union(offspringPopulation);

                    // Ranking the union
                    RankingASFs ranking = new RankingASFs(union, asf, weights_, normalization);

                    int remain = populationSize;
                    int index = 0;
                    SolutionSet front;
                    population.clear();

                    // Obtain the next front
                    front = ranking.getSubfront(index);

                    while ((remain > 0) && (remain >= front.size())) {
                        //Add the individuals of this front
                        for (int k = 0; k < front.size(); k++) {
                            population.add(front.get(k));
                        } // for

                        //Decrement remain
                        remain = remain - front.size();

                        //Obtain the next front
                        index++;
                        if (remain > 0) {
                            front = ranking.getSubfront(index);
                        } // if        
                    } // while

                    // Remain is less than front(index).size, insert only the best one
                    if (remain > 0) {  // front contains individuals to insert                                         
                        for (int k = 0; k < remain; k++) {
                            population.add(front.get(k));
                        } // for

                        remain = 0;
                    } // if                               

                    localGenerations++;
                    publish(localGenerations);
                } // while

                return population;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(0));
            }

            @Override
            protected void done() {
                progressBar.setValue(progressBar.getMaximum());
                window.dispose();
            }
        };
        worker.execute();

        SolutionSet result = new SolutionSet();

        try {
            result = worker.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(iWASFGA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(iWASFGA.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public SolutionSet[] executeFirstIteration() throws JMException, ClassNotFoundException {
        SolutionSet[] result = new SolutionSet[2];

        result[0] = new SolutionSet().union(execute());
        result[1] = new RankingASFs(result[0], asf, this.weights_, normalization).getSubfront(0);

        deleteWithChildren(folderForOutputFiles + "/" + problem_.getName() + "/");
        try {
            new File(this.folderForOutputFiles + "/" + problem_.getName()).mkdirs();
            referencePoint.writeInFile(this.folderForOutputFiles + "/" + problem_.getName() + "/REFERENCE_POINTS.rl");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        result[0].printObjectivesToFile(folderForOutputFiles + "/" + problem_.getName() + "/POPULATION IN 1.txt");
        result[1].printObjectivesToFile(folderForOutputFiles + "/" + problem_.getName() + "/SOLUTIONS IN 1.txt");

        executedIterations = 1;

        return result;
    } // execute

    public SolutionSet[] doIteration(final SolutionSet population, final ReferencePoint newReferencePoint, final int numberOfSolutions) throws JMException, ClassNotFoundException {
        final JFrame window = new JFrame("Evaluating ...");
        final JProgressBar progressBar = new JProgressBar();

        SwingWorker<SolutionSet, Integer> worker = new SwingWorker<SolutionSet, Integer>() {
            @Override
            protected SolutionSet doInBackground() throws Exception {
                progressBar.setStringPainted(true);
                window.setPreferredSize(new Dimension(300, 80));
                window.getContentPane().add(progressBar);
                window.setAlwaysOnTop(true);
                window.setResizable(false);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.pack();
                window.setLocationRelativeTo(null);
                window.setVisible(true);

                int evaluations;

                SolutionSet offspringPopulation;
                SolutionSet union;

                Operator mutationOperator;
                Operator crossoverOperator;
                Operator selectionOperator;

                if (numberOfWeights != numberOfSolutions) {
                    numberOfWeights = numberOfSolutions;
                    if (problem_.getNumberOfObjectives() == 2) {
                        weights_ = Weights.initUniformWeights2D(0.01, numberOfWeights);
                    } else {
                        String weightsDirectory = getInputParameter("weightsDirectory").toString();
                        String weightsFileName = getInputParameter("weightsFileName").toString();
                        weights_ = Weights.getWeightsFromFile(weightsDirectory + File.separator + weightsFileName);
                    }
                }

                referencePoint = newReferencePoint;
                try {
                    asf.setReferencePoint(referencePoint);
                } catch (CloneNotSupportedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //Initialize the variables
                //population = new SolutionSet(populationSize);
                evaluations = 0;

                //Read the operators
                mutationOperator = operators_.get("mutation");
                crossoverOperator = operators_.get("crossover");
                selectionOperator = operators_.get("selection");

                // Create the initial solutionSet
                if (estimatePoints) {
                    initializeBounds();
                    updateLowerBounds(population);
                    updateUpperBounds(population);
                }

                // Generations 
                int localGenerations = 0;
                progressBar.setMaximum(generations);
                while (localGenerations < generations) {
                    // Create the offSpring solutionSet      
                    offspringPopulation = new SolutionSet(populationSize);
                    Solution[] parents = new Solution[2];
                    for (int i = 0; i < (populationSize / 2); i++) {
                        //obtain parents
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

                        if (estimatePoints) {
                            updateBounds(offSpring[0]);
                            updateBounds(offSpring[1]);
                        }
                    } // for                     

                    // Create the solutionSet union of solutionSet and offSpring
                    union = ((SolutionSet) population).union(offspringPopulation);

                    // Ranking the union
                    RankingASFs ranking = new RankingASFs(union, asf, weights_, normalization);

                    int remain = populationSize;
                    int index = 0;
                    SolutionSet front;
                    population.clear();

                    // Obtain the next front
                    front = ranking.getSubfront(index);

                    while ((remain > 0) && (remain >= front.size())) {
                        //Add the individuals of this front
                        for (int k = 0; k < front.size(); k++) {
                            population.add(front.get(k));
                        } // for

                        //Decrement remain
                        remain = remain - front.size();

                        //Obtain the next front
                        index++;
                        if (remain > 0) {
                            front = ranking.getSubfront(index);
                        } // if        
                    } // while

                    // Remain is less than front(index).size, insert only the best one
                    if (remain > 0) {  // front contains individuals to insert                                        
                        for (int k = 0; k < remain; k++) {
                            population.add(front.get(k));
                        } // for

                        remain = 0;
                    } // if                               

                    localGenerations++;
                    publish(localGenerations);
                } // while

                return population;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(0));
            }

            @Override
            protected void done() {
                progressBar.setValue(progressBar.getMaximum());
                window.dispose();
            }
        };

        SolutionSet solutions = new SolutionSet();

        worker.execute();
        try {
            solutions = worker.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(iWASFGA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(iWASFGA.class.getName()).log(Level.SEVERE, null, ex);
        }

        SolutionSet[] result = new SolutionSet[2];
        result[0] = new SolutionSet().union(solutions);
        result[1] = new RankingASFs(solutions, asf, this.weights_, normalization).getSubfront(0);

        executedIterations++;

        try {
            referencePoint.appendInFile(this.folderForOutputFiles + "/" + problem_.getName() + "/REFERENCE_POINTS.rl");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        result[0].printObjectivesToFile(folderForOutputFiles + "/" + problem_.getName() + "/POPULATION IN " + executedIterations + ".txt");
        result[1].printObjectivesToFile(folderForOutputFiles + "/" + problem_.getName() + "/SOLUTIONS IN " + executedIterations + ".txt");

        return result;
    }

    /**
    * Update lower bounds (ideal point in a minimization problem) of the Achievement Scalarizing Function
    * considering the values of the given <code>Solution</code>
    * 
    * @param individual
    */
    void updateLowerBounds(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < this.asf.getIdeal()[n]) {
                this.asf.setIdeal(n, individual.getObjective(n));
            }
        }
    } // updateLowerBounds

    /**
    * Update upper bounds (nadir point in a minimization problem) of the Achievement Scalarizing Function
    * considering the values of the given <code>Solution</code>
    * 
    * @param individual
    */
    void updateUpperBounds(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) > this.asf.getNadir()[n]) {
                this.asf.setNadir(n, individual.getObjective(n));
            }
        }
    } // updateUpperBounds

    /**
    * Update lower and upper bounds (ideal and nadir points in a minimization
    * problem) of the Achievement Scalarizing Function considering the values of the given
    * <code>Solution</code>
    * 
    * @param individual
    */
    void updateBounds(Solution individual) {
        updateLowerBounds(individual);
        updateUpperBounds(individual);
    } // updateBounds

    /**
    * Update upper bounds (nadir point in a minimization problem) of the Achievement Scalarizing Function
    * considering the values of the given <code>population</code>
    * 
    * @param population
    */
    void updateUpperBounds(SolutionSet population) {
        initializeUpperBounds(population.get(0));

        for (int i = 1; i < population.size(); i++) {
            updateUpperBounds(population.get(i));
        }
    } // updateUpperBounds

    /**
    * Update lower bounds (ideal point in a minimization problem) of the Achievement Scalarizing Function
    * considering the values of the given <code>population</code>
    * 
    * @param population
    */
    void updateLowerBounds(SolutionSet population) {
        initializeLowerBounds(population.get(0));

        for (int i = 1; i < population.size(); i++) {
            updateLowerBounds(population.get(i));
        }
    } // updateLowerBounds

    /**
    * Initialize upper bounds (nadir point in a minimization problem) of the
    * Achievement Scalarizing Function considering the values of the given <code>Solution</code>
    * 
    * @param individual
    */
    void initializeUpperBounds(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            this.asf.setNadir(n, individual.getObjective(n));
        }
    } // initializeUpperBounds

    
    void initializeUpperBounds() {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            this.asf.setNadir(n, Double.MIN_VALUE);
        }
    } // initializeUpperBounds

    /**
    * Initialize lower bounds (ideal point in a minimization problem) of the
    * Achievement Scalarizing Function
    * 
    */
    void initializeLowerBounds() {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            this.asf.setIdeal(n, Double.MAX_VALUE);
        }
    } // initializeLowerBounds

    /**
    * Initialize lower bounds (ideal point in a minimization problem) of the
    * Achievement Scalarizing Function considering the values of the given <code>Solution</code>
    * 
    * @param individual
    */
    void initializeLowerBounds(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            this.asf.setIdeal(n, individual.getObjective(n));
        }
    } // initializeUpperBounds

    /**
    * Initialize nadir and ideal points of the Achievement Scalarizing Function to the worst values
    */
    void initializeBounds() {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            this.asf.setIdeal(n, Double.MAX_VALUE);
            this.asf.setNadir(n, Double.MIN_VALUE);
        }
    } // initializeBounds

    /**
     * Deletes the given path and, if it is a directory, deletes all its
     * children.
     */
    public boolean deleteWithChildren(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (!file.isDirectory()) {
            return file.delete();
        }
        return this.deleteChildren(file) && file.delete();
    }

    private boolean deleteChildren(File dir) {
        File[] children = dir.listFiles();
        boolean childrenDeleted = true;
        for (int i = 0; children != null && i < children.length; i++) {
            File child = children[i];
            if (child.isDirectory()) {
                childrenDeleted = this.deleteChildren(child) && childrenDeleted;
            }
            if (child.exists()) {
                childrenDeleted = child.delete() && childrenDeleted;
            }
        }
        return childrenDeleted;
    }
} // GWASFGA
