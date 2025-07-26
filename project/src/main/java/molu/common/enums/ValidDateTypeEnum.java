package molu.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValidDateTypeEnum {

    PERMANENT(0),
    CUSTOM(1) ;

    @Getter
    private final int type;

    public ValidDateTypeEnum getType() {
        return this;
    }

    public static ValidDateTypeEnum of(Integer type) {
        if (type == null) return null;
        for (ValidDateTypeEnum value : values()) {
            if (value.type == type) {
                return value;
            }
        }
        throw new IllegalArgumentException("无效类型: " + type);
    }
}
