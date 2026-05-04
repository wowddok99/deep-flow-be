package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SearchTypeInvalidException extends CustomException {
    public SearchTypeInvalidException() {
        super(ErrorCode.SEARCH_TYPE_INVALID);
    }
}
