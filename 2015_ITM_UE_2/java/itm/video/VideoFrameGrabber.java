package itm.video;

/*******************************************************************************
 This file is part of the ITM course 2015
 (c) University of Vienna 2009-2015
 *******************************************************************************/


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaListener;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

/**
 * 
 * This class creates JPEG thumbnails from from video frames grabbed from the
 * middle of a video stream It can be called with 2 parameters, an input
 * filename/directory and an output directory.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */

public class VideoFrameGrabber {

	public static int TOTAL_ITERATIONS = 0;
	public static long TOTAL_TIMESTAMPS = 0;
	public static boolean ALREADY_ITERATED = false;
	
	
	/**
	 * Constructor.
	 */
	public VideoFrameGrabber() {
	}

	/**
	 * Processes the passed input video file / video file directory and stores
	 * the processed files in the output directory.
	 * 
	 * @param input
	 *            a reference to the input video file / input directory
	 * @param output
	 *            a reference to the output directory
	 */
	public ArrayList<File> batchProcessVideoFiles(File input, File output) throws IOException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		ArrayList<File> ret = new ArrayList<File>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {
				if (f.isDirectory())
					continue;

				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv")
						|| ext.equals("mp4")) {
					File result = processVideo(f, output);
					System.out.println("converted " + f + " to " + result);
					ret.add(result);
				}

			}

		} else {
			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv") || ext.equals("mp4")) {
				File result = processVideo(input, output);
				System.out.println("converted " + input + " to " + result);
				ret.add(result);
			}
		}
		return ret;
	}

	/**
	 * Processes the passed audio file and stores the processed file to the
	 * output directory.
	 * 
	 * @param input
	 *            a reference to the input audio File
	 * @param output
	 *            a reference to the output directory
	 */
	protected File processVideo(File input, File output) throws IOException, IllegalArgumentException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		File outputFile = new File(output, input.getName() + "_thumb.jpg");
		
		// load the input video file
		IMediaReader mediaReader = ToolFactory.makeReader(input.getPath());
		
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		
		mediaReader.addListener(new ImageSnapListener(input, output));		
		
		
		while (mediaReader.readPacket() == null);
		while (mediaReader.readPacket() == null);
		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************

		return outputFile;

	}
	
	private static class ImageSnapListener extends MediaListenerAdapter {

		private File input;
		private File output;
		private long lastValue;
		private boolean alreadyPrint;
		
		public ImageSnapListener(File in, File o){
			this.input=in;
			this.output=o;
			this.lastValue=0;
			this.alreadyPrint=false;
		}
		
        public void onVideoPicture(IVideoPictureEvent event) {

        	if (!alreadyPrint){
        		BufferedImage buf = null;
        		System.out.println("Timestamp: "+event.getTimeStamp());

        	
        		if (ALREADY_ITERATED && event.getTimeStamp()>=TOTAL_TIMESTAMPS/2){
        		
        			buf=event.getImage();
        	
        			File f = null;
        			try{
        				f = new File(output.getAbsolutePath()+"\\"+input.getName()+"_FRAME.jpeg");
        				ImageIO.write(buf, "jpeg", f);
        				System.out.println("Writing complete!");
        			
        			}
        			catch (IOException e){	
        				System.err.println("Writing Error!");
        			}
        		
        			alreadyPrint=true;
        		}
        	
        		if(TOTAL_TIMESTAMPS!=0 && event.getTimeStamp()==0){
        			ALREADY_ITERATED=true;
        		}
        		if (!ALREADY_ITERATED) TOTAL_TIMESTAMPS=event.getTimeStamp();
        		
        		
        		//lastValue=event.getTimeStamp();
        		
        		
        		//System.out.println("LastValue: "+lastValue);
        		System.out.println("TOTAL TIMESTAMPS: "+TOTAL_TIMESTAMPS);
        	}
        }
	}
	
        

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		// args = new String[] { "./media/video", "./test" };

		if (args.length < 2) {
			System.out.println("usage: java itm.video.VideoFrameGrabber <input-videoFile> <output-directory>");
			System.out.println("usage: java itm.video.VideoFrameGrabber <input-directory> <output-directory>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		VideoFrameGrabber grabber = new VideoFrameGrabber();
		grabber.batchProcessVideoFiles(fi, fo);
	}

}
