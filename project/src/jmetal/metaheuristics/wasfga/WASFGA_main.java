package jmetal.metaheuristics.wasfga;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.wasfga.WASFGA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.problems.ZDT.ZDT1;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.AchievementScalarizingFunction;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.ReferencePoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/** 
 * Class to configure and execute the WASFGA algorithm.  
 *     
 * @author Rubén Saborido Infantes
 * 
 *         This algorithm is described in the paper: A.B. Ruiz, R. Saborido, M.
 *         Luque "A Preference-based Evolutionary Algorithm for Multiobjective
 *         Optimization: The Weighting Achievement Scalarizing Function Genetic
 *         Algorithm" Published in Journal of Global Optimization in 2014 
 *         DOI = {10.1007/s10898-014-0214-y}
 */ 

public class WASFGA_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage if objectives number is equal to two:
   *      - jmetal.metaheuristics.wasfga.WASFGA_main problemName referencePointFile
   *      - jmetal.metaheuristics.wasfga.WASFGA_main problemName referencePointFile paretoFrontFile
   *
   * Usage if objectives number is greater than two:
   *      - jmetal.metaheuristics.wasfga.WASFGA_main problemName referencePointFile weightsDirectory
   *      - jmetal.metaheuristics.wasfga.WASFGA_main problemName referencePointFile paretoFrontFile weightsDirectory 
   *      
   * The referencePointFile is a text file containing, in one row, the wished value of each objective, separated by spaces
   * 
   * The weightsDirectory is a folder containing the weights vector files.
   * The name of these files are WND_P.txt, where N is the number of objectives and P is the population size (one vector for each solution)
   */
  public static void main(String [] args) throws 
                                  JMException, 
                                  SecurityException, 
                                  IOException, 
                                  ClassNotFoundException {
    Problem   problem   ; // The problem to solve
    Algorithm algorithm ; // The algorithm to use
    Operator  crossover ; // Crossover operator
    Operator  mutation  ; // Mutation operator
    Operator  selection ; // Selection operator
    
    HashMap  parameters ; // Operator parameters
    
    QualityIndicator indicators ; // Object to get quality indicators
    ReferencePoint referencePoint;
    String weightsDirectory;

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("WASFGA_main.log"); 
    logger_.addHandler(fileHandler_) ;
        
    indicators = null ;
    if (args.length == 2) {
        Object [] params = {"Real"};
        problem = (new ProblemFactory()).getProblem(args[0],params);
        referencePoint = new ReferencePoint(args[1]);
        weightsDirectory = "";
        
        if (problem.getNumberOfObjectives() > 2)
        {
        	logger_.severe("If the number of objectives is greater than two, a weights vector folder must be specified. ");
        	System.exit(-1);
        }
      } // if
    else if (args.length == 3) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      referencePoint = new ReferencePoint(args[1]);      
      
      if (problem.getNumberOfObjectives() > 2)
    	  weightsDirectory = args[2];
	  else
	  {
		  indicators = new QualityIndicator(problem, args[2]) ;
		  weightsDirectory = "";
	  }
    } // if
    else if (args.length == 4) {
        Object [] params = {"Real"};
        problem = (new ProblemFactory()).getProblem(args[0],params);
        referencePoint = new ReferencePoint(args[1]);                         	  	 
  		indicators = new QualityIndicator(problem, args[2]);
  		weightsDirectory = args[3];
    } // if
    else { // Default problem
      problem = new ZDT1("ArrayReal", 30);
      double[] rp = new double[2];
      rp[0] = 0.0; rp[1] = 0.0;
      referencePoint = new ReferencePoint(rp);   
      weightsDirectory="";
    }
    
    //Checks if the reference point's dimension is correct
    if (referencePoint.size() != problem.getNumberOfObjectives())
    {
    	logger_.severe("The number of components of the reference point must be equal to the number of objectives.");
    	System.exit(-1);
    }
    
    algorithm = new WASFGA(problem);

    // Algorithm parameters
    algorithm.setInputParameter("populationSize",100);
    algorithm.setInputParameter("maxEvaluations",30000);    
    algorithm.setInputParameter("weightsDirectory",weightsDirectory);        
    algorithm.setInputParameter("normalization", true);
    algorithm.setInputParameter("asf", new AchievementScalarizingFunction(problem.getNumberOfObjectives()));
    algorithm.setInputParameter("estimatePoints", true);
    algorithm.setInputParameter("referencePoint", referencePoint);    

    // Mutation and Crossover for Real codification 
    parameters = new HashMap() ;
    parameters.put("probability", 0.9) ;
    parameters.put("distributionIndex", 20.0) ;
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);                   

    parameters = new HashMap() ;
    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("distributionIndex", 20.0) ;
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

    // Selection Operator 
    parameters = null ;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;                           

    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Add the indicator object to the algorithm
    algorithm.setInputParameter("indicators", indicators) ;
    
    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    
    // Result messages 
    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Variables values have been writen to file VAR_WASFGA");
    population.printVariablesToFile("VAR_WASFGA");    
    logger_.info("Objectives values have been writen to file FUN_WASFGA");
    population.printObjectivesToFile("FUN_WASFGA");
  
    if (indicators != null) {
      logger_.info("Quality indicators") ;
      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
      logger_.info("GD         : " + indicators.getGD(population)) ;
      logger_.info("IGD        : " + indicators.getIGD(population)) ;
      logger_.info("Spread     : " + indicators.getSpread(population)) ;
      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;  
     
      int evaluations = ((Integer)algorithm.getOutputParameter("evaluations")).intValue();
      logger_.info("Speed      : " + evaluations + " evaluations") ;      
    } // if
  } //main
} // WASFGA_main