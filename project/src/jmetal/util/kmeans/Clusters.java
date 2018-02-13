package jmetal.util.kmeans;

import java.util.*;

public class Clusters extends ArrayList<Cluster> {

	private static final long serialVersionUID = 1L;
	private final List<Point> allPoints;
	private boolean isChanged;
	
	public Clusters(List<Point> allPoints){
		this.allPoints = allPoints;
	}
	
	/**@param point
	 * @return the index of the Cluster nearest to the point
	 */
	public Integer getNearestCluster(Point point){
		double minSquareOfDistance = Double.MAX_VALUE;
		int itsIndex = -1;
		for (int i = 0 ; i < size(); i++){
			double squareOfDistance = point.getSquareOfDistance(get(i).getCentroid());
			if (squareOfDistance < minSquareOfDistance){
				minSquareOfDistance = squareOfDistance;
				itsIndex = i;
			}
		}
		return itsIndex;
	}

	public boolean updateClusters(){
		for (Cluster cluster : this){
			cluster.updateCentroid();
			cluster.getPoints().clear();
		}
		isChanged = false;
		assignPointsToClusters();
		return isChanged;
	}
	
	public void assignPointsToClusters(){
		for (Point point : allPoints){
			int previousIndex = point.getIndex();
			int newIndex = getNearestCluster(point);
			if (previousIndex != newIndex)
				isChanged = true;
			Cluster target = get(newIndex);
			point.setIndex(newIndex);
			target.getPoints().add(point);
		}
	}
}
