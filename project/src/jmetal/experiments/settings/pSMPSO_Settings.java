//  pSMPSO_Settings.java 
//
//  Authors:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//
//  Copyright (c) 2013 Antonio J. Nebro
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

package jmetal.experiments.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import jmetal.metaheuristics.smpso.*;

import java.util.Properties;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.experiments.Settings;
import jmetal.util.PropUtils;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.problems.ProblemFactory;
import jmetal.problems.ZDT.ZDT1;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.parallel.IParallelEvaluator;
import jmetal.util.parallel.MultithreadedEvaluator;

/**
 * Settings class of algorithm pSMPSO
 */
public class pSMPSO_Settings extends Settings{
  
	public int    swarmSize_                 ;
  public int    maxIterations_             ;
  public int    archiveSize_               ;
  public double mutationDistributionIndex_ ;
  public double mutationProbability_       ;
  public int    numberOfThreads_           ;

  /**
   * Constructor
   */
  public pSMPSO_Settings(String problem) {
    super(problem) ;
    
    Object [] problemParams = {"Real"};
    try {
	    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
    } catch (JMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }      

    // Default settings
    swarmSize_                 = 100 ;
    maxIterations_             = 250 ;
    archiveSize_               = 100 ;
    mutationDistributionIndex_ = 20.0 ;
    mutationProbability_       = 1.0/problem_.getNumberOfVariables() ;
    numberOfThreads_           = 8 ; // 0 - number of available cores
  } // SMPSO_Settings
  
  /**
   * Configure SMPSO with user-defined parameter settings
   * @return A SMPSO algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    Mutation  mutation ;
    
    QualityIndicator indicators ;
    
    HashMap  parameters ; // Operator parameters

    IParallelEvaluator parallelEvaluator = new MultithreadedEvaluator(numberOfThreads_) ;

    algorithm = new pSMPSO(problem_, parallelEvaluator) ;
    
    // Algorithm parameters
    algorithm.setInputParameter("swarmSize", swarmSize_);
    algorithm.setInputParameter("maxIterations", maxIterations_);
    algorithm.setInputParameter("archiveSize", archiveSize_);
    
    parameters = new HashMap() ;
    parameters.put("probability", mutationProbability_) ;
    parameters.put("distributionIndex", mutationDistributionIndex_) ;
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

    algorithm.addOperator("mutation",mutation);

    return algorithm ;
  } // Configure
} // pSMPSO_Settings
