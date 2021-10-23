package me.shaposhnik.hlrbot.files.exception;

import me.shaposhnik.hlrbot.exception.BaseException;

public class DownloadFileException extends BaseException {

    @Override
    public String getMessageKey() {
        return "exception.download-file";
    }

    public DownloadFileException(Throwable cause) {
        super(cause);
    }
}
