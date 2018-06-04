package com.scrat.app.richtexteditor;

import android.text.style.ForegroundColorSpan;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        int color = com.scrat.app.richtext.R.color.md_purple_500_color_code;
        ForegroundColorSpan f = new ForegroundColorSpan(color);
//        int newColor = f.getForegroundColor();
        String c = Integer.toHexString(color);
    }

}