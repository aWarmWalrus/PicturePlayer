package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImgProcessor {
	
	public static ImgProcessor instance;
	private int mode;
	/*
	 * 1: Ionian
	 * 2: Dorian
	 * 3: Phrygian
	 * 4: Lydian
	 * 5: Mixolydian
	 * 6: Aeolian
	 * 7: Locrian
	 */
	
	protected ImgProcessor() {
		
	}
	
	public static ImgProcessor getProcessor() {
		if(instance == null) instance = new ImgProcessor();
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
			System.out.println(c++);
			BufferedImage img = ImageIO.read(i);
//			img./
			
		} catch (IOException e) {
			System.out.println();
		}
	}
}
