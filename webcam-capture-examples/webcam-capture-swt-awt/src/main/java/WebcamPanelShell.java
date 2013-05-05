import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JRootPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * Example of how WebcamPanel can be used in SWT using SWT_AWT bridge.
 * 
 * @author Dimitrios Chondrokoukis
 */
public class WebcamPanelShell extends Shell {

	private Webcam webcam;
	private WebcamPanel panel;

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public WebcamPanelShell(Display display) {

		super(display, SWT.SHELL_TRIM);

		Dimension size = WebcamResolution.QVGA.getSize();

		setLayout(new FillLayout(SWT.HORIZONTAL));
		setText("WebcamPanel in SWT Application");
		setSize(size.width, size.height);

		Composite composite = new Composite(this, SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(composite);

		JRootPane root = new JRootPane();
		frame.add(root);

		webcam = Webcam.getDefault();
		webcam.setViewSize(size);

		panel = new WebcamPanel(webcam, size, false);
		panel.setFPSDisplayed(true);

		root.getContentPane().add(panel);

		panel.start();

	}

	@Override
	protected void checkSubclass() {
		// disable the check that prevents subclassing of SWT components
	}
}
