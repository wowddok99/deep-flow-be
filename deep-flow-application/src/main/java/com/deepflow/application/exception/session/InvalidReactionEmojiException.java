package com.deepflow.application.exception.session;

import com.deepflow.application.exception.CustomException;
import com.deepflow.application.exception.ErrorCode;

public class InvalidReactionEmojiException extends CustomException {
    public InvalidReactionEmojiException() {
        super(ErrorCode.INVALID_REACTION_EMOJI);
    }
}
