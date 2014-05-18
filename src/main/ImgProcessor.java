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
	private Midi harmon1;
	private Midi harmon2;
	private Midi harmon3;
	private static double maxH = Double.MIN_VALUE;
	private static double minH = Double.MAX_VALUE;

	/*
	 * 1: Ionian 2: Dorian 3: Phrygian 4: Lydian 5: Mixolydian 6: Aeolian 7:
	 * Locrian
	 */

	protected ImgProcessor() {
		albert = new Midi();
		harmon1 = new Midi();
		harmon2 = new Midi();
		harmon3 = new Midi();
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

	public void setMode(File[] column) {
		System.out.println("* Determining the mode for this column...");
		int c = 0;
		ArrayList<Vector3d> trackingFileMean = new ArrayList<Vector3d>();
		for (File file : column)
			try {
				Vector3d pixel;
				Vector3d quaker = new Vector3d();
				quaker.x = quaker.y = quaker.z = 0;
				int num = 0;
				BufferedImage img = ImageIO.read(file);
				ArrayList<ArrayList<Vector3d>> HSB = new ArrayList<ArrayList<Vector3d>>();
				int[][] pixels = convertTo2DWithoutUsingGetRGB(img);
				for (int i = 0; i < pixels.length; i += 10) {
					for (int j = 0; j < pixels[i].length; j += 10) {
						pixel = toRGB(pixels[i][j]);
						float[] hsv = Color.RGBtoHSB((int) (pixel.x),
								(int) (pixel.y), (int) (pixel.z), new float[3]);
						quaker.x += hsv[0];
						quaker.y += hsv[1];
						quaker.z += hsv[2];
						num++;
					}
				}
				Vector3d it = new Vector3d();
				it.x = quaker.x / num;
				it.y = quaker.y / num;
				it.z = quaker.z / num;
				trackingFileMean.add(it);
			} catch (IOException e) {
				System.out.println();
			}
		Vector3d avg = getAvg(trackingFileMean);
		setMode(avg.x);
	}

	void setMode(double d) {
		int m = (int) d * 7 + 1;
		mode = m;
		Modes mody = Midi.Modes.modes(m);
		albert.mode = mody;
	}

	int[] harmonize(int note) {
		int[] chord;
		int k;
		Random rand = new Random();
		do {
			k = rand.nextInt(Midi.mode.chords.length);
			chord = Midi.mode.chords[k];
		} while (hasNote(note, chord));
		return chord;
	}

	boolean hasNote(int note, int[] a_chord) {
		for (int i : a_chord)
			if (i == note)
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
		if (stdDev.x > maxH)
			maxH = stdDev.x;
		if (stdDev.x < minH)
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

	static Vector3d getAvg(ArrayList<Vector3d> colours) {
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
			System.out.println("meany: " + mean.y);
//			System.out.println("stdy: " + std.y);
			double currDur = mean.y > 0.90 ? 400 : (mean.y > 0.7 ? 200
					: (mean.y > 0.3 ? 100 : (mean.y > 0.1 ? 50 : 25)));
			System.out.println("currdur = " + currDur);

			int currVel = (int) (Math.round(Math.abs(mean.z) * 100) + 30);
			System.out.println("currvel = " + currVel);
//			int currPitch = toScale((int) (mean.x * 60) + 60);
			int currPitch = (int) (mean.x * 60) % 7;
			albert.run(currPitch, (int) currDur, currVel);
			Random rand = new Random();
//			if(rand.nextInt() > 7){
			System.out.println(albert.mode);
				int[] harmonies = harmonize(currPitch);
				harmon1.run(harmonies[0], (int)currDur, currVel);
				harmon2.run(harmonies[1], (int)currDur, currVel);
				harmon3.run(harmonies[2], (int)currDur, currVel);
//			}
		} catch (final IOException e) {
			System.err.println("crap happened");
		}
	}

	private int toScale(int pitch) {
		int relPitch = pitch % 12;
		switch (relPitch) {
		case 0:
			return pitch;
		case 1:
			return pitch + 1;
		case 2:
			return pitch;
		case 3:
			return pitch + 1;
		case 4:
			return pitch;
		case 5:
			return pitch + 2;
		case 6:
			return pitch + 1;
		case 7:
			return pitch;
		case 8:
			return pitch + 1;
		case 9:
			return pitch;
		case 10:
			return pitch + 2;
		case 11:
			return pitch + 1;
		default:
			return pitch;
		}
	}
}
