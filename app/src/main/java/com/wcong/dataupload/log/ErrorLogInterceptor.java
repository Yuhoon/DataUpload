package com.wcong.dataupload.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by wangcong on 2017/7/5.
 * <p>
 * 用于拦截网络请求时出现的异常，OkHttpClient.addInterceptor
 */
public class ErrorLogInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            Response response = chain.proceed(request);
            if (response.code() < 200 || response.code() >= 300) {
                Logger.error(requestFormat(request) + responseFormat(response));
            }
            return response;
        } catch (Throwable e) {
            Logger.error(requestFormat(request) + exceptionFormat(e));
            throw e;
        }
    }

    private String requestFormat(Request request) throws IOException {

        boolean compressed = false;

        String curlCmd = "\n----------request----------\n";
        curlCmd += "curl -X " + request.method();

        Headers headers = request.headers();
        for (int i = 0, count = headers.size(); i < count; i++) {
            String name = headers.name(i);
            String value = headers.value(i);
            if ("Accept-Encoding".equalsIgnoreCase(name) && "gzip".equalsIgnoreCase(value)) {
                compressed = true;
            }
            curlCmd += " -H " + "\"" + name + ": " + value + "\"";
        }

        RequestBody requestBody = request.body();
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            curlCmd += " --data $'" + buffer.readString(charset).replace("\n", "\\n") + "'";
        }

        curlCmd += ((compressed) ? " --compressed " : " ") + request.url();
        return curlCmd;
    }

    private String responseFormat(Response response) throws IOException {
        String result = "\n\n----------response----------\n";
        result += String.format("%n%s", response.headers());
        result += String.format("%nStatusCode: %s", response.code());
        // 复制response
        Buffer bufferClone = response.body().source().buffer().clone();
        ResponseBody responseBody = response.body().create(response.body().contentType(),
                response.body().contentLength(), bufferClone);
        result += String.format("\nBody: \n%s", responseBody.string());
        return result;
    }

    private String exceptionFormat(Throwable e) {
        String sb = "\n\n----------exception----------\n";
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        return sb + result.toString();
    }
}
