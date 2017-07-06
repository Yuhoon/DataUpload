package com.wcong.dataupload.log;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wcong.dataupload.util.DeviceInfoUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by wangcong on 2017/7/5.
 * <p>
 */

public class LogStashDescription implements BaseDestination {
    private String serverUrl = LogField.URL;
    private String token;
    private Context context;
    private List<LogLevel> level;

    private int interval = 1000 * 60 * 5;

    private static int THRESHOLD = 20;
    private static int MAX_THRESHOLD = 50;
    private static int MIN_THRESHOLD = 1;
    private File entriesFile;
    private File sendingFile;
    private Gson gson;
    private int points;
    private boolean initialSending = true;
    private boolean isSending = false;
    private OkHttpClient client;

    private Map<UploadPolicy, Boolean> policies;

    private String TAG = getClass().getSimpleName() + "-";

    public LogStashDescription(Context context, LogLevel... level) {
        this(context);
        this.level = Arrays.asList(level);
    }

    public LogStashDescription(Context context) {
        this.context = context;

        entriesFile = new File(context.getFilesDir().getAbsolutePath(), "logger.json");
        sendingFile = new File(context.getFilesDir().getAbsolutePath(), "logger.sending.json");

        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss SSS").create();
        policies = initPolicies();
    }

    private Map<UploadPolicy, Boolean> initPolicies() {
        Map<UploadPolicy, Boolean> map = new HashMap<>();
        map.put(UploadPolicy.UPLOAD_POLICY_DEBUG, false);
        map.put(UploadPolicy.UPLOAD_POLICY_REALTIME, false);
        map.put(UploadPolicy.UPLOAD_POLICY_WIFI, false);
        return map;
    }

    public void send(Map data) {

        try {
            FileWriter writer = new FileWriter(entriesFile, true);
            try {
                writer.write(gson.toJson(data) + "\n");
                writer.flush();
            } finally {
                writer.close();
            }

            points += ((LogLevel) data.get(LogField.LEVEL)).point;
            if (getPolicy().get(UploadPolicy.UPLOAD_POLICY_WIFI) && !DeviceInfoUtil.getNetworkType(context).equals("WIFI")) {
                Log.e(TAG, "只能在wifi下上传");
                return;
            }
            if (getPolicy().get(UploadPolicy.UPLOAD_POLICY_REALTIME))
                initialSending = true;
            if (points >= THRESHOLD && points >= MIN_THRESHOLD || points > MAX_THRESHOLD) {
                sendNow();
            } else if (initialSending) {
                initialSending = false;
                List<Map> logModels = readFromFile(entriesFile);
                if (logModels.size() > 1) {
                    sendNow();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setFileName(String fileName) {
        entriesFile = new File(context.getFilesDir().getAbsolutePath(), fileName + ".json");
        sendingFile = new File(context.getFilesDir().getAbsolutePath(), fileName + ".sending.json");
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public void setThreshold(int threshold) {
        THRESHOLD = threshold;
    }

    @Override
    public void setnterval(int interval) {
        this.interval = interval;
    }

    @Override
    public boolean accept(LogLevel level) {
        if (this.level == null || this.level.size() == 0)
            return true;
        for (LogLevel logLevel : this.level) {
            if (level.equals(logLevel))
                return true;
        }
        return false;
    }

    @Override
    public Map<UploadPolicy, Boolean> getPolicy() {
        return policies;
    }

    public void setPolicy(UploadPolicy... policy) {
        List<UploadPolicy> list = Arrays.asList(policy);
        for (UploadPolicy uploadPolicy : list) {
            getPolicy().put(uploadPolicy, true);
        }
    }

    public synchronized void sendNow() {
        if (sendingFile.exists()) {
            points = 0;
        } else {
            try {
                sendingFile.createNewFile();
                FileInputStream fis = new FileInputStream(entriesFile);
                FileOutputStream fos = new FileOutputStream(sendingFile);
                try {
                    int n = 0;
                    byte[] buffer = new byte[1024 * 4];
                    while (-1 != (n = fis.read(buffer))) {
                        fos.write(buffer, 0, n);
                    }
                } finally {
                    fis.close();
                    fos.close();
                }

                boolean deleted = entriesFile.delete();
                Log.i(TAG + "delete entriesFile", "" + deleted);
                if (deleted) {
                    entriesFile.createNewFile();
                }
            } catch (IOException e) {
                return;
            }
        }

        if (!isSending) {
            isSending = true;
            List<Map> logModels = readFromFile(sendingFile);
            if (logModels.size() <= 0) {
                boolean deleted = sendingFile.delete();
                Log.i(TAG + "delete sendingFile", "empty " + deleted);
                isSending = false;
                return;
            } else {
                try {
                    Collections.reverse(logModels);
                    points = 0;
                    OkHttpClient client = getClient();
                    RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                            gson.toJson(logModels));
                    Request request = new Request.Builder().url(serverUrl).post(body).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            isSending = false;
                            Log.e(TAG + "上传日志到服务器的Request请求失败", e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                boolean deleted = sendingFile.delete();
                                Log.e("delete sendingFile", "" + deleted);
                                isSending = false;
                                points = 0;
                            } else {
                                isSending = false;
                                Log.e(TAG + "上传日志到服务器的Request请求失败", response.message());
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    isSending = false;
                    Log.e(TAG + "上传日志错误", e.getMessage());
                }
            }

        }
    }

    private OkHttpClient getClient() {
        if (client == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            client = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                    .authenticator(new Authenticator() {
                        @Override
                        public Request authenticate(Route route, Response response) throws IOException {
                            String credential = "Basic " + token;
                            if (credential.equals(response.request().header("Authorization"))) {
                                return null;
                            }
                            return response.request().newBuilder().addHeader("Authorization", credential)
                                    .header("Content-Type", "application/json").build();
                        }
                    }).addInterceptor(logging).build();
        }
        return client;
    }

    private List<Map> readFromFile(File file) {
        List<Map> result = new LinkedList<>();
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);

            try {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    result.add(gson.fromJson(line, Map.class));
                }
            } finally {
                bufferedReader.close();
                reader.close();
            }
        } catch (Exception e) {
            System.out.println("---read------" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}
