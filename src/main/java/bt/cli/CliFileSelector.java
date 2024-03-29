
package bt.cli;

import bt.metainfo.TorrentFile;
import bt.torrent.fileselector.SelectionResult;
import bt.torrent.fileselector.TorrentFileSelector;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CliFileSelector extends TorrentFileSelector {
    private static final String PROMPT_MESSAGE_FORMAT = "Download '%s'? (hit <Enter> or type 'y' to confirm or type 'n' to skip)";
    private static final String ILLEGAL_KEYPRESS_WARNING = "*** Invalid key pressed. Please, use only <Enter>, 'y' or 'n' ***";

    private AtomicReference<Thread> currentThread;
    private AtomicBoolean shutdown;

    public CliFileSelector() {
        this.currentThread = new AtomicReference<>(null);
        this.shutdown = new AtomicBoolean(false);
    }

    @Override
    protected SelectionResult select(TorrentFile file) {
        while (!shutdown.get()) {
            System.out.println(getPromptMessage(file));

            try {
                switch (readNextCommand(new Scanner(System.in))) {
                    case "":
                    case "y":
                    case "Y": {
                        return SelectionResult.select().build();
                    }
                    case "n":
                    case "N": {
                        System.out.println("Skipping...");
                        return SelectionResult.skip();
                    }
                    default: {
                        System.out.println(ILLEGAL_KEYPRESS_WARNING);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalStateException("Shutdown");
    }

    private String readNextCommand(Scanner scanner) throws IOException {
        currentThread.set(Thread.currentThread());
        try {
            return scanner.nextLine().trim();
        } finally {
            currentThread.set(null);
        }
    }

    private static String getPromptMessage(TorrentFile file) {
        return String.format(PROMPT_MESSAGE_FORMAT, String.join("/", file.getPathElements()));
    }

    public void shutdown() {
        this.shutdown.set(true);
        Thread currentThread = this.currentThread.get();
        if (currentThread != null) {
            currentThread.interrupt();
        }
    }
}
