package com.dbtask;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DbWriterPrinterTest {
    @Test
    void startsTwoRunnables() {
        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        DbWriterPrinter writerPrinter = new DbWriterPrinter(null);
        writerPrinter.setExecutorService(executorService);

        writerPrinter.writeCurrentTime();

        verify(executorService, times(2))
                .scheduleAtFixedRate(any(Runnable.class), eq(1L), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    void addRunnableTest() {
        Queue<Document> documentQueue = mock(Queue.class);
        DbWriterPrinter writerPrinter = new DbWriterPrinter(null);
        writerPrinter.setDocumentQueue(documentQueue);

        writerPrinter.getAddRunnable().run();

        verify(documentQueue, only()).add(any());
    }

    @Test
    void insertRunnableTest() {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        DbWriterPrinter writerPrinter = new DbWriterPrinter(collection);
        Queue<Document> queue = mock(Queue.class);
        Iterator<Document> iterator = mock(Iterator.class);
        when(queue.iterator()).thenReturn(iterator);
        Document document = mock(Document.class);
        when(iterator.next()).thenReturn(document).thenReturn(null);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        writerPrinter.setDocumentQueue(queue);

        writerPrinter.getInsertRunnable().run();

        verify(collection, only()).insertOne(document);
        verify(queue, atMostOnce()).remove(document);
    }

    @Test
    void insertRunnableThrowsExceptionTest() {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        DbWriterPrinter writerPrinter = new DbWriterPrinter(collection);
        Queue<Document> queue = mock(Queue.class);
        Iterator<Document> iterator = mock(Iterator.class);
        when(queue.iterator()).thenReturn(iterator);
        Document document = mock(Document.class);
        doThrow(new RuntimeException()).when(collection).insertOne(document);
        when(iterator.next()).thenReturn(document).thenReturn(null);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        writerPrinter.setDocumentQueue(queue);

        assertDoesNotThrow(() -> writerPrinter.getInsertRunnable().run());
        verify(collection, only()).insertOne(document);
        verify(queue, atMostOnce()).remove(document);
    }

    @Test
    void insertRunnableInterruptedExceptionTest() {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        DbWriterPrinter writerPrinter = new DbWriterPrinter(collection);
        Queue<Document> queue = mock(Queue.class);
        Iterator<Document> iterator = mock(Iterator.class);
        when(queue.iterator()).thenReturn(iterator);
        Document document = mock(Document.class);
        doThrow(new RuntimeException()).when(collection).insertOne(document);
        when(iterator.next()).thenReturn(document).thenReturn(null);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        writerPrinter.setDocumentQueue(queue);
        Thread.currentThread().interrupt();
        assertDoesNotThrow(() -> writerPrinter.getInsertRunnable().run());
    }

    @Test
    void listAllTimeEntries() {
        Document document = mock(Document.class);
        ArgumentCaptor<Consumer<Document>> captor = ArgumentCaptor.forClass(Consumer.class);
        MongoCollection<Document> collection = mock(MongoCollection.class);
        FindIterable<Document> iterable = mock(FindIterable.class);
        when(collection.find()).thenReturn(iterable);
        DbWriterPrinter writerPrinter = new DbWriterPrinter(collection);

        writerPrinter.listAllTimeEntries();

        verify(collection, only()).find();
        verify(iterable, only()).forEach(captor.capture());
        Consumer<Document> consumer = captor.getValue();
        assertDoesNotThrow(() -> consumer.accept(document));
    }
}