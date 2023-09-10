/*
 * Copyright (C) 2018 Duy Tran Le
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.symja.evaluator.config;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Duy on 26-Jun-17.
 */
public class EvaluationConfig implements Serializable {

    private CalculationMode evalMode = CalculationMode.SYMBOLIC;
    private boolean outputMultiline = false;
    private ArrayList<EvalConfigOption> options = new ArrayList<>();

    @SuppressWarnings("unused")
    private EvaluationConfig(@NonNull EvaluationConfig other) {
        this.evalMode = other.evalMode;
        this.outputMultiline = other.outputMultiline;
        this.options = new ArrayList<>(other.options);
    }

    private EvaluationConfig() {
    }

    public static EvaluationConfig newInstance() {
        return new EvaluationConfig();
    }

    public CalculationMode getEvaluateMode() {
        return evalMode;
    }

    public EvaluationConfig setEvalMode(CalculationMode evalMode) {
        this.evalMode = evalMode;
        return this;
    }

    public boolean isOutputMultiline() {
        return this.outputMultiline;
    }

    public EvaluationConfig setOutputMultiline(boolean outputMultiline) {
        this.outputMultiline = outputMultiline;
        return this;
    }

    public EvaluationConfig addOptions(EvalConfigOption... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    public boolean hasOption(EvalConfigOption option) {
        return this.options.contains(option);
    }

    public enum CalculationMode {
        NUMERIC(0),
        SYMBOLIC(1);

        private final int value;

        CalculationMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
