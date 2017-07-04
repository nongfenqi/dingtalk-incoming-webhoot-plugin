package com.nongfenqi.rundeck.plugins.dingtalk;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Chenlm on 01/07/2017.
 */
@Plugin(service = "Notification", name = "DingtalkNotification")
@PluginDescription(title = "钉钉机器人", description = "发送消息到钉钉机器人")
public class DingtalkNotificationPlugin implements NotificationPlugin {


    private static final Configuration FREEMARKER_CFG = new Configuration();
    private static final String DINGTALK_MESSAGE_TEMPLATE = "dingtalk-incoming-message.ftl";

    private static final String TRIGGER_START = "start";
    private static final String TRIGGER_SUCCESS = "success";
    private static final String TRIGGER_FAILURE = "failure";

    @PluginProperty(title = "钉钉机器人hook url", description = "钉钉机器人hook url", required = true, defaultValue = "https://oapi.dingtalk.com/robot/send")
    private String dingtalkHookUrl;

    @PluginProperty(title = "钉钉机器人token", description = "钉钉机器人token", required = true)
    private String dingtalkToken;


    @Override
    public boolean postNotification(String trigger, Map executionData, Map config) {

        ClassTemplateLoader builtInTemplate = new ClassTemplateLoader(DingtalkNotificationPlugin.class, "/templates");
        TemplateLoader[] loaders = new TemplateLoader[]{builtInTemplate};
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        FREEMARKER_CFG.setTemplateLoader(mtl);

        try {
            FREEMARKER_CFG.setSetting(Configuration.CACHE_STORAGE_KEY, "strong:20, soft:250");
        } catch (Exception e) {
            System.err.printf("Got and exception from Freemarker: %s", e.getMessage());
        }

        String webhookUrl = this.dingtalkHookUrl + "?access_token=" + this.dingtalkToken;

        String message = generateMessage(trigger, executionData, config);
        String dingtalkResponse = invokeDingtalkAPIMethod(webhookUrl, message);

        if ("{\"errmsg\":\"ok\",\"errcode\":0}".equals(dingtalkResponse)) {
            return true;
        } else {
            // Unfortunately there seems to be no way to obtain a reference to the plugin logger within notification plugins,
            // but throwing an exception will result in its message being logged.
            throw new DingtalkNotificationPluginException("Unknown status returned from Dingtalk API: [" + dingtalkResponse + "].");
        }
    }

    private String invokeDingtalkAPIMethod(String webhookUrl, String message) {
        URL requestUrl = toURL(webhookUrl);

        HttpURLConnection connection = null;
        InputStream responseStream = null;
        try {
            connection = openConnection(requestUrl);
            putRequestStream(connection, message);
            responseStream = getResponseStream(connection);
            return getDingtalkResponse(responseStream);

        } finally {
            closeQuietly(responseStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException malformedURLEx) {
            throw new DingtalkNotificationPluginException("Dingtalk API URL is malformed: [" + malformedURLEx.getMessage() + "].", malformedURLEx);
        }
    }

    private HttpURLConnection openConnection(URL requestUrl) {
        try {
            return (HttpURLConnection) requestUrl.openConnection();
        } catch (IOException ioEx) {
            throw new DingtalkNotificationPluginException("Error opening connection to Dingtalk URL: [" + ioEx.getMessage() + "].", ioEx);
        }
    }

    private void putRequestStream(HttpURLConnection connection, String message) {
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoInput(true);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(message.getBytes());
            wr.flush();
            wr.close();
        } catch (IOException ioEx) {
            throw new DingtalkNotificationPluginException("Error putting data to Dingtalk URL: [" + ioEx.getMessage() + "].", ioEx);
        }
    }

    private InputStream getResponseStream(HttpURLConnection connection) {
        InputStream input = null;
        try {
            input = connection.getInputStream();
        } catch (IOException ioEx) {
            input = connection.getErrorStream();
        }
        return input;
    }

    private String getDingtalkResponse(InputStream responseStream) {
        try {
            return new Scanner(responseStream, "UTF-8").useDelimiter("\\A").next();
        } catch (Exception ioEx) {
            throw new DingtalkNotificationPluginException("Error reading Dingtalk API JSON response: [" + ioEx.getMessage() + "].", ioEx);
        }
    }

    private void closeQuietly(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ioEx) {
                // ignore
            }
        }
    }

    private String generateMessage(String trigger, Map executionData, Map config) {

        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("trigger", trigger);
        model.put("executionData", executionData);
        model.put("config", config);

        StringWriter sw = new StringWriter();
        try {
            Template template = FREEMARKER_CFG.getTemplate(DINGTALK_MESSAGE_TEMPLATE);
            template.process(model, sw);

        } catch (IOException ioEx) {
            throw new DingtalkNotificationPluginException("Error loading Dingtalk notification message template: [" + ioEx.getMessage() + "].", ioEx);
        } catch (TemplateException templateEx) {
            throw new DingtalkNotificationPluginException("Error merging Dingtalk notification message template: [" + templateEx.getMessage() + "].", templateEx);
        }

        return sw.toString();
    }
}
