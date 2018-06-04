package com.scrat.app.richtext.parser;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BulletSpan;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Stack;

public class MyTagHandler implements Html.TagHandler {
    private static final String BULLET_LI = "li";
    private static final String STRIKETHROUGH_S = "s";
    private static final String STRIKETHROUGH_STRIKE = "strike";
    private static final String STRIKETHROUGH_DEL = "del";
    private static final String FONT = "size";//自定义的tag标签 <size value="1"><size/> 来定义字体大小

    private static class Li {
    }

    private static class Strike {
    }


    /**
     * html 标签的开始下标
     */
    private Stack<Integer> startIndex;

    /**
     * html的标签的属性值 value，如:<size value='16'></size>
     * 注：value的值不能带有单位,默认就是sp
     */
    private Stack<String> propertyValue;

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (opening) {
            if (tag.equalsIgnoreCase(BULLET_LI)) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                start(output, new Li());
            } else if (tag.equalsIgnoreCase(STRIKETHROUGH_S) || tag.equalsIgnoreCase(STRIKETHROUGH_STRIKE) || tag.equalsIgnoreCase(STRIKETHROUGH_DEL)) {
                start(output, new Strike());
            } else if (tag.equalsIgnoreCase(FONT)) {
                handlerStartSIZE(output, xmlReader);
            }
        } else {
            if (tag.equalsIgnoreCase(BULLET_LI)) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                end(output, Li.class, new BulletSpan());
            } else if (tag.equalsIgnoreCase(STRIKETHROUGH_S) || tag.equalsIgnoreCase(STRIKETHROUGH_STRIKE) || tag.equalsIgnoreCase(STRIKETHROUGH_DEL)) {
                end(output, Strike.class, new StrikethroughSpan());
            } else if (tag.equalsIgnoreCase(FONT)) {
                handlerEndSIZE(output);
            }
        }
    }

    private void handlerStartSIZE(Editable output, XMLReader xmlReader) {
        if (startIndex == null) {
            startIndex = new Stack<>();
        }
        startIndex.push(output.length());

        if (propertyValue == null) {
            propertyValue = new Stack<>();
        }

        propertyValue.push(getProperty(xmlReader, "value"));
    }

    /**
     * 利用反射获取html标签的属性值
     *
     * @param xmlReader
     * @param property
     * @return
     */
    private String getProperty(XMLReader xmlReader, String property) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(atts);

            for (int i = 0; i < len; i++) {
                // 这边的property换成你自己的属性名就可以了
                if (property.equals(data[i * 5 + 1])) {
                    return data[i * 5 + 4];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 集合是否为空
     *
     * @param collection
     * @return
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    private void handlerEndSIZE(Editable output) {

        if (!isEmpty(propertyValue)) {
            try {
                int value = Integer.parseInt(propertyValue.pop());
                output.setSpan(new AbsoluteSizeSpan(getSize(value), true), startIndex.pop(), output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 从 1 到 7 的数字。浏览器默认值是 3。
     */
    private int getSize(int s) {
        int size = 0;
        switch (s) {
            case 1:
                size = 12;
                break;
            case 2:
                size = 14;
                break;
            case 3:
                size = 16;
                break;
            case 4:
                size = 18;
                break;
            case 5:
                size = 20;
                break;
            case 6:
                size = 22;
                break;
            case 7:
                size = 24;
                break;
        }
        return size;
    }


    private void start(Editable output, Object mark) {
        output.setSpan(mark, output.length(), output.length(), Spanned.SPAN_MARK_MARK);
    }

    private void end(Editable output, Class kind, Object... replaces) {
        Object last = getLast(output, kind);
        int start = output.getSpanStart(last);
        int end = output.length();
        output.removeSpan(last);

        if (start != end) {
            for (Object replace : replaces) {
                output.setSpan(replace, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static Object getLast(Editable text, Class kind) {
        Object[] spans = text.getSpans(0, text.length(), kind);

        if (spans.length == 0) {
            return null;
        } else {
            for (int i = spans.length; i > 0; i--) {
                if (text.getSpanFlags(spans[i - 1]) == Spannable.SPAN_MARK_MARK) {
                    return spans[i - 1];
                }
            }

            return null;
        }
    }
}
