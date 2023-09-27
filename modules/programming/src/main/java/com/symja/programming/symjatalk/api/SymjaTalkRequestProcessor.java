package com.symja.programming.symjatalk.api;

import android.Manifest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.WorkerThread;

import com.duy.ide.common.utils.DLog;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.symja.common.datastrcture.Data;
import com.symja.evaluator.Symja;
import com.symja.programming.console.models.CalculationItem;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.matheclipse.api.Pods;
import org.matheclipse.api.SymjaServer;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SymjaTalkRequestProcessor {
    public static final String DEFAULT_HOST = "https://ncalc.xyz";

    private static final String TAG = "SymjaTalkRequestProcess";

    private static final String HEADER_APPID = "appid";
    private static final String HEADER_INPUT = "input";
    private static final String HEADER_FORMAT = "format";

    private static final String HEADER_APPID_VALUE = "DEMO";

    @Nullable
    private final String symjaHost;
    private final boolean offlineMode;

    public SymjaTalkRequestProcessor() {
        this(DEFAULT_HOST, false);
    }

    public SymjaTalkRequestProcessor(@Nullable String symjaHost, boolean offlineMode) {
        this.symjaHost = symjaHost == null || symjaHost.isEmpty() ? DEFAULT_HOST : symjaHost;
        this.offlineMode = offlineMode;
    }

    @NonNull
    @WorkerThread
    public SymjaTalkResult startRequest(@NotNull SymjaTalkRequest singleRequest) throws JSONException {
        if (offlineMode || symjaHost == null) {
            return startRequestFromLocal(singleRequest);
        } else {
            try {
                return startRequestFromServer(symjaHost, singleRequest);
            } catch (IOException e) {
                if (DLog.DEBUG) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return startRequestFromLocal(singleRequest);
            }
        }
    }

    @NotNull
    @WorkerThread
    private SymjaTalkResult startRequestFromLocal(@NotNull SymjaTalkRequest singleRequest) throws JSONException {
        SymjaServer.initAPI();

        final String input = singleRequest.getInput();
        if (DLog.DEBUG) {
            Log.d(TAG, "startRequestFromLocal: input = " + input);
        }
        List<SymjaFormat> listFormats = singleRequest.getOutputFormats();

        EvalEngine evalEngine = Symja.getInstance().getExprEvaluator().getEvalEngine();
        EvalEngine.set(evalEngine);
        ObjectNode result = Pods.createResult(input,
                Pods.internFormat(listFormats.stream().map(SymjaFormat::getRawValue).toArray(String[]::new)),
                false, evalEngine);
        String jsonResult = result.toString();
        if (DLog.DEBUG) {
            Log.d(TAG, "startRequestFromLocal: jsonResult = " + jsonResult);
        }
        JSONObject jsonObject = new JSONObject(jsonResult);
        return parseResult(jsonObject);
    }

    @RequiresPermission(value = Manifest.permission.INTERNET)
    @NotNull
    private SymjaTalkResult startRequestFromServer(String symjaHost, @NotNull SymjaTalkRequest singleRequest) throws IOException, JSONException {
        final String input = singleRequest.getInput();

        OkHttpClient client = new OkHttpClient();
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.add(HEADER_APPID, HEADER_APPID_VALUE);
        bodyBuilder.add(HEADER_INPUT, input);
        for (SymjaFormat format : singleRequest.getOutputFormats()) {
            bodyBuilder.add(HEADER_FORMAT, format.getRawValue());
        }
        if (singleRequest.getOutputFormats().isEmpty()) {
            bodyBuilder.add(HEADER_FORMAT, SymjaFormat.PLAIN_TEXT.getRawValue());
        }
        FormBody body = bodyBuilder.build();
        String url = symjaHost + (symjaHost.endsWith("/v1/api") ? "" : "/v1/api");
        if (DLog.DEBUG) {
            Log.d(TAG, "startRequestFromServer: url = " + url + " body = " + body);
        }
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (DLog.DEBUG) {
                    Log.d(TAG, "startRequestFromServer: response = " + response);
                }
                throw new IOException("Fail to send request");
            }

            ResponseBody responseBody = response.body();
            String stringValue = responseBody.string();
            if (DLog.DEBUG)
                Log.d(TAG, "startRequestFromServer: stringValue = " + stringValue);
            JSONObject json = new JSONObject(stringValue);
            if (DLog.DEBUG) {
                DLog.d(TAG, json.toString(2));
            }
            return parseResult(json);
        }
    }

    @NotNull
    private SymjaTalkResult parseResult(@NotNull JSONObject jsonObject) throws JSONException {
        JSONObject json = jsonObject.getJSONObject("queryresult");
        SymjaTalkResult result = new SymjaTalkResult();
        if (json.has("success")) {
            result.setSuccess(json.getBoolean("success"));
        }
        if (json.has("error")) {
            result.setError(json.getBoolean("error"));
        }
        if (json.has("version")) {
            result.setVersion(json.getString("version"));
        }

        if (json.has("pods")) {
            JSONArray podsJson = json.getJSONArray("pods");
            for (int i = 0; i < podsJson.length(); i++) {
                JSONObject podJson = podsJson.getJSONObject(i);
                String title = podJson.getString("title");
                if (podJson.has("subpods")) {
                    JSONArray subpodsJson = podJson.getJSONArray("subpods");
                    for (int j = 0; j < subpodsJson.length(); j++) {
                        JSONObject subpodJson = subpodsJson.getJSONObject(j);
                        CalculationItem item = parsePod(title, subpodJson);
                        if (item != null) {
                            result.addResult(item);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Nullable
    private CalculationItem parsePod(String title, @NonNull JSONObject json) throws JSONException {

        @Nullable String symjaResult = null;
        @Nullable Data symjaInput = null;

        if (json.has(SymjaFormat.SYMJA.getRawValue())) {
            // Limit(999,x-&gt;2)&amp;&amp;{1/2*2*x}
            String value = StringEscapeUtils.unescapeHtml4(json.getString(SymjaFormat.SYMJA.getRawValue()));
            symjaInput = new Data(Data.Format.TEXT_APPLICATION_SYMJA, value);
        }
        List<Data> dataList = new ArrayList<>();

        // text
        if (json.has(SymjaFormat.PLAIN_TEXT.getRawValue())) {
            final String data = json.getString(SymjaFormat.PLAIN_TEXT.getRawValue());
            final Data.Format type = Data.Format.TEXT_PLAIN;
            symjaResult = data;
            dataList.add(new Data(type, data));

            // Example: Graphics result
            try {
                IExpr expr = Symja.getInstance().parse(symjaResult);
                String html = F.show(expr);
                if (html != null && !html.trim().isEmpty()) {
                    dataList.add(new Data(Data.Format.HTML, html));
                }
            } catch (Exception e) {
                if (DLog.DEBUG) {
                    Log.w(TAG, e.getMessage());
                }
            }

        }

        if (json.has(SymjaFormat.MARKDOWN.getRawValue())) {
            String data = StringEscapeUtils.unescapeHtml4(json.getString(SymjaFormat.MARKDOWN.getRawValue()));
            Data.Format type = Data.Format.MARKDOWN;
            dataList.add(new Data(type, data));

        }

        if (json.has(SymjaFormat.LATEX.getRawValue())) {

            Data.Format type = Data.Format.LATEX;
            String value = json.getString(SymjaFormat.LATEX.getRawValue());
            dataList.add(new Data(type, value));

        }

        if (json.has(SymjaFormat.MATHML.getRawValue())) {
            String data = json.getString(SymjaFormat.MATHML.getRawValue());
            data = StringEscapeUtils.unescapeXml(data);
            Data.Format type = Data.Format.TEXT_MATHML;
            dataList.add(new Data(type, data));

        }

        if (json.has(SymjaFormat.JSXGRAPH.getRawValue())) {
            String data = json.getString(SymjaFormat.JSXGRAPH.getRawValue());
            // data = StringEscapeUtils.unescapeHtml4(data); !! DO NOT ESCAPE HTML4
            Data.Format type = Data.Format.IFRAME;
            dataList.add(new Data(type, data));

        }

        if (json.has(SymjaFormat.MATHCELL.getRawValue())) {
            String data = json.getString(SymjaFormat.MATHCELL.getRawValue());
            // data = StringEscapeUtils.unescapeHtml4(data);  !! DO NOT ESCAPE HTML4
            Data.Format type = Data.Format.IFRAME;
            dataList.add(new Data(type, data));

        }

        if (json.has(SymjaFormat.PLOTLY.getRawValue())) {
            String data = json.getString(SymjaFormat.PLOTLY.getRawValue());
            // data = StringEscapeUtils.unescapeHtml4(data);  !! DO NOT ESCAPE HTML4
            Data.Format type = Data.Format.IFRAME;
            dataList.add(new Data(type, data));

        }

        if (json.has(SymjaFormat.VISJS.getRawValue())) {
            String data = json.getString(SymjaFormat.VISJS.getRawValue());
            // data = StringEscapeUtils.unescapeHtml4(data); !! DO NOT ESCAPE HTML4
            Data.Format type = Data.Format.IFRAME;
            dataList.add(new Data(type, data));

        }

        if (json.has(SymjaFormat.HTML.getRawValue())) {
            String data = StringEscapeUtils.unescapeHtml4(json.getString(SymjaFormat.HTML.getRawValue()));
            Data.Format type = Data.Format.IFRAME;
            dataList.add(new Data(type, data));
        }

        if (dataList.isEmpty()) {
            return null;
        }
        CalculationItem calculationItem = new CalculationItem(symjaInput, symjaResult, dataList);
        calculationItem.setTitle(title);
        return calculationItem;
    }
}
