package org.patriques.input.technicalindicators;

import org.patriques.input.ApiParameter;

public enum SlowKMaType implements ApiParameter {
    SMA(0),
    EMA(1),
    WMA(2),
    DEMA(3),
    TEMA(4),
    TRIMA(5),
    T3(6),
    KAMA(7),
    MAMA(8);

    private final int type;

    SlowKMaType(int type) {
        this.type = type;
    }

    @Override
    public String getKey() {
        return "slowkmatype";
    }

    @Override
    public String getValue() {
        return String.valueOf(type);
    }
}