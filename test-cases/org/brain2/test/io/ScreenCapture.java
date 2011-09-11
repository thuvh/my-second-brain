package org.brain2.test.io;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;

class ScreenCapture {
	public static void main(String args[]) throws AWTException, IOException {
		// capture the whole screen
		// BufferedImage screencapture = new Robot().createScreenCapture(
		// new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );
		Runtime.getRuntime().exec("cmd");
		
		Robot robot = new Robot();

		// SET THE MOUSE X Y POSITION
		robot.delay(3000);
		

		
		//robot.mouseMove(300, 550);
		// MIDDLE WHEEL CLICK
//        robot.mousePress(InputEvent.BUTTON3_MASK);
//        robot.mouseRelease(InputEvent.BUTTON3_MASK);

        // SCROLL THE MOUSE WHEEL
        //robot.mouseWheel(-100);
        
        
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(KeyEvent.VK_J); 
		robot.keyPress(KeyEvent.VK_A); 
		robot.keyPress(KeyEvent.VK_V); 
		robot.keyPress(KeyEvent.VK_A); 
		robot.keyPress(KeyEvent.VK_SPACE); 
		robot.keyPress(KeyEvent.VK_MINUS); 
		robot.keyPress(KeyEvent.VK_V);
		robot.keyPress(KeyEvent.VK_E);
		robot.keyPress(KeyEvent.VK_R);
		robot.keyPress(KeyEvent.VK_S);
		robot.keyPress(KeyEvent.VK_I);
		robot.keyPress(KeyEvent.VK_O);
		robot.keyPress(KeyEvent.VK_N);		
		robot.keyPress(KeyEvent.VK_ENTER);
		

//		Rectangle myframe = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize() );
//		BufferedImage screencapture = robot.createScreenCapture(new Rectangle((int) myframe.getX(), (int) myframe
//				.getY(), (int) myframe.getWidth(), (int) myframe.getHeight()));
//
//		// Save as JPEG
//		File file = new File("D:/screencapture.jpg");
//		ImageIO.write(screencapture, "jpg", file);

		// Save as PNG
		// File file = new File("screencapture.png");
		// ImageIO.write(screencapture, "png", file);
	}
}