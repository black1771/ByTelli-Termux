package com.byblack.filemanager;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int OPEN_TREE=7;
    private LinearLayout list;
    private TextView path,status;
    private Uri current;
    private final ArrayDeque<Uri> history=new ArrayDeque<>();
    private final ArrayList<Item> items=new ArrayList<>();
    private String filter="";
    private int sortMode=0;

    static class Item {
        Uri uri; String name,mime; long size,modified; boolean dir;
        Item(Uri u,String n,String m,long s,long mod,boolean d){uri=u;name=n;mime=m;size=s;modified=mod;dir=d;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18,18,18,18); root.setBackgroundColor(Color.rgb(7,14,22));

        TextView title=new TextView(this); title.setText("BYBLACK FILE MANAGER V2"); title.setTextColor(Color.CYAN);
        title.setTextSize(22); title.setPadding(0,8,0,10); root.addView(title);

        LinearLayout bar=new LinearLayout(this);
        Button choose=button("STORAGE"); choose.setOnClickListener(v->chooseTree());
        Button back=button("BACK"); back.setOnClickListener(v->goBack());
        Button mkdir=button("+ FOLDER"); mkdir.setOnClickListener(v->newFolder());
        Button sort=button("SORT"); sort.setOnClickListener(v->chooseSort());
        bar.addView(choose);bar.addView(back);bar.addView(mkdir);bar.addView(sort);root.addView(bar);

        EditText search=new EditText(this); search.setHint("Search files...");
        search.setTextColor(Color.WHITE); search.setHintTextColor(Color.GRAY);
        search.setSingleLine(true); root.addView(search);
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int a,int c,int d){}
            public void onTextChanged(CharSequence s,int a,int b,int c){filter=s.toString().toLowerCase(Locale.ROOT);render();}
            public void afterTextChanged(android.text.Editable e){}
        });

        path=new TextView(this); path.setTextColor(Color.LTGRAY); path.setPadding(0,10,0,8); root.addView(path);
        status=new TextView(this); status.setTextColor(Color.GRAY); status.setPadding(0,0,0,8); root.addView(status);

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);

        Uri saved=b==null?null:(Uri)b.getParcelable("current");
        if(saved!=null){current=saved;load(current);} else path.setText("Tap STORAGE and select Internal storage.");
    }

    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);return b;}
    private void chooseTree(){startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),OPEN_TREE);}

    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(r==OPEN_TREE&&c==RESULT_OK&&d!=null){
            current=d.getData();
            int flags=d.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try{getContentResolver().takePersistableUriPermission(current,flags);}catch(Exception ignored){}
            history.clear();load(current);
        }
    }

    private void load(Uri tree){
        items.clear();
        String id=DocumentsContract.getTreeDocumentId(tree);
        path.setText("📂 "+prettyPath(id));
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,id);
        String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED};
        try(android.database.Cursor c=getContentResolver().query(children,cols,null,null,null)){
            if(c!=null)while(c.moveToNext()){
                String did=c.getString(0),name=c.getString(1),mime=c.getString(2);
                long size=c.isNull(3)?-1:c.getLong(3),mod=c.isNull(4)?0:c.getLong(4);
                Uri u=DocumentsContract.buildDocumentUriUsingTree(tree,did);
                boolean dir=DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
                items.add(new Item(u,name,mime,size,mod,dir));
            }
            render();
        }catch(Exception e){Toast.makeText(this,"Cannot read: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    private void render(){
        list.removeAllViews();
        ArrayList<Item> shown=new ArrayList<>();
        for(Item x:items)if(filter.length()==0||x.name.toLowerCase(Locale.ROOT).contains(filter))shown.add(x);
        Collections.sort(shown,(a,b)->{
            if(a.dir!=b.dir)return a.dir?-1:1;
            if(sortMode==1)return Long.compare(b.modified,a.modified);
            if(sortMode==2)return Long.compare(b.size,a.size);
            return a.name.compareToIgnoreCase(b.name);
        });
        status.setText(shown.size()+" items  •  "+(sortMode==0?"Name":sortMode==1?"Newest":"Size"));
        for(Item x:shown){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(14,14,14,14);
            TextView n=new TextView(this);n.setText(icon(x)+"  "+x.name);n.setTextColor(Color.WHITE);n.setTextSize(17);
            TextView meta=new TextView(this);meta.setText(x.dir?"Folder":human(x.size)+(x.modified>0?"  •  "+date(x.modified):""));
            meta.setTextColor(Color.GRAY);meta.setTextSize(12);
            row.addView(n);row.addView(meta);
            row.setOnClickListener(v->{if(x.dir){history.push(current);current=x.uri;load(current);}else openFile(x.uri,x.mime);});
            row.setOnLongClickListener(v->{actions(x);return true;});
            list.addView(row);
            View line=new View(this);line.setBackgroundColor(Color.rgb(35,45,55));list.addView(line,new LinearLayout.LayoutParams(-1,1));
        }
    }

    private String icon(Item x){
        if(x.dir)return "📁";
        String n=x.name.toLowerCase(Locale.ROOT);
        if(n.endsWith(".apk"))return "📦";
        if(n.endsWith(".zip")||n.endsWith(".rar")||n.endsWith(".7z"))return "🗜";
        if(n.endsWith(".jpg")||n.endsWith(".jpeg")||n.endsWith(".png")||n.endsWith(".webp"))return "🖼";
        if(n.endsWith(".mp4")||n.endsWith(".mkv")||n.endsWith(".avi"))return "🎬";
        if(n.endsWith(".mp3")||n.endsWith(".wav")||n.endsWith(".flac"))return "🎵";
        if(n.endsWith(".pdf"))return "📕";
        return "📄";
    }
    private String human(long n){if(n<0)return "";if(n<1024)return n+" B";if(n<1048576)return String.format(Locale.US,"%.1f KB",n/1024.0);if(n<1073741824)return String.format(Locale.US,"%.1f MB",n/1048576.0);return String.format(Locale.US,"%.2f GB",n/1073741824.0);}
    private String date(long t){return new SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(new Date(t));}
    private String prettyPath(String id){int p=id.indexOf(':');return p>=0?"Internal storage/"+id.substring(p+1):id;}

    private void chooseSort(){
        new AlertDialog.Builder(this).setTitle("Sort").setItems(new String[]{"Name A-Z","Newest first","Largest first"},(d,w)->{sortMode=w;render();}).show();
    }
    private void openFile(Uri u,String mime){
        try{Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(u,mime==null?"*/*":mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}
        catch(Exception e){Toast.makeText(this,"No app can open this file",Toast.LENGTH_SHORT).show();}
    }
    private void actions(Item x){
        new AlertDialog.Builder(this).setTitle(x.name).setItems(new String[]{"Rename","Delete","Share"},(d,w)->{
            if(w==0)rename(x);else if(w==1)delete(x);else share(x);
        }).show();
    }
    private void rename(Item x){
        EditText e=new EditText(this);e.setText(x.name);
        new AlertDialog.Builder(this).setTitle("Rename").setView(e).setPositiveButton("SAVE",(d,w)->{
            String n=e.getText().toString().trim();if(n.isEmpty())return;
            try{DocumentsContract.renameDocument(getContentResolver(),x.uri,n);load(current);}catch(Exception z){Toast.makeText(this,"Rename failed",Toast.LENGTH_SHORT).show();}
        }).setNegativeButton("CANCEL",null).show();
    }
    private void delete(Item x){
        new AlertDialog.Builder(this).setTitle("Delete "+x.name+"?").setMessage("This cannot be undone.")
        .setPositiveButton("DELETE",(d,w)->{try{DocumentsContract.deleteDocument(getContentResolver(),x.uri);load(current);}catch(Exception e){Toast.makeText(this,"Delete failed",Toast.LENGTH_SHORT).show();}})
        .setNegativeButton("CANCEL",null).show();
    }
    private void share(Item x){
        if(x.dir){Toast.makeText(this,"Folder sharing is not supported",Toast.LENGTH_SHORT).show();return;}
        try{Intent i=new Intent(Intent.ACTION_SEND);i.setType(x.mime==null?"*/*":x.mime);i.putExtra(Intent.EXTRA_STREAM,x.uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share file"));}
        catch(Exception e){Toast.makeText(this,"Share failed",Toast.LENGTH_SHORT).show();}
    }
    private void newFolder(){
        if(current==null){Toast.makeText(this,"Select storage first",Toast.LENGTH_SHORT).show();return;}
        EditText e=new EditText(this);
        new AlertDialog.Builder(this).setTitle("New folder").setView(e).setPositiveButton("CREATE",(d,w)->{
            String n=e.getText().toString().trim();if(n.isEmpty())return;
            try{DocumentsContract.createDocument(getContentResolver(),current,DocumentsContract.Document.MIME_TYPE_DIR,n);load(current);}catch(Exception z){Toast.makeText(this,"Create failed",Toast.LENGTH_SHORT).show();}
        }).setNegativeButton("CANCEL",null).show();
    }
    private void goBack(){if(!history.isEmpty()){current=history.pop();load(current);}}
    @Override public void onBackPressed(){if(!history.isEmpty())goBack();else super.onBackPressed();}
    @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putParcelable("current",current);}
}
