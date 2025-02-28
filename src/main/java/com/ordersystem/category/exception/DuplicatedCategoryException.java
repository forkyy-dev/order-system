package com.ordersystem.category.exception;

import com.ordersystem.common.exception.ApplicationException;

public class DuplicatedCategoryException extends ApplicationException {
    public DuplicatedCategoryException(String categoryName) {
        super(String.format("동일한 이름의 카테고리가 존재합니다. - 입력값: { %s }", categoryName));
    }
}
