package com.sap.bookshop.utils;

import java.util.ArrayList;
import java.util.List;

import static com.sap.bookshop.utils.Constants.CHANGESET;
import static com.sap.bookshop.utils.Constants.CRLF;

public class ChangeSetBatchEntry implements BatchEntry {

    private List<ChangeSetEntry> entries = new ArrayList<>();
    private String number;

    public ChangeSetBatchEntry(String number) {
        this.number = number;
    }

    @Override
    public String getPayload(String batchNumber) {
        StringBuilder builder = new StringBuilder();
        builder.append("--batch_" + batchNumber + CRLF);
        builder.append("Content-Type: multipart/mixed; boundary=changeset_" + number + CRLF);
        builder.append(CRLF);
        builder.append(getPayload());
        builder.append("" + CRLF);
        builder.append("" + CRLF);
        return builder.toString();
    }

    public void add(ChangeSetEntry entry) {
        entries.add(entry);
    }

    public String getPayload() {
        StringBuilder builder = new StringBuilder();
        entries.stream().forEach(e -> {
            builder.append(e.getPayload(number));
        });
        builder.append(CHANGESET + number + "--" + CRLF);
        return builder.toString();
    }

    public String getNumber() {
        return number;
    }
}
