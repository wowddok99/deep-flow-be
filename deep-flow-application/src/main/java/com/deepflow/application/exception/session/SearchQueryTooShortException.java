package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class SearchQueryTooShortException extends CustomException {
    public SearchQueryTooShortException() {
        super(ErrorCode.SEARCH_QUERY_TOO_SHORT);
    }
}
