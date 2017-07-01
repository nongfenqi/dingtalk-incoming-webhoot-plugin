package com.nongfenqi.rundeck.plugins.dingtalk;

/**
 * Created by Chenlm on 01/07/2017.
 */
public class DingtalkNotificationPluginException extends RuntimeException {
    /**
     * Constructor.
     *
     * @param message error message
     */
    public DingtalkNotificationPluginException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message error message
     * @param cause   exception cause
     */
    public DingtalkNotificationPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}
