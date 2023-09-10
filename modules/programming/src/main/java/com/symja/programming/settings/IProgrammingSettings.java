package com.symja.programming.settings;

import org.jetbrains.annotations.Nullable;

public interface IProgrammingSettings extends IUserSettings {

    boolean isUseRelaxedSyntax();

    boolean isDominantImplicitTimes();

    boolean isExplicitTimesOperator();

    int getRecursionLimit();

    int getIterationLimit();

    int getIntegrateRecursionLimit();

    int getLHospitalLimit();

    int getMaxAstSize();

    int getMaxOutputSize();

    String getLastEditedDocumentName();

    void setLastEditedDocument(String lastEditedDocument);

    boolean isShowSymbolBar();

    @Nullable
    String getSymjaServer();

    boolean isUseSymjaTalkOffline();

}
