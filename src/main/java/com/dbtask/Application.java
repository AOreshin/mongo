package com.dbtask;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Arrays;

public class Application {
    private Application() {}

    private static MongoCollection<Document> collection = new DbHelper().getCollection();
    private static DbWriterPrinter writerPrinter = new DbWriterPrinter(collection);

    public static void main(String[] args) {
        if (args.length == 0) {
            writerPrinter.writeCurrentTime();
        } else if (args.length == 1 && args[0].equals("-p")) {
            writerPrinter.listAllTimeEntries();
        } else {
            throw new RuntimeException("Parameters " + Arrays.toString(args) + " are not supported");
        }
    }

    /**
     * Only for testing
     */
    static void setWriterPrinter(DbWriterPrinter writerPrinter) {
        Application.writerPrinter = writerPrinter;
    }
}
