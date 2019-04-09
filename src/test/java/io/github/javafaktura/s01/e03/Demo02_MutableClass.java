package io.github.javafaktura.s01.e03;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"squid:S00101"})
class Demo02_MutableClass {

    private Counter counter = new SimpleCounter();

    @Test
    void shouldIncrementCounter100kTimes() throws InterruptedException {

        // when: incrementing counter 100k times in 20 concurrent threads
        Collection<Thread> threads = new ArrayList<>();
        for (int threadIdx = 0; threadIdx < 20; threadIdx++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < 100_000 / 20; i++) {
                    counter.increment();
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // then: counter value should be 100k
        assertEquals(100_000, counter.total(),
                "Counter wasn't incremented expected number of times");
    }
}

interface Counter {
    void increment();
    int total();
}

class SimpleCounter implements Counter {

    private int count = 0;

    @Override
    public void increment() {
        count++;
    }

    @Override
    public int total() {
        return count;
    }
}













































class SynchronizedCounter implements Counter {

    private int count = 0;

    // it's thread-safe because the entire method is synchronized
    @Override
    public synchronized void increment() {
        count++;
    }

    @Override
    public int total() {
        return count;
    }
}

class AtomicCounter implements Counter {

    // it's thread-safe because AtomicX classes use CAS algorithm
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    @Override
    public int total() {
        return count.get();
    }
}