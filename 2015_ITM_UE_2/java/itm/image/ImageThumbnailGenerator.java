package itm.image;

/*******************************************************************************
    This file is part of the ITM course 2015
    (c) University of Vienna 2009-2015
*******************************************************************************/

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
    This class converts images of various formats to PNG thumbnails files.
    It can be called with 3 parameters, an input filename/directory, an output directory and a compression quality parameter.
    It will read the input image(s), grayscale and scale it/them and convert it/them to a PNG file(s) that is/are written to the output directory.

    If the input file or the output directory do not exist, an exception is thrown.
*/
public class ImageThumbnailGenerator 
{

    /**
        Constructor.
    */
    public ImageThumbnailGenerator()
    {
    }

    /**
        Processes an image directory in a batch process.
        @param input a reference to the input image file
        @param output a reference to the output directory
        @param rotation
        @param overwrite indicates whether existing thumbnails should be overwritten or not
        @return a list of the created files
    */
    public ArrayList<File> batchProcessImages( File input, File output, double rotation, boolean overwrite ) throws IOException
    {
        if ( ! input.exists() ) {
            throw new IOException( "Input file " + input + " was not found!" );
        }
        if ( ! output.exists() ) {
            throw new IOException( "Output directory " + output + " not found!" );
        }
        if ( ! output.isDirectory() ) {
            throw new IOException( output + " is not a directory!" );
        }

        ArrayList<File> ret = new ArrayList<File>();

        if ( input.isDirectory() ) {
            File[] files = input.listFiles();
            for ( File f : files ) {
                try {
                    File result = processImage( f, output, rotation, overwrite );
                    System.out.println( "converted " + f + " to " + result );
                    ret.add( result );
                } catch ( Exception e0 ) {
                    System.err.println( "Error converting " + input + " : " + e0.toString() );
                }
            }
        } else {
            try {
                File result = processImage( input, output, rotation, overwrite );
                System.out.println( "converted " + input + " to " + result );
                ret.add( result );
            } catch ( Exception e0 ) {
                System.err.println( "Error converting " + input + " : " + e0.toString() );
            }
        } 
        return ret;
    }  

    /**
        Processes the passed input image and stores it to the output directory.
        This function should not do anything if the outputfile already exists and if the overwrite flag is set to false.
        @param input a reference to the input image file
        @param output a reference to the output directory
        @param dimx the width of the resulting thumbnail
        @param dimy the height of the resulting thumbnail
        @param overwrite indicates whether existing thumbnails should be overwritten or not
    */
    protected File processImage( File input, File output, double rotation, boolean overwrite ) throws IOException, IllegalArgumentException
    {
        if ( ! input.exists() ) {
            throw new IOException( "Input file " + input + " was not found!" );
        }
        if ( input.isDirectory() ) {
            throw new IOException( "Input file " + input + " is a directory!" );
        }
        if ( ! output.exists() ) {
            throw new IOException( "Output directory " + output + " not found!" );
        }
        if ( ! output.isDirectory() ) {
            throw new IOException( output + " is not a directory!" );
        }

        // create outputfilename and check whether thumb already exists
        File outputFile = new File( output, input.getName() + ".thumb.png" );
        if ( outputFile.exists() ) {
            if ( ! overwrite ) {
                return outputFile;
            }
        }

        // load the input image
        ImageConverter conv = new ImageConverter();
        BufferedImage oldImg = conv.loadImage(input);
        double scaleFactor = 200.0/oldImg.getWidth();
        double height = scaleFactor*oldImg.getHeight();
        int newHeight = (int)height;
        BufferedImage newImg = null;
        

        BufferedImage landscapedImg = null;
        
        // rotate by the given parameter the image - do not crop image parts!
        if (oldImg.getHeight()>oldImg.getWidth()){
        	AffineTransform at = new AffineTransform();
        	at.translate(oldImg.getHeight()/2, oldImg.getWidth()/2);
        	at.rotate(Math.PI/2);
        	at.translate(-oldImg.getWidth()/2, -oldImg.getHeight()/2);
        	
        	AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        	
        	landscapedImg = new BufferedImage(oldImg.getHeight(), oldImg.getWidth(), oldImg.getType());
        	op.filter(oldImg, landscapedImg);
        }
        
        
        // scale the image to a maximum of [ 200 w X 100 h ] pixels - do not distort!
        if ((landscapedImg!=null && (landscapedImg.getWidth()>200 && landscapedImg.getHeight()>100)) || 
        		(landscapedImg==null && (oldImg.getWidth()>200 && oldImg.getHeight()>100))){
        	newImg = new BufferedImage(200, newHeight, BufferedImage.TYPE_INT_ARGB);
        	Graphics2D g2 = newImg.createGraphics();
        	AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
        	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        	if (landscapedImg!=null){
        		g2.drawImage(landscapedImg, at, null);
        	}
        	else{
        		g2.drawImage(oldImg, at, null);
        	}
        	g2.dispose();
        }
        
        // if the image is smaller than [ 200 w X 100 h ] - print it on a [ dim X dim ] canvas!
        else{
        	newImg = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        	Graphics2D g2 = newImg.createGraphics();
        	g2.setBackground(Color.black);
        	g2.setPaint(Color.black);
        	g2.fillRect(0, 0, 200, 100);
        	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        	g2.drawImage(oldImg, 200/2-oldImg.getWidth()/2, 100/2-oldImg.getHeight()/2, oldImg.getWidth(), oldImg.getHeight(), null);
        	g2.dispose();
        
        }
        
        
        // rotate your image by the given rotation parameter
        if (rotation>=0 && rotation<=360){
        	BufferedImage rotatedImg = null;
        	if (rotation%90==0){
        		if (rotation==180){
        			AffineTransform transform = new AffineTransform();
        		    transform.rotate(Math.toRadians(rotation), newImg.getWidth()/2, newImg.getHeight()/2);
        		    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        		    
        			
        			rotatedImg = new BufferedImage(newImg.getWidth(), newImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        			rotatedImg = op.filter(newImg, null);

        		}
        		if (rotation==0){
        			System.out.println("Hallo");
        			rotatedImg=newImg;
        		}
        		else{
        			AffineTransform at = new AffineTransform();
                	at.translate(newImg.getHeight()/2, newImg.getWidth()/2);
                	at.rotate(Math.toRadians(rotation));
                	at.translate(-newImg.getWidth()/2, -newImg.getHeight()/2);
                	
                	AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                	
                	BufferedImage tempImg = new BufferedImage(newImg.getHeight(), newImg.getWidth(), newImg.getType());
                	op.filter(newImg, tempImg);
        		    
        			
        			rotatedImg = new BufferedImage(newImg.getHeight(), newImg.getWidth(), BufferedImage.TYPE_INT_ARGB);
        			Graphics2D g2 = rotatedImg.createGraphics();
                	g2.drawImage(tempImg, 0, 0, tempImg.getWidth(), tempImg.getHeight(), null);
                	g2.dispose();
        		}
        	}
        	else{
            	int newSize=(int)Math.sqrt(Math.pow(newImg.getWidth(), 2)+ Math.pow(newImg.getHeight(), 2));
            	
            	rotatedImg = new BufferedImage(newSize, newSize, newImg.getType());
            	
            	AffineTransform at = new AffineTransform();
            	at.translate(newSize/2, newSize/2);
            	at.rotate(Math.toRadians(rotation));
            	at.translate(-newImg.getWidth()/2, -newImg.getHeight()/2);
            	
            	Graphics2D g2=(Graphics2D)rotatedImg.getGraphics();
            	g2.drawImage(newImg, at, null);
            	
        	}
        	            	    
        	// save as extra file - say: don't return as output file
       		File f=null;
       		try{
        		f = new File(output.getAbsolutePath()+"\\"+input.getName()+".thumb.rotated.png");
        		ImageIO.write(rotatedImg, "png", f);
        	}
        	catch (IOException e){
        		System.err.println("Writing Error!");
        	}
        }
        
        // encode and save the image
        File f=null;
    	try{
    		f = new File(output.getAbsolutePath()+"\\"+input.getName()+".thumb.png");
    		ImageIO.write(newImg, "png", f);
    		outputFile=f;
    	}
    	catch (IOException e){
    		System.err.println("Writing Error!");
    	}
        
        return outputFile;

        /**
            ./ant.sh ImageThumbnailGenerator -Dinput=media/img/ -Doutput=test/ -Drotation=90
        */
    }

    /**
        Main method. Parses the commandline parameters and prints usage information if required.
    */
    public static void main( String[] args ) throws Exception
    {
        if ( args.length < 3 ) {
            System.out.println( "usage: java itm.image.ImageThumbnailGenerator <input-image> <output-directory> <rotation degree>" );
            System.out.println( "usage: java itm.image.ImageThumbnailGenerator <input-directory> <output-directory> <rotation degree>" );
            System.exit( 1 );
        }
        File fi = new File( args[0] );
        File fo = new File( args[1] );
        double rotation = Double.parseDouble( args[2] );

        ImageThumbnailGenerator itg = new ImageThumbnailGenerator();
        itg.batchProcessImages( fi, fo, rotation, true );
    }    
}