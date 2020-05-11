package com.github.sarxos.webcam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class WebcamProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(WebcamProcessor.class);

    /**
     * Thread doing supersync processing.
     *
     * @author sarxos
     */
    public static final class ProcessorThread extends Thread {

        private static final AtomicInteger N = new AtomicInteger(0);

        public ProcessorThread(Runnable r) {
            super(r, String.format("atomic-processor-%d", N.incrementAndGet()));
        }
    }

    /**
     * Thread factory for processor.
     *
     * @author Bartosz Firyn (SarXos)
     */
    private static final class ProcessorThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new ProcessorThread(r);
            t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
            t.setDaemon(true);
            t.setName(String.format("WebCamThread-%d", count.getAndIncrement()));
            return t;
        }
    }

    /**
     * Heart of overall processing system. This class process all native calls wrapped in tasks, by
     * doing this all tasks executions are super-synchronized.
     *
     * @author Bartosz Firyn (SarXos)
     */
    private static final class AtomicProcessor implements Runnable {

        private SynchronousQueue<WebcamTask> inbound = new SynchronousQueue<WebcamTask>(true);
        private SynchronousQueue<WebcamTask> outbound = new SynchronousQueue<WebcamTask>(true);

        /**
         * Process task.
         *
         * @param task the task to be processed
         * @throws InterruptedException when thread has been interrupted
         */
        public void process(WebcamTask task) throws InterruptedException {
            if (task == null) {
                throw new IllegalArgumentException("null WebcamTask");
            }
            if (inbound.offer(task, 3, TimeUnit.SECONDS)) {

                Throwable t = outbound.take().getThrowable();
                if (t != null) {
                    throw new WebcamException("Cannot execute task", t);
                }
            } else {
                LOG.debug("task not accepted");
            }
        }

        @Override
        public void run() {
            final AtomicBoolean running = new AtomicBoolean(true);
            while (running.get()) {
                WebcamTask t = null;
                try {
                    (t = inbound.take()).handle();
                } catch (InterruptedException e) {
                    LOG.debug("handle() interrupted and will now exit", e);
                    running.set(false);
                } catch (Throwable e) {
                    if (t != null) {
                        t.setThrowable(e);
                    }
                } finally {
                    if (t != null) {
                        try {
                            outbound.put(t);
                        } catch (InterruptedException e) {
                            LOG.debug("thread interrupted and will now exit", e);
                            running.set(false);
                        } catch (Exception e) {
                            running.set(false);
                            LOG.debug("exception while processing output", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Is processor started?
     */
    private static final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Execution service.
     */
    private static ExecutorService runner = null;

    /**
     * Static processor.
     */
    private static final AtomicProcessor processor = new AtomicProcessor();

    /**
     * Singleton instance.
     */
    private static final WebcamProcessor INSTANCE = new WebcamProcessor();

    private final ProcessorThreadFactory processorThreadFactory = new ProcessorThreadFactory();

    private WebcamProcessor() {
    }

    /**
     * Process single webcam task.
     *
     * @param task the task to be processed
     * @throws InterruptedException when thread has been interrupted
     */
    public void process(WebcamTask task) throws InterruptedException {

        if (started.compareAndSet(false, true)) {
            runner = Executors.newSingleThreadExecutor(processorThreadFactory);
            runner.execute(processor);
        }

        if (!runner.isShutdown()) {
            processor.process(task);
        } else {
            throw new RejectedExecutionException("Cannot process because processor runner has been already shut down");
        }
    }

    public void shutdown() {
        if (started.compareAndSet(true, false)) {

            LOG.debug("Shutting down webcam processor");

            runner.shutdown();

            LOG.debug("Awaiting tasks termination");

            while (runner.isTerminated()) {

                try {
                    runner.awaitTermination(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    return;
                }

                runner.shutdownNow();
            }

            LOG.debug("All tasks has been terminated");
        }

    }

    public static synchronized WebcamProcessor getInstance() {
        return INSTANCE;
    }
}
