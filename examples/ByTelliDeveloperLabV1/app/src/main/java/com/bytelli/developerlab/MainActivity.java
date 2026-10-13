package com.bytelli.developerlab;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    LinearLayout root, fileList;
    EditText editor;
    TextView status, fileName, lineInfo;
    File projects, currentProject, currentFile;
    Uri externalFile;
    final int OPEN = 10, CREATE = 11;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xff101827);
        getWindow().setNavigationBarColor(0xff101827);
        projects = new File(getFilesDir(), "projects");
        projects.mkdirs();
        buildUI();
        refreshProjects();
    }

    void buildUI() {
        root = new LinearLayout(this);
        root.setOrientation(1);
        root.setBackgroundColor(0xff101827);
        root.setPadding(10, 10, 10, 10);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(1);
        page.setBackgroundColor(0xff101827);

        TextView title = new TextView(this);
        title.setText("ByTelli Developer Lab FINAL V1.0");
        title.setTextSize(19);
        title.setTextColor(0xff59d9ff);
        page.addView(title);

        status = new TextView(this);
        status.setText("Android " + Build.VERSION.RELEASE +
            " | " + Build.SUPPORTED_ABIS[0]);
        status.setTextColor(0xffb6c8dc);
        page.addView(status);

        button(page, "NEW PROJECT", () -> askName("Project name", name -> {
            File f = new File(projects, name);
            if (f.mkdir()) {
                currentProject = f;
                refreshProjects();
            } else message("Project exists or invalid");
        }));

        button(page, "NEW SOURCE FILE", () -> {
            if (currentProject == null) {
                message("Select a project first");
                return;
            }
            askName("File name (example: Main.java)", name -> {
                try {
                    File f = safeChild(currentProject, name);
                    if (!f.createNewFile()) {
                        message("File already exists");
                        return;
                    }
                    openLocal(f);
                    refreshProjects();
                } catch (Exception e) { message(e.getMessage()); }
            });
        });

        button(page, "OPEN EXTERNAL FILE", this::openExternal);
        button(page, "SAVE FILE", this::saveFile);
        button(page, "SAVE AS EXTERNAL FILE", this::createExternal);

        button(page, "SHOW / HIDE PROJECTS", () -> {
            fileList.setVisibility(
                fileList.getVisibility() == View.VISIBLE
                ? View.GONE : View.VISIBLE);
        });

        fileList = new LinearLayout(this);
        fileList.setOrientation(1);
        page.addView(fileList);

        fileName = new TextView(this);
        fileName.setText("No file selected");
        fileName.setTextColor(0xffffd166);
        page.addView(fileName);

        lineInfo = new TextView(this);
        lineInfo.setText("Line 1 | Total 1");
        lineInfo.setTextSize(13);
        lineInfo.setTextColor(0xff7de2a7);
        page.addView(lineInfo);

        editor = new CodeEditor(this);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setBackgroundColor(0xff172235);
        editor.setTextColor(0xffe7edf8);
        editor.setHintTextColor(0xff8293ac);
        editor.setHighlightColor(0xff36567b);
        editor.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE |
            android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setSingleLine(false);
        editor.setTextSize(15);
        editor.setSelectAllOnFocus(false);
        editor.setPadding(dp(55), dp(12), dp(12), dp(12));
        editor.setGravity(Gravity.TOP);
        editor.setMinLines(28);
        editor.setHorizontallyScrolling(true);
        editor.setHint("Write your source code here...");
        editor.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count) {
                int lines = 1;
                for (int i=0;i<s.length();i++)
                    if (s.charAt(i)=='\n') lines++;
                int cursor = Math.min(editor.getSelectionStart(),s.length());
                int line = 1;
                for (int i=0;i<cursor;i++)
                    if (s.charAt(i)=='\n') line++;
                lineInfo.setText("Line " + line + " | Total " + lines);
            }
            boolean coloring = false;

            public void afterTextChanged(Editable e) {
                if (coloring) return;
                coloring = true;
                try {
                    colorCode(e);
                } finally {
                    coloring = false;
                }
            }
        });
        page.addView(editor);

        scroll.addView(page);
        root.addView(scroll);
        setContentView(root);
    }

    void button(LinearLayout p,String label,Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setTextColor(0xfff2f6ff);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(0xff263a57));
        b.setOnClickListener(v -> action.run());
        p.addView(b);
    }

    interface NameCallback { void accept(String name); }

    void askName(String title,NameCallback callback) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK",(d,w) -> {
                String name=input.getText().toString().trim();
                if (!name.matches("[A-Za-z0-9_.-]+") ||
                    name.equals(".") || name.equals("..")) {
                    message("Use letters, numbers, dot, _ or -");
                    return;
                }
                callback.accept(name);
            })
            .setNegativeButton("CANCEL",null)
            .show();
    }

    File safeChild(File parent,String name) throws IOException {
        File f = new File(parent,name);
        if (!f.getCanonicalFile().getParentFile()
              .equals(parent.getCanonicalFile()))
            throw new IOException("Invalid file path");
        return f;
    }

    void refreshProjects() {
        fileList.removeAllViews();
        TextView heading = new TextView(this);
        heading.setText("PROJECT EXPLORER");
        heading.setTextSize(18);
        heading.setTextColor(0xff59d9ff);
        fileList.addView(heading);

        File[] dirs = projects.listFiles();
        if (dirs == null) return;

        for (File dir: dirs) {
            if (!dir.isDirectory()) continue;
            button(fileList,
                (dir.equals(currentProject) ? "▶ " : "📁 ") + dir.getName(),
                () -> {
                    currentProject=dir;
                    refreshProjects();
                });
            if (dir.equals(currentProject)) {
                File[] files=dir.listFiles();
                if (files != null)
                    for (File f:files)
                        if (f.isFile())
                            button(fileList,"    " + f.getName(),
                                () -> openLocal(f));
            }
        }
    }

    void openLocal(File f) {
        try {
            if (f.length()>1024*1024) {
                message("Maximum file size: 1 MB");
                return;
            }
            byte[] data=new byte[(int)f.length()];
            try(FileInputStream in=new FileInputStream(f)) {
                int offset=0,n;
                while(offset<data.length &&
                      (n=in.read(data,offset,data.length-offset))!=-1)
                    offset+=n;
                if(offset!=data.length) throw new IOException("Incomplete read");
            }
            editor.setText(new String(data,StandardCharsets.UTF_8));
            currentFile=f;
            externalFile=null;
            fileName.setText("Editing: " + f.getName());
        } catch(Exception e){ message(e.getMessage()); }
    }

    void saveFile() {
        try {
            byte[] data=editor.getText().toString()
                .getBytes(StandardCharsets.UTF_8);
            if(currentFile!=null) {
                try(FileOutputStream out=new FileOutputStream(currentFile)) {
                    out.write(data);
                }
            } else if(externalFile!=null) {
                try(OutputStream out=getContentResolver()
                    .openOutputStream(externalFile,"wt")) {
                    if(out==null) throw new IOException("Cannot write file");
                    out.write(data);
                }
            } else {
                message("Select or create a file first");
                return;
            }
            message("Saved successfully");
        } catch(Exception e){ message(e.getMessage()); }
    }

    void openExternal() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,OPEN);
    }

    void createExternal() {
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("text/plain");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_TITLE,"ByTelli-Code.txt");
        startActivityForResult(i,CREATE);
    }

    @Override protected void onActivityResult(int req,int res,Intent data) {
        super.onActivityResult(req,res,data);
        if(res!=RESULT_OK || data==null || data.getData()==null) return;
        Uri uri=data.getData();
        if(req==CREATE) {
            currentFile=null;
            externalFile=uri;
            fileName.setText("External: " + uri.getLastPathSegment());
            saveFile();
        } else if(req==OPEN) {
            try(InputStream in=getContentResolver().openInputStream(uri);
                ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buf=new byte[4096];
                int n;
                while((n=in.read(buf))!=-1) {
                    if(out.size()+n>1024*1024)
                        throw new IOException("Maximum file size: 1 MB");
                    out.write(buf,0,n);
                }
                editor.setText(out.toString("UTF-8"));
                currentFile=null;
                externalFile=uri;
                fileName.setText("External: " + uri.getLastPathSegment());
            } catch(Exception e){ message(e.getMessage()); }
        }
    }

    int dp(float value) {
        return (int)(value * getResources()
            .getDisplayMetrics().density + 0.5f);
    }

    class CodeEditor extends EditText {
        final android.graphics.Paint numberPaint =
            new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG);

        CodeEditor(android.content.Context context) {
            super(context);
            numberPaint.setColor(0xff7b8ca8);
            numberPaint.setTextSize(
                12 * getResources()
                    .getDisplayMetrics().scaledDensity);
            numberPaint.setTextAlign(
                android.graphics.Paint.Align.RIGHT);
        }

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);

            android.text.Layout layout = getLayout();
            if (layout == null) return;

            int save = canvas.save();
            canvas.clipRect(
                0, 0, dp(49), getHeight());

            int scroll = getScrollY();
            int first = layout.getLineForVertical(
                Math.max(0, scroll));
            int last = layout.getLineForVertical(
                Math.max(0, scroll + getHeight()));

            int logicalLine = 1;
            CharSequence source = getText();
            int firstStart = layout.getLineStart(first);

            for (int j = 0; j < firstStart; j++)
                if (source.charAt(j) == '\n')
                    logicalLine++;

            for (int i = first; i <= last &&
                 i < layout.getLineCount(); i++) {

                int lineStart = layout.getLineStart(i);

                if (i > first && lineStart > 0 &&
                    source.charAt(lineStart - 1) == '\n')
                    logicalLine++;

                if (i > 0 && lineStart > 0 &&
                    source.charAt(lineStart - 1) != '\n')
                    continue;

                float y = getTotalPaddingTop()
                    + layout.getLineBaseline(i)
                    - scroll;

                canvas.drawText(
                    String.valueOf(logicalLine),
                    dp(43), y, numberPaint);
            }

            canvas.restoreToCount(save);
        }
    }

    class SyntaxSpan extends android.text.style.ForegroundColorSpan {
        SyntaxSpan(int color) { super(color); }
    }

    void colorCode(android.text.Editable e) {
        if (e.length() > 100000) return;

        SyntaxSpan[] spans =
            e.getSpans(0, e.length(),
                SyntaxSpan.class);

        for (SyntaxSpan span : spans)
            e.removeSpan(span);

        String code = e.toString();

        String words =
            "\\b(class|public|private|protected|static|void|" +
            "int|long|float|double|boolean|char|String|new|" +
            "return|if|else|for|while|switch|case|break|" +
            "continue|import|package|extends|implements|" +
            "def|print|from|as|elif|except|try|finally|" +
            "include|using|namespace|struct|const|auto|" +
            "true|false|True|False|None)\\b";

        java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile(
                "//[^\\n]*|#[^\\n]*|\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|" +
                "'(?:\\\\.|[^'\\\\])*'|" + words);

        java.util.regex.Matcher m = pattern.matcher(code);

        while (m.find()) {
            String token = m.group();
            int color;

            if (token.startsWith("//") || token.startsWith("#"))
                color = 0xff65c98a;
            else if (token.startsWith("\\\"") || token.startsWith("'"))
                color = 0xffffb86c;
            else
                color = 0xff8ab4ff;

            e.setSpan(
                new SyntaxSpan(color),
                m.start(), m.end(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    void message(String s) {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }
}
