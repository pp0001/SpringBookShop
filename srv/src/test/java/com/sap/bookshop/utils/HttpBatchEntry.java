package com.sap.bookshop.utils;

import org.springframework.http.HttpMethod;

import java.security.cert.CRL;

import static com.sap.bookshop.utils.Constants.*;

public class HttpBatchEntry implements BatchEntry {
    private String httpMethod;
    private String url;

    public HttpBatchEntry(String httpMethod, String url) {
        this.httpMethod = httpMethod;
        this.url = url;
    }

    @Override
    public String getPayload(String batchNumber) {
        StringBuilder builder = new StringBuilder();
        builder.append(BATCH + batchNumber + CRLF);
        builder.append("Content-Type: application/http" + CRLF);
        builder.append("Content-Transfer-Encoding: binary" + CRLF);
        builder.append(CRLF);
        builder.append(httpMethod + " " + url + " " + "HTTP/1.1" + CRLF);
        builder.append("sap-cancel-on-close: true" + CRLF);
        builder.append("sap-contextid-accept: header" + CRLF);
        builder.append("Accept: application/json" + CRLF);
        builder.append("x-csrf-token: foFy4UDm-q07u1Op3lYG51ci6Y0El4uhSt8M" + CRLF);
        builder.append("Accept-Language: en-US" + CRLF);
        builder.append("DataServiceVersion: 2.0" + CRLF);
        builder.append("MaxDataServiceVersion: 2.0" + CRLF);
        builder.append("" + CRLF);
        builder.append("" + CRLF);
        return builder.toString();
    }
}
