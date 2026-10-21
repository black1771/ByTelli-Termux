package com.byblack.battlechess;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import java.nio.*;
import java.util.*;

public class Arena3D extends GLSurfaceView {
  Renderer3D renderer;
  volatile int selected=-1;
  int[][] board=new int[8][8];
  boolean white=true;
  long start=0;
  int fx,fy,tx,ty,moving,captured;
  boolean anim=false;
  Random rnd=new Random();
  boolean versusCpu=true;
  int difficulty=1;
  android.content.SharedPreferences saves;
  void settings(boolean cpu,int level){versusCpu=cpu;difficulty=level;reset();}
  void saveGame(){
    StringBuilder b=new StringBuilder();
    for(int y=0;y<8;y++)for(int x=0;x<8;x++)b.append(board[y][x]).append(',');
    saves.edit().putString("board",b.toString()).putBoolean("white",white)
      .putBoolean("cpu",versusCpu).putInt("difficulty",difficulty).apply();
  }
  boolean loadGame(){
    String v=saves.getString("board","");String[] cells=v.split(",");
    if(cells.length!=64)return false;
    int[][] tmp=new int[8][8];
    try{for(int i=0;i<64;i++){int n=Integer.parseInt(cells[i]);if(n< -6||n>6)return false;tmp[i/8][i%8]=n;}}
    catch(Exception e){return false;}
    for(int y=0;y<8;y++)System.arraycopy(tmp[y],0,board[y],0,8);
    white=saves.getBoolean("white",true);versusCpu=saves.getBoolean("cpu",true);
    difficulty=saves.getInt("difficulty",1);selected=-1;anim=false;
    if(versusCpu&&!white)cpuMove();
    return true;
  }
  int value(int piece){int[] v={0,100,320,330,500,900,20000};return v[Math.abs(piece)];}
  int scoreAfter(int[] m){
    int a=board[m[1]][m[0]],t=board[m[3]][m[2]];
    board[m[3]][m[2]]=a;board[m[1]][m[0]]=0;
    int score=value(t)*10;
    if(difficulty>=1){
      for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(board[y][x]>0)
        for(int yy=0;yy<8;yy++)for(int xx=0;xx<8;xx++)
          if(raw(x,y,xx,yy)&&xx==m[2]&&yy==m[3])score-=value(a)*7;
    }
    if(difficulty>=2)score+=(3-Math.abs(3-m[2]))*4+(3-Math.abs(3-m[3]))*4;
    board[m[1]][m[0]]=a;board[m[3]][m[2]]=t;
    return score;
  }
  void cpuMove(){
    if(anim||white||!versusCpu)return;
    ArrayList<int[]> moves=new ArrayList<>();
    for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(board[y][x]<0)
      for(int yy=0;yy<8;yy++)for(int xx=0;xx<8;xx++)
        if(raw(x,y,xx,yy))moves.add(new int[]{x,y,xx,yy});
    if(moves.isEmpty())return;
    int[] best=moves.get(rnd.nextInt(moves.size()));
    if(difficulty>0){int top=Integer.MIN_VALUE;
      for(int[] m:moves){int score=scoreAfter(m)+rnd.nextInt(difficulty==1?80:12);
        if(score>top){top=score;best=m;}}
    }
    begin(best[0],best[1],best[2],best[3]);
  }

  public Arena3D(Context c){
    super(c);
    setEGLContextClientVersion(2);
    saves=c.getSharedPreferences("battlechess_v132",Context.MODE_PRIVATE);
    renderer=new Renderer3D();
    setRenderer(renderer);
    setRenderMode(RENDERMODE_CONTINUOUSLY);
    reset();
  }
  void reset(){
    int[] back={4,2,3,5,6,3,2,4};
    for(int y=0;y<8;y++)for(int x=0;x<8;x++)
      board[y][x]=(y==0?-back[x]:y==1?-1:y==6?1:y==7?back[x]:0);
    selected=-1;white=true;anim=false;
  }
  boolean inside(int x,int y){return x>=0&&x<8&&y>=0&&y<8;}
  boolean raw(int x,int y,int xx,int yy){
    if(!inside(x,y)||!inside(xx,yy)||(x==xx&&y==yy))return false;
    int a=board[y][x],t=board[yy][xx];
    if(a==0||a*t>0||Math.abs(t)==6)return false;
    int dx=xx-x,dy=yy-y,ax=Math.abs(dx),ay=Math.abs(dy),k=Math.abs(a);
    if(k==1)return a>0?
      ((dx==0&&dy==-1&&t==0)||(y==6&&dx==0&&dy==-2&&t==0&&board[5][x]==0)||(ax==1&&dy==-1&&t<0)):
      ((dx==0&&dy==1&&t==0)||(y==1&&dx==0&&dy==2&&t==0&&board[2][x]==0)||(ax==1&&dy==1&&t>0));
    if(k==2)return ax*ay==2;
    if(k==6)return ax<=1&&ay<=1;
    if(k==3&&ax!=ay)return false;
    if(k==4&&dx!=0&&dy!=0)return false;
    if(k==5&&!(ax==ay||dx==0||dy==0))return false;
    int sx=Integer.signum(dx),sy=Integer.signum(dy);
    for(int cx=x+sx,cy=y+sy;cx!=xx||cy!=yy;cx+=sx,cy+=sy)
      if(board[cy][cx]!=0)return false;
    return true;
  }
  void begin(int x,int y,int xx,int yy){
    fx=x;fy=y;tx=xx;ty=yy;
    moving=board[y][x];captured=board[yy][xx];
    start=System.currentTimeMillis();anim=true;selected=-1;
  }
  void complete(){
    board[ty][tx]=moving;board[fy][fx]=0;
    if(Math.abs(moving)==1&&(ty==0||ty==7))board[ty][tx]=moving>0?5:-5;
    anim=false;white=!white;
    if(!white&&versusCpu)cpuMove();
  }
  @Override public boolean onTouchEvent(MotionEvent e){
    if(e.getAction()!=MotionEvent.ACTION_UP)return true;
    if(anim||(!white&&versusCpu))return true;

    float nx=2f*e.getX()/getWidth()-1f;
    float ny=1f-2f*e.getY()/getHeight();

    float[] pv=new float[16];
    float[] inv=new float[16];

    float[] touchView=new float[16];
    renderer.setBattleCamera(touchView, false);
    Matrix.multiplyMM(pv,0,renderer.proj,0,touchView,0);
    if(!Matrix.invertM(inv,0,pv,0))return true;

    float[] near={nx,ny,-1f,1f};
    float[] far={nx,ny,1f,1f};
    float[] a=new float[4],b=new float[4];

    Matrix.multiplyMV(a,0,inv,0,near,0);
    Matrix.multiplyMV(b,0,inv,0,far,0);

    if(Math.abs(a[3])<.00001f||Math.abs(b[3])<.00001f)return true;

    for(int i=0;i<3;i++){
      a[i]/=a[3];
      b[i]/=b[3];
    }

    float dy=b[1]-a[1];
    if(Math.abs(dy)<.00001f)return true;

    float t=(.085f-a[1])/dy;
    if(t<0)return true;

    float wx=a[0]+t*(b[0]-a[0]);
    float wz=a[2]+t*(b[2]-a[2]);

    int x=(int)Math.floor(wx+4f);
    int y=(int)Math.floor(wz+4f);

    if(!inside(x,y))return true;

    if(selected>=0&&raw(selected%8,selected/8,x,y)){
      begin(selected%8,selected/8,x,y);
    }else{
      selected=(board[y][x]!=0 && (board[y][x]>0)==white)?y*8+x:-1;
    }

    return true;
  }

  class Renderer3D implements GLSurfaceView.Renderer {
    int program,posLoc,colLoc,mvpLoc;
    float[] proj=new float[16],view=new float[16],model=new float[16],mv=new float[16],mvp=new float[16];
    float[] cube={
      -1,-1,1, 1,-1,1, 1,1,1, -1,-1,1,1,1,1,-1,1,1,
      1,-1,-1,-1,-1,-1,-1,1,-1,1,-1,-1,-1,1,-1,1,1,-1,
      -1,-1,-1,-1,-1,1,-1,1,1,-1,-1,-1,-1,1,1,-1,1,-1,
      1,-1,1,1,-1,-1,1,1,-1,1,-1,1,1,1,-1,1,1,1,
      -1,1,1,1,1,1,1,1,-1,-1,1,1,1,1,-1,-1,1,-1,
      -1,-1,-1,1,-1,-1,1,-1,1,-1,-1,-1,1,-1,1,-1,-1,1
    };
    FloatBuffer vb;
    int shader(int type,String src){
      int sh=GLES20.glCreateShader(type);
      GLES20.glShaderSource(sh,src);GLES20.glCompileShader(sh);
      return sh;
    }
    @Override public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl,
      javax.microedition.khronos.egl.EGLConfig config){
      GLES20.glClearColor(.11f,.14f,.17f,1f);
      GLES20.glEnable(GLES20.GL_DEPTH_TEST);
      String vs="attribute vec3 aPos;uniform mat4 uMVP;void main(){gl_Position=uMVP*vec4(aPos,1.0);}";
      String fs="precision mediump float;uniform vec4 uColor;void main(){gl_FragColor=uColor;}";
      program=GLES20.glCreateProgram();
      GLES20.glAttachShader(program,shader(GLES20.GL_VERTEX_SHADER,vs));
      GLES20.glAttachShader(program,shader(GLES20.GL_FRAGMENT_SHADER,fs));
      GLES20.glLinkProgram(program);
      posLoc=GLES20.glGetAttribLocation(program,"aPos");
      colLoc=GLES20.glGetUniformLocation(program,"uColor");
      mvpLoc=GLES20.glGetUniformLocation(program,"uMVP");
      ByteBuffer bb=ByteBuffer.allocateDirect(cube.length*4).order(ByteOrder.nativeOrder());
      vb=bb.asFloatBuffer();vb.put(cube).position(0);
      loadKnight();
      knightV26=new KnightV26(getContext());
      kingV35=new KingV35(getContext());
      unitsV36=new UnitV37[7];
      v113Ivory=new UnitV37[7];
      v113Sun=new UnitV37[7];
      v115Kings=new UnitV37[2];
      v115Kings[0]=new UnitV37(getContext(),"ivory_king");
      v115Kings[1]=new UnitV37(getContext(),"sun_king");

      for(int k:new int[]{1,3,4}){
        String role=k==1?"pawn":k==3?"bishop":"rook";
        v113Ivory[k]=new UnitV37(getContext(),"ivory_"+role);
        v113Sun[k]=new UnitV37(getContext(),"sun_"+role);
      }

      unitsV36[1]=new UnitV37(getContext(),"pawn");
      unitsV36[2]=new UnitV37(getContext(),"knight");
      unitsV36[3]=new UnitV37(getContext(),"bishop");
      unitsV36[4]=new UnitV37(getContext(),"rook");
      unitsV36[5]=new UnitV37(getContext(),"queen");
      unitsV54Rider=new UnitV37(getContext(),"rider");
      unitsV36[6]=new UnitV37(getContext(),"king");
      for(UnitV37 unit:unitsV36)if(unit!=null)unit.initGL(getContext());
      for(UnitV37 unit:v113Ivory)
        if(unit!=null)unit.initGL(getContext());
      for(UnitV37 unit:v113Sun)
        if(unit!=null)unit.initGL(getContext());
      for(UnitV37 unit:v115Kings)if(unit!=null)unit.initGL(getContext());

      unitsV54Rider.initGL(getContext());
      loadEquipment();
    }
    @Override public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl,int w,int h){
      GLES20.glViewport(0,0,w,h);
      v127Aspect=(float)w/h;
      Matrix.perspectiveM(proj,0,58f,v127Aspect,.1f,100f);
    }
    void box(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b){
      Matrix.setIdentityM(model,0);
      Matrix.translateM(model,0,x,y,z);
      Matrix.scaleM(model,0,sx,sy,sz);
      Matrix.multiplyMM(mv,0,view,0,model,0);
      Matrix.multiplyMM(mvp,0,proj,0,mv,0);
      GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
      GLES20.glUniform4f(colLoc,r,g,b,1);
      vb.position(0);
      GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,vb);
      GLES20.glEnableVertexAttribArray(posLoc);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);
    }


    FloatBuffer sphereBuffer;
    int sphereVertices;
    void prepareSphere(){
      final int rings=10, slices=16;
      float[] data=new float[rings*slices*18];
      int at=0;
      for(int i=0;i<rings;i++){
        double a0=-Math.PI/2+Math.PI*i/rings;
        double a1=-Math.PI/2+Math.PI*(i+1)/rings;
        for(int j=0;j<slices;j++){
          double b0=2*Math.PI*j/slices,b1=2*Math.PI*(j+1)/slices;
          float[] v0={(float)(Math.cos(a0)*Math.cos(b0)),(float)Math.sin(a0),(float)(Math.cos(a0)*Math.sin(b0))};
          float[] v1={(float)(Math.cos(a1)*Math.cos(b0)),(float)Math.sin(a1),(float)(Math.cos(a1)*Math.sin(b0))};
          float[] v2={(float)(Math.cos(a1)*Math.cos(b1)),(float)Math.sin(a1),(float)(Math.cos(a1)*Math.sin(b1))};
          float[] v3={(float)(Math.cos(a0)*Math.cos(b1)),(float)Math.sin(a0),(float)(Math.cos(a0)*Math.sin(b1))};
          for(float[] v:new float[][]{v0,v1,v2,v0,v2,v3}) for(float f:v)data[at++]=f;
        }
      }
      ByteBuffer bb=ByteBuffer.allocateDirect(at*4).order(ByteOrder.nativeOrder());
      sphereBuffer=bb.asFloatBuffer();sphereBuffer.put(data,0,at);sphereBuffer.position(0);
      sphereVertices=at/3;
    }
    void orb(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b){
      if(sphereBuffer==null)prepareSphere();
      Matrix.setIdentityM(model,0);
      Matrix.translateM(model,0,x,y,z);
      Matrix.scaleM(model,0,sx,sy,sz);
      Matrix.multiplyMM(mv,0,view,0,model,0);
      Matrix.multiplyMM(mvp,0,proj,0,mv,0);
      GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
      GLES20.glUniform4f(colLoc,r,g,b,1);
      sphereBuffer.position(0);
      GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,sphereBuffer);
      GLES20.glEnableVertexAttribArray(posLoc);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,sphereVertices);
    }
    UnitV37[] unitsV36;
    UnitV37[] v113Ivory,v113Sun;
      UnitV37[] v115Kings;
    UnitV37 unitsV54Rider;
    KingV35 kingV35;
    KnightV26 knightV26;
    FloatBuffer[] knightMeshes;
    int[] knightCounts;
    float[][] knightColors;
    void loadKnight(){
      String[] files=new String[]{"knight_0.bin","knight_1.bin","knight_2.bin"};
      int[] counts=new int[]{1194,1308,366};
      float[][] colors=new float[][]{{0.1218600f,0.1218600f,0.1218600f},{0.5987200f,0.4461570f,0.2585100f},{0.0248510f,0.0100880f,0.0063240f}};
      knightMeshes=new FloatBuffer[files.length];
      knightCounts=counts; knightColors=colors;
      for(int k=0;k<files.length;k++){
        try(java.io.InputStream in=getContext().getAssets().open(files[k])){
          byte[] data=new byte[counts[k]*3*4];
          int off=0,n;
          while(off<data.length&&(n=in.read(data,off,data.length-off))!=-1)off+=n;
          if(off!=data.length)throw new java.io.IOException("Incomplete mesh");
          ByteBuffer bb=ByteBuffer.allocateDirect(data.length).order(ByteOrder.LITTLE_ENDIAN);
          bb.put(data);bb.position(0);
          knightMeshes[k]=bb.asFloatBuffer();
        }catch(Exception e){android.util.Log.e("ByblackV18","OBJ load error",e);knightMeshes=null;return;}
      }
    }
    void drawKnight(int v,float x,float z,float t,boolean falling){
      if(knightV26!=null){
        int clip=0;
        float seconds=(System.currentTimeMillis()%1000000L)/1000f;
        if(anim && v==moving){
          float elapsed=(System.currentTimeMillis()-start)/1000f;
          clip=captured!=0 && elapsed>=.91f?2:1;
          seconds=clip==2?Math.max(0f,elapsed-.91f):elapsed;
        }else if(anim && falling){
          clip=3;
          seconds=Math.max(0f,(System.currentTimeMillis()-start)/1000f-1.30f);
        }
        if(knightV26.draw(clip,seconds,view,proj,x,z,v,posLoc,colLoc,mvpLoc))return;
      }
      if(knightMeshes==null)return;
      Matrix.setIdentityM(model,0);
      Matrix.translateM(model,0,x,.095f,z);
      Matrix.rotateM(model,0,v>0?180f:0f,0,1,0);
      float h=falling?Math.max(.18f,1f-Math.min(1f,t)*.75f):1f;
      Matrix.scaleM(model,0,.29f,.29f*h,.29f);
      Matrix.multiplyMM(mv,0,view,0,model,0);
      Matrix.multiplyMM(mvp,0,proj,0,mv,0);
      GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
      for(int k=0;k<knightMeshes.length;k++){
        float[] c=knightColors[k];
        float side=v>0?1f:.69f;
        GLES20.glUniform4f(colLoc,Math.min(1f,(c[0]*.58f+.40f)*side),Math.min(1f,(c[1]*.58f+.39f)*side),Math.min(1f,(c[2]*.58f+.43f)*(v>0?1f:1.10f)),1f);
        knightMeshes[k].position(0);
        GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,knightMeshes[k]);
        GLES20.glEnableVertexAttribArray(posLoc);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,knightCounts[k]);
      }
    }
    FloatBuffer[][] equipmentMeshes;
    int[][] equipmentCounts;
    float[][][] equipmentColors;
    void loadEquipment(){
      String[][] files=new String[][]{{"v20_helmet1_0.bin","v20_helmet1_1.bin"},{"v20_sword_0.bin","v20_sword_1.bin","v20_sword_2.bin","v20_sword_3.bin"},{"v20_shoulderpads_0.bin"}};
      int[][] counts=new int[][]{{924,780},{168,240,1008,156},{288}};
      float[][][] colors=new float[][][]{{{0.3433750f,0.3433750f,0.3433750f},{0.2248030f,0.2248030f,0.2248030f}},{{0.3975350f,0.3975350f,0.3975350f},{0.1139180f,0.0124010f,0.0055250f},{0.5200000f,0.2268810f,0.0393730f},{0.0852250f,0.0372500f,0.0163930f}},{{0.3074950f,0.3074950f,0.3074950f}}};
      equipmentMeshes=new FloatBuffer[files.length][];
      equipmentCounts=counts;equipmentColors=colors;
      try{
        for(int k=0;k<files.length;k++){
          equipmentMeshes[k]=new FloatBuffer[files[k].length];
          for(int i=0;i<files[k].length;i++){
            byte[] data=new byte[counts[k][i]*12];
            try(java.io.InputStream in=getContext().getAssets().open(files[k][i])){
              int off=0,n;
              while(off<data.length&&(n=in.read(data,off,data.length-off))!=-1)off+=n;
              if(off!=data.length)throw new java.io.IOException("Incomplete equipment mesh");
            }
            ByteBuffer bb=ByteBuffer.allocateDirect(data.length).order(ByteOrder.LITTLE_ENDIAN);
            bb.put(data);bb.position(0);equipmentMeshes[k][i]=bb.asFloatBuffer();
          }
        }
      }catch(Exception e){android.util.Log.e("ByblackV20","Equipment load failed",e);equipmentMeshes=null;}
    }
    float swordAction=0f;
    void drawEquipment(int type,int v,float x,float y,float z,float sx,float sy,float sz){
      if(equipmentMeshes==null)return;
      Matrix.setIdentityM(model,0);
      Matrix.translateM(model,0,x,y,z);
      if(type==1 && swordAction>0f){
        // Rotate the imported sword around its grip during a capture.
        Matrix.rotateM(model,0,-75f*swordAction,0,0,1);
        Matrix.rotateM(model,0,30f*swordAction,1,0,0);
      }
      Matrix.rotateM(model,0,v>0?180f:0f,0,1,0);
      Matrix.scaleM(model,0,sx,sy,sz);
      Matrix.multiplyMM(mv,0,view,0,model,0);
      Matrix.multiplyMM(mvp,0,proj,0,mv,0);
      GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
      for(int i=0;i<equipmentMeshes[type].length;i++){
        float[] c=equipmentColors[type][i];
        float r=Math.min(1f,c[0]*.45f+(v>0?.48f:.28f));
        float g=Math.min(1f,c[1]*.45f+(v>0?.40f:.34f));
        float b=Math.min(1f,c[2]*.45f+(v>0?.22f:.48f));
        GLES20.glUniform4f(colLoc,r,g,b,1f);
        FloatBuffer mesh=equipmentMeshes[type][i];mesh.position(0);
        GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,mesh);
        GLES20.glEnableVertexAttribArray(posLoc);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,equipmentCounts[type][i]);
      }
    }
    boolean v34MovingPiece=false;
    void v34Identity(int v,float x,float z){
      int kind=Math.abs(v); boolean gold=v>0;
      float r=gold?.95f:.36f,g=gold?.70f:.49f,b=gold?.23f:.83f;
      box(x,.018f,z,.32f,.024f,.32f,r*.65f,g*.65f,b*.65f);
      if(kind==1){orb(x,1.12f,z,.13f,.08f,.13f,r,g,b);}
      else if(kind==2){
        orb(x,1.05f,z-.22f,.16f,.12f,.24f,r,g,b);
        box(x-.10f,1.24f,z-.12f,.035f,.12f,.045f,r,g,b);
        box(x+.10f,1.24f,z-.12f,.035f,.12f,.045f,r,g,b);
      }else if(kind==3){
        box(x,1.20f,z,.115f,.21f,.115f,r,g,b);
        orb(x,1.42f,z,.055f,.07f,.055f,1f,.92f,.70f);
      }else if(kind==4){
        box(x,1.14f,z,.27f,.095f,.24f,r,g,b);
        for(int i=-1;i<=1;i++)box(x+i*.19f,1.26f,z,.067f,.10f,.19f,r,g,b);
      }else if(kind==5){
        box(x,1.16f,z,.23f,.07f,.22f,r,g,b);
        for(int i=-2;i<=2;i++)box(x+i*.10f,1.29f,z,.037f,.10f+(2-Math.abs(i))*.025f,.045f,r,g,b);
      }else if(kind==6){
        box(x,1.23f,z,.09f,.20f,.08f,r,g,b);
        box(x,1.36f,z,.22f,.045f,.08f,r,g,b);
      }
    }
    void warrior(int v,float x,float z,float t,boolean falling){
      int v36Role=Math.abs(v);
      UnitV37[] selectedUnits=unitsV36;
      if(v36Role==6){
        selectedUnits=new UnitV37[7];
        selectedUnits[6]=v>0?v115Kings[0]:v115Kings[1];
      } else if(v36Role==1||v36Role==3||v36Role==4){
        selectedUnits=v>0?v113Ivory:v113Sun;
      }

      if(unitsV36!=null && v36Role<selectedUnits.length &&
         selectedUnits[v36Role]!=null){
        int clip=0;
        float seconds=0f;
        if(anim && falling){
          clip=3;
          seconds=Math.max(0f,(System.currentTimeMillis()-start)/1000f-1.30f);
        }else if(anim && v34MovingPiece){
          float elapsed=Math.max(0f,(System.currentTimeMillis()-start)/1000f);
          if(captured!=0 && elapsed>=.91f){
            clip=2;
            seconds=elapsed-.91f;
          }else{
            clip=1;
            seconds=elapsed;
          }
        }
        boolean drawn=selectedUnits[v36Role].draw(
            clip,seconds,view,proj,x,z,
            v,posLoc,colLoc,mvpLoc,program);
        if(drawn){
          if(v36Role==2 && unitsV54Rider!=null){
            // V46: Atin uzerinde gecici insan binici.
            // Hareket ederken atla ayni konumu izler.
            int riderClip=(clip==3)?3:((clip==2)?2:((clip==1)?1:0));
            float[] saddle=selectedUnits[v36Role].saddleWorldOffset(clip,seconds,v);
            // V88: sync normalized animation phase, not raw seconds.
            // Horse/rider durations differ (e.g. attack 2.5s vs 1.067s).
            float riderSeconds=selectedUnits[v36Role].mountedRiderTime(
                clip,seconds,unitsV54Rider,riderClip);
            unitsV54Rider.drawMountedRotated(
                riderClip,riderSeconds,view,proj,x+saddle[0],z+saddle[2],
                v,posLoc,colLoc,mvpLoc,program,
                saddle[1],0.46f,
                selectedUnits[v36Role].saddleRelativeQuaternion(clip,seconds));
          }
          return;
        }
      }

      // V35 KING GLB
      // V35 KING GLB: stationary pieces use idle pose; only active pieces animate.
      if(Math.abs(v)==6 && kingV35!=null){
        int kc=0;float ks=0f;
        if(anim && falling){kc=3;ks=Math.max(0f,(System.currentTimeMillis()-start)/1000f-1.25f);}
        else if(anim && v34MovingPiece){
          float elapsed=Math.max(0f,(System.currentTimeMillis()-start)/1000f);
          if(captured!=0 && elapsed>=.91f){kc=2;ks=elapsed-.91f;}
          else {kc=1;ks=elapsed;}
        }
        if(kingV35.draw(kc,ks,view,proj,x,z,v,posLoc,colLoc,mvpLoc))return;
      }

      // V34 FIX2: only the active moving piece plays walk/attack.
      if(knightV26!=null){
        int clip=0;
        float seconds=0f;
        if(anim && falling){
          clip=3;
          seconds=Math.max(0f,(System.currentTimeMillis()-start)/1000f-1.25f);
        }else if(anim && v34MovingPiece){
          float elapsed=Math.max(0f,(System.currentTimeMillis()-start)/1000f);
          if(captured!=0 && elapsed>=.91f){clip=2;seconds=elapsed-.91f;}
          else {clip=1;seconds=elapsed;}
        }
        if(knightV26.draw(clip,seconds,view,proj,x,z,v,posLoc,colLoc,mvpLoc)){
          v34Identity(v,x,z);
          return;
        }
      }
      if(Math.abs(v)==2 && knightMeshes!=null){
        float shrink=falling?Math.max(.25f,1f-Math.min(1f,t)*.75f):1f;
        box(x,.095f,z,.36f,.040f,.36f,v>0?.72f:.30f,v>0?.57f:.38f,v>0?.23f:.57f);
        drawKnight(v,x,z,t,falling);
        drawEquipment(0,v,x,1.46f*shrink,z,.54f,.40f*shrink,.54f);
        drawEquipment(2,v,x,.98f*shrink,z,.80f,.42f*shrink,.62f);
        // Animate only the active knight during a capture; stationary pieces stay still.
        swordAction=(anim && captured!=0 && v==moving && t>0.20f && t<0.80f)
          ? (float)Math.sin(Math.PI*(t-.20f)/.60f) : 0f;
        if(!(anim && captured!=0 && v==moving && t>=.35f && t<=.83f))
          drawEquipment(1,v,x+.40f,.70f*shrink,z,.13f,.86f*shrink,.13f);
        swordAction=0f;
        return;
      }
      // V31_REAL_FBX_ALL_PIECES: real skinned FBX geometry for all chess roles.
      if(knightV26!=null){
        int role=Math.abs(v);
        float sec=(System.currentTimeMillis()%1000000L)/1000f;
        int clip=0;
        if(anim && v==moving){
          float elapsed=(System.currentTimeMillis()-start)/1000f;
          clip=captured!=0 && elapsed>=.91f?2:1;
          sec=clip==2?Math.max(0f,elapsed-.91f):elapsed;
        }else if(anim && falling){
          clip=3; sec=Math.max(0f,(System.currentTimeMillis()-start)/1000f-1.25f);
        }
        if(knightV26.draw(clip,sec,view,proj,x,z,v,posLoc,colLoc,mvpLoc)){
          // Small role markers. The base character is the actual FBX mesh.
          float cr=v>0?.84f:.42f,cg=v>0?.65f:.48f,cb=v>0?.26f:.70f;
          if(role==1) orb(x,1.36f,z,.075f,.075f,.075f,cr,cg,cb);
          if(role==3){orb(x,1.43f,z,.10f,.18f,.10f,cr,cg,cb);}
          if(role==4){box(x,1.39f,z,.22f,.11f,.22f,cr,cg,cb);}
          if(role==5){orb(x,1.48f,z,.20f,.09f,.20f,cr,cg,cb);orb(x,1.60f,z,.07f,.07f,.07f,.98f,.84f,.35f);}
          if(role==6){orb(x,1.47f,z,.20f,.10f,.20f,cr,cg,cb);box(x,1.67f,z,.05f,.14f,.05f,.98f,.84f,.35f);}
          return;
        }
      }
      int kind=Math.abs(v);
      boolean good=v>0;

      float r=good?.58f:.28f;
       float g=good?.61f:.31f;
       float b=good?.68f:.39f;

      float fall=falling?Math.min(1f,t):0f;
      float h=1f-fall*.75f;
      float bob=falling?0f:(float)Math.abs(Math.sin(t*12f))*.06f;

      // Ground base and boots
      // Every unit has a readable faction-coloured base.
       box(x,.025f,z,.31f,.035f,.31f,good?.17f:.37f,good?.30f:.10f,good?.48f:.12f);
       box(x,.067f,z,.27f,.016f,.27f,good?.74f:.48f,good?.57f:.27f,good?.23f:.22f);
      box(x-.13f,.16f*h,z,.10f,.16f*h,.13f,.19f,.19f,.22f);
      box(x+.13f,.16f*h,z,.10f,.16f*h,.13f,.19f,.19f,.22f);

      // Armored torso
      orb(x,.49f*h+bob,z,.28f,.33f*h,.22f,r,g,b);

      // Shoulder armor
      orb(x-.32f,.66f*h+bob,z,.15f,.14f*h,.21f,.42f,.44f,.48f);
      orb(x+.32f,.66f*h+bob,z,.15f,.14f*h,.21f,.42f,.44f,.48f);

      // Head and helmet
      orb(x,.94f*h+bob,z,.18f,.19f*h,.18f,.70f,.58f,.43f);
      orb(x,1.08f*h+bob,z,.23f,.13f*h,.23f,r*.65f,g*.65f,b*.65f);

      // Shield on left arm
      box(x-.43f,.49f*h+bob,z+.10f,.07f,.30f*h,.25f,
          good?.14f:.47f,good?.32f:.10f,good?.66f:.13f);

      // Sword on right arm
      float swing=falling?0f:(float)Math.sin(t*18f)*.12f;
      box(x+.44f+swing,.64f*h+bob,z,.045f,.34f*h,.045f,
          .78f,.84f,.89f);
      box(x+.44f+swing,.36f*h+bob,z,.16f,.035f,.055f,
          .82f,.65f,.26f);

      if(kind==1){
        // Pawn helmet
        box(x,1.20f*h+bob,z,.12f,.07f*h,.12f,.65f,.55f,.30f);
      }
      if(kind==2){
        // Knight crest
        box(x,1.27f*h+bob,z-.12f,.11f,.20f*h,.22f,.43f,.30f,.20f);
      }
      if(kind==3){
        // Bishop tall helmet
        box(x,1.30f*h+bob,z,.12f,.25f*h,.12f,.73f,.67f,.47f);
      }
      if(kind==4){
        // Rook battlements
        for(int i=-1;i<=1;i++)
          box(x+i*.17f,1.23f*h+bob,z,.075f,.15f*h,.18f,.40f,.43f,.47f);
      }
      if(kind==5){
        // Queen golden crown
        box(x,1.23f*h+bob,z,.25f,.12f*h,.25f,.96f,.70f,.17f);
        box(x,1.39f*h+bob,z,.09f,.12f*h,.09f,.98f,.82f,.27f);
      }
      if(kind==6){
        // King crown
        box(x,1.24f*h+bob,z,.26f,.12f*h,.26f,.95f,.73f,.19f);
        box(x,1.45f*h+bob,z,.06f,.16f*h,.06f,.95f,.80f,.30f);
        box(x,1.49f*h+bob,z,.17f,.045f*h,.045f,.95f,.80f,.30f);
      }

      // V14: distinctive silhouettes (all shapes are actual 3D geometry).
      if(kind==1){ // Pawn: short spear banner
        box(x-.25f,.82f*h+bob,z,.035f,.38f*h,.035f,.72f,.76f,.80f);
      }else if(kind==2){ // Knight: horse muzzle, mane and ears
        orb(x,.97f*h+bob,z-.31f,.23f,.21f*h,.33f,.50f,.34f,.22f);
        box(x,1.15f*h+bob,z-.46f,.14f,.09f*h,.18f,.70f,.54f,.34f);
        box(x-.13f,1.32f*h+bob,z-.24f,.06f,.17f*h,.07f,.39f,.25f,.16f);
        box(x+.13f,1.32f*h+bob,z-.24f,.06f,.17f*h,.07f,.39f,.25f,.16f);
      }else if(kind==3){ // Bishop: tall ceremonial staff
        box(x-.47f,.83f*h+bob,z,.04f,.67f*h,.04f,.85f,.72f,.30f);
        box(x-.47f,1.51f*h+bob,z,.12f,.13f*h,.12f,.96f,.82f,.37f);
      }else if(kind==4){ // Rook: wide stone tower
        orb(x,.76f*h+bob,z,.31f,.43f*h,.29f,.40f,.43f,.48f);
        for(int q=-1;q<=1;q++)
          box(x+q*.27f,1.30f*h+bob,z,.10f,.19f*h,.34f,.58f,.59f,.61f);
      }else if(kind==5){ // Queen: two raised golden ornaments
        box(x-.19f,1.47f*h+bob,z,.07f,.20f*h,.07f,.99f,.77f,.17f);
        box(x+.19f,1.47f*h+bob,z,.07f,.20f*h,.07f,.99f,.77f,.17f);
      }else if(kind==6){ // King: high cross and broad royal shoulders
        box(x,1.66f*h+bob,z,.07f,.18f*h,.07f,.99f,.82f,.20f);
        box(x,1.70f*h+bob,z,.23f,.055f*h,.07f,.99f,.82f,.20f);
        box(x,.73f*h+bob,z,.39f,.12f*h,.25f,.75f,.56f,.16f);
      }
    }
    // V23: visible rotating blade, with the rotation pivot at the hand.
    // This is a procedural animation, not FBX skeletal animation.
    void v23Sword(float x,float y,float z,float phase,int side){
      float swing=(float)Math.sin(Math.PI*Math.max(0f,Math.min(1f,phase)));
      float angle=-115f+230f*phase;
      Matrix.setIdentityM(model,0);
      Matrix.translateM(model,0,x,y,z);
      Matrix.rotateM(model,0,side>0?180f:0f,0f,1f,0f);
      Matrix.rotateM(model,0,angle,0f,0f,1f);
      Matrix.translateM(model,0,0f,.56f,0f);
      Matrix.scaleM(model,0,.065f,.58f,.065f);
      Matrix.multiplyMM(mv,0,view,0,model,0);
      Matrix.multiplyMM(mvp,0,proj,0,mv,0);
      GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
      GLES20.glUniform4f(colLoc,.86f+.14f*swing,.90f+.10f*swing,1f,1f);
      vb.position(0);
      GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,vb);
      GLES20.glEnableVertexAttribArray(posLoc);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);
    }
    // V24: articulated-looking procedural attack: arm and sword share a shoulder pivot.
    // The imported FBX skeleton is NOT used here.
    void v24Part(float[] parent,float dx,float dy,float dz,float sx,float sy,float sz,
                 float r,float g,float b){
      float[] part=new float[16];
      Matrix.setIdentityM(part,0);
      Matrix.translateM(part,0,dx,dy,dz);
      Matrix.scaleM(part,0,sx,sy,sz);
      float[] world=new float[16];
      Matrix.multiplyMM(world,0,parent,0,part,0);
      Matrix.multiplyMM(mv,0,view,0,world,0);
      Matrix.multiplyMM(mvp,0,proj,0,mv,0);
      GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
      GLES20.glUniform4f(colLoc,r,g,b,1f);
      vb.position(0);
      GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,vb);
      GLES20.glEnableVertexAttribArray(posLoc);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);
    }
    void v24Attack(float x,float z,float phase,int side){
      float q=Math.max(0f,Math.min(1f,phase));
      // Raise, strike, recover: all driven by one normalized attack clock.
      float angle=q<.30f ? -65f-65f*(q/.30f)
                  :q<.68f ? -130f+235f*((q-.30f)/.38f)
                  :105f-105f*((q-.68f)/.32f);
      float[] root=new float[16];
      Matrix.setIdentityM(root,0);
      Matrix.translateM(root,0,x+(side>0?.31f:-.31f),.95f,z);
      Matrix.rotateM(root,0,side>0?180f:0f,0,1,0);
      Matrix.rotateM(root,0,angle,0,0,1);
      // Upper arm, hand, blade, crossguard: physically attached to the same pivot.
      v24Part(root,0,-.24f,0,.115f,.30f,.12f,.47f,.48f,.54f);
      v24Part(root,0,-.53f,0,.12f,.10f,.12f,.68f,.55f,.39f);
      v24Part(root,0,-.99f,0,.060f,.43f,.065f,.85f,.92f,1f);
      v24Part(root,0,-.59f,0,.23f,.045f,.09f,.94f,.75f,.26f);
    }

    // V102: Tasa gore farkli gorunen saldiri efektleri.
    void specialAttack(float x,float z,float tx,float tz,
                       float phase,int piece){
      float q=Math.max(0f,Math.min(1f,phase));
      float power=(float)Math.sin(Math.PI*q);
      int role=Math.abs(piece);

      switch(role){
        case 1: // Piyon: yakin dovus
          v24Attack(x,z,q, piece);
          break;
        case 2: // At: ileri atilma ve darbe izi
          box(tx,.55f,tz,.20f+power*.20f,.08f,.20f+power*.20f,
              .78f,.73f,.62f);
          break;
        case 3: // Fil: buyu isigi
          for(int i=0;i<4;i++){
            double a=i*Math.PI/2+q*5;
            box(tx+(float)Math.cos(a)*(.18f+power*.22f),
                .9f+power*.35f,
                tz+(float)Math.sin(a)*(.18f+power*.22f),
                .065f,.16f,.065f,.42f,.42f,1f);
          }
          break;
        case 4: // Kale: agir darbe
          box(tx,.18f,tz,.20f+power*.35f,.08f,.20f+power*.35f,
              .75f,.52f,.25f);
          break;
        case 5: // Vezir: enerji patlamasi
          for(int i=0;i<6;i++){
            double a=i*Math.PI/3;
            box(tx+(float)Math.cos(a)*power*.35f,
                .85f+power*.25f,
                tz+(float)Math.sin(a)*power*.35f,
                .07f,.20f,.07f,.86f,.28f,.78f);
          }
          break;
        case 6: // Sah: agir kilic saldirisi
          v24Attack(x,z,q,piece);
          box(tx,1.15f,tz,.10f+power*.20f,.06f,.10f+power*.20f,
              1f,.80f,.28f);
          break;
        default:
          v24Attack(x,z,q,piece);
      }
    }

    // V29: identical camera for rendering and touch ray-casting.
    float v127Aspect=1f;

    float v128Blend(){
      if(!anim || captured==0)return 0f;

      float elapsed=Math.max(0f,
          (System.currentTimeMillis()-start)/1000f);

      float enter=Math.min(1f,elapsed/.85f);
      enter=enter*enter*(3f-2f*enter);

      float remaining=Math.max(0f,3.8f-elapsed);
      float leave=Math.min(1f,remaining/.95f);
      leave=leave*leave*(3f-2f*leave);

      return Math.max(0f,Math.min(enter,leave));
    }

    void setBattleCamera(float[] out, boolean cinematic){
      float eyeX=0f,eyeY=11.5f,eyeZ=14.0f;
      float lookX=0f,lookY=0f,lookZ=0f;

      if(cinematic){
        float blend=v128Blend();

        // Fight location is fixed for the entire capture.
        float mx=(fx+tx)*.5f-3.5f;
        float mz=(fy+ty)*.5f-3.5f;

        float dx=tx-fx;
        float dz=ty-fy;
        float separation=(float)Math.sqrt(dx*dx+dz*dz);

        // V130: position camera perpendicular to the attack direction.
        // Both fighters remain separated horizontally on screen.
        float safeSep=Math.max(0.001f,separation);
        float sideX=-dz/safeSep;
        float sideZ=dx/safeSep;
        float distance=9.0f+Math.min(2.0f,separation*.35f);
        float combatX=mx+sideX*distance;
        float combatY=4.1f;
        float combatZ=mz+sideZ*distance;

        eyeX+=(combatX-eyeX)*blend;
        eyeY+=(combatY-eyeY)*blend;
        eyeZ+=(combatZ-eyeZ)*blend;

        lookX+=mx*blend;
        lookY+=0.75f*blend;
        lookZ+=mz*blend;
      }

      Matrix.setLookAtM(out,0,
          eyeX,eyeY,eyeZ,
          lookX,lookY,lookZ,
          0f,1f,0f);
    }
    @Override public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl){
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
      GLES20.glUseProgram(program);
      boolean v127Combat=anim && captured!=0;
      setBattleCamera(view,v127Combat);

      // V128: one shared blend for view and projection.
      float v127Fov=58f-2f*v128Blend();

      Matrix.perspectiveM(
          proj,0,v127Fov,v127Aspect,.1f,100f);
      // BYBLACK_V27_SAGLAM: stone-and-bronze board, layered squares, clear highlights
      box(0,-.39f,0,4.75f,.10f,4.75f,.025f,.031f,.043f);
      box(0,-.30f,0,4.68f,.055f,4.68f,.53f,.39f,.20f);
      box(0,-.235f,0,4.56f,.040f,4.56f,.10f,.13f,.17f);
      box(0,-.185f,0,4.44f,.020f,4.44f,.71f,.56f,.30f);
      // V29: stone floor beyond board edges.
      box(0f,-.62f,0f,30f,.08f,30f,.075f,.085f,.10f);
      // V28: clean dark stone chessboard, no noisy stripes.
      box(0,-.32f,0,4.48f,.12f,4.48f,.055f,.065f,.085f);
      box(0,-.225f,0,4.40f,.055f,4.40f,.39f,.29f,.17f);
      box(0,-.15f,0,4.23f,.040f,4.23f,.10f,.12f,.16f);
      // V30: neutral stone courtyard instead of empty black void.
      box(0f,-.73f,0f,32f,.09f,32f,.26f,.30f,.33f);
      box(0f,-.59f,0f,5.25f,.10f,5.25f,.22f,.24f,.25f);
      box(0f,-.48f,0f,4.83f,.045f,4.83f,.40f,.32f,.19f);
      for(int y=0;y<8;y++)for(int x=0;x<8;x++){
        float xx=x-3.5f,zz=y-3.5f;
        boolean light=((x+y)&1)==0;
        boolean picked=selected==y*8+x;
        boolean target=selected>=0 && raw(selected%8,selected/8,x,y);
        float rr=light?.68f:.095f,gg=light?.67f:.115f,bb=light?.61f:.155f;
        if(picked){rr=.85f;gg=.65f;bb=.20f;}
        else if(target && board[y][x]!=0){rr=.67f;gg=.18f;bb=.16f;}
        else if(target){rr=.20f;gg=.53f;bb=.33f;}
        box(xx,.055f,zz,.495f,.026f,.495f,rr,gg,bb);
      }
      float t=anim?Math.min(1,(System.currentTimeMillis()-start)/(captured!=0?3800f:700f)):0;
      for(int y=0;y<8;y++)for(int x=0;x<8;x++){
        int v=board[y][x];
        if(v==0)continue;
        if(anim&&x==fx&&y==fy)continue;
        if(anim&&x==tx&&y==ty){
          if(captured!=0&&t<1f)
            warrior(captured,x-3.5f,y-3.5f,
                Math.max(0f,(t-(1.30f/3.8f))*3.8f),
                 t>=(1.30f/3.8f));
          continue;
        }
        // V120: Combat close-up shows only the fighters.
        // V130: hide bystanders only while the close-up is active.
        // V131: preserve bystanders and board context during combat.
        warrior(v,x-3.5f,y-3.5f,0,false);
      }
      if(anim){
        float u=captured!=0
            ?Math.min(1f,(System.currentTimeMillis()-start)/910f)
            :t;
        u=u*u*(3-2*u);
        float x=fx+(tx-fx)*u-3.5f,z=fy+(ty-fy)*u-3.5f;
        v34MovingPiece=true;
        // V109: hareket vektoru +X,+Z duzleminde.
        // GLB'nin yerel ileri yonu +Z kabul edilir.
        float dx=tx-fx, dz=ty-fy;
        float heading=(float)Math.toDegrees(Math.atan2(dx,dz));
        UnitV37.v109Heading=heading;
        try {
          warrior(moving,x,z,t,false);
        } finally {
          UnitV37.v109Heading=Float.NaN;
        }
        v34MovingPiece=false;
        // V23: sword pivoted at attacker's hand and impact sparks.
        if(captured!=0 && t>=.35f && t<=.83f){
          float phase=(t-.35f)/.48f;
          float cx=tx-3.5f,cz=ty-3.5f;

        }

        if(t>=1)complete();
      }
    }
  }
}
