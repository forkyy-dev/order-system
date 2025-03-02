package com.ordersystem.common.utils;

import com.ordersystem.order.domain.StatusChangeReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusChangeReasonConverter implements AttributeConverter<StatusChangeReason, String> {
    @Override
    public String convertToDatabaseColumn(StatusChangeReason attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("StatusChangeReason이 NULL입니다");
        }

        return attribute.getCode();
    }

    @Override
    public StatusChangeReason convertToEntityAttribute(String code) {
        if (code == null) {
            throw new IllegalArgumentException("StatusChangeReason의 code가 NULL입니다");
        }

        return StatusChangeReason.getByCode(code);
    }
}
