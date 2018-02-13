package jmetal.util;

import jmetal.core.SolutionSet;
import java.util.*;

/**
 * This class implements some facilities for ranking solutions. Given a
 * <code>SolutionSet</code> object, their solutions are ranked according to
 * scheme proposed in NSGA-II, but using several achievement scalarizing
 * function (ASF) and not Pareto dominance; as a result, a set of subsets are
 * obtained. The subsets are numbered starting from 0 (in NSGA-II, the numbering
 * starts from 1); thus, subset 0 contains the best solutions considering the
 * values of all ASFs, subset 1 contains the solutions after removing those 
 * belonging to subset 0, and so on.
 */
public class RankingASFs {

    /**
     * The <code>SolutionSet</code> to rank
     */
    private SolutionSet solutionSet_;
    
    /**
     * An array containing all the fronts found during the search
     */
    private SolutionSet[] ranking_;
    
    /**
     * Internal structure to sort by the value of the ASF
     */
    private class Node {
        public int solutionIndex;
        public double[] asfs;
    }

    /**
     * Ranking solutions in frontiers considering several ASFs (one by each vector of weights)
     * @param solutionSet The <code>SolutionSet</code> to be ranked
     * @param asf The <code>AchievementScalarizingFunction</code> to be used
     * @param weights The weight vectors to be used for each <code>AchievementScalarizingFunction</code>
     * @param normalization True if the ASF should be normalized
     */
    public RankingASFs(SolutionSet solutionSet, AchievementScalarizingFunction asf, double[][] weights, boolean normalization) {
        this.solutionSet_ = solutionSet;

        // List that contains the index of a solution and the value of the ASF (one for each vector of weights)        
        LinkedList<Node> l = new LinkedList<Node>();
        LinkedList<Node> solutionsToRemove = new LinkedList<Node>();
        Node bestNode;        
        int solutionIndex, asfIndex, frontierIndex, numberOfFrontiersInNewPopulation;
        double[][] evaluationsAsfs;

        if (normalization) {
            evaluationsAsfs = asf.evaluateNormalizing(solutionSet, weights);
        } else {
            evaluationsAsfs = asf.evaluate(solutionSet, weights);
        }

        //The list l is used to allow delete a solution when it is assigned to a frontier        
        //Each node in the list contain the index of the solution in the SolutioSet because the size of the list changes
        for (solutionIndex = 0; solutionIndex < solutionSet.size(); solutionIndex++) {
            Node node = new Node();

            node.solutionIndex = solutionIndex;
            node.asfs = evaluationsAsfs[solutionIndex];

            l.add(node);
        }

        //Each frontier will have many solutions as weight vectors.
        //So we can calculate the number of frontiers
        if (solutionSet.size() % weights.length == 0) {
            numberOfFrontiersInNewPopulation = solutionSet.size() / weights.length;
        } else {
            numberOfFrontiersInNewPopulation = (solutionSet.size() / weights.length) + 1;
        }

        this.ranking_ = new SolutionSet[numberOfFrontiersInNewPopulation];

        //Assign each solution in the correct frontier
        solutionIndex = 0;
        for (frontierIndex = 0; frontierIndex < numberOfFrontiersInNewPopulation; frontierIndex++) {
            this.ranking_[frontierIndex] = new SolutionSet(weights.length);                                
            solutionsToRemove.clear();
        
            //Search the best solution for the current ASF
            for (asfIndex = 0; (asfIndex < weights.length); asfIndex++) {
            	//if l.size() == o is due there is not more solutions to classify in the last front
            	//It happens when the population size and the number of weight vectors are not divisible
            	if(l.size() == 0)
                    break;
            	
                //Initially, the best solution is the first
                bestNode = l.get(0);

                for (Node node : l) {
                    if (node.asfs[asfIndex] < bestNode.asfs[asfIndex]) {
                        bestNode = node;
                    }
                }
                
                this.ranking_[frontierIndex].add(solutionSet.get(bestNode.solutionIndex));

                l.remove(bestNode);                

                solutionIndex++;
            }           
        }
    }

    /**
     * Ranking solutions in frontiers considering several ASFs (one by each vector of weights)
     * @param solutionSet The <code>SolutionSet</code> to be ranked
     * @param asfInUtopian The <code>AchievementScalarizingFunction</code> to be used in the utopian point
     * @param pairWeights The weight vectors to be used for each <code>AchievementScalarizingFunction</code> in the utopian point
     * @param asfInNadir The <code>AchievementScalarizingFunction</code> to be used in the nadir point
     * @param oddWeights The weight vectors to be used for each <code>AchievementScalarizingFunction</code> in the nadir point
     * @param normalization True if the ASF should be normalized
     */
    public RankingASFs(SolutionSet solutionSet, AchievementScalarizingFunction asfInUtopian, double[][] pairWeights, AchievementScalarizingFunction asfInNadir, double[][] oddWeights, boolean normalization) {
        this.solutionSet_ = solutionSet;

        int numberOfWeights = oddWeights.length + pairWeights.length;

        // List that contains the index of a solution and the value of the ASFs (one for each vector of weights)        
        LinkedList<Node> l = new LinkedList<Node>();
        LinkedList<Node> solutionsToRemove = new LinkedList<Node>();
        Node bestNode;        
        int solutionIndex, asfIndex, frontierIndex, numberOfFrontiersInNewPopulation;
        double[][] evaluationsAsfs, evaluationsOddAsfs, evaluationsPairAsfs;

        if (normalization) {
            evaluationsOddAsfs = asfInUtopian.evaluateNormalizing(solutionSet, oddWeights);
            evaluationsPairAsfs = asfInNadir.evaluateNormalizing(solutionSet, pairWeights);
        } else {
            evaluationsOddAsfs = asfInUtopian.evaluate(solutionSet, oddWeights);
            evaluationsPairAsfs = asfInNadir.evaluate(solutionSet, pairWeights);
        }

        evaluationsAsfs = unionPairAndOddASFValues(evaluationsPairAsfs, evaluationsOddAsfs);

        //The list l is used to allow delete a solution when it is assigned to a frontier        
        //Each node in the list contain the index of the solution in the SolutioSet because the size of the list changes
        for (solutionIndex = 0; solutionIndex < solutionSet.size(); solutionIndex++) {
            Node node = new Node();

            node.solutionIndex = solutionIndex;
            node.asfs = evaluationsAsfs[solutionIndex];

            l.add(node);
        }

        //Each frontier will have many solutions as weight vectors.
        //So we can calculate the number of frontiers
        if (solutionSet.size() % numberOfWeights == 0) {
            numberOfFrontiersInNewPopulation = solutionSet.size() / numberOfWeights;
        } else {
            numberOfFrontiersInNewPopulation = (solutionSet.size() / numberOfWeights) + 1;
        }

        this.ranking_ = new SolutionSet[numberOfFrontiersInNewPopulation];

        //Assign each solution in the correct frontier
        solutionIndex = 0;
        for (frontierIndex = 0; frontierIndex < numberOfFrontiersInNewPopulation; frontierIndex++) {
            this.ranking_[frontierIndex] = new SolutionSet(numberOfWeights);                                            
            solutionsToRemove.clear();

            //Search the best solution for each ASF (weight vector)
            for (asfIndex = 0; (asfIndex < numberOfWeights); asfIndex++) {
            	//if l.size() == o is due there is not more solutions to classify in the last front
            	//It happens when the population size and the number of weight vectors are not divisible
            	if(l.size() == 0)
                    break;
            	
                //Initially, the best solution is the first
                bestNode = l.get(0);

                for (Node node : l) {
                    if (node.asfs[asfIndex] < bestNode.asfs[asfIndex]) {
                        bestNode = node;
                    }
                }

                this.ranking_[frontierIndex].add(solutionSet.get(bestNode.solutionIndex));                

                l.remove(bestNode);                

                solutionIndex++;
            }           
        }
    }    

    /**
     * Returns a <code>SolutionSet</code> containing the solutions of a given rank    
     * @param rank The rank
     * @return Object representing the <code>SolutionSet</code>
     */
    public SolutionSet getSubfront(int rank) {
        return ranking_[rank];
    } 

    /**
     * Returns the total number of subFronts founds
     * @return The total number of subFronts founds
     */
    public int getNumberOfSubfronts() {
        return ranking_.length;
    }

    /**
     * If a set of weight vectors was divided considering the pair and odd positions, this method can be used to combine both set of weight vectors to generate the original set of weight vectors. 
     * @param pairweights A set of pair weight vectors (in a previous set of weight vectors)
     * @param oddWeights A set of odd weight vectors (in a previous set of weight vectors)
     * @return A set of weight vectors mixing the pair and odd weight vectors
     */
    private double[][] unionPairAndOddASFValues(double[][] pairweights, double[][] oddWeights) {
        double[][] result = new double[oddWeights.length][pairweights[0].length + oddWeights[0].length];

        int pairIndex;
        int oddIndex;

        for (int indexValue = 0; indexValue < oddWeights.length; indexValue++) {
            pairIndex = 0;
            oddIndex = 0;

            for (int componentIndex = 0; componentIndex < pairweights[indexValue].length + oddWeights[indexValue].length; componentIndex++) {
                if (componentIndex % 2 == 0) {
                    result[indexValue][componentIndex] = pairweights[indexValue][pairIndex];
                    pairIndex++;
                } else {
                    result[indexValue][componentIndex] = oddWeights[indexValue][oddIndex];
                    oddIndex++;
                }
            }
        }
        return result;
    }
}