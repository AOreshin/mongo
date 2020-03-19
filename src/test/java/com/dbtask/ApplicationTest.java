package com.dbtask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.SAME_THREAD)
class ApplicationTest {
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
       Application.main(new String[0]);
       verify(writerPrinter, only()).writeCurrentTime();
    }

    @Test
    void mainWrongParameters() {
       assertThrows(RuntimeException.class, () -> Application.main(new String[]{"-z"}));
    }

    @Test
    void mainWrongParametersSize() {
       assertThrows(RuntimeException.class, () -> Application.main(new String[]{"-p", "-z"}));
    }
}