package main;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector3d;

import main.Midi.Modes;

public class ImageAnalyzer extends Applet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// File representing the folder that you select using a FileChooser
	static final File dir = new File(System.getProperty("user.dir"));

	// array of supported extensions (use a List if you prefer)
	static final String[] EXTENSIONS = new String[]{
		"gif", "png", "bmp", "jpg" // and other formats you need
	};
	// filter to identify images based on their extensions
	static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
		public boolean accept(final File dir, final String name) {
			for (final String ext : EXTENSIONS) {
				if (name.endsWith("." + ext)) {
					return (true);
				}
			}
			return (false);
		}
	};

	public static BufferedImage img;
	
	public static int SAMPLE_SIZE = 1;

	public void paint(Graphics g) {
		g.drawImage(img, 0,0, null);
	}
	
	public static void main(String[] args) {
	//	BufferedImage d = new BufferedImage();
		
	}
	
	@Override
	public void init() {//main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("hey ");
		System.out.println(dir.isDirectory());
		//    BufferedImage hugeImage = ImageIO.read(PerformanceTest.class.getResource("12000X12000.jpg"));
		if (dir.isDirectory()) { // make sure it's a directory


			/*for (final File f : dir.listFiles(IMAGE_FILTER)) {
              BufferedImage img = null;	        
              try {
                  img = ImageIO.read(f);

                  // you probably want something more involved here
                  // to display in your UI
                  System.out.println("image: " + f.getName());
                  System.out.println(" width : " + img.getWidth());
                  System.out.println(" height: " + img.getHeight());
                  System.out.println(" size  : " + f.length());
              } catch (final IOException e) {
                  // handle errors here
              }
          }*/

			try{
				System.out.println(dir.getParent());
				File f = new File(dir.getParent()+"/tmpimg/00,00.jpg");
				System.out.println(f.getAbsolutePath());
				// 		dir.listFiles(IMAGE_FILTER)[0];
				img = ImageIO.read(f);

				System.out.println("image: " + f.getName());
				System.out.println(" width : " + img.getWidth());
				System.out.println(" height: " + img.getHeight());
				System.out.println(" size  : " + f.length());
				
				Vector3d pixel;
				ArrayList<ArrayList<Color>> RGB = new ArrayList<ArrayList<Color>>();
				ArrayList<ArrayList<Vector3d>> HSB = new ArrayList<ArrayList<Vector3d>>();
				HashMap<Tuple2d, Color> colours = new HashMap<Tuple2d, Color>();
				HashMap<Tuple2d, Color> hsvColors = new HashMap<Tuple2d, Color>();
				
				int[][] pixels = convertTo2DWithoutUsingGetRGB(img);
				for(int i = 0; i < pixels.length; i++)
				{
					RGB.add(new ArrayList<Color>());
					HSB.add(new ArrayList<Vector3d>());
					for(int j = 0; j < pixels[i].length; j++)
					{
						
						pixel = toRGB(pixels[i][j]);
						RGB.get(RGB.size()-1).add(new Color((float)(pixel.x/255),(float)(pixel.y/255),(float) (pixel.z/255)));
					//	System.out.println(pixel);
					//	colours.put(new Vector2d(i,j),new Color((float)(pixel.y/255),(float)(pixel.z/255),(float) (pixel.w/255)));
						float[] hsv = Color.RGBtoHSB((int)(pixel.x), (int)(pixel.y), (int)(pixel.z), new float[3]);
						HSB.get(HSB.size()-1).add(new Vector3d(hsv[0],hsv[1],hsv[2]));
//						System.out.println(pixel.x + " " + pixel.y + " " + pixel.z);
//						System.out.println(hsv[0] + " " + hsv[1] + " " + hsv[2]);
					//	hsvColors.put(new Vector2d(i,j), Color.getHSBColor(hsv[0],hsv[1],hsv[2]));
					}
				}
				for(int i = 0; i < HSB.size(); i++)
				{
					Vector3d avg = getAvg(HSB.get(i));
					Vector3d stdDev = getStdDev(HSB.get(i), avg);
					Modes mode = chooseMode(avg.x);
					for(int j = 0; j < HSB.get(i).size(); j++)
					{
						
					}
				}
				
				
				
				
				
				
			} catch (final IOException e) {
				// handle errors here
				System.err.println("crap happened");
			}
			
/*			// Obtain information about all the installed synthesizers.
			Vector synthInfos = null;
			MidiDevice device;
			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			for (int i = 0; i < infos.length; i++) {
			    try {
			        device = MidiSystem.getMidiDevice(infos[i]);
			        if (device instanceof Synthesizer) {
				        synthInfos.add(infos[i]);
				    }
			        System.out.println(infos);
			    } catch (MidiUnavailableException e) {
			          // Handle or throw exception...
			    }
			   
			}*/
			
/*			Midi m = new Midi();
			playStream x = m.playList.get(0);
			playStream y = m.playList.get(1);
			x.random();
			y.random();*/
			
			
			
		}


	}

	static Vector3d getAvg(ArrayList<Vector3d> colours)
	{
		Vector3d mean = new Vector3d();
		for(Vector3d c: colours)
		{
			mean.x += c.x;
			mean.y += c.y;
			mean.z += c.z;
		}
		mean.x /= colours.size();
		mean.y /= colours.size();
		mean.z /= colours.size();
		return mean;
	}
	
	static Modes chooseMode(double d)
	{
		int m = (int)Math.floor(d*7);
		
		return Midi.Modes.modes(m+1);
	}

	static Vector3d getStdDev(ArrayList<Vector3d> colours, Vector3d mean)
	{
		Vector3d stdDev = new Vector3d();
		for(Vector3d c: colours)
		{
			stdDev.x += Math.pow((c.x - mean.x),2);
			stdDev.y += Math.pow((c.y - mean.y),2);
			stdDev.z += Math.pow((c.z - mean.z),2);
		}
		stdDev.scale(1/colours.size());
		stdDev.x = Math.sqrt(stdDev.x);
		stdDev.y = Math.sqrt(stdDev.y);
		stdDev.z = Math.sqrt(stdDev.z);
		return stdDev;
	}

	public Vector3d toRGB(int rgb)
	{
		return new Vector3d((rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8, (rgb & 0xFF));
	}
	
	public int filterRGB(int rgb, boolean red, boolean green, boolean blue) {
		// Filter the colors
		int r = red ? 0 : ((rgb >> 16) & 0xff);
		int g = green ? 0 : ((rgb >> 8) & 0xff);
		int b = blue ? 0 : ((rgb >> 0) & 0xff);

		// Return the result
		return (rgb & 0xff000000) | (r << 16) | (g << 8) | (b << 0);
	}

	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return result;
	}
}
