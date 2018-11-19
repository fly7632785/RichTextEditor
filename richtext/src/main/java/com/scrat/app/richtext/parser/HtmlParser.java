package com.scrat.app.richtext.parser;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

public class HtmlParser {
    public static Spanned fromHtml(String source, Html.ImageGetter imageGetter) {

        return Html.fromHtml(source, imageGetter, new MyTagHandler());
    }

    public static Spanned fromHtml(String source) {
        return Html.fromHtml(source, null, new MyTagHandler());
    }

    public static String toHtml(Spanned text) {
        StringBuilder out = new StringBuilder();
        withinHtml(out, text);
        return tidy(out.toString());
    }

    private static void withinHtml(StringBuilder out, Spanned text) {
        int next;

        for (int i = 0; i < text.length(); i = next) {
            next = text.nextSpanTransition(i, text.length(), ParagraphStyle.class);

            ParagraphStyle[] styles = text.getSpans(i, next, ParagraphStyle.class);
            if (styles.length == 2) {
                if (styles[0] instanceof BulletSpan && styles[1] instanceof QuoteSpan) {
                    // Let a <br> follow the BulletSpan or QuoteSpan end, so next++
                    withinBulletThenQuote(out, text, i, next++);
                } else if (styles[0] instanceof QuoteSpan && styles[1] instanceof BulletSpan) {
                    withinQuoteThenBullet(out, text, i, next++);
                } else if (styles[0] instanceof AlignmentSpan || styles[1] instanceof AlignmentSpan) {
                    withAlign(out, text, i, next);
                } else {
                    withinContent(out, text, i, next);
                }
            } else if (styles.length == 1) {
                if (styles[0] instanceof BulletSpan) {
                    withinBullet(out, text, i, next++);
                } else if (styles[0] instanceof QuoteSpan) {
                    withinQuote(out, text, i, next++);
                } else if (styles[0] instanceof AlignmentSpan) {
                    withAlign(out, text, i, next);
                } else {
                    withinContent(out, text, i, next);
                }
            } else if (styles.length == 3) { // quote bullet align
                if (styles[0] instanceof AlignmentSpan || styles[1] instanceof AlignmentSpan || styles[2] instanceof AlignmentSpan) {
                    withAlign(out, text, i, next);
                }
            } else {
                withinContent(out, text, i, next);
            }
        }
    }

    private static void withAlign(StringBuilder out, Spanned text, int start, int end) {
        int next;

        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, AlignmentSpan.class);

            AlignmentSpan[] alignmentSpans = text.getSpans(i, next, AlignmentSpan.class);
            for (AlignmentSpan alignmentSpan : alignmentSpans) {

                String elements = "";
                if (alignmentSpan.getAlignment() == Layout.Alignment.ALIGN_CENTER) {
                    elements = "\"center\" " + elements;
                } else if (alignmentSpan.getAlignment() == Layout.Alignment.ALIGN_OPPOSITE) {
                    elements = "\"right\" " + elements;
                } else {
                    elements = "\"left\" " + elements;
                }

                out.append("<align value=" + elements)
                        .append(">")
                        .append("<div ")
                        .append("align=" + elements)
                        .append(">");

            }

            withinContent(out, text, i, next);
            for (AlignmentSpan alignmentSpan : alignmentSpans) {
                out.append("</div>");
                out.append("</align>");
            }
        }
    }

    private static void withinBulletThenQuote(StringBuilder out, Spanned text, int start, int end) {
        out.append("<ul><li>");
        withinQuote(out, text, start, end);
        out.append("</li></ul>");
    }

    private static void withinQuoteThenBullet(StringBuilder out, Spanned text, int start, int end) {
        out.append("<blockquote>");
        withinBullet(out, text, start, end);
        out.append("</blockquote>");
    }

    private static void withinBullet(StringBuilder out, Spanned text, int start, int end) {
        out.append("<ul>");

        int next;

        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, BulletSpan.class);

            BulletSpan[] spans = text.getSpans(i, next, BulletSpan.class);
            for (BulletSpan span : spans) {
                out.append("<li>");
            }

            withinContent(out, text, i, next);
            for (BulletSpan span : spans) {
                out.append("</li>");
            }
        }

        out.append("</ul>");
    }

    private static void withinQuote(StringBuilder out, Spanned text, int start, int end) {
        int next;

        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, QuoteSpan.class);

            QuoteSpan[] quotes = text.getSpans(i, next, QuoteSpan.class);
            for (QuoteSpan quote : quotes) {
                out.append("<blockquote>");
            }

            withinContent(out, text, i, next);
            for (QuoteSpan quote : quotes) {
                out.append("</blockquote>");
            }
        }
    }

    private static void withinContent(StringBuilder out, Spanned text, int start, int end) {
        int next;

        for (int i = start; i < end; i = next) {
            next = TextUtils.indexOf(text, '\n', i, end);
            if (next < 0) {
                next = end;
            }

            int nl = 0;
            while (next < end && text.charAt(next) == '\n') {
                next++;
                nl++;
            }

            withinParagraph(out, text, i, next - nl, nl);
        }
    }

    // Copy from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java,
    // remove some tag because we don't need them in Knife.
    private static void withinParagraph(StringBuilder out, Spanned text, int start, int end, int nl) {
        int next;

        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, CharacterStyle.class);

            CharacterStyle[] spans = text.getSpans(i, next, CharacterStyle.class);
            for (CharacterStyle span : spans) {
                if (span instanceof StyleSpan) {
                    int style = ((StyleSpan) span).getStyle();

                    if ((style & Typeface.BOLD) != 0) {
                        out.append("<b>");
                    }

                    if ((style & Typeface.ITALIC) != 0) {
                        out.append("<i>");
                    }
                }

                if (span instanceof UnderlineSpan) {
                    out.append("<u>");
                }


                // Use standard strikethrough tag <del> rather than <s> or <strike>
                if (span instanceof StrikethroughSpan) {
                    out.append("<del>");
                }

                if (span instanceof URLSpan) {
                    out.append("<a href=\"");
                    out.append(((URLSpan) span).getURL());
                    out.append("\">");
                }
                if (span instanceof ForegroundColorSpan) {
                    //sub 2 exclude ff
                    out.append("<font color=\"#" + Integer.toHexString(((ForegroundColorSpan) span).getForegroundColor()).substring(2) + "\">");
                }
                if (span instanceof AbsoluteSizeSpan) {
                    //从 1 到 7 的数字。浏览器默认值是 3。
                    out.append("<font size=\"" + getSize(((AbsoluteSizeSpan) span).getSize()) + "\">");
                    //自定义的字体大小tag 因为默认的HTML解析不支持font size，所以这里自定义一个
                    out.append("<size value=\"" + getSize(((AbsoluteSizeSpan) span).getSize()) + "\">");
                }

                if (span instanceof ImageSpan) {
                    out.append("<img width=\"100%\" src=\"");
                    out.append(((ImageSpan) span).getSource());
                    out.append("\">");

                    // Don't output the dummy character underlying the image.
                    i = next;
                }
            }

            withinStyle(out, text, i, next);
            for (int j = spans.length - 1; j >= 0; j--) {
                if (spans[j] instanceof AbsoluteSizeSpan) {
                    out.append("</size>");
                    out.append("</font>");
                }

                if (spans[j] instanceof ForegroundColorSpan) {
                    out.append("</font>");
                }
                if (spans[j] instanceof URLSpan) {
                    out.append("</a>");
                }

                if (spans[j] instanceof StrikethroughSpan) {
                    out.append("</del>");
                }

                if (spans[j] instanceof UnderlineSpan) {
                    out.append("</u>");
                }

                if (spans[j] instanceof StyleSpan) {
                    int style = ((StyleSpan) spans[j]).getStyle();

                    if ((style & Typeface.BOLD) != 0) {
                        out.append("</b>");
                    }

                    if ((style & Typeface.ITALIC) != 0) {
                        out.append("</i>");
                    }
                }
            }
        }

        for (int i = 0; i < nl; i++) {
            out.append("<br>");
        }
    }

    /**
     * 从 1 到 7 的数字。浏览器默认值是 3。
     */
    private static int getSize(int s) {
        int size = 3;
        if (s <= 12) {
            size = 1;
        } else if (s <= 14) {
            size = 2;
        } else if (s <= 16) {
            size = 3;
        } else if (s <= 18) {
            size = 4;
        } else if (s <= 20) {
            size = 5;
        } else if (s <= 22) {
            size = 6;
        } else {
            size = 7;
        }
        return size;
    }

    private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
        out.append(text.subSequence(start, end));
//        for (int i = start; i < end; i++) {
//            char c = text.charAt(i);
//
//            if (c == '<') {
//                out.append("&lt;");
//            } else if (c == '>') {
//                out.append("&gt;");
//            } else if (c == '&') {
//                out.append("&amp;");
//            } else if (c >= 0xD800 && c <= 0xDFFF) {
//                if (c < 0xDC00 && i + 1 < end) {
//                    char d = text.charAt(i + 1);
//                    if (d >= 0xDC00 && d <= 0xDFFF) {
//                        i++;
//                        int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
//                        out.append("&#").append(codepoint).append(";");
//                    }
//                }
//            } else if (c > 0x7E || c < ' ') {
//                out.append("&#").append((int) c).append(";");
//            } else if (c == ' ') {
//                while (i + 1 < end && text.charAt(i + 1) == ' ') {
//                    out.append("&nbsp;");
//                    i++;
//                }
//
//                out.append(' ');
//            } else {
//                out.append(c);
//            }
//        }
    }

    private static String tidy(String html) {
        return html.replaceAll("</ul>(<br>)?", "</ul>")
                .replaceAll("</blockquote>(<br>)?", "</blockquote>")
                .replaceAll("</align>(<br>)?", "</align>");
    }
}
