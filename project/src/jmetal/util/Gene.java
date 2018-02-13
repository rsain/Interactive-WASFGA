package jmetal.util;

// This class represents a gene with associated expression data
public class Gene 
{
  // Data members
  private String geneName; //gene name
  private double[] exprValues; // Actual values of expression data
  private int[] exprRanks;     // Ranks of expression data
  
  // Constructor; sets up instance fields
  public Gene(String name, double[] expressionVals) 
  {
    geneName = name;
    exprValues = new double[expressionVals.length];
    for(int i = 0; i < exprValues.length; i++)
      exprValues[i] = expressionVals[i];
    exprRanks = new int[exprValues.length];
    for(int i = 0; i < exprValues.length; i++)
      for(int j = 0; j < exprValues.length; j++)
        if(exprValues[j] < exprValues[i])
          exprRanks[i]++; 
  }
  
  // Gets expression values
  public double[] getValues() 
  {
    return exprValues; 
  }
  
  // Gets gene name
  public String getName() 
  {
    return geneName; 
  }
  
  // Computes Euclidean distance to another gene. Distance = 0 indicates
  // identical expression data, with higher values representing increasingly
  // dissimilar expression data.
  public double euclideanDistance(Gene other) 
  {
    // TODO
    // This should calculate and return the Euclidean distance between
    // the calling Gene and the argument Gene.
    return 0; 
  }
  
  // Computes Spearman distance between the two genes. The range of this measure
  // is between 0 and 2, with 0 representing perfectly correlated expression
  // data, 1 representing uncorrelated data, and 2 representing perfectly
  // anticorrelated data.
  public double spearmanDistance(Gene other) 
  {
    // TODO
    // This should calculate and return the distance between the calling
    // Gene and the argument Gene determined from the Spearman Correlation
    return 0; 
  }
}
