package jmetal.util.kmeans;

import java.io.*;
import java.util.*;

public class KMeans {

	private static final Random random = new Random();
	public final List<Point> allPoints;
	public final int k;
	private Clusters pointClusters;	//the k Clusters

	/**@param pointsFile : the csv file for input points
	 * @param k : number of clusters
	 */
	public KMeans(String pointsFile, int k) {
		if (k < 2)
			new Exception("The value of k should be 2 or more.").printStackTrace();
		this.k = k;
		List<Point> points = new ArrayList<Point>();
		try {
			InputStreamReader read = new InputStreamReader(
					new FileInputStream(pointsFile), "UTF-8");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while ((line = reader.readLine()) != null) 
				points.add(getPointByLine(line));
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.allPoints = Collections.unmodifiableList(points);
	}

	private Point getPointByLine(String line) {
		String[] points = line.split(",");
                double[] values = new double[points.length];
                                
                for (int i = 0; i < values.length; i++)
                    values[i] = Double.parseDouble(points[i]);
                
		return new Point(values);
	}

	/**step 1: get random seeds as initial centroids of the k clusters
	 */
	private void getInitialKRandomSeeds(){
		pointClusters = new Clusters(allPoints);
		List<Point> kRandomPoints = getKRandomPoints();                                
		for (int i = 0; i < k; i++){
			kRandomPoints.get(i).setIndex(i);
			pointClusters.add(new Cluster(kRandomPoints.get(i)));
		}	
	}
	
	private List<Point> getKRandomPoints() {
		List<Point> kRandomPoints = new ArrayList<Point>();
		boolean[] alreadyChosen = new boolean[allPoints.size()];
		int size = allPoints.size();
		for (int i = 0; i < k; i++) {
			int index = -1, r = random.nextInt(size--) + 1;
			for (int j = 0; j < r; j++) {
				index++;
				while (alreadyChosen[index])
					index++;
			}
			kRandomPoints.add(allPoints.get(index));
			alreadyChosen[index] = true;
		}
		return kRandomPoints;
	}
	
	/**step 2: assign points to initial Clusters
	 */
	private void getInitialClusters(){
		pointClusters.assignPointsToClusters();
	}
	
	/** step 3: update the k Clusters until no changes in their members occur
	 */
	private void updateClustersUntilNoChange(){
		boolean isChanged = pointClusters.updateClusters();
		while (isChanged)
			isChanged = pointClusters.updateClusters();
	}
	
	/**do K-means clustering with this method and return the point of each cluster
	 */
	public List<Cluster> getPointsClusters() {
		if (pointClusters == null) {
			getInitialKRandomSeeds();
			getInitialClusters();
			updateClustersUntilNoChange();
		}
		return pointClusters;
	}
        
        /**do K-means clustering with this method and return the centroids
	 */
	public List<Point> getCentroids() {                
		List<Cluster> clusters = getPointsClusters();
                List<Point> result = new ArrayList<>();                
                
                for (Cluster c : clusters)
                    result.add(c.getCentroid());
                
                return result;
	}
	
	public static void main(String[] args) {
		String pointsFilePath = "Pesos.dat";
		KMeans kMeans = new KMeans(pointsFilePath, 8);
		List<Cluster> pointsClusters = kMeans.getPointsClusters();
                
                for (int i = 0 ; i < 8; i++)
			System.out.println(pointsClusters.get(i).getCentroid().toString());
                
		for (int i = 0 ; i < kMeans.k; i++)
			System.out.println("Cluster " + i + ": " + pointsClusters.get(i));
	}
}
