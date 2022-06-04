package org.schabi.newpipe.extractor.streamdata.stream.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class NewPipeStreamCollectors {
    private NewPipeStreamCollectors() {
        // No impl
    }

    public static <T extends org.schabi.newpipe.extractor.streamdata.stream.Stream>
        Collector<T, ?, List<T>> toDistinctList() {
        return deduplicateEqualStreams(x -> x);
    }

    public static <T extends org.schabi.newpipe.extractor.streamdata.stream.Stream>
        Collector<T, ?, Stream<T>> toDistinctStream() {
        return deduplicateEqualStreams(List::stream);
    }

    public static <T extends org.schabi.newpipe.extractor.streamdata.stream.Stream, R>
        Collector<T, ?, R> deduplicateEqualStreams(final Function<List<T>, R> finisher) {
        return new CollectorImpl<>(
                (Supplier<List<T>>) ArrayList::new,
                List::add,
                (left, right) -> {
                    for(final T rightElement : right) {
                        if (NewPipeStreamUtil.containSimilarStream(rightElement, left)) {
                            left.add(rightElement);
                        }
                    }
                    return left;
                },
                finisher,
                CH_ID);
    }

    /**
     * Copied from {@link java.util.stream.Collectors}
     */
    static final Set<Collector.Characteristics> CH_ID =
            Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

    /**
     * Copied from {@link java.util.stream.Collectors}
     */
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(final Supplier<A> supplier,
                      final BiConsumer<A, T> accumulator,
                      final BinaryOperator<A> combiner,
                      final Function<A,R> finisher,
                      final Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }
}
