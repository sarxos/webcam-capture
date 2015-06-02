package com.github.sarxos.webcam;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MultipointMotionDetectionExample implements WebcamMotionListener, WebcamPanel.Painter {

    public static void main(String[] args) throws InterruptedException {
        new MultipointMotionDetectionExample();
    }

    private static final int INTERVAL = 100; // ms

    public static Webcam webcam;
    public static WebcamPanel.Painter painter = null;

    public MultipointMotionDetectionExample(){
        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setPreferredSize(WebcamResolution.VGA.getSize());
        panel.setPainter(this);
        panel.setFPSDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(20);
        panel.setPainter(this);
        panel.start();

        painter = panel.getDefaultPainter();

        JFrame window = new JFrame("Multipoint-motion detection");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);


        WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

        //Sets the max amount of motion points to 300 and the minimum range between them to 40
        detector.setMaxMotionPoints(300);
        detector.setPointRange(40);

        detector.setInterval(INTERVAL);
        detector.addMotionListener(this);

        detector.start();
    }


    //A HashMap to store all the current points and the current amount of times it has been rendered
    //Time rendered is used to remove the point after a certain amount of time
    public static HashMap<Point, Integer> motionPoints = new HashMap<Point, Integer>();

    //Gets the motion points from the motion detector and adds it to the HashMap
    @Override
    public void motionDetected(WebcamMotionEvent wme) {
        for(Point p : wme.getPoints()){
            motionPoints.put(p, 0);
        }
    }

    @Override
    public void paintPanel(WebcamPanel panel, Graphics2D g2) {
        if (painter != null) {
            painter.paintPanel(panel, g2);
        }
    }


    //Used to render the effect for the motion points
    private static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);

    //The amount of time each point should be rendered for before being removed
    public static final int renderTime = 3;

    //The actual size of the rendered effect for each point
    public static final int renderSize = 20;

    @Override
    public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {


        if (painter != null) {
            painter.paintImage(panel, image, g2);
        }

        //Gets all the points and updates the amount of time they have been rendered for
        //And removes the ones that exceed the renderTime variable

        ArrayList<Point> rem = new ArrayList<Point>();

        for (Map.Entry<Point, Integer> ent : motionPoints.entrySet()) {
            Point p = ent.getKey();

            if (ent.getValue() != null) {
                int tt = ent.getValue();
                if (tt >= renderTime) {
                    rem.add(ent.getKey());

                } else {
                    int temp = ent.getValue() + 1;
                    motionPoints.put(p, temp);
                }

            }
        }

        for(Point p : rem){
            motionPoints.remove(p);
        }


        //Gets all the remaining points after removing the exceeded ones and then renders the current ones as a red square
        for(Map.Entry<Point, Integer> ent : motionPoints.entrySet()){
            Point p = ent.getKey();

            int xx = p.x - (renderSize / 2), yy = p.y - (renderSize / 2);

            Rectangle bounds = new Rectangle(xx, yy, renderSize, renderSize);

            int dx = (int) (0.1 * bounds.width);
            int dy = (int) (0.2 * bounds.height);
            int x = (int) bounds.x - dx;
            int y = (int) bounds.y - dy;
            int w = (int) bounds.width + 2 * dx;
            int h = (int) bounds.height + dy;

            g2.setStroke(STROKE);
            g2.setColor(Color.RED);
            g2.drawRect(x, y, w, h);

        }

    }
}
