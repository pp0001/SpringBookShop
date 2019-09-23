package com.sap.bookshop.utils;

import java.security.PrivateKey;

import static com.sap.bookshop.utils.Constants.*;

public class ChangeSetEntry {
    private String body;
    private String httpMethod;
    private String entity;

    public ChangeSetEntry(String body, String httpMethod, String entity) {
        this.body = body;
        this.httpMethod = httpMethod;
        this.entity = entity;
    }


    public String getPayload(String changeSetNumber) {
        String entry;
        StringBuilder builder = new StringBuilder();
        builder.append(CHANGESET + changeSetNumber + CRLF);
        builder.append("Content-Type: application/http" + CRLF +
                "Content-Transfer-Encoding: binary" + CRLF);
        builder.append(CRLF);
        builder.append(httpMethod + " " + entity + " HTTP/1.1" + CRLF);
        builder.append("Content-Type: application/json" + CRLF +
                "sap-contextid-accept: header" + CRLF +
                "Accept: application/json" + CRLF +
                "x-csrf-token: j7CZhMts-OdeMKMCd8zGT2E_8d6dXFvOS5uw" + CRLF +
                "Accept-Language: en-US" + CRLF +
                "DataServiceVersion: 2.0" + CRLF +
                "MaxDataServiceVersion: 2.0" + CRLF);
        builder.append("Content-Length: " + body.length() + CRLF);
        builder.append(CRLF);
        builder.append(body);
        builder.append(CRLF);
        return builder.toString();
    }
}
