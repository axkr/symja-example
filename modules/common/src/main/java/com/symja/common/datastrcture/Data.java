package com.symja.common.datastrcture;

import androidx.annotation.NonNull;

import com.symja.common.datastrcture.json.JSONException;
import com.symja.common.datastrcture.json.JSONObject;
import com.symja.common.datastrcture.json.JsonIO;
import com.symja.common.datastrcture.json.JsonMap;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

public class Data implements Serializable, JsonIO, Cloneable {

    @NonNull
    private final Format format;
    private final String value;

    public Data(@NonNull Format format, String value) {
        this.format = format;
        this.value = value;
    }

    public Data(String format, String value) {
        this.format = Format.valueOf(format);
        this.value = value;
    }

    public Data(@NonNull JsonMap jsonMap) {
        this.format = Format.valueOf(jsonMap.getString("format"));
        this.value = jsonMap.getString("value");
    }

    @NonNull
    @Contract(value = "_ -> new", pure = true)
    public static Data text(String s) {
        return new Data(Format.TEXT_PLAIN, s);
    }

    @NonNull
    @Contract(value = "_ -> new", pure = true)
    public static Data html(String html) {
        return new Data(Format.HTML, html);
    }

    @NonNull
    @Contract(value = "_ -> new", pure = true)
    public static Data latex(String tex) {
        return new Data(Format.LATEX, tex);
    }

    public String getValue() {
        return value;
    }

    @NonNull
    public Format getFormat() {
        return format;
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data that = (Data) o;
        return format == that.format && Objects.equals(value, that.value);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NonNull
    @Override
    public Data clone() {
        return new Data(this.format, this.value);
    }

    @NonNull
    @Override
    public String toString() {
        return "Data{" +
                "format=" + format +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public void writeToJson(JSONObject inputObj) throws JSONException {
        inputObj.put("format", getFormat().name());
        inputObj.put("value", value);
    }

    public enum Format {
        NULL(""), // pseudo type to indicate null data

        TEXT_PLAIN("Text"),
        LATEX("Latex"),
        SVG("Svg"),
        HTML("Html"),

        // Display text in code editor
        TEXT_HTML("Html"),
        TEXT_MATHML("MathML"),
        TEXT_LATEX("Latex"),
        TEXT_APPLICATION_JAVA("Java"),
        TEXT_APPLICATION_JAVASCRIPT("Javascript"),
        TEXT_APPLICATION_SYMJA("Symja"),
        FACTOR_DIAGRAM("Factor Diagram"),

        // Display markdown in WebView
        MARKDOWN("Markdown"),

        // Display iframe inside WebView
        IFRAME("iframe");

        private final String name;

        Format(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


}
