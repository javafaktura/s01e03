package io.github.javafaktura.s01.e03;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"squid:S00101"})
class DemoAppendix01_ExceptionsInThreads {

    @Test
    void exceptionsInNewThreads() {
        Thread thread = new Thread(this::throwException);

        // that doesn't change anything
//        thread.setUncaughtExceptionHandler((t, e) -> {
//            throw new RuntimeException(e);
//        });

        assertThrows(IllegalStateException.class, () -> {
                    thread.start();
                    thread.join();
                },
                "Expected exception wasn't propagated to main thread");
    }

    private void throwException() {
        throw new IllegalStateException("Test Exception");
    }
}
