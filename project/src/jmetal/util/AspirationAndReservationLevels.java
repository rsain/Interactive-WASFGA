/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.comparators.ObjectiveComparator;

/**
 *
 * @author rubén
 */
public class AspirationAndReservationLevels {

    Vector<Double> aspirationLevel_;
    Vector<Double> reservationLevel_;

    public enum ReferenceLevelTypes {ACHIEVABLES, UNACHIEVABLES, MIXED};
    
	public AspirationAndReservationLevels(String aspirationFileName, String reservationFileName) throws IOException {        
            // Open the aspiration file
            FileInputStream fis = new FileInputStream(aspirationFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            aspirationLevel_.clear();            
            String aux = br.readLine();
            while (aux != null) {
                StringTokenizer st = new StringTokenizer(aux);                            
                
                while (st.hasMoreTokens()) {
                    Double value = (new Double(st.nextToken()));
                    aspirationLevel_.add(value);                    
                }  
                aux = br.readLine();
            }
            br.close();
            
            
            // Open the aspiration file
            fis = new FileInputStream(reservationFileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            
            reservationLevel_.clear();            
            aux = br.readLine();
            while (aux != null) {
                StringTokenizer st = new StringTokenizer(aux);                            
                
                while (st.hasMoreTokens()) {
                    Double value = (new Double(st.nextToken()));
                    reservationLevel_.add(value);                    
                }                
                aux = br.readLine();
            }
            br.close();
    }

    public AspirationAndReservationLevels(Vector<Double> aspirationLevel, Vector<Double> reservationLevel) throws JMException {
        if (aspirationLevel.size() != reservationLevel.size())
        {
            throw new JMException("AspirationAndReservationLevels: Aspirations a reservation levels must have equal number of components.");
        }
        else
        {
            Boolean aspirationDominatesReservation = (aspirationLevel.get(0) <= reservationLevel.get(0));
        
            int index = 1;
            while (aspirationDominatesReservation && index < aspirationLevel.size())
            {          
                aspirationDominatesReservation = (aspirationLevel.get(index) <= reservationLevel.get(index));
                index++;
            }
            
            if (aspirationDominatesReservation)
            {
                aspirationLevel_ = aspirationLevel;
                reservationLevel_ = reservationLevel;                   
            }   
            else
                throw new JMException("AspirationAndReservationLevels: The aspiration point must dominated the reservation point.");
        }
    }
    
    public Vector<Double> getAspirationLevel() {
        return this.aspirationLevel_;
    }
    
    public Vector<Double> getReservationLevel() {
        return this.reservationLevel_;
    }

    public AspirationAndReservationLevels(ReferenceLevelTypes type, String paretoFrontFileName) throws JMException {
        int randomIndexPoint;
        double [] minimumValues, maximumValues;
        double[][] front;
        int index, objective, numberOfObjectives;        
        MetricsUtil metrics = new MetricsUtil();
        
        front = metrics.readFront(paretoFrontFileName);
                
        numberOfObjectives = front[0].length;        
                
        minimumValues = metrics.getMinimumValues(front, numberOfObjectives);
        maximumValues = metrics.getMaximumValues(front, numberOfObjectives);
        
        randomIndexPoint = PseudoRandom.randInt(0, front.length);
        
        reservationLevel_ = new Vector<Double>();
        aspirationLevel_ = new Vector<Double>();
        
        switch (type)
        {
            case ACHIEVABLES:
                for (index = 0; index < numberOfObjectives; index++)
                {
                    this.reservationLevel_.add(PseudoRandom.randDouble(front[randomIndexPoint][index], maximumValues[index]));
                    this.aspirationLevel_.add(PseudoRandom.randDouble(front[randomIndexPoint][index], reservationLevel_.get(index)));
                }
                break;
                
            case UNACHIEVABLES:
                for (index = 0; index < numberOfObjectives; index++)
                {
                    this.reservationLevel_.add(PseudoRandom.randDouble(minimumValues[index], front[randomIndexPoint][index]));
                    this.aspirationLevel_.add(PseudoRandom.randDouble(minimumValues[index], reservationLevel_.get(index)));
                }
                break;
                
            case MIXED:
                for (index = 0; index < numberOfObjectives; index++)
                {
                    this.reservationLevel_.add(PseudoRandom.randDouble(front[randomIndexPoint][index], maximumValues[index]));
                    this.aspirationLevel_.add(PseudoRandom.randDouble(minimumValues[index], front[randomIndexPoint][index]));
                }
                break;
        }
    }
    
    public double[][] referenceLevelsInRegionOfInterest(int numberOfPointsInObjective) {
        int index, objective;
        int numberOfObjectives = aspirationLevel_.size();
        double [][] result;
        Vector<Double> step = new Vector<Double>();
        Vector referenceLevel = new Vector();
        Vector<Vector<Double>> referenceLevels = new Vector<Vector<Double>>();

        for (index = 0; index < numberOfObjectives; index++) {
            step.add((reservationLevel_.get(index) - aspirationLevel_.get(index)) / (numberOfPointsInObjective -1));//;(Math.pow(numberOfPoints, 1.0 / numberOfObjectives) - 1));
        }

        if (numberOfObjectives == 2) {
            //Genera solamente los límites del coorespondientes al nivel de aspiracion (en forma de L), no el interior			
            for (double x = aspirationLevel_.get(0).doubleValue(); x < reservationLevel_.get(0).doubleValue() + 0.000001; x = x + step.get(0).doubleValue()) {
                referenceLevel.clear();
                referenceLevel.add(x);
                referenceLevel.add(aspirationLevel_.get(1));
                referenceLevels.add((Vector<Double>)referenceLevel.clone());
            }
            for (double y = aspirationLevel_.get(1).doubleValue() + step.get(1).doubleValue(); y < reservationLevel_.get(1).doubleValue() + 0.000001; y = y + step.get(1).doubleValue()) {
                referenceLevel.clear();
                referenceLevel.add(aspirationLevel_.get(0));
                referenceLevel.add(y);
                referenceLevels.add((Vector<Double>)referenceLevel.clone());
            }
        } else if (numberOfObjectives == 3) {
            //Genera solamente las caras (planos) coorespondientes al nivel de aspiracion
            //Plano lateral izquierdo
            for (double y = aspirationLevel_.get(1).doubleValue(); y < reservationLevel_.get(1).doubleValue() + 0.000001; y = y + step.get(1).doubleValue()) {
                for (double z = aspirationLevel_.get(2).doubleValue(); z < reservationLevel_.get(2).doubleValue() + 0.000001; z = z + step.get(2).doubleValue()) {
                    referenceLevel.clear();
                    referenceLevel.add(aspirationLevel_.get(0));
                    referenceLevel.add(y);
                    referenceLevel.add(z);
                    referenceLevels.add((Vector<Double>)referenceLevel.clone());
                }
            }
            //Plano frontal
            for (double x = aspirationLevel_.get(0).doubleValue() + step.get(0).doubleValue(); x < reservationLevel_.get(0).doubleValue() + 0.000001; x = x + step.get(0).doubleValue()) {
                for (double y = aspirationLevel_.get(1).doubleValue(); y < reservationLevel_.get(1).doubleValue() + 0.000001; y = y + step.get(1).doubleValue()) {
                    referenceLevel.clear();
                    referenceLevel.add(x);
                    referenceLevel.add(y);
                    referenceLevel.add(aspirationLevel_.get(2));
                    referenceLevels.add((Vector<Double>)referenceLevel.clone());
                }
            }
            //Plano inferior (suelo)
            for (double x = aspirationLevel_.get(0).doubleValue() + step.get(0).doubleValue(); x < reservationLevel_.get(0).doubleValue() + 0.000001; x = x + step.get(0).doubleValue()) {
                for (double z = aspirationLevel_.get(2).doubleValue() + step.get(2).doubleValue(); z < reservationLevel_.get(2).doubleValue() + 0.000001; z = z + step.get(2).doubleValue()) {
                    referenceLevel.clear();
                    referenceLevel.add(x);
                    referenceLevel.add(aspirationLevel_.get(1));
                    referenceLevel.add(z);
                    referenceLevels.add((Vector<Double>)referenceLevel.clone());
                }
            }
        }
        
        result = new double[referenceLevels.size()][numberOfObjectives];

        for (index=0; index < referenceLevels.size(); index++) {
            for (objective = 0; objective < numberOfObjectives; objective++){
                result[index][objective] = referenceLevels.get(index).get(objective).doubleValue();
            }
        }
        
        return result;
    }
        
    public double[][] referenceLevelsInRegionOfInterest(int numberOfPointsInObjective, String fileName) throws IOException {
        int index, objective;
        int numberOfObjectives = aspirationLevel_.size();
        double [][] result;
        Vector<Double> step = new Vector<Double>();
        Vector referenceLevel = new Vector();
        Vector<Vector<Double>> referenceLevels = new Vector<Vector<Double>>();

        for (index = 0; index < numberOfObjectives; index++) {
            step.add((reservationLevel_.get(index) - aspirationLevel_.get(index)) / (numberOfPointsInObjective -1));//;(Math.pow(numberOfPoints, 1.0 / numberOfObjectives) - 1));
        }

        if (numberOfObjectives == 2) {
            //Genera solamente los límites del coorespondientes al nivel de aspiracion (en forma de L), no el interior			
            for (double x = aspirationLevel_.get(0).doubleValue(); x < reservationLevel_.get(0).doubleValue() + 0.000001; x = x + step.get(0).doubleValue()) {
                referenceLevel.clear();
                referenceLevel.add(x);
                referenceLevel.add(aspirationLevel_.get(1));
                referenceLevels.add((Vector<Double>)referenceLevel.clone());
            }
            for (double y = aspirationLevel_.get(1).doubleValue() + step.get(1).doubleValue(); y < reservationLevel_.get(1).doubleValue() + 0.000001; y = y + step.get(1).doubleValue()) {
                referenceLevel.clear();
                referenceLevel.add(aspirationLevel_.get(0));
                referenceLevel.add(y);
                referenceLevels.add((Vector<Double>)referenceLevel.clone());
            }
        } else if (numberOfObjectives == 3) {
            //Genera solamente las caras (planos) coorespondientes al nivel de aspiracion
            //Plano lateral izquierdo
            for (double y = aspirationLevel_.get(1).doubleValue(); y < reservationLevel_.get(1).doubleValue() + 0.000001; y = y + step.get(1).doubleValue()) {
                for (double z = aspirationLevel_.get(2).doubleValue(); z < reservationLevel_.get(2).doubleValue() + 0.000001; z = z + step.get(2).doubleValue()) {
                    referenceLevel.clear();
                    referenceLevel.add(aspirationLevel_.get(0));
                    referenceLevel.add(y);
                    referenceLevel.add(z);
                    referenceLevels.add((Vector<Double>)referenceLevel.clone());
                }
            }
            //Plano frontal
            for (double x = aspirationLevel_.get(0).doubleValue() + step.get(0).doubleValue(); x < reservationLevel_.get(0).doubleValue() + 0.000001; x = x + step.get(0).doubleValue()) {
                for (double y = aspirationLevel_.get(1).doubleValue(); y < reservationLevel_.get(1).doubleValue() + 0.000001; y = y + step.get(1).doubleValue()) {
                    referenceLevel.clear();
                    referenceLevel.add(x);
                    referenceLevel.add(y);
                    referenceLevel.add(aspirationLevel_.get(2));
                    referenceLevels.add((Vector<Double>)referenceLevel.clone());
                }
            }
            //Plano inferior (suelo)
            for (double x = aspirationLevel_.get(0).doubleValue() + step.get(0).doubleValue(); x < reservationLevel_.get(0).doubleValue() + 0.000001; x = x + step.get(0).doubleValue()) {
                for (double z = aspirationLevel_.get(2).doubleValue() + step.get(2).doubleValue(); z < reservationLevel_.get(2).doubleValue() + 0.000001; z = z + step.get(2).doubleValue()) {
                    referenceLevel.clear();
                    referenceLevel.add(x);
                    referenceLevel.add(aspirationLevel_.get(1));
                    referenceLevel.add(z);
                    referenceLevels.add((Vector<Double>)referenceLevel.clone());
                }
            }
        }
        
        
        result = new double[referenceLevels.size()][numberOfObjectives];
        FileWriter file = new FileWriter(fileName, false);
        
        for (index=0; index < referenceLevels.size(); index++) {
            for (objective = 0; objective < numberOfObjectives; objective++){
                result[index][objective] = referenceLevels.get(index).get(objective).doubleValue();
                file.write(referenceLevels.get(index).get(objective).toString() + " ");
            }
            file.write("\n");
        }

        file.close();
        
        return result;
    }
    
    public void writeInFile(String aspirationFileName, String reservationFileName) throws IOException {
        int index;
        
        FileWriter aspirationFile = new FileWriter(aspirationFileName, false);
        FileWriter reservationFile = new FileWriter(reservationFileName, false);
     
        for (index = 0; index < aspirationLevel_.size(); index++)
        {
            aspirationFile.write(aspirationLevel_.get(index).toString() + " ");        
            reservationFile.write(reservationLevel_.get(index).toString() + " ");
        }
        
        aspirationFile.close();
        reservationFile.close();
    }
}
