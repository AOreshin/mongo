package com.dbtask;

import com.mongodb.client.MongoCollection;
import com.typesafe.config.ConfigFactory;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class DbWriterPrinter {
    private static final int TIMEOUT = ConfigFactory.load().getInt("mongoTimeout");
    private MongoCollection<Document> collection;
    private Queue<Document> documentQueue = new ConcurrentLinkedQueue<>();
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    DbWriterPrinter(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    void writeCurrentTime() {
        executorService.scheduleAtFixedRate(getAddRunnable(), 1, 1, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(getInsertRunnable(), 1, 1, TimeUnit.SECONDS);
    }

    private Document getCurrentTimeDocument() {
        return new Document().append("time", LocalDateTime.now());
    }

    void listAllTimeEntries() {
        collection
                .find()
                .forEach((Consumer<? super Document>) document ->
                        System.out.println(document.get("time"))
                );
    }

    /**
     * Only for testing
     */
    void setDocumentQueue(Queue<Document> documentQueue) {
        this.documentQueue = documentQueue;
    }

    /**
     * Only for testing
     */
    void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Only for testing
     */
    Runnable getAddRunnable() {
        return () -> {
            Document document = getCurrentTimeDocument();
            System.out.println("Adding " + document + " to the queue");
            documentQueue.add(document);
        };
    }

    /**
     * Only for testing
     */
    Runnable getInsertRunnable() {
        return () -> {
            try {
                for (Document document: documentQueue) {
                    System.out.println("Inserting " + document);
                    collection.insertOne(document);
                    documentQueue.remove(document);
                }
            } catch (Exception e) {
                System.out.println("Failed to insert document. Waiting " + TIMEOUT + " ms for the next try");

                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
