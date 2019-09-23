package com.sap.bookshop.utils;

import java.util.ArrayList;
import java.util.List;

import static com.sap.bookshop.utils.Constants.*;

public class Batch {
    private List<BatchEntry> entries = new ArrayList<>();
    private String number;

    public Batch(String number) {
        this.number = number;
    }

    public void add(BatchEntry batchEntry) {
        entries.add(batchEntry);
    }

    public String getPayload() {
        StringBuilder builder = new StringBuilder();
        entries.stream().forEach(e -> {
            builder.append(e.getPayload(number));
        });
        builder.append(BATCH + number + "--" + CRLF);
        return builder.toString();

    }
}
