import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.medialist.MediaListItem;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.vlcj.VlcjDriver;


public class WebcamPanelForWindows {

    private static List<MediaListItem> EMPTY = new ArrayList<MediaListItem>();

    /* NOTE!
     * 
     * The vlclib does not implement video device discovery on Windows. 
     * Therefore, to make it working on this operating system one needs
     * to manually provide the list of media list items from vlcj. This
     * is not necessary on Linux and Mac.
     */

    private static final MediaListItem dev0 = new MediaListItem("HP HD Webcam [Fixed]", "dshow://", EMPTY);
    private static final MediaListItem dev1 = new MediaListItem("USB2.0 Camera", "dshow://", EMPTY);
    private static final MediaListItem dev2 = new MediaListItem("Logitech Webcam", "dshow://", EMPTY);

    static {
        Webcam.setDriver(new VlcjDriver(Arrays.asList(dev0, dev1, dev2)));
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("Webcam Panel");
        window.add(new WebcamPanel(Webcam.getDefault()));
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true); 
    }
}
