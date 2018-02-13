package jmetal.util.kmeans;

public class Point implements Comparable{
	
	private int index = -1;	//denotes which Cluster it belongs to
	public double[] points;
	
	public Point(double[] points) {
		this.points = points.clone();
	}
	
	public Double getSquareOfDistance(Point anotherPoint){
                Double result = new Double(0);
                for (int i = 0; i < points.length; i++)
                {
                    result = result + ((points[i] - anotherPoint.points[i])*(points[i] - anotherPoint.points[i]));
                }
		
                return  result;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public String toString(){
                String result = new String("(");
                
                for (int i = 0; i < points.length-1; i++)
                {
                    result = result + points[i] + ",";
                }
                result = result + points[points.length-1] + ")";
                
		return result;
	}	
        
        public String toStringForFileFormat(){
                String result = new String();
                
                for (int i = 0; i < points.length-1; i++)
                {
                    result = result + points[i] + "\t";
                }
                result = result + points[points.length-1];
                
		return result;		
	}

    @Override
    public int compareTo(Object t) {
        int result=0;
        
        for (int i = 0; i < points.length && result == 0; i++)
        {
            if ( points[i] < ((Point)t).points[i] )
                result = -1;    
            else if ( points[i] > ((Point)t).points[i] )
                result = 1;
        }        
        
        return result;
    }
}
