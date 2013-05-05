import org.eclipse.swt.widgets.Display;


/**
 * Example of how WebcamPanel can be used in SWT using SWT_AWT bridge.
 * 
 * @author Dimitrios Chondrokoukis
 */
public class WebcamPanelSwtAwtExample {

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			WebcamPanelShell shell = new WebcamPanelShell(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
