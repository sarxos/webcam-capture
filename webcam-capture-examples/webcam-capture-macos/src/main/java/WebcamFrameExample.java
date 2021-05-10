import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.javacv.JavaCvDriver;

import javax.swing.JFrame;

public class WebcamFrameExample {
  public static void main(String[] args) {
    
    Webcam.setDriver(new JavaCvDriver());
    System.out.println(Webcam.getWebcams());

    Webcam webcam = Webcam.getDefault();

    if(webcam != null) {
      JFrame frame = new JFrame("JavaCv Capture Driver Demo");
      frame.add(new WebcamPanel(webcam));
      frame.pack();
      frame.setVisible(true);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
  }
}
