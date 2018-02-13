//  PSO_main.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.metaheuristics.singleObjective.particleSwarmOptimization;

import java.io.IOException;

import jmetal.core.*;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.singleObjective.Griewank;
import jmetal.problems.singleObjective.Sphere;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.* ;
import jmetal.util.Configuration;
import jmetal.util.JMException ;

import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.qualityIndicator.QualityIndicator;

/**
 * Class for configuring and running a single-objective PSO algorithm
 */
public class PSO_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

  /**
   * @param args Command line arguments. The first (optional) argument specifies 
   *             the problem to solve.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage: three options
   *      - jmetal.metaheuristics.mocell.MOCell_main
   *      - jmetal.metaheuristics.mocell.MOCell_main problemName
   *      - jmetal.metaheuristics.mocell.MOCell_main problemName ParetoFrontFile
   */
  public static void main(String [] args) 
  		throws JMException, IOException, ClassNotFoundException {
    Problem   problem   ;  // The problem to solve
    Algorithm algorithm ;  // The algorithm to use
    Mutation  mutation  ;  // "Turbulence" operator
    
    QualityIndicator indicators ; // Object to get quality indicators
        
    HashMap  parameters ; // Operator parameters

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("PSO_main.log"); 
    logger_.addHandler(fileHandler_) ;

    //problem = new Sphere("Real", 20) ;
    //problem = new Easom("Real") ;
    // problem = new Griewank("Real", 10) ;

    problem = new Sphere("Real", 20); 

    algorithm = new PSO(problem) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("swarmSize",50);
    algorithm.setInputParameter("maxIterations",5000);
    
    parameters = new HashMap() ;
    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("distributionIndex", 20.0) ;
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

    algorithm.addOperator("mutation", mutation);

    // Execute the Algorithm 
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();
    long estimatedTime = System.currentTimeMillis() - initTime;
    
    // Result messages 
    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");                         
  } //main
} // PSO_main
