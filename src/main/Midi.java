package main;

import java.util.ArrayList;
import java.util.Random;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class Midi {

	public ArrayList<playStream> playList = new ArrayList<playStream>();

	public Midi() {
		playList = new ArrayList<playStream>();
		playStream x = new playStream();
		playList.add(x);
		playList.add(new playStream());
	}

	public enum Modes {
		IONIAN (new int[]{0,2,4,5,7,9,11}),
		DORIAN (new int[]{0,2,3,5,7,9,10}),
		PHRYGIAN (new int[]{0,1,3,5,7,8,10}),
		LYDIAN (new int[]{0,2,4,6,7,9,11}),
		MIXOLYDIAN (new int[]{0,2,4,5,7,9,10}),
		AEOLIAN (new int[]{0,2,3,5,7,8,10}),
		LOCRIAN (new int[]{0,1,3,5,6,8,10});

		Modes(int[] mode) {}

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

	/**
	 * @param args
	 * @throws MidiUnavailableException 
	 */
	public static void main(String[] args) throws MidiUnavailableException {
		Midi m = new Midi();
		playStream x = m.playList.get(0);
		playStream y = m.playList.get(1);
		x.start();
		y.start();

		//		try {
		//			Synthesizer x = MidiSystem.getSynthesizer();
		//			Soundbank s = x.getDefaultSoundbank();
		//			MidiChannel[] o = x.getChannels();
		//
		//			Instrument[] l = new Instrument[s.getInstruments().length];
		//			for (int i = 0; i < s.getInstruments().length; i++) {
		//				l[i] = s.getInstruments()[i];
		//				System.out.println(i + ": " + l[i].getName() );
		//			}
		//			x.open();
		//			final MidiChannel mc1 = o[0];
		//			
		//			Random rand = new Random();
		//			while (true) {
		//				int i = rand.nextInt(50) + 30;
		//				int duration = rand.nextInt(3) + 2;
		//				duration *= 200;
		//				mc1.noteOn(i, 80);
		//				Thread.sleep(duration);
		//				if(i == 50) break;
		//			}
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		Timer t = new Timer();
		//		t.schedule(new TimerTask(){
		//			@Override
		//			public void run() {
		//				mc1.noteOn(60, 50);
		//				System.out.println("playing");
		//			}
		//			
		//		}, 33);
	}

	public class playStream extends Thread {

		private boolean playing = true;

		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName());
			int root = 50;
			try {
				Synthesizer x = MidiSystem.getSynthesizer();
				MidiChannel[] o = x.getChannels();
				x.open();
				final MidiChannel mc1 = o[0];

				Random rand = new Random();
				while (playing) {
					int i = rand.nextInt(7);
					Modes.IONIAN.values()[i];
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
		public void random() {
			System.out.println(Thread.currentThread().getName());
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
				// TODO Auto-generated catch block
				System.out.println("interrupting");
				return;
			}
		}
	}
}