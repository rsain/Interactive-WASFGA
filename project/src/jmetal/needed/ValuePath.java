/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jmetal.needed;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.core.SolutionSet;
import jmetal.util.AchievementScalarizingFunction;
import jmetal.util.ReferencePoint;

/**
 *
 * @author Ruben
 */
public class ValuePath {    
    private static final String FILE_NAME_FOR_SOLUTIONS = "solutionsFotValuePath.txt";
    private static final String FILE_NAME_FOR_REFERENCE_POINT = "referencePoint.txt";
    private static final String FILE_NAME_FOR_IDEAL_POINT = "idealPoint.txt";
    private static final String FILE_NAME_FOR_NADIR_POINT = "nadirPoint.txt";
    
    /**
     * Write a solution set in a text file, using an especial GNUPlot format.
     * @return true if the operation was successful, false in other case.
     */   
    public static boolean writeSolutionsInFile(SolutionSet ss, String folderPath)
    {
        boolean result = false;
        int colorIndex;
        FileWriter fw = null;
        
        if (new File(folderPath).isDirectory())
        {
            try 
            {            
                fw = new FileWriter(folderPath + "\\" + FILE_NAME_FOR_SOLUTIONS, false);
                result = true;
                colorIndex = 0;
                for (int solutionIndex = 0; solutionIndex < ss.size(); solutionIndex++)
                {
                    for (int objectiveIndex = 0; objectiveIndex < ss.get(solutionIndex).numberOfObjectives(); objectiveIndex++)                
                    {
                        fw.write((objectiveIndex+1) + " " + ss.get(solutionIndex).getObjective(objectiveIndex) + " " + colorIndex + "\n");
                    }   
                    fw.write("\n");

                    colorIndex++;
                }
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(ValuePath.class.getName()).log(Level.SEVERE, null, ex);
            } 
            finally 
            {
                try 
                {
                    fw.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(ValuePath.class.getName()).log(Level.SEVERE, null, ex);
                }               
            }
        }
        return result;
    }
    
    /**
     * Get the solution set in an especial GNUPlot format.
     * @return A matrix with the solutions for the GNUPlot value path.
     */   
    public static double[][] getSolutionsForPlot(SolutionSet ss)
    {
        double[][] result = new double[ss.size()*ss.get(0).numberOfObjectives()][2];
                
        int rowIndex = 0;
        for (int solutionIndex = 0; solutionIndex < ss.size(); solutionIndex++)
        {
            for (int objectiveIndex = 0; objectiveIndex < ss.get(solutionIndex).numberOfObjectives(); objectiveIndex++)                
            {
                result[rowIndex][0] = objectiveIndex+1;
                result[rowIndex][1] = ss.get(solutionIndex).getObjective(objectiveIndex);
                
                rowIndex++;
            }              
        }
        
        return result;
    }
    
    /**
     * Get the reference point in an especial GNUPlot format.
     * @return A matrix with the reference point for the GNUPlot value path.
     */   
    public static double[][] getReferencePointForPlot(ReferencePoint rp)
    {
        double[][] result = new double[rp.size()][2];
                
        for (int objectiveIndex = 0; objectiveIndex < rp.size(); objectiveIndex++)                
        {
            result[objectiveIndex][0] = objectiveIndex+1;
            result[objectiveIndex][1] = rp.get(objectiveIndex);           
        }
        
        return result;
    }
    
    /**
     * Get the point in an especial GNUPlot format.
     * @return A matrix with the point for the GNUPlot value path.
     */   
    public static double[][] getPointForPlot(double[] point)
    {
        double[][] result = new double[point.length][2];
                
        for (int objectiveIndex = 0; objectiveIndex < point.length; objectiveIndex++)                
        {
            result[objectiveIndex][0] = objectiveIndex+1;
            result[objectiveIndex][1] = point[objectiveIndex];           
        }
        
        return result;
    }
    
    /**
     * Write a the reference point, the nadir and ideal in different text files in a special GNUPlot format.
     * @return true if the operation was successful, false in other case.
     */  
    public static boolean writePreferenceInformationInFile(ReferencePoint rp, double[] nadir, double[] ideal, String folderPath) 
    {
        boolean result = false;
        FileWriter fwRp = null, fwNadir = null, fwIdeal = null;
      
        if (new File(folderPath).isDirectory())
        {
            try 
            {                        
                fwRp = new FileWriter(folderPath + "\\" + FILE_NAME_FOR_REFERENCE_POINT, false);
                fwIdeal = new FileWriter(folderPath + "\\" + FILE_NAME_FOR_IDEAL_POINT, false);
                fwNadir = new FileWriter(folderPath + "\\" + FILE_NAME_FOR_NADIR_POINT, false);
                
                for (int i = 0; i < rp.size(); i++)                
                {
                    fwRp.write(i+1 + " " + rp.get(i) + "\n");
                    fwIdeal.write(i+1 + " " + ideal[i] + "\n");
                    fwNadir.write(i+1 + " " + nadir[i] + "\n");
                }
                                                               
                result = true;
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(ValuePath.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally 
            {
                try 
                {
                    fwRp.close();
                    fwIdeal.close();
                    fwNadir.close();
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(ValuePath.class.getName()).log(Level.SEVERE, null, ex);
                }               
            }
        }        
        
        return result;
    }
}
