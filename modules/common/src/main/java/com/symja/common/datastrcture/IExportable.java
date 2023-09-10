package com.symja.common.datastrcture;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface IExportable {

    List<Data.Format> getExportFormats();

    @Nullable
    default Data exportNow(Data.Format format) {
        return null;
    }

    void export(Data.Format format, Consumer<Data> callback);
}
