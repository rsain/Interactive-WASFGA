//  MOCell_Settings.java 
//
//  Authors:
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

package jmetal.experiments.settings;

import jmetal.metaheuristics.mocell.*;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;

import java.util.HashMap;
import java.util.Properties;

import jmetal.core.*;
import jmetal.experiments.Settings;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.Configuration.*;

/**
 * Settings class of algorithm MOCell
 */
public class MOCell_Settings extends Settings{

	public int populationSize_                ;
	public int maxEvaluations_                ;
	public int archiveSize_                   ;
	public int feedback_                      ;
	public double mutationProbability_        ;
	public double crossoverProbability_       ;
  public double crossoverDistributionIndex_ ;
  public double mutationDistributionIndex_  ;

	/**
	 * Constructor
	 */
	public MOCell_Settings(String problemName) {
		super(problemName) ;
		
    Object [] problemParams = {"Real"};
    try {
	    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
    } catch (JMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }      

  	// Default settings
  	populationSize_             = 100   ;
  	maxEvaluations_             = 25000 ;
  	archiveSize_                = 100   ;
  	feedback_                   = 20    ;
  	mutationProbability_        = 1.0/problem_.getNumberOfVariables();
  	crossoverProbability_       = 0.9   ;
    crossoverDistributionIndex_ = 20.0  ;
    mutationDistributionIndex_  = 20.0  ;
	} // MOCell_Settings

	/**
	 * Configure the MOCell algorithm with default parameter settings
	 * @return an algorithm object
	 * @throws jmetal.util.JMException
	 */
	public Algorithm configure() throws JMException {
		Algorithm algorithm ;
		
		Crossover crossover ;
		Mutation  mutation  ;
		Operator  selection ;

		QualityIndicator indicators ;

    HashMap  parameters ; // Operator parameters

		// Selecting the algorithm: there are six MOCell variants
    Object [] problemParams = {"Real"};
    problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);      
		//algorithm = new sMOCell1(problem_) ;
		//algorithm = new sMOCell2(problem_) ;
		//algorithm = new aMOCell1(problem_) ;
		//algorithm = new aMOCell2(problem_) ;
		//algorithm = new aMOCell3(problem_) ;
		algorithm = new MOCell(problem_) ;

		// Algorithm parameters
		algorithm.setInputParameter("populationSize", populationSize_);
		algorithm.setInputParameter("maxEvaluations", maxEvaluations_);
		algorithm.setInputParameter("archiveSize",archiveSize_ );
		algorithm.setInputParameter("feedBack",feedback_);


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
    selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters) ;   
    
		// Add the operators to the algorithm
		algorithm.addOperator("crossover", crossover);
		algorithm.addOperator("mutation", mutation);
		algorithm.addOperator("selection", selection);

    /* Deleted since jMetal 4.2
   // Creating the indicator object
   if ((paretoFrontFile_!=null) && (!paretoFrontFile_.equals(""))) {
      indicators = new QualityIndicator(problem_, paretoFrontFile_);
      algorithm.setInputParameter("indicators", indicators) ;  
   } // if
   */

		return algorithm ;
	} // configure
} // MOCell_Settings
