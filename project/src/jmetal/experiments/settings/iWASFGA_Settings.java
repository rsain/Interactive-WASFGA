//  GWASFGA_Settings.java 
//
//  Authors:
//       Rubén Saborido Infantes <rsain@uma.es>
//
//  Copyright (c) 2013 Rubén Saborido Infantes
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

import java.util.HashMap;

import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.core.*;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.iwasfga.iWASFGA;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.AchievementScalarizingFunction;
import jmetal.util.JMException;
import jmetal.util.ReferencePoint;

/**
 * Settings class of algorithm GWASFGA (real encoding)
 */
public class iWASFGA_Settings extends Settings {
  public int populationSize_                 ; 
  public int generations_                    ;
  public double mutationProbability_         ;
  public double crossoverProbability_        ;
  public double mutationDistributionIndex_   ;
  public double crossoverDistributionIndex_  ;
  QualityIndicator convergenceIndicator_     ;
  public int requiredEvaluations_            ;
  public int numberOfWeights_                ;
  public boolean allowRepetitions_           ;
  public boolean normalization_              ;
  public String weightsDirectory_            ;
  public String weightsFileName_             ;
  public AchievementScalarizingFunction asf_ ;
  public ReferencePoint referencePoint_      ;
  public boolean estimatePoints_             ; 
  public String folderForOutputFiles_	     ;
  
  /**
   * Constructor
   * @throws JMException 
   */
  public iWASFGA_Settings(String problem) throws JMException {
    super(problem) ;
    String paretoFrontDirectory = new String("data/paretoFronts/");   
    
    Object [] problemParams = {"Real"};
    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);        
    
    // Default settings
    populationSize_              = 100   ; 
    generations_                 = 200 ;
    mutationProbability_         = 1.0/problem_.getNumberOfVariables() ;
    crossoverProbability_        = 0.9   ;
    mutationDistributionIndex_   = 20.0  ;
    crossoverDistributionIndex_  = 20.0  ;        
    numberOfWeights_             = populationSize_;
    weightsDirectory_            = new String("data");   
    weightsFileName_            = new String("weightsFileName");   
    allowRepetitions_            = false;
    normalization_               = true;
    estimatePoints_              = true;        
    referencePoint_	         = new ReferencePoint(problem_.getNumberOfObjectives());        
    folderForOutputFiles_        = ".";
    requiredEvaluations_          = 0;
  } // GWASFGA_Settings

  /**
   * Constructor
   * @throws JMException 
   */
  public iWASFGA_Settings(String problem, int objectivesNumber) throws JMException {
    super(problem) ;
    String paretoFrontDirectory = new String("data/paretoFronts/");   
    
    Object [] problemParams = {"Real", objectivesNumber};
    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);        
    
    // Default settings
    populationSize_              = 100   ; 
    generations_                 = 200 ;
    mutationProbability_         = 1.0/problem_.getNumberOfVariables() ;
    crossoverProbability_        = 0.9   ;
    mutationDistributionIndex_   = 20.0  ;
    crossoverDistributionIndex_  = 20.0  ;        
    numberOfWeights_             = populationSize_;
    weightsDirectory_            = new String("weightsDirectory");   
    weightsFileName_            =  new String("weightsFileName"); 
    allowRepetitions_            = false;
    normalization_               = true;
    estimatePoints_              = true;        
    referencePoint_	         = new ReferencePoint(problem_.getNumberOfObjectives());        
    folderForOutputFiles_        = ".";
    requiredEvaluations_          = 0;
  } // GWASFGA_Settings
  
  /**
   * Configure GWASFGA with user-defined parameter settings
   * @return A NSGAII algorithm object
   * @throws jmetal.util.JMException
   */
  public Algorithm configure() throws JMException {
    Algorithm algorithm ;
    Selection  selection ;
    Crossover  crossover ;
    Mutation   mutation  ;

    HashMap  parameters ; // Operator parameters  
    
    QualityIndicator indicators ;
    
    // Creating the algorithm.
    algorithm = new iWASFGA(problem_) ;    
    
    // Algorithm parameters
    algorithm.setInputParameter("populationSize",populationSize_);
    algorithm.setInputParameter("generations",generations_);
    algorithm.setInputParameter("numberOfWeights",numberOfWeights_);
    algorithm.setInputParameter("weightsDirectory",weightsDirectory_);
    algorithm.setInputParameter("weightsFileName",weightsFileName_);    
    algorithm.setInputParameter("indicators",convergenceIndicator_);    
    algorithm.setInputParameter("allowRepetitions",allowRepetitions_);
    algorithm.setInputParameter("normalization",normalization_);
    algorithm.setInputParameter("asf",asf_);
    algorithm.setInputParameter("estimatePoints",estimatePoints_);
    algorithm.setInputParameter("referencePoint",referencePoint_);
    algorithm.setInputParameter("folderForOutputFiles", folderForOutputFiles_);

    // Mutation and Crossover for Real codification
    parameters = new HashMap() ;
    parameters.put("probability", crossoverProbability_) ;
    parameters.put("distributionIndex", crossoverDistributionIndex_) ;
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);                   

    parameters = new HashMap() ;
    parameters.put("probability", mutationProbability_) ;
    parameters.put("distributionIndex", mutationDistributionIndex_) ;
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                        

    // Selection Operator 
    parameters = null ;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;     

    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);
   
    return algorithm ;
  } // configure
} // GWASFGA_Settings