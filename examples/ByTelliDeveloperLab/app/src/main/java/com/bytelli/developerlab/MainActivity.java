package com.bytelli.developerlab;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity {
    private TextView status;
    private EditText editor;
    private Uri openedFile;
    private static final int OPEN = 10, CREATE = 11;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        ScrollView scroll = new ScrollView(this);
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("ByTelli Developer Lab • V0.1");
        title.setTextSize(22);
        controls.addView(title);
        status = new TextView(this);
        status.setText("Android " + Build.VERSION.RELEASE + " • SDK " + Build.VERSION.SDK_INT
            + "\nDevice: " + Build.MANUFACTURER + " " + Build.MODEL
            + "\nABI: " + android.text.TextUtils.join(", ", Build.SUPPORTED_ABIS)
            + "\nFiles: " + getFilesDir().getAbsolutePath());
        controls.addView(status);
        addButton(controls, "Open text file", v -> openDocument());
        addButton(controls, "Save as new text file", v -> createDocument());
        addButton(controls, "Save opened file", v -> saveOpened());
        editor = new EditText(this);
        editor.setGravity(android.view.Gravity.TOP);
        editor.setTextSize(14);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setMinLines(12);
        editor.setHint("Edit Java, Python, C or C++ text...");
        controls.addView(editor);
        scroll.addView(controls);
        layout.addView(scroll);
        setContentView(layout);
    }
    private void addButton(LinearLayout parent, String label, View.OnClickListener click) {
        Button b = new Button(this); b.setText(label); b.setOnClickListener(click); parent.addView(b);
    }
    private void openDocument() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, OPEN);
    }
    private void createDocument() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("text/plain");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_TITLE, "source.txt");
        startActivityForResult(i, CREATE);
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (request == OPEN) {
            try (InputStream in = getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) != -1) {
                    if (out.size() + n > 1024 * 1024) throw new IOException("File exceeds 1 MB editor limit");
                    out.write(buf, 0, n);
                }
                editor.setText(out.toString("UTF-8"));
                openedFile = uri;
                status.setText("Opened: " + uri);
            } catch (Exception e) { showError(e); }
        } else if (request == CREATE) {
            openedFile = uri;
            saveOpened();
        }
    }
    private void saveOpened() {
        if (openedFile == null) { Toast.makeText(this, "Open or create a file first", Toast.LENGTH_SHORT).show(); return; }
        try (OutputStream out = getContentResolver().openOutputStream(openedFile, "wt")) {
            if (out == null) throw new IOException("Cannot open output");
            out.write(editor.getText().toString().getBytes("UTF-8"));
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { showError(e); }
    }
    private void showError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
