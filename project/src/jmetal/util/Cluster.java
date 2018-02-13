package jmetal.util;

// This class represents a cluster of genes with similar expression data. It
// contains methods for creating an image of the cluster's expression data and
// for returning the centroid of the cluster.

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.io.File;

public class Cluster 
{
  // Constants for creating Jpeg images. Do not change these.
  private static final double c_dCutoff = 2;
  private static final int c_iX  = 8;
  private static final int c_iY  = 4;
  
  // Data members
  private Vector<Gene> geneVector; // List of genes in cluster
  
  // Constructor; creates an empty cluster.
  public Cluster() 
  {
    // TODO
    // make a new Vector of objects of type Gene
    geneVector = new Vector<Gene>(); 
  }
  
  // Prints the names of all genes in the cluster
  public void printGeneNames () 
  {
    // TODO
    // This should print the name of each gene in the cluster
    // with one name printed per line
  }
  
  // Adds a gene to the cluster
  public void addGene(Gene gene) 
  {
    // Append a gene to the end of the genes Vector
    geneVector.add(gene); 
  }
  
  // Returns the centroid of the cluster as a Gene object with name "centroid".
  // The expression data of the returned gene is the centroid of the cluster.
  public Gene centroid( ) 
  {
    // TODO
    // Add code here to calculate and return the centroid
    // of the cluster of genes
    return null; 
  }
  
  // Creates an image of this cluster's expression data. The image will be
  // stored in file "<fileName><id>.jpg". Do not change this method.
  public void createJPG( String fileName, int id ) 
  {
    String   strOut;
    int    i, j, iGenes, iConditions;
    double   dValue;
    BufferedImage bimg;
    Graphics2D  gr2d;
    Color   colr;
    
    strOut = fileName + id + ".jpg";
    
    // Initialize some values
    iGenes = geneVector.size( );
    iConditions = (geneVector.get( 0 )).getValues( ).length;
    
    // Create the empty image and graphics2D
    bimg = new BufferedImage( c_iX * iConditions, c_iY * iGenes,
                             BufferedImage.TYPE_INT_RGB );
    gr2d = bimg.createGraphics( );
    
    // Draw a rectangle for each gene/condition pair
    for( i = 0; i < iGenes; ++i ) 
    {
      for( j = 0; j < iConditions; ++j ) 
      {
        dValue = (geneVector.get( i )).getValues( )[ j ];
        if( dValue < 0 )
          colr = new Color( 0.0f, ( dValue < -c_dCutoff ) ? 1.0f :
                                 (float)( dValue / -c_dCutoff ), 0.0f );
        else
          colr = new Color( ( dValue > c_dCutoff ) ? 1.0f :
                                 (float)( dValue / c_dCutoff ), 0.0f, 0.0f );
        gr2d.setColor( colr );
        gr2d.fill( new Rectangle2D.Float( j * c_iX, i * c_iY, c_iX, c_iY ) ); 
      } 
    }
    gr2d.dispose();
    
    try 
    {
      // Output the image to file
      File outFile = new File(strOut);
      ImageIO.write(bimg, "jpg", outFile);
    }
    catch( IOException e ) 
    {
      System.out.println( "ERROR: Unable to write image in " + strOut + "." ); 
    } 
  }
}

