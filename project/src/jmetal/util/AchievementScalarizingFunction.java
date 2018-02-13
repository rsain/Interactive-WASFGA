package jmetal.util;

import java.util.LinkedList;
import jmetal.core.SolutionSet;

/**
 * This class models the achievement scalarizing function (ASF) defined by Wierzbicki
 * @author Rubén
 */
public class AchievementScalarizingFunction {
    private ReferencePoint referencePoint_;
    private double[] nadir_;
    private double[] ideal_;    
    private double p_ = 0.001; //augmentation coefficient

    /**
	 * Construct an empty ASF given a number of objectives
	 * @param numberOfObjectives The number of objectives	 
	 */
    public AchievementScalarizingFunction(int numberOfObjectives) {
        this.referencePoint_ = new ReferencePoint(numberOfObjectives);
        this.nadir_ = new double[numberOfObjectives];
        this.ideal_ = new double[numberOfObjectives];
        
        for (int index=0;index < numberOfObjectives; index++)
        {
            this.referencePoint_.set(index, new Double(0.0));
            this.nadir_[index] = Double.MIN_VALUE;
            this.ideal_[index] = Double.MAX_VALUE;
        }        
    }
    
    /**
	 * Construct an empty ASF given a number of objectives
	 * @param numberOfObjectives The number of objectives
	 * @param p The augmentation coefficient	 
	 */
    public AchievementScalarizingFunction(int numberOfObjectives, double p) {
        this.referencePoint_ = new ReferencePoint(numberOfObjectives);
        this.nadir_ = new double[numberOfObjectives];
        this.ideal_ = new double[numberOfObjectives];
        
        for (int index=0;index < numberOfObjectives; index++)
        {
            this.referencePoint_.set(index, new Double(0.0));
            this.nadir_[index] = Double.MIN_VALUE;
            this.ideal_[index] = Double.MAX_VALUE;
        }
        
        this.p_ = p;
    }
    
    /**
   	 * Construct an empty ASF given a reference point, the nadir and the ideal points (the augmentation coefficient is 0.001)
   	 * @param referencePoint The reference point
   	 * @param nadir The nadir point
   	 * @param ideal The ideal point
   	 * @throws CloneNotSupportedException	 
   	 */
    public AchievementScalarizingFunction(ReferencePoint referencePoint, double[] nadir, double[] ideal) throws CloneNotSupportedException {
        this.referencePoint_ = referencePoint;
        this.nadir_ = nadir.clone();
        this.ideal_ = ideal.clone();
    }
  
    /**
   	 * Construct an empty ASF given a reference point, the nadir and the ideal points, and the augmentation coefficient
   	 * @param referencePoint The reference point
   	 * @param nadir The nadir point
   	 * @param ideal The ideal point
   	 * @param p The augmentation coefficient
   	 * @throws CloneNotSupportedException	 
   	 */
    public AchievementScalarizingFunction(ReferencePoint referencePoint, double[] nadir, double[] ideal, double p) throws CloneNotSupportedException {
        this.referencePoint_ = referencePoint;
        this.nadir_ = nadir.clone();
        this.ideal_ = ideal.clone();
        this.p_ = p;
    }

    /**
   	 * Return the reference used in the ASF 
   	 * @return The reference point used in the ASF	 
   	 */ 
    public ReferencePoint getReferencePoint() {
        return referencePoint_;
    }

    /** 
     * Set the reference point used in the ASF
     * @param referencePoint The new reference point
     * @throws CloneNotSupportedException 
     */  
    public void setReferencePoint(ReferencePoint referencePoint) throws CloneNotSupportedException {
        this.referencePoint_ = referencePoint;
    }
    
    /** 
     * Modify a component of the reference point used in the ASF
     * @param objective Component of the reference point to modify
     * @param referenceValue New value of the specified component 
     */  
    public void setReferencePoint(int objective, double referenceValue) {
        this.referencePoint_.set(objective, referenceValue);
    }

    /** 
     * Get the nadir point used in the ASF
     * @return The nadir point used in the ASF
     */  
    public double[] getNadir() {
        return nadir_;
    }

    /** 
     * Set the nadir point used in the ASF
     * @param nadir The nadir point used in the ASF
     */
    public void setNadir(double[] nadir) {
        this.nadir_ = nadir.clone();
    }

    /** 
     * Modify a component of the nadir point 
     * @param objective Component of the nadir point to modify
     * @param value New value of the specified component 
     */  
    public void setNadir(int objective, double value) {
        this.nadir_[objective] = value;
    }
    
    /** 
     * Get the ideal point used in the ASF
     * @return The ideal point used in the ASF
     */ 
    public double[] getIdeal() {
        return ideal_;
    }

    /** 
     * Set the ideal point used in the ASF
     * @param nadir The ideal point used in the ASF
     */
    public void setIdeal(double[] ideal) {
        this.ideal_ = ideal.clone();
    }
    
    /** 
     * Modify a component of the ideal point 
     * @param objective Component of the ideal point to modify
     * @param value New value of the specified component 
     */  
    public void setIdeal(int objective, double value) {
        this.ideal_[objective] = value;
    }

    /** 
     * Get the augmentation coefficient used in the ASF
     * @return the augmentation coefficient used in the ASF
     */  
    public double getP() {
        return p_;
    }

    /** 
     * Set the augmentation coefficient
     * @param p The augmentation coefficient used in the ASF
     */  
    public void setP(double p) {
        this.p_ = p;
    }
     
    /** 
     * Return the value of the ASF, given an objective vector and a weights vector
     * @param objectives Objective vector
     * @param weights Weights vector
     * @return The value of the ASF    
     */  
    public double evaluate(double[] objectives, double[] weights) {
        int component_index;
        double first_sum, second_sum = 0, temp_product, difference;
        
        first_sum = -1e10;
        for (component_index=0; component_index < this.referencePoint_.size(); component_index++)
        {				
            difference = (objectives[component_index] - this.referencePoint_.get(component_index));

            temp_product = (weights[component_index])*(difference);

            if (temp_product > first_sum)
                first_sum = temp_product;

            second_sum = second_sum + difference;				
        }

        return first_sum + (this.p_ * second_sum);                
    }    
    
    /** 
     * Return the normalized value of the ASF, given an objective vector and a weights vector
     * @param objectives Objective vector
     * @param weights Weights vector
     * @return The normalized value of the ASF    
     */  
    public double evaluateNormalizing(double[] objectives, double[] weights) {
        int component_index;
        double first_sum, second_sum = 0, temp_product, normalizedDifference;
        
        first_sum = -1e10;
        for (component_index=0; component_index < this.referencePoint_.size(); component_index++)
        {				
            normalizedDifference = (objectives[component_index] - this.referencePoint_.get(component_index))/(this.nadir_[component_index]-this.ideal_[component_index]);

            temp_product = weights[component_index]*(normalizedDifference);

            if (temp_product > first_sum)
                first_sum = temp_product;

            second_sum = second_sum + normalizedDifference;				
        }

        return first_sum + (this.p_ * second_sum);                
    }
    
    /** 
     * Return the value of the ASF for each solution in a <code>SolutionSet</code> and for each weight vector in a set of weight vectors
     * @param solutionSet Set of solutions
     * @param weights Set of weight vectors
     * @return The value of the ASF for each solution and each weight vector   
     */  
    public double[][] evaluate(SolutionSet solutionSet, double[][] weights) {        
        double [] objectives = new double[solutionSet.get(0).numberOfObjectives()];
        double[][] result = new double[solutionSet.size()][weights.length];                
        
        int solutionIndex, weightIndex, objectiveIndex;        
        
        for (solutionIndex=0; solutionIndex < solutionSet.size(); solutionIndex++)
        {
            for (weightIndex=0; weightIndex < weights.length; weightIndex++)
            {	                
                for (objectiveIndex = 0; objectiveIndex < objectives.length; objectiveIndex++)
                {
                    objectives[objectiveIndex] = solutionSet.get(solutionIndex).getObjective(objectiveIndex);
                }
                                
                result[solutionIndex][weightIndex] = this.evaluate(objectives, weights[weightIndex]);
            }                          
        }
        return result;   
    }
    
    /** 
     * Return the value of the ASF for each solution in a <code>SolutionSet</code> and for each weight vector in a list of weight vectors
     * @param solutionSet Set of solutions
     * @param weights List of weight vectors
     * @return The value of the ASF for each solution and each weight vector     
     */  
     public double[][] evaluate(SolutionSet solutionSet, LinkedList<double[]> weights) {        
         double [] objectives = new double[solutionSet.get(0).numberOfObjectives()];
         double[][] result = new double[solutionSet.size()][weights.size()];                
         
         int solutionIndex, weightIndex, objectiveIndex;        
         
         for (solutionIndex=0; solutionIndex < solutionSet.size(); solutionIndex++)
         {
             for (weightIndex=0; weightIndex < weights.size(); weightIndex++)
             {	                
                 for (objectiveIndex = 0; objectiveIndex < objectives.length; objectiveIndex++)
                 {
                     objectives[objectiveIndex] = solutionSet.get(solutionIndex).getObjective(objectiveIndex);
                 }
                                 
                 result[solutionIndex][weightIndex] = this.evaluate(objectives, weights.get(weightIndex));
             }                          
         }
         return result;   
     }
    
     /** 
      * Return the normalized value of the ASF for each solution in a <code>SolutionSet</code> and for each weight vector in a set of weight vectors
      * @param solutionSet Set of solutions
      * @param weights Set of weight vectors
      * @return The normalized value of the ASF for each solution and each weight vector     
      */  
    public double[][] evaluateNormalizing(SolutionSet solutionSet, double[][] weights) {        
        double [] objectives = new double[solutionSet.get(0).numberOfObjectives()];
        double[][] result = new double[solutionSet.size()][weights.length];                
        
        int solutionIndex, weightIndex, objectiveIndex;        
        
        for (solutionIndex=0; solutionIndex < solutionSet.size(); solutionIndex++)
        {
            for (weightIndex=0; weightIndex < weights.length; weightIndex++)
            {	                
                for (objectiveIndex = 0; objectiveIndex < objectives.length; objectiveIndex++)
                {
                    objectives[objectiveIndex] = solutionSet.get(solutionIndex).getObjective(objectiveIndex);
                }
                                
                result[solutionIndex][weightIndex] = this.evaluateNormalizing(objectives, weights[weightIndex]);
            }                          
        }
        return result;  
    }   
    
    /** 
     * Return the normalized value of the ASF for each solution in a <code>SolutionSet</code> and for each weight vector in a list of weight vectors
     * @param solutionSet Set of solutions
     * @param weights List of weight vectors
     * @return The normalized value of the ASF for each solution and each weight vector     
     */  
     public double[][] evaluateNormalizing(SolutionSet solutionSet, LinkedList<double[]> weights) {        
         double [] objectives = new double[solutionSet.get(0).numberOfObjectives()];
         double[][] result = new double[solutionSet.size()][weights.size()];                
         
         int solutionIndex, weightIndex, objectiveIndex;        
         
         for (solutionIndex=0; solutionIndex < solutionSet.size(); solutionIndex++)
         {
             for (weightIndex=0; weightIndex < weights.size(); weightIndex++)
             {	                
                 for (objectiveIndex = 0; objectiveIndex < objectives.length; objectiveIndex++)
                 {
                     objectives[objectiveIndex] = solutionSet.get(solutionIndex).getObjective(objectiveIndex);
                 }
                                 
                 result[solutionIndex][weightIndex] = this.evaluateNormalizing(objectives, weights.get(weightIndex));
             }                          
         }
         return result;  
     } 
}