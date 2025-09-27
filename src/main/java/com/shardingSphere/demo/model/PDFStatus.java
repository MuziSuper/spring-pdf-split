package com.shardingSphere.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum PDFStatus {
    PROCESSING(0,"processing"),
    SUCCESS(1, "success"),
    FAILED(2,"failed");
    private final Integer code;
    private final String message;
    private static final Map<PDFStatus, Set<PDFStatus>> STATE_TRANSITIONS = Map.of(
            PROCESSING, Set.of(SUCCESS, FAILED),
            SUCCESS, Set.of(),
            FAILED, Set.of(PROCESSING)
    );

    public boolean canTransferTo(PDFStatus target) {
        return STATE_TRANSITIONS.get(this).contains(target);
    }
    public static PDFStatus of(Integer code) {
        for (PDFStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
