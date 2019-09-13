package com.github.sarxos.webcam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;


/**
 * Deallocator which goal is to release all devices resources when SIGTERM
 * signal is detected.
 *
 * @author Bartosz Firyn (SarXos)
 */
final class WebcamDeallocator {

    private static final WebcamDeallocator HANDLER = new WebcamDeallocator(new Webcam[0]);

    private final List<Webcam> webcams = new ArrayList<>();

    /**
     * This constructor is used internally to create new deallocator for the
     * given devices array.
     *
     * @param devices the devices to be stored in deallocator
     */
    private WebcamDeallocator(Webcam[] devices) {
        this.webcams.addAll(asList(devices));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                deallocate();
            }
        });
    }

    /**
     * Store devices to be deallocated when TERM signal has been received.
     *
     * @param webcams the webcams array to be stored in deallocator
     */
    static void store(Webcam[] webcams) {
        HANDLER.webcams.addAll(asList(webcams));
    }

    static void unstore() {
        HANDLER.webcams.clear();
    }

    void deallocate() {
        for (Webcam w : webcams) {
            try {
                w.dispose();
            } catch (Throwable t) {
                caugh(t);
            }
        }
    }

    private void caugh(Throwable t) {
        File f = new File(String.format("webcam-capture-hs-%s", System.currentTimeMillis()));
        PrintStream ps = null;
        try {
            t.printStackTrace(ps = new PrintStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

}
