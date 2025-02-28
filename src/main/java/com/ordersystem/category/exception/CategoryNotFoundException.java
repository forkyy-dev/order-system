package com.ordersystem.category.exception;

import com.ordersystem.common.exception.ApplicationException;

public class CategoryNotFoundException extends ApplicationException {

    public CategoryNotFoundException(Long id) {
        super(String.format("해당 id의 카테고리를 찾을 수 없습니다. - 입력값: { %s }", id));
    }
}
