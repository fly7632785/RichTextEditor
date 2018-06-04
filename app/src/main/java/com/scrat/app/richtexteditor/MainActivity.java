package com.scrat.app.richtexteditor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.scrat.app.richtext.RichEditText;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GET_CONTENT = 666;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 444;
    private static final String INTENT_KEY_HTML = "intent_key_html";
    private RichEditText richEditText;
    public int[] colors = new int[]{
            com.scrat.app.richtext.R.color.md_black_color_code,
            com.scrat.app.richtext.R.color.md_red_500_color_code,
            com.scrat.app.richtext.R.color.md_purple_500_color_code,
            com.scrat.app.richtext.R.color.md_indigo_500_color_code,
            com.scrat.app.richtext.R.color.md_light_blue_500_color_code,
            com.scrat.app.richtext.R.color.md_green_500_color_code,
            com.scrat.app.richtext.R.color.md_lime_500_color_code,
            com.scrat.app.richtext.R.color.md_orange_500_color_code
    };

    public static void launch(Context context, String html) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(INTENT_KEY_HTML, html);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String html = getIntent().getStringExtra(INTENT_KEY_HTML);

        richEditText = (RichEditText) findViewById(R.id.rich_text);
        if (TextUtils.isEmpty(html)) {
            richEditText.fromHtml(
                    "<blockquote>Android 端的富文本编辑器</blockquote>" +
                            "<ul>" +
                            "<li>支持实时编辑</li>" +
                            "<li>支持图片插入,加粗,斜体,下划线,删除线,列表,引用块,超链接,撤销与恢复等</li>" +
                            "<li>使用<u>Glide 4</u>加载图片</li>" +
                            "</ul>" +
                            "<img src=\"http://biuugames.huya.com/221d89ac671feac1.gif\"><br><br>" +
                            "<img src=\"http://biuugames.huya.com/5-160222145918.jpg\"><br><br>"
            );
        } else {
            richEditText.fromHtml(html);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undo:
                richEditText.undo();
                break;
            case R.id.redo:
                richEditText.redo();
                break;
            case R.id.export:
                Log.e("xxx", richEditText.toHtml());
                WebViewActivity.launch(this, richEditText.toHtml());
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (data == null || data.getData() == null || requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            return;

        final Uri uri = data.getData();
        final int width = richEditText.getMeasuredWidth() - richEditText.getPaddingLeft() - richEditText.getPaddingRight();
        richEditText.image(uri, width);
    }


    /**
     * 字体支持12-24 分为7档
     * 对应浏览器的 font size 1-7 默认大小是3
     */
    public void setSize12(View v) {
        richEditText.setFontSize(12);
    }

    public void setSize13(View v) {
        richEditText.setFontSize(14);
    }

    public void setSize14(View v) {
        richEditText.setFontSize(16);
    }

    public void setSize16(View v) {
        richEditText.setFontSize(18);
    }

    public void setSize18(View v) {
        richEditText.setFontSize(20);
    }

    public void setSize20(View v) {
        richEditText.setFontSize(22);
    }

    public void setSize24(View v) {
        richEditText.setFontSize(24);
    }


    public void setBlack(View v) {
        richEditText.setFontColor(colors[0]);
    }

    public void setRed(View v) {
        richEditText.setFontColor(colors[1]);
    }

    public void setPurple(View v) {
        richEditText.setFontColor(colors[2]);
    }

    public void setIndigo(View v) {
        richEditText.setFontColor(colors[3]);
    }

    public void setBlue(View v) {
        richEditText.setFontColor(colors[4]);
    }

    public void setGreen(View v) {
        richEditText.setFontColor(colors[5]);
    }

    public void setLime(View v) {
        richEditText.setFontColor(colors[6]);
    }

    public void setOrange(View v) {
        richEditText.setFontColor(colors[7]);
    }


    /**
     * 加粗
     */
    public void insertBold(View v) {
        v.setSelected(!v.isSelected());
        //设置仅仅使用selected来表示是否选中，建议src drawable改为两种选中和未选中状态图片
        richEditText.setBold(v.isSelected());
    }

    /**
     * 斜体
     */
    public void insertItalic(View v) {
        v.setSelected(!v.isSelected());
        richEditText.setItalicSpan(v.isSelected());
    }

    /**
     * 下划线
     */
    public void insertUnderline(View v) {
        v.setSelected(!v.isSelected());
        richEditText.setUnderline(v.isSelected());
    }

    /**
     * 删除线
     */
    public void insertStrikethrough(View v) {
        v.setSelected(!v.isSelected());
        richEditText.setStrikeThrough(v.isSelected());
    }

    /**
     * link
     */
    public void insertLink(View v) {
        v.setSelected(!v.isSelected());
        richEditText.setLink(v.isSelected());
    }


    /**
     * 加粗
     */
    public void setBold(View v) {
        richEditText.bold(!richEditText.contains(RichEditText.FORMAT_BOLD));
    }

    /**
     * 斜体
     */
    public void setItalic(View v) {
        richEditText.italic(!richEditText.contains(RichEditText.FORMAT_ITALIC));
    }

    /**
     * 下划线
     */
    public void setUnderline(View v) {
        richEditText.underline(!richEditText.contains(RichEditText.FORMAT_UNDERLINED));
    }

    /**
     * 删除线
     */
    public void setStrikethrough(View v) {
        richEditText.strikethrough(!richEditText.contains(RichEditText.FORMAT_STRIKETHROUGH));
    }

    /**
     * link
     */
    public void setLink(View v) {
        v.setSelected(!v.isSelected());
        //通过str是否为空来表示 是把选中的文字变为link或者非link
        /**
         * 注意这里这是测试 把一段文字变为 url或者非url
         * 使用selected仅仅用于测试，最好的方式是，一个设置为link按钮，一个设置为清除link按钮
         * selected对于同一个按钮是不可靠的
         */
        if (v.isSelected()) {
            richEditText.link("http://baidu.com");
        } else {
            richEditText.link("");
        }
    }

    /**
     * 序号
     */
    public void setBullet(View v) {
        richEditText.bullet(!richEditText.contains(RichEditText.FORMAT_BULLET));
    }

    /**
     * 引用块
     */
    public void setQuote(View v) {
        richEditText.quote(!richEditText.contains(RichEditText.FORMAT_QUOTE));
    }

    public void insertImg(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
        getImage.addCategory(Intent.CATEGORY_OPENABLE);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_CODE_GET_CONTENT);
    }

}
