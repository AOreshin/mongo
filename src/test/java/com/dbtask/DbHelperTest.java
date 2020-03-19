package com.dbtask;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DbHelperTest {
    @Test
    void collectionIsSortedAscending() {
        Spliterator<LocalDateTime> spliterator = new DbHelper()
                .getCollection()
                .find()
                .map(document -> (Date) document.get("time"))
                .map(date -> LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
                .spliterator();

        List<LocalDateTime> list = StreamSupport
                .stream(spliterator, false)
                .collect(Collectors.toList());

        assertTrue(list.size() != 0);
        assertTrue(isSorted(list));
    }

    private <T extends Comparable<? super T>> boolean isSorted(List<T> array){
        for (int i = 0; i < array.size() - 1; i++) {
            if (array.get(i).compareTo(array.get(i + 1)) > 0) {
                return false;
            }
        }

        return true;
    }
}