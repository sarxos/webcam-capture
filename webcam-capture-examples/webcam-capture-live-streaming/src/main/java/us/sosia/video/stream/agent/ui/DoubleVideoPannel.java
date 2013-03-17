package us.sosia.video.stream.agent.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.coobird.thumbnailator.makers.ScaledThumbnailMaker;

public class DoubleVideoPannel extends JPanel{
	private static final long serialVersionUID = 2449923105339853274L;	
	protected BufferedImage bigImage;
	protected BufferedImage smallImage;
 	protected final ExecutorService worker = Executors.newSingleThreadExecutor();
 	protected final ScaledThumbnailMaker scaleUpMaker = new ScaledThumbnailMaker(2);
 	protected final ScaledThumbnailMaker scaleDownMaker = new ScaledThumbnailMaker(0.5);
 	//here,cause we know the smale image's size,so i just write it down here
 	protected final int smallImageWidth = 160;
 	protected final int smallImageHeight = 120;
 	protected final JButton exchange = new JButton("swap");
 	protected volatile boolean swap = false;
 	
 	public void close(){
 		worker.shutdown();
 		}
 	
	public DoubleVideoPannel() {
		super();
		add(exchange);
		//
		exchange.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//
				//here start to swap the image
				swap = !swap;
				worker.execute(new Runnable() {
					
					@Override
					public void run() {
						repaint();
					}
				});
			}
		});
	}

	@Override
	protected  void   paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;	
		if (bigImage == null) {
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setBackground(Color.BLACK);
			g2.fillRect(0, 0, getWidth(), getHeight());

			int cx = (getWidth() - 70) / 2;
			int cy = (getHeight() - 40) / 2;

			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRoundRect(cx, cy, 70, 40, 10, 10);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 5, cy + 5, 30, 30);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillOval(cx + 10, cy + 10, 20, 20);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 12, cy + 12, 16, 16);
			g2.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
			g2.fillRect(cx + 63, cy + 25, 7, 2);
			g2.fillRect(cx + 63, cy + 28, 7, 2);
			g2.fillRect(cx + 63, cy + 31, 7, 2);

			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(3));
			g2.drawLine(0, 0, getWidth(), getHeight());
			g2.drawLine(0, getHeight(), getWidth(), 0);

			String str = "No Image";
			FontMetrics metrics = g2.getFontMetrics(getFont());
			int w = metrics.stringWidth(str);
			int h = metrics.getHeight();

			g2.setColor(Color.WHITE);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.drawString(str, (getWidth() - w) / 2, cy - h);
			
		} else {
 			//owner.getGraphics().drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			//g2.clearRect(0, 0, image.getWidth(), image.getHeight());
			int width = bigImage.getWidth();
			int height = bigImage.getHeight();
			g2.clearRect(0, 0, width, height);
			g2.drawImage(bigImage, 0, 0,width,height,null);
			//setBounds(getBounds().x	, getBounds().y, image.getWidth(), image.getHeight());
			//going to draw the smale picture
			if (smallImage != null) {
				int smaleWidth = smallImage.getWidth();
				int smaleHeight = smallImage.getHeight();
				g2.drawImage(smallImage,width - smaleWidth,height-smaleHeight,smaleWidth,smaleHeight,null);
			}
		//	bigImage = null;
			setSize(width, height);
		}
		//draw the smale image
		if (smallImage == null) {
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setBackground(Color.BLACK);
			//draw at the right down
			int width = getWidth();
			int height = getHeight();
			int beginWidth = width - smallImageWidth;
			int beginHeight = height - smallImageHeight;
			
			
			g2.fillRect(beginWidth, beginHeight, smallImageWidth, smallImageHeight);

			int cx = width - smallImageWidth/2 - 35;
			int cy = height - smallImageHeight/2 -20;

			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRoundRect(cx, cy, 70, 40, 10, 10);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 5, cy + 5, 30, 30);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillOval(cx + 10, cy + 10, 20, 20);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 12, cy + 12, 16, 16);
			g2.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
			g2.fillRect(cx + 63, cy + 25, 7, 2);
			g2.fillRect(cx + 63, cy + 28, 7, 2);
			g2.fillRect(cx + 63, cy + 31, 7, 2);

			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(3));
			
			g2.drawLine(beginWidth, beginHeight, width, height);
			g2.drawLine(beginWidth, height, width, beginHeight);
		}else {

			//setBounds(getBounds().x	, getBounds().y, image.getWidth(), image.getHeight());
			//going to draw the smale picture
 			int smaleWidth = smallImage.getWidth();
			int smaleHeight = smallImage.getHeight();
			int width = getPreferredSize().width;
			int height = getPreferredSize().height;
			int beginWidth = width - smaleWidth;
			int beginHeight = height-smaleHeight;
			g2.clearRect(beginWidth, beginHeight, width, height);
			g2.drawImage(smallImage,beginWidth,beginHeight,smaleWidth,smaleHeight,null);
			//smallImage = null;
		}
		
		int buttonHeight = exchange.getHeight();
		int buttonWidth = exchange.getWidth();
		exchange.setLocation(getWidth() - buttonWidth,buttonHeight);
		exchange.paintComponents(g2);
		
 	}

	public void updateBigImage(final BufferedImage image){
		worker.execute(new Runnable() {
			
			@Override
			public void run() {
				if (!swap) {
					bigImage = scaleUpMaker.make(image);
				}else{
					if (smallImage == null) {
						bigImage = null;
					}
					smallImage = scaleDownMaker.make(image);
				}
				repaint();
			}
		});
	}
	
	public void updateSmallImage(final BufferedImage image){
		worker.execute(new Runnable() {
			
			@Override
			public void run() {
				if (!swap) {
					smallImage = scaleDownMaker.make(image);
				}else{
					if (bigImage == null) {
						smallImage = null;
					}
					bigImage = scaleUpMaker.make(image);
				}
				repaint();
			}
		});
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
