package org.unibl.etf.mdp.common.ui;

import javax.swing.SwingWorker;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Runs network calls off the Swing thread and reports failures to the user instead of hiding them. */
public final class Tasks {
    private static final Logger logger = Logger.getLogger(Tasks.class.getName());

    private Tasks() {
    }

    public interface Action {
        void run() throws Exception;
    }

    /** Runs {@code task} in the background, then {@code onSuccess} on the Swing thread, or shows the error. */
    public static <T> void load(Component parent, String failure, Callable<T> task, Consumer<T> onSuccess) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    logger.log(Level.WARNING, failure, cause);
                    BookViews.error(parent, failure + "\n" + cause.getMessage());
                }
            }
        }.execute();
    }

    public static void run(Component parent, String failure, Action task, Runnable onSuccess) {
        load(parent, failure, () -> {
            task.run();
            return null;
        }, ignored -> onSuccess.run());
    }
}
