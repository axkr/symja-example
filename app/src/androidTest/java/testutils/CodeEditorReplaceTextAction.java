/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testutils;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import java.util.Locale;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;

import io.github.rosemoe.sora.widget.CodeEditor;

/** Replaces view text by setting {@link EditText}s text property to given String. */
public final class CodeEditorReplaceTextAction implements ViewAction {
  final String stringToBeSet;

  public CodeEditorReplaceTextAction(String value) {
    checkNotNull(value);
    this.stringToBeSet = value;
  }

  @NonNull
  @Override
  public Matcher<View> getConstraints() {
    return allOf(isDisplayed(), isAssignableFrom(CodeEditor.class));
  }

  @Override
  public void perform(UiController uiController, View view) {
    ((CodeEditor) view).setText(stringToBeSet);
  }

  @NonNull
  @Override
  public String getDescription() {
    return String.format(Locale.ROOT, "replace text(%s)", stringToBeSet);
  }
}
