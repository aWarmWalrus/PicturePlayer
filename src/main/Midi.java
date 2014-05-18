package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class Midi {

	// public ArrayList<playStream> playList = new ArrayList<playStream>();
	Synthesizer x;
	MidiChannel[] o;
	MidiChannel mc1;
	int bass_note  = 60;
	public static Modes mode;

	public Midi() {
		Synthesizer x;
		MidiChannel[] o;
		try {
			x = MidiSystem.getSynthesizer();
			o = x.getChannels();
			x.open();
			mc1 = o[5];
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			return;
		}
	}

	public enum Modes {
		IONIAN(new int[] { 0, 2, 4, 5, 7, 9, 11 }, new int[][] { { 1, 3, 5 },
				{ 4, 6, 1 }, { 5, 7, 2, 4 }, { 2, 4, 6, 1 } }), DORIAN(
				new int[] { 0, 2, 3, 5, 7, 9, 10 }, new int[][] {
						{ 1, 3, 5, 6, 7 }, { 6, 2, 4 }, { 3, 5, 7 },
						{ 5, 7, 2 } }), PHRYGIAN(new int[] { 0, 1, 3, 5, 7, 8,
				10 },
				new int[][] { { 1, 3, 5, 7 }, { 7, 2, 4, 6 }, { 2, 4, 6 } }), LYDIAN(
				new int[] { 0, 2, 4, 6, 7, 9, 11 }, new int[][] {
						{ 1, 3, 4, 5, 7 }, { 2, 4, 6, 1 }, { 5, 7, 2, 4 },
						{ 4, 6, 1, 3 } }), MIXOLYDIAN(new int[] { 0, 2, 4, 5,
				7, 9, 10 }, new int[][] { { 1, 3, 5, 7 }, { 5, 7, 2, 4 },
				{ 4, 6, 1 }, { 7, 2, 4, 6 } }), AEOLIAN(new int[] { 0, 2, 3, 5,
				7, 8, 10 }, new int[][] { { 1, 3, 5, 7 }, { 7, 2, 4 },
				{ 6, 1, 3 }, { 5, 7, 2, 4 }, { 4, 6, 1, 3 } }), LOCRIAN(
				new int[] { 0, 1, 3, 5, 6, 8, 10 }, new int[][] {
						{ 1, 3, 5, 7 }, { 1, 4, 6, 2 }, { 5, 7, 1, 4 },
						{ 3, 5, 7, 1 } });

		int[] scale;
		int[][] chords;

		Modes(int[] mode, int[][] thoseChords) {
			scale = mode;
			chords = thoseChords;
		}

		public static Modes modes(int mode) {
			switch (mode) {
			case 1:
				return Modes.IONIAN;
			case 2:
				return Modes.DORIAN;
			case 3:
				return Modes.PHRYGIAN;
			case 4:
				return Modes.LYDIAN;
			case 5:
				return Modes.MIXOLYDIAN;
			case 6:
				return Modes.AEOLIAN;
			case 7:
				return Modes.LOCRIAN;
			default:
				return Modes.IONIAN;
			}
		}
	}

	public int genNote(int rel_note) {
		int currPitch = bass_note + mode.scale[rel_note%7]
				+ (new Random().nextInt(4) - 2) * 12;
		return currPitch;
	}

	synchronized public void run(int frequency, int duration, int volume) {
		try {
			mc1.noteOn(genNote(frequency), volume);
			Thread.sleep(duration);
			System.out.println("    duration: " + duration);
			mc1.noteOff(frequency);
		} catch (InterruptedException e) {
			return;
		}
	}

	public class playStream extends Thread {

		private boolean playing = true;

		@Override
		public void run() {
			// System.out.println(Thread.currentThread().getName());

			try {
				Synthesizer x = MidiSystem.getSynthesizer();
				MidiChannel[] o = x.getChannels();
				x.open();
				final MidiChannel mc1 = o[0];

				Random rand = new Random();
				while (playing) {
					int i = rand.nextInt(50) + 30;
					int duration = rand.nextInt(3) + 2;
					duration *= 200;
					mc1.noteOn(i, 80);
					Thread.sleep(duration);
					if (i == 50)
						break;
				}
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-ge0nerated catch block
				System.out.println("interrupted. Ruude!");
				return;
			}
		}
	}
}