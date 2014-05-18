package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import main.Midi.Modes;

public class ImgProcessor {

	public static ImgProcessor instance;
	private int mode;
	private Vector3d std;
	private int[] modal_scale;
	private int[][] modal_chords;
	private int bass_note;
	private Midi albert;
	private static double maxH = Double.MIN_VALUE;
	private static double minH = Double.MAX_VALUE;

	/*
	 * 1: Ionian 2: Dorian 3: Phrygian 4: Lydian 5: Mixolydian 6: Aeolian 7:
	 * Locrian
	 */

	protected ImgProcessor() {
		albert = new Midi();
	}

	public static ImgProcessor getProcessor() {
		if (instance == null)
			instance = new ImgProcessor();
		return instance;
	}

	public static void analyzeImg(File g) {
		try {
			BufferedImage img = ImageIO.read(g);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setMode(File[] column){
		System.out.println("* Determining the mode for this column...");
		int c = 0;
		for(File i : column)
		try{
//			System.out.println(c++);
			BufferedImage img = ImageIO.read(i);
//			img./
//			img.getRGB(arg0, arg1)
//			
		} catch (IOException e) {
			System.out.println();
		}
		
		Vector3d avg = getAvg(HSB.get(i));
		Vector3d stdDev = getStdDev(HSB.get(i), avg);
		Modes mode = chooseMode(avg.x);
	}

	
	
	void setMode(double d) {
		int m = (int) d * 7 + 1;
		mode = m;
		Modes mody =  Midi.Modes.modes(m);
		modal_scale = mody.scale;
		modal_chords = mody.chords;
	}
	
	int[] harmonize(int note)
	{
		int[] chord;
		int k;
		Random rand = new Random(modal_chords.length);
		do{
			k = rand.nextInt();
			chord = modal_chords[k];
		}while(hasNote(note, chord));
		return chord;
	}
	
	boolean hasNote(int note, int[] a_chord)
	{
		for(int i: a_chord)
			if(i == note)
				return true;
		return false;
	}

	static Vector3d getStdDev(ArrayList<Vector3d> colours, Vector3d mean) {
		Vector3d stdDev = new Vector3d();
		for (Vector3d c : colours) {
			stdDev.x += Math.pow((c.x - mean.x), 2);
			stdDev.y += Math.pow((c.y - mean.y), 2);
			stdDev.z += Math.pow((c.z - mean.z), 2);
		}
		stdDev.scale(1 / colours.size());
		stdDev.x = Math.sqrt(stdDev.x);
		stdDev.y = Math.sqrt(stdDev.y);
		stdDev.z = Math.sqrt(stdDev.z);
		if(stdDev.x > maxH)
			maxH = stdDev.x;
		if(stdDev.x < minH)
			minH = stdDev.x;
		return stdDev;
	}

	public static Vector3d toRGB(int rgb) {
		return new Vector3d((rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8,
				(rgb & 0xFF));
	}

	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
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

	static Vector3d avgCol(ArrayList<Vector3d> colours) {
		Vector3d mean = new Vector3d();
		for (Vector3d c : colours) {
			mean.x += c.x;
			mean.y += c.y;
			mean.z += c.z;
		}
		mean.x /= colours.size();
		mean.y /= colours.size();
		mean.z /= colours.size();
		return mean;
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
	
	int getPitch(double H) {
		int p = (int) Math.floor(H / (maxH - minH / 7));
		return p;
	}

	public void playNote(File f) {
		System.out.println("hey ");
		try {
			BufferedImage img = ImageIO.read(f);
			long c0Time = System.currentTimeMillis();
			Vector3d pixel;
			ArrayList<ArrayList<Vector3d>> HSB = new ArrayList<ArrayList<Vector3d>>();

			int[][] pixels = convertTo2DWithoutUsingGetRGB(img);
			for (int i = 0; i < pixels.length; i++) {
				HSB.add(new ArrayList<Vector3d>());
				for (int j = 0; j < pixels[i].length; j++) {
					pixel = toRGB(pixels[i][j]);
					float[] hsv = Color.RGBtoHSB((int) (pixel.x),
							(int) (pixel.y), (int) (pixel.z), new float[3]);
					HSB.get(HSB.size() - 1).add(
							new Vector3d(hsv[0], hsv[1], hsv[2]));
				}
			}
			
			long c1Time = System.currentTimeMillis();
			Vector3d mean = new Vector3d();
			
			int i, j = 0;
			for (i = 0; i < HSB.size(); i++) {
				for (j = 0; j < HSB.get(i).size(); j++) {
					Vector3d c = HSB.get(i).get(j);
					mean.x += c.x;
					mean.y += c.y;
					mean.z += c.z;
				}
			}

			long c2Time = System.currentTimeMillis();
			mean.x /= i * j;
			mean.y /= i * j;
			mean.z /= i * j;
			int currDur = (int) Math.abs(mean.y - std.y);
			int currVel = (int)(Math.round(Math.abs(mean.z - std.z)));
			int currPitch = getPitch(mean.x);
			System.out.println("hue: " + mean.x);
//			System.out.println("sat: " + mean.y);
//			System.out.println("val: " + mean.z);
			albert.run(currDur,
					currVel,
					currPitch);
//			g.run();
			System.out.println("time elapsed from c0: " + (System.currentTimeMillis() - c0Time));
			System.out.println("time elapsed from c1: " + (System.currentTimeMillis() - c1Time));
			System.out.println("time elapsed from c2: " + (System.currentTimeMillis() - c2Time));

		} catch (final IOException e) {
			System.err.println("crap happened");
		}

	}
}
