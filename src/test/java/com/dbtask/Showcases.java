package com.dbtask;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@Execution(ExecutionMode.SAME_THREAD)
class Showcases {
    private static final RuntimeException EXCEPTION = new RuntimeException("Something's happened to our precious database!");
    private static final long WAIT = 30000;
    private static final String[] EMPTY_ARGS = new String[0];

    @Test
    void regularShowcaseWithRealDbTest() throws InterruptedException {
        launchAppAndWait();
    }

    @Test
    void regularShowcaseWithMockDb() throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doNothing()
                .when(collection)
                .insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        launchAppAndWait();
    }

    @Test
    void slowConnectionShowcase() throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doAnswer(new AnswersWithDelay(10000, Answers.CALLS_REAL_METHODS))
                .when(collection).insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        launchAppAndWait();
    }

    @Test
    void dbUnavailableShowcase() throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doThrow(EXCEPTION)
                .when(collection)
                .insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        launchAppAndWait();
    }

    @ParameterizedTest
    @MethodSource("answerProvider")
    void dbChangesStatesShowcase(Answer<?> answer) throws InterruptedException {
        MongoCollection<Document> collection = mock(MongoCollection.class);
        doAnswer(answer)
                .when(collection)
                .insertOne(any());
        Application.setWriterPrinter(new DbWriterPrinter(collection));

        launchAppAndWait();
    }

    private static Stream<Arguments> answerProvider() {
        return Stream.of(
                //Db is unavailable for 10 seconds, then works for 10 seconds, then breaks again
                Arguments.of(new Answer<>() {
                    private LocalDateTime start = now();

                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        if (isNotElapsed(start, now(), Duration.ofSeconds(10))) {
                            throw EXCEPTION;
                        } else if (isNotElapsed(start, now(), Duration.ofSeconds(20))) {
                            return null;
                        }

                        throw EXCEPTION;
                    }
                }),
                //Db is available for 10 seconds, then breaks for 10 seconds, then available again
                Arguments.of(new Answer<>() {
                    private LocalDateTime start = now();

                    @Override
                    public Object answer(InvocationOnMock invocation) {
                        if (isNotElapsed(start, now(), Duration.ofSeconds(10))) {
                            return null;
                        } else if (isNotElapsed(start, now(), Duration.ofSeconds(20))) {
                            throw EXCEPTION;
                        }

                        return null;
                    }
                })
        );
    }

    private static boolean isNotElapsed(LocalDateTime start, LocalDateTime now, Duration duration) {
        return Duration.between(start, now).compareTo(duration) < 0;
    }

    private void launchAppAndWait() throws InterruptedException {
        Application.main(EMPTY_ARGS);
        Thread.sleep(WAIT);
    }
}
