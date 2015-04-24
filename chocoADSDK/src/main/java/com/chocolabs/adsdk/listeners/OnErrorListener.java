package com.chocolabs.adsdk.listeners;

public interface OnErrorListener {
    void onException(Throwable throwable);
    void onFail(String reason);
}