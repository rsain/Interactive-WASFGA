package jmetal.util.kmeans;

import java.util.*;

public class Cluster {

	private final List<Point> points;
	private Point centroid;
	
	public Cluster(Point firstPoint) {
		points = new ArrayList<Point>();
		centroid = firstPoint;
	}
	
	public Point getCentroid(){
		return centroid;
	}
	
	public void updateCentroid(){
                double[] newValues = new double[centroid.points.length];
                for (int i = 0; i < newValues.length; i++)
                    newValues[i] = 0d;
		
		for (Point point : points)
                {
                    for (int i = 0; i < newValues.length; i++)
                    {
                        newValues[i]+=point.points[i];
                    }			
		}
                
                for (int i = 0; i < newValues.length; i++)
                    newValues[i] = (newValues[i]/points.size());
                
		centroid = new Point(newValues);
	}
	
	public List<Point> getPoints() {
		return points;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder("This cluster contains the following points:\n");
		for (Point point : points)
			builder.append(point.toString() + ",\n");
		return builder.deleteCharAt(builder.length() - 2).toString();	
	}
}
