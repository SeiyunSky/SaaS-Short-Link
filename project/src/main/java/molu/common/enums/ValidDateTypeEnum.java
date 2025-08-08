package molu.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValidDateTypeEnum {

    PERMANENT(0),
    CUSTOM(1) ;

    @Getter
    private final int type;
}
