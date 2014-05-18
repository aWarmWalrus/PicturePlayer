package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import java.util.Timer;

public class GUI extends AnchorPane {
	
	final Stage main;
	final Group root;
	final String address;
	static int SUBIMAGE_LENGTH = 50;
	static int BORDER_OFFSET = SUBIMAGE_LENGTH / 2;
	public File tempDir;
	BufferedImage img;
	Timer updateSelected;
	int c;
	int r;
	
	private Rectangle selected;
	private boolean playing = false;
	private Scan scan;
	private int columns;
	private int rows;
	private Rectangle[][] rectangles;
	boolean hasImage = false;
	
	//FXML IDs
	@FXML Button playButton;
	
	public GUI(Stage mainStage) {
		main = mainStage;
		root = (Group) main.getScene().getRoot();
		//Load the FXML
		FXMLLoader fxmlLoader = 
				new FXMLLoader(getClass().getResource("gui.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        address = System.getProperty("user.dir");
        selected = new Rectangle();
        selected.setMouseTransparent(true);
        updateSelected = new Timer();
        updateSelected.schedule(new TimerTask(){
        	@Override
        	public void run() {
        		updateSelected();
        	}
        }, 0, 30);
	}
	
	@FXML protected void loadImage() {
		cleanRoot();
		//Get the image file from the user.
		FileChooser fc = new FileChooser();
		File imageFile = fc.showOpenDialog(main);
		String filePath = imageFile.getAbsolutePath();
		Image pic = new Image("file:///" + filePath);
		try {
			img = ImageIO.read(imageFile);
		} catch (IOException e) {
			System.out.println("what");
		}
		
		//create the new directory
		tempDir = new File(address + "\\tmpimg");
		if(tempDir.mkdir()){
			System.out.println("* Temporary directory successfully created:"
					+ "\n   " + tempDir.getAbsolutePath());
		} else {
			System.out.println("! Error: directory already initialized. Attempting clean...");
			if(clean()){
				System.out.println("* Clean successful.");
				if(tempDir.mkdir()) {
					System.out.println("* Temporary directory successfully created:"
							+ "\n   " + tempDir.getAbsolutePath());
				} else {
					System.out.println("! Error: attempt to reinitialize failed.");
					return;
				}
			} else {
				System.out.println("! Error: attempt to reclean failed.");
				return;
			}
		}
		
		root.getChildren().add(selected);
		//each block is 100x100 pixels
		columns = (int) pic.getWidth() / SUBIMAGE_LENGTH;
		rows = (int) pic.getHeight() / SUBIMAGE_LENGTH;
		
		rectangles = new Rectangle[columns+1][rows+1];
		
		for(int col = 0; col < columns; col++) {
			for(int row = 0; row < rows; row++) {
				final int c = col;
				final int r = row;
				File newImage = 
						new File(address+"\\tmpimg\\"+col+","+row+".jpg");
				try {
					ImageIO.write(img.getSubimage(col * SUBIMAGE_LENGTH,
												  row * SUBIMAGE_LENGTH, 
												  SUBIMAGE_LENGTH, 
												  SUBIMAGE_LENGTH), 
								  "jpg", 
								  newImage);
				} catch (IOException e) {
					System.out.println("!Error while writing to a file");
					return;
				}
				final Image sub = new Image("file:///" + newImage.getAbsolutePath());
				final Rectangle subIV = new Rectangle(col*50 + BORDER_OFFSET, 
						row*50 + BORDER_OFFSET, 
						SUBIMAGE_LENGTH,
						SUBIMAGE_LENGTH);
				rectangles[col][row] = subIV;
				subIV.setFill(new ImagePattern(sub));
				subIV.setStroke(Color.BLACK);
				subIV.setStrokeWidth(1);
				subIV.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent arg0) {
						if(playing) return;
						setSelected(c, r);
					}
				});
				subIV.setOnMouseExited(new EventHandler<MouseEvent>() {@Override
					public void handle(MouseEvent arg0) {}});
				subIV.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						if(playing) return;
						playing = true;
						scan = new Scan(c, r);
						scan.start();
					}
				});
				root.getChildren().add(subIV);
			}
		}

		main.setHeight(pic.getHeight() + 4 * BORDER_OFFSET);
		main.setWidth(pic.getWidth() + 4 * BORDER_OFFSET);
		hasImage = true;
	}
	
	@FXML
	protected void handlePlay() {
		if(playing) {
			scan.interrupt();
			System.out.println("interrupted!");
			playing = false;
		} else {
			playing = true;
			scan = new Scan();
			scan.start();
		}
	}
	
	private boolean clean() {
		for(File f : tempDir.listFiles()){
			if(!f.delete()) {
				System.out.println("! Error deleting file: " + f.getName());
				return false;
			}
		}
		if(tempDir.delete()) {
			System.out.println("* Temporary directory successfully deleted");
		} else {
			System.out.println("bad");
			return false;
		}
		return true;
	}
	
	private void cleanRoot() {
		ArrayList<Rectangle> toGo = new ArrayList<Rectangle>();
		for(Node i : root.getChildren()) {
			if(i instanceof Rectangle) toGo.add((Rectangle) i);
		}
		for(Rectangle i : toGo) {
			root.getChildren().remove(i);
		}
		toGo.clear();
	}
	
	private void setSelected(int col, int row) {
		c = col;
		r = row;
	}
	
	private void updateSelected() {
		if(!hasImage) return;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Rectangle rect = rectangles[c][r];
				selected.setX(c*SUBIMAGE_LENGTH);
				selected.setY(r*SUBIMAGE_LENGTH);
				selected.setWidth(SUBIMAGE_LENGTH*2);
				selected.setHeight(SUBIMAGE_LENGTH*2);
				selected.setFill(rect.getFill());
				selected.setStroke(Color.BLACK);
				selected.toFront();
				if(playing) playButton.setText("Pause");
				else playButton.setText("Play");
			}});
	}
	
	class Scan extends Thread {
		
		int c;
		int r;
		boolean stitch = false;
		
		Scan(){}
		
		Scan(int c, int r) {
			this.c = c;
			this.r = r;
			stitch = true;
		}
		
		@Override
		public void run() {
			System.out.println(Thread.currentThread());
			for(int col = c; col < columns; col++) {
				for(int row = stitch? r : 0; row < rows; row++) {
					stitch = false;
					setSelected(col, row);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
			playing = false;
		}
		
	}
}
