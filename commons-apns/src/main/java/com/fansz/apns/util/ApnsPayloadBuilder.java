package com.fansz.apns.util;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * <p>
 * 用于创建Payload的工具类，注意该类是线程不安全的
 * </p>
 *
 * @see <a
 *      href="http://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW1">
 *      Local and Push Notification Programming Guide - Apple Push Notification Service - The Notification Payload</a>
 */
public class ApnsPayloadBuilder {

    private String alertBody = null;

    private String localizedAlertKey = null;

    private String[] localizedAlertArguments = null;

    private String alertTitle = null;

    private String localizedAlertTitleKey = null;

    private String[] localizedAlertTitleArguments = null;

    private String launchImageFileName = null;

    private boolean showActionButton = true;

    private String localizedActionButtonKey = null;

    private Integer badgeNumber = null;

    private String soundFileName = null;

    private String categoryName = null;

    private boolean contentAvailable = false;

    private static final String APS_KEY = "aps";

    private static final String ALERT_KEY = "alert";

    private static final String BADGE_KEY = "badge";

    private static final String SOUND_KEY = "sound";

    private static final String CATEGORY_KEY = "category";

    private static final String CONTENT_AVAILABLE_KEY = "content-available";

    private static final String ALERT_TITLE_KEY = "title";

    private static final String ALERT_BODY_KEY = "body";

    private static final String ALERT_TITLE_LOC_KEY = "title-loc-key";

    private static final String ALERT_TITLE_ARGS_KEY = "title-loc-args";

    private static final String ACTION_LOC_KEY = "action-loc-key";

    private static final String ALERT_LOC_KEY = "loc-key";

    private static final String ALERT_ARGS_KEY = "loc-args";

    private static final String LAUNCH_IMAGE_KEY = "launch-image";

    private final HashMap<String, Object> customProperties = new HashMap<String, Object>();

    private static final int DEFAULT_PAYLOAD_SIZE = 2048;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * 推送通知默认的声音 ({@value DEFAULT_SOUND_FILENAME}).
     *
     * @see ApnsPayloadBuilder#setSoundFileName(String)
     */
    public static final String DEFAULT_SOUND_FILENAME = "default";

    public ApnsPayloadBuilder() {
    }

    /**
     * 设置推送展现的消息，默认不展示消息
     *
     * @see ApnsPayloadBuilder#setLocalizedAlertMessage(String, String[])
     */
    public ApnsPayloadBuilder setAlertBody(final String alertBody) {
        if (alertBody != null && this.localizedAlertKey != null) {
            throw new IllegalStateException(
                    "Cannot set a literal alert body when a localized alert key has already been set.");
        }

        this.alertBody = alertBody;

        return this;
    }

    /**
     * 设置推送展示的消息体，通过知道消息的KEY和参数实现
     */
    public ApnsPayloadBuilder setLocalizedAlertMessage(final String localizedAlertKey, final String[] alertArguments) {
        if (localizedAlertKey != null && this.alertBody != null) {
            throw new IllegalStateException(
                    "Cannot set a localized alert key when a literal alert body has already been set.");
        }

        if (localizedAlertKey == null && alertArguments != null) {
            throw new IllegalArgumentException(
                    "Cannot set localized alert arguments without a localized alert message key.");
        }

        this.localizedAlertKey = localizedAlertKey;
        this.localizedAlertArguments = alertArguments;

        return this;
    }

    /**
     * 设置消息标题
     *
     * @see ApnsPayloadBuilder#setLocalizedAlertTitle(String, String[])
     */
    public ApnsPayloadBuilder setAlertTitle(final String alertTitle) {
        if (alertTitle != null && this.localizedAlertTitleKey != null) {
            throw new IllegalStateException(
                    "Cannot set a literal alert title when a localized alert title key has already been set.");
        }

        this.alertTitle = alertTitle;

        return this;
    }

    /**
     * 设置本地化消息标题，通过设置KEY和参数实现
     */
    public ApnsPayloadBuilder setLocalizedAlertTitle(final String localizedAlertTitleKey,
            final String[] alertTitleArguments) {
        if (localizedAlertTitleKey != null && this.alertTitle != null) {
            throw new IllegalStateException(
                    "Cannot set a localized alert key when a literal alert body has already been set.");
        }

        if (localizedAlertTitleKey == null && alertTitleArguments != null) {
            throw new IllegalArgumentException(
                    "Cannot set localized alert arguments without a localized alert message key.");
        }

        this.localizedAlertTitleKey = localizedAlertTitleKey;
        this.localizedAlertTitleArguments = alertTitleArguments;

        return this;
    }

    /**
     * 设置接收消息的APP启动图片
     */
    public ApnsPayloadBuilder setLaunchImageFileName(final String launchImageFilename) {
        this.launchImageFileName = launchImageFilename;
        return this;
    }

    /**
     * 设置是否显示Action按钮，默认显示
     */
    public ApnsPayloadBuilder setShowActionButton(final boolean showActionButton) {
        this.showActionButton = showActionButton;
        return this;
    }

    /**
     * 设置Action按钮的标题，通过指定标题的KEY实现
     */
    public ApnsPayloadBuilder setLocalizedActionButtonKey(final String localizedActionButtonKey) {
        this.localizedActionButtonKey = localizedActionButtonKey;
        return this;
    }

    /**
     * 设置App的badge展现内容，如果设置为0，将不展示badge，如果设置为null,将不改变当前的badge;
     */
    public ApnsPayloadBuilder setBadgeNumber(final Integer badgeNumber) {
        this.badgeNumber = badgeNumber;
        return this;
    }

    /**
     * <p>
     * Sets the name of the action category name for interactive remote notifications.
     * </p>
     *
     * @param categoryName the action category name
     */
    public ApnsPayloadBuilder setCategoryName(final String categoryName) {
        this.categoryName = categoryName;
        return this;
    }

    /**
     * 设置消息到达播放的声音，如果为null,将不播放声音，默认为@see ApnsPayloadBuilder#DEFAULT_SOUND_FILENAME
     */
    public ApnsPayloadBuilder setSoundFileName(final String soundFileName) {
        this.soundFileName = soundFileName;
        return this;
    }

    /**
     * <p>
     * 设置payload是否包含标识，用于在后台通知接收消息的APP有新的内容到达，默认在payload中不包含该标识
     * </p>
     *
     * @see <a
     *      href="https://developer.apple.com/library/ios/documentation/iPhone/Conceptual/iPhoneOSProgrammingGuide/ManagingYourApplicationsFlow/ManagingYourApplicationsFlow.html#//apple_ref/doc/uid/TP40007072-CH4-SW24">
     *      iOS App Programming Guide - App States and Multitasking - Background Execution and Multitasking -
     *      Implementing Long-Running Background Tasks</a>
     */
    public ApnsPayloadBuilder setContentAvailable(final boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
        return this;
    }

    /**
     * 添加自定义属性 According to Apple's documentation: <blockquote>Providers can specify custom payload values outside the
     * Apple-reserved {@code aps} namespace. Custom values must use the JSON structured and primitive types: dictionary
     * (object), array, string, number, and Boolean. You should not include customer information (or any sensitive data)
     * as custom payload data. Instead, use it for such purposes as setting context (for the user interface) or internal
     * metrics. For example, a custom payload value might be a conversation identifier for use by an instant-message
     * client application or a timestamp identifying when the provider sent the notification. Any action associated with
     * an alert message should not be destructive—for example, it should not delete data on the device.</blockquote>
     *
     * @param key the key of the custom property in the payload object
     * @param value the value of the custom property
     */
    public ApnsPayloadBuilder addCustomProperty(final String key, final Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    /**
     * 返回payload的JSON格式内容，如果长度超出了最大长度（2048byte），则截取alert body的内容；如果alert body不存在，则抛出异常；
     */
    public String buildWithDefaultMaximumLength() {
        return this.buildWithMaximumLength(DEFAULT_PAYLOAD_SIZE);
    }

    /**
     * <p>
     * Returns a JSON representation of the push notification payload under construction. If the payload length is
     * longer than the given maximum, the literal alert body will be shortened if possible. If the alert body cannot be
     * shortened or is not present, an {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param maximumPayloadLength the maximum length of the payload in bytes
     * @return a JSON representation of the payload under construction (possibly with an abbreviated alert body)
     */
    public String buildWithMaximumLength(final int maximumPayloadLength) {
        final Map<String, Object> payload = new HashMap<String, Object>();

        final Map<String, Object> aps = new HashMap<String, Object>();

        if (this.badgeNumber != null) {
            aps.put(BADGE_KEY, this.badgeNumber);
        }

        if (this.soundFileName != null) {
            aps.put(SOUND_KEY, this.soundFileName);
        }

        if (this.categoryName != null) {
            aps.put(CATEGORY_KEY, this.categoryName);
        }

        if (this.contentAvailable) {
            aps.put(CONTENT_AVAILABLE_KEY, 1);
        }

        final Object alertObject = this.createAlertObject();

        if (alertObject != null) {
            aps.put(ALERT_KEY, alertObject);
        }

        payload.put(APS_KEY, aps);

        for (final Map.Entry<String, Object> entry : this.customProperties.entrySet()) {
            payload.put(entry.getKey(), entry.getValue());
        }

        final String payloadString = JSON.toJSONString(payload);
        final int initialPayloadLength = payloadString.getBytes(UTF8).length;

        if (initialPayloadLength <= maximumPayloadLength) {
            return payloadString;
        }
        else {
            if (this.alertBody != null) {
                this.replaceMessageBody(payload, "");
                final int payloadLengthWithEmptyMessage = JSON.toJSONString(payload).getBytes(UTF8).length;

                if (payloadLengthWithEmptyMessage > maximumPayloadLength) {
                    throw new IllegalArgumentException(
                            "Payload exceeds maximum length even with an empty message body.");
                }

                int maximumMessageBodyLength = maximumPayloadLength - payloadLengthWithEmptyMessage;

                this.replaceMessageBody(payload, this.abbreviateString(this.alertBody, maximumMessageBodyLength--));

                while (JSON.toJSONString(payload).getBytes(UTF8).length > maximumPayloadLength) {
                    this.replaceMessageBody(payload, this.abbreviateString(this.alertBody, maximumMessageBodyLength--));
                }

                return JSON.toJSONString(payload);

            }
            else {
                throw new IllegalArgumentException(String.format(
                        "Payload length is %d bytes (with a maximum of %d bytes) and cannot be shortened.",
                        initialPayloadLength, maximumPayloadLength));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceMessageBody(final Map<String, Object> payload, final String messageBody) {
        final Map<String, Object> aps = (Map<String, Object>)payload.get(APS_KEY);
        final Object alert = aps.get(ALERT_KEY);

        if (alert != null) {
            if (alert instanceof String) {
                aps.put(ALERT_KEY, messageBody);
            }
            else {
                final Map<String, Object> alertObject = (Map<String, Object>)alert;

                if (alertObject.get(ALERT_BODY_KEY) != null) {
                    alertObject.put(ALERT_BODY_KEY, messageBody);
                }
                else {
                    throw new IllegalArgumentException("Payload has no message body.");
                }
            }
        }
        else {
            throw new IllegalArgumentException("Payload has no message body.");
        }
    }

    private final String abbreviateString(final String string, final int maximumLength) {
        if (string.length() <= maximumLength) {
            return string;
        }
        else {
            if (maximumLength <= 3) {
                throw new IllegalArgumentException("Cannot abbreviate string to fewer than three characters.");
            }
            return string.substring(0, maximumLength - 3) + "...";
        }
    }

    private Object createAlertObject() {
        if (this.hasAlertContent()) {
            if (this.shouldRepresentAlertAsString()) {
                return this.alertBody;
            }
            else {
                final JSONObject alert = new JSONObject();

                if (this.alertBody != null) {
                    alert.put(ALERT_BODY_KEY, this.alertBody);
                }

                if (this.alertTitle != null) {
                    alert.put(ALERT_TITLE_KEY, this.alertTitle);
                }

                if (this.showActionButton) {
                    if (this.localizedActionButtonKey != null) {
                        alert.put(ACTION_LOC_KEY, this.localizedActionButtonKey);
                    }
                }
                else {
                    // To hide the action button, the key needs to be present,
                    // but the value needs to be null
                    alert.put(ACTION_LOC_KEY, null);
                }

                if (this.localizedAlertKey != null) {
                    alert.put(ALERT_LOC_KEY, this.localizedAlertKey);

                    if (this.localizedAlertArguments != null) {
                        final JSONArray alertArgs = new JSONArray();

                        for (final String arg : this.localizedAlertArguments) {
                            alertArgs.add(arg);
                        }

                        alert.put(ALERT_ARGS_KEY, alertArgs);
                    }
                }

                if (this.localizedAlertTitleKey != null) {
                    alert.put(ALERT_TITLE_LOC_KEY, this.localizedAlertTitleKey);

                    if (this.localizedAlertTitleArguments != null) {
                        final JSONArray alertTitleArgs = new JSONArray();

                        for (final String arg : this.localizedAlertTitleArguments) {
                            alertTitleArgs.add(arg);
                        }

                        alert.put(ALERT_TITLE_ARGS_KEY, alertTitleArgs);
                    }
                }

                if (this.launchImageFileName != null) {
                    alert.put(LAUNCH_IMAGE_KEY, this.launchImageFileName);
                }

                return alert;
            }
        }
        else {
            return null;
        }
    }

    private boolean hasAlertContent() {
        return this.alertBody != null || this.alertTitle != null || this.localizedAlertTitleKey != null
                || this.localizedAlertKey != null || this.localizedActionButtonKey != null
                || this.launchImageFileName != null || this.showActionButton == false;
    }

    /**
     * <p>
     * 根据Apple 推送文档:
     * </p>
     * <blockquote>If you want the device to display the message text as-is in an alert that has both the Close and View
     * buttons, then specify a string as the direct value of {@code alert}. Don't specify a dictionary as the value of
     * {@code alert} if the dictionary only has the {@code body} property.</blockquote>
     *
     */
    private boolean shouldRepresentAlertAsString() {
        return this.alertBody != null && this.launchImageFileName == null && this.showActionButton
                && this.localizedActionButtonKey == null && this.alertTitle == null
                && this.localizedAlertTitleKey == null && this.localizedAlertKey == null
                && this.localizedAlertArguments == null && this.localizedAlertTitleArguments == null;
    }
}
