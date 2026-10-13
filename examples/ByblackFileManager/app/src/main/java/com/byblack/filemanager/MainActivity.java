package com.byblack.filemanager;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int OPEN_TREE=7;
    private LinearLayout list;
    private TextView path;
    private Uri current;
    private final ArrayDeque<Uri> history=new ArrayDeque<>();

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,20,20,20); root.setBackgroundColor(Color.rgb(8,16,24));

        TextView title=new TextView(this); title.setText("BYBLACK FILE MANAGER"); title.setTextColor(Color.CYAN);
        title.setTextSize(22); title.setPadding(0,8,0,12); root.addView(title);

        LinearLayout bar=new LinearLayout(this);
        Button choose=button("OPEN FOLDER"); choose.setOnClickListener(v->chooseTree());
        Button back=button("BACK"); back.setOnClickListener(v->goBack());
        Button mkdir=button("NEW FOLDER"); mkdir.setOnClickListener(v->newFolder());
        bar.addView(choose); bar.addView(back); bar.addView(mkdir); root.addView(bar);

        path=new TextView(this); path.setTextColor(Color.LTGRAY); path.setPadding(0,14,0,14); root.addView(path);
        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        Uri saved=b==null?null:b.getParcelable("current");
        if(saved!=null){current=saved; show(current);} else path.setText("Select a folder. Android will ask for permission.");
    }
    private Button button(String s){ Button b=new Button(this); b.setText(s); return b; }
    private void chooseTree(){ startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),OPEN_TREE); }
    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d);
        if(r==OPEN_TREE && c==RESULT_OK && d!=null){
            current=d.getData();
            int flags=d.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try{getContentResolver().takePersistableUriPermission(current,flags);}catch(Exception ignored){}
            history.clear(); show(current);
        }
    }
    private void show(Uri tree){
        list.removeAllViews(); path.setText(tree.toString());
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree));
        String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE};
        try(android.database.Cursor cur=getContentResolver().query(children,cols,null,null,DocumentsContract.Document.COLUMN_DISPLAY_NAME+" ASC")){
            if(cur==null)return;
            while(cur.moveToNext()){
                String id=cur.getString(0), name=cur.getString(1), mime=cur.getString(2);
                long size=cur.isNull(3)?-1:cur.getLong(3);
                Uri u=DocumentsContract.buildDocumentUriUsingTree(tree,id);
                boolean dir=DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
                TextView row=new TextView(this);
                row.setText((dir?"📁 ":"📄 ")+name+(dir?"":size>=0?"   "+size+" B":""));
                row.setTextColor(Color.WHITE); row.setTextSize(17); row.setPadding(12,18,12,18);
                row.setOnClickListener(v->{ if(dir){history.push(current); current=u; show(u);} else openFile(u,mime); });
                row.setOnLongClickListener(v->{ actions(u,name); return true; });
                list.addView(row);
            }
        }catch(Exception e){ Toast.makeText(this,"Cannot read folder: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }
    private void openFile(Uri u,String mime){
        try{Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(u,mime==null?"*/*":mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(i);}
        catch(Exception e){Toast.makeText(this,"No app can open this file",Toast.LENGTH_SHORT).show();}
    }
    private void actions(Uri u,String old){
        new AlertDialog.Builder(this).setTitle(old).setItems(new String[]{"Rename","Delete"},(d,w)->{
            if(w==0)rename(u,old); else delete(u);
        }).show();
    }
    private void rename(Uri u,String old){
        EditText e=new EditText(this); e.setText(old);
        new AlertDialog.Builder(this).setTitle("Rename").setView(e).setPositiveButton("SAVE",(d,w)->{
            try{DocumentsContract.renameDocument(getContentResolver(),u,e.getText().toString().trim()); show(current);}
            catch(Exception x){Toast.makeText(this,"Rename failed",Toast.LENGTH_SHORT).show();}
        }).setNegativeButton("CANCEL",null).show();
    }
    private void delete(Uri u){
        new AlertDialog.Builder(this).setTitle("Delete?").setMessage("This cannot be undone.")
        .setPositiveButton("DELETE",(d,w)->{try{DocumentsContract.deleteDocument(getContentResolver(),u);show(current);}catch(Exception e){Toast.makeText(this,"Delete failed",Toast.LENGTH_SHORT).show();}})
        .setNegativeButton("CANCEL",null).show();
    }
    private void newFolder(){
        if(current==null)return;
        EditText e=new EditText(this);
        new AlertDialog.Builder(this).setTitle("New folder").setView(e).setPositiveButton("CREATE",(d,w)->{
            try{DocumentsContract.createDocument(getContentResolver(),current,DocumentsContract.Document.MIME_TYPE_DIR,e.getText().toString().trim());show(current);}
            catch(Exception x){Toast.makeText(this,"Create failed",Toast.LENGTH_SHORT).show();}
        }).setNegativeButton("CANCEL",null).show();
    }
    private void goBack(){ if(!history.isEmpty()){current=history.pop();show(current);} }
    @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putParcelable("current",current);}
}
