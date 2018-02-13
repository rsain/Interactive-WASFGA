/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.util;

import java.util.Vector;

import jmetal.core.Solution;

/**
 *
 * @author Rubén
 */
public class ParetoDominance {
		
    public static int checkParetoDominance(double[] v1, double[] v2) {
        int dominate1; // dominate1 indicates if some objective of solution1 
        // dominates the same objective in solution2. dominate2
        int dominate2; // is the complementary of dominate1.

        dominate1 = 0;
        dominate2 = 0;

        int flag; //stores the result of the comparison	

        double value1, value2;
        for (int i = 0; i < v1.length; i++) {
            value1 = v1[i];
            value2 = v2[i];
            if (value1 < value2) {
                flag = -1;
            } else if (value1 > value2) {
                flag = 1;
            } else {
                flag = 0;
            }

            if (flag == -1) {
                dominate1 = 1;
            }

            if (flag == 1) {
                dominate2 = 1;
            }
        }

        if (dominate1 == dominate2) {
            return 0; //No one dominate the other
        }
        if (dominate1 == 1) {
            return -1; // solution1 dominate
        }
        return 1;    // solution2 dominate		
    }
    
    public static int checkParetoDominance(Solution s1, Solution s2) {
        int dominate1; // dominate1 indicates if some objective of solution1 
        // dominates the same objective in solution2. dominate2
        int dominate2; // is the complementary of dominate1.

        dominate1 = 0;
        dominate2 = 0;
        
        int flag; //stores the result of the comparison	

        double value1, value2;
        for (int i = 0; i < s1.numberOfObjectives(); i++) {
            value1 = s1.getObjective(i);
            value2 = s2.getObjective(i);
            if (value1 < value2) {
                flag = -1;
            } else if (value1 > value2) {
                flag = 1;
            } else {
                flag = 0;
            }

            if (flag == -1) {
                dominate1 = 1;
            }

            if (flag == 1) {
                dominate2 = 1;
            }
        }

        if (dominate1 == dominate2) {
            return 0; //No one dominate the other
        }
        if (dominate1 == 1) {
            return -1; // solution1 dominate
        }
        return 1;    // solution2 dominate		
    }    
}
