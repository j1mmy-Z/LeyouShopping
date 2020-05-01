package com.leyou.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum PayState {
    NOT_PAY(0),SUCCESS(1),FAIL(2);

    int value;

    public int getValue() {
        return this.value;
    }
}
