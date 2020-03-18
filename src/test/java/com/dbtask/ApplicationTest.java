package com.dbtask;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.SAME_THREAD)
class ApplicationTest {
    private static final long WAIT = 30000;
    private static final String[] EMPTY_ARGS = new String[0];
    private static final RuntimeException EXCEPTION = new RuntimeException("Something's happened to our precious database!");
    private DbWriterPrinter writerPrinter = mock(DbWriterPrinter.class);

    @Test
    void mainWithParameter() {
       Application.setWriterPrinter(writerPrinter);
       Application.main(new String[]{"-p"});
       verify(writerPrinter, only()).listAllTimeEntries();
    }

    @Test
    void mainWithoutParameter() {
       Application.setWriterPrinter(writerPrinter);
       Application.main(EMPTY_ARGS);
       verify(writerPrinter, only()).writeCurrentTime();
    }

    @Test
    void mainWrongParameters() {
       assertThrows(RuntimeException.class, () -> Application.main(new String[]{"-z"}));
    }

    @Disabled
    @Test
    void regularShowcaseWithRealDb() throws InterruptedException {
        Application.main(EMPTY_ARGS);
        Thread.sleep(WAIT);
    }

    @Disabled
    @Test
    void regularShowcaseWithMockDb() throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doNothing()
                .when(collection)
                .insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        Application.main(EMPTY_ARGS);
        Thread.sleep(WAIT);
    }

    @Disabled
    @Test
    void slowConnectionShowcase() throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doAnswer(new AnswersWithDelay(10000, Answers.CALLS_REAL_METHODS))
                .when(collection).insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        Application.main(EMPTY_ARGS);
        Thread.sleep(WAIT);
    }

    @Disabled
    @Test
    void dbUnavailableShowcase() throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doThrow(EXCEPTION)
                .when(collection)
                .insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        Application.main(EMPTY_ARGS);
        Thread.sleep(WAIT);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("answerProvider")
    void dbChangesStatesShowcase(Answer<?> answer) throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doAnswer(answer)
                .when(collection)
                .insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        Application.main(EMPTY_ARGS);
        Thread.sleep(WAIT);
    }

    private static Stream<Arguments> answerProvider() {
        return Stream.of(
                //Db is unavailable, then quickly does a few queries, then breaks again
                Arguments.of(new Answer<>() {
                    private int count;

                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        count++;
                        if (count < 5) {
                            throw EXCEPTION;
                        } else if (count < 8) {
                            return null;
                        }

                        throw EXCEPTION;
                    }
                }),
                //Db is available, then breaks, then available again
                Arguments.of(new Answer<>() {
                    private int count;

                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        count++;

                        if (count < 5) {
                            return null;
                        } else if (count < 8) {
                            throw EXCEPTION;
                        }

                        return null;
                    }
                })
        );
    }
}