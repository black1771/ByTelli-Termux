package com.byblack.battlechess;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.opengl.*;
import java.util.*;

public class MainActivity extends Activity {
  Game game;
  Arena3D arena;
  @Override public void onCreate(Bundle b){super.onCreate(b);arena=new Arena3D(this);setContentView(arena);showMenu();}
  @Override public void onBackPressed(){showMenu();}
  void showMenu(){
    final String[] items={"Yeni Oyun", "2 Kisilik", "CPU Kolay", "CPU Orta", "CPU Zor", "Oyunu Kaydet", "Oyunu Yukle", "Oyuna Don"};
    new android.app.AlertDialog.Builder(this).setTitle("BYBLACK BATTLE CHESS V132")
      .setItems(items,(d,n)->{
        switch(n){
          case 0: arena.reset();break;
          case 1: arena.settings(false,0);break;
          case 2: arena.settings(true,0);break;
          case 3: arena.settings(true,1);break;
          case 4: arena.settings(true,2);break;
          case 5: arena.saveGame();android.widget.Toast.makeText(this,"Oyun kaydedildi",0).show();break;
          case 6: android.widget.Toast.makeText(this,arena.loadGame()?"Oyun yuklendi":"Kayit bulunamadi",0).show();break;
        }
      }).setNegativeButton("Kapat",(d,w)->{}).show();
  }
  class Game extends View {
    Paint p=new Paint(3); int[][] board=new int[8][8]; int selected=-1; boolean whiteTurn=true, ended=false, animating=false;
    String status="Sıra sende • Beyaz", message=""; Random random=new Random();
    float tile,left,top; long animStart; int fx,fy,tx,ty,moving,captured; boolean computerNext=false;
    int[] glyph={0,0x2659,0x2658,0x2657,0x2656,0x2655,0x2654};
    Game(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);reset();}
    void reset(){int[] back={4,2,3,5,6,3,2,4};for(int y=0;y<8;y++)for(int x=0;x<8;x++)board[y][x]=0;for(int x=0;x<8;x++){board[0][x]=-back[x];board[1][x]=-1;board[6][x]=1;board[7][x]=back[x];}whiteTurn=true;ended=false;animating=false;computerNext=false;selected=-1;status="Sıra sende • Beyaz";invalidate();}
    void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);p.clearShadowLayer();}
    int rgb(String s){return Color.parseColor(s);}
    void rect(float x,float y,float w,float h,int c){color(c);drawRectSafe(x,y,w,h);}
    Canvas canvas;
    void drawRectSafe(float x,float y,float w,float h){canvas.drawRect(x,y,x+w,y+h,p);}
    void text(String s,float x,float y,float size,int c,boolean bold){color(c);p.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));p.setTextSize(size);canvas.drawText(s,x,y,p);}
    float boardY,boardH,buttonY;
    float rowTop(int y){float t=y/8f;return boardY+boardH*(.67f*t+.33f*t*t);}
    float halfWidth(float y){return getWidth()*(.335f+.145f*y/8f);}
    float cellX(int x,float y){return getWidth()/2f-halfWidth(y)+2*halfWidth(y)*x/8f;}
    float centerX(float x,float y){return getWidth()/2f-halfWidth(y)+2*halfWidth(y)*(x+.5f)/8f;}
    float centerY(float y){return boardY+boardH*(.67f*(y+.5f)/8f+.33f*(y+.5f)*(y+.5f)/64f);}
    void poly(float[] pts,int fill){color(fill);Path path=new Path();path.moveTo(pts[0],pts[1]);for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);path.close();canvas.drawPath(path,p);}
    void line(float x,float y,float xx,float yy,int col,float sw){color(col);p.setStrokeWidth(sw);canvas.drawLine(x,y,xx,yy,p);}
    void circle(float x,float y,float r,int col){color(col);canvas.drawCircle(x,y,r,p);}
    void scene(float w,float h){
      rect(0,0,w,h,rgb("#16191c"));
      rect(0,h*.12f,w,h*.28f,rgb("#55554b"));
      for(int i=0;i<8;i++){float yy=h*(.12f+i*.035f);line(0,yy,w,yy,rgb("#33362f"),3);}
      for(int i=0;i<9;i++){float xx=w*i/8f;line(xx,h*.12f,xx,h*.4f,rgb("#41453c"),2);}
      rect(w*.08f,h*.17f,w*.19f,h*.21f,rgb("#292b29"));rect(w*.72f,h*.17f,w*.19f,h*.21f,rgb("#292b29"));
      circle(w*.5f,h*.25f,w*.075f,rgb("#282b29"));
      rect(0,h*.4f,w,h*.6f,rgb("#252723"));
      text("BYBLACK   /   BATTLE CHESS",w*.055f,h*.065f,Math.min(27,w*.047f),rgb("#e6c48e"),true);
      text("V10   •   SAVAS ARENASI",w*.055f,h*.095f,Math.min(17,w*.028f),rgb("#c5b9a6"),false);
    }
    @Override protected void onDraw(Canvas c){super.onDraw(c);canvas=c;float w=getWidth(),h=getHeight();scene(w,h);
      boardY=h*.33f;boardH=Math.min(h*.46f,w*.87f);buttonY=Math.min(h-68,boardY+boardH+75);
      // Trapezoidal perspective board: far rows are narrower.
      for(int y=0;y<8;y++)for(int x=0;x<8;x++){
        float y0=rowTop(y),y1=rowTop(y+1),x0=cellX(x,y),x1=cellX(x+1,y),xx0=cellX(x,y+1),xx1=cellX(x+1,y+1);
        int col=(x+y)%2==0?rgb("#72976b"):rgb("#263e31");
        poly(new float[]{x0,y0,x1,y0,xx1,y1,xx0,y1},col);
        if(selected==y*8+x)poly(new float[]{x0,y0,x1,y0,xx1,y1,xx0,y1},0x99efcd67);
        if(selected>=0&&legal(board,selected%8,selected/8,x,y,true))circle(centerX(x,y),centerY(y),w*.011f,0xfff4d56a);
      }
      poly(new float[]{cellX(0,8),rowTop(8),cellX(8,8),rowTop(8),cellX(8,8),rowTop(8)+12,cellX(0,8),rowTop(8)+12},rgb("#514338"));
      // Draw back rows first, then front rows to allow character overlap.
      for(int y=0;y<8;y++)for(int x=0;x<8;x++){
        if(animating&&(x==fx&&y==fy))continue;
        if(animating&&x==tx&&y==ty){if(captured!=0){
          float t=Math.min(1f,(System.currentTimeMillis()-animStart)/2700f);
          if(t<.83f){
            float ex=centerX(x,y),ey=centerY(y);
            float r=getWidth()*(.050f+.028f*y/8f);
            canvas.save();
            if(t>.48f){
              float fall=Math.min(1f,(t-.48f)/.35f);
              canvas.rotate(fall*85,ex,ey);
              canvas.translate(fall*r*.5f,fall*r*.25f);
            }
            drawWarrior(captured,ex,ey,r,t,false);
            canvas.restore();
          }
        }
        continue;}
        if(board[y][x]!=0)drawPiece(board[y][x],x,y);
      }
      if(animating){
        float t=Math.min(1f,(System.currentTimeMillis()-animStart)/(captured!=0?2700f:700f));
        float move=captured!=0?Math.min(1f,t/.30f):t;
        float ease=move*move*(3-2*move);
        float ax=fx+(tx-fx)*ease,ay=fy+(ty-fy)*ease;
        float cx=centerX(ax,ay),cy=centerY(ay);
        float r=w*(.050f+.028f*ay/8f);

        if(captured!=0){
          float ex=centerX(tx,ty),ey=centerY(ty);
          if(t<.30f){
            drawWarrior(moving,cx,cy-(float)Math.abs(Math.sin(t*35))*r*.18f,r,t,false);
          }else if(t<.67f){
            float phase=(t-.30f)/.37f;
            float lunge=(float)Math.sin(phase*Math.PI*3)*r*.28f;
            drawWarrior(moving,ex+lunge,ey,r,t,true);
            if(phase>.15f){
              float hit=(float)Math.abs(Math.sin(phase*18));
              circle(ex,ey-r*.40f,r*(.12f+hit*.28f),0xffffc850);
              for(int i=0;i<8;i++){
                double a=i*Math.PI/4+phase*4;
                line(ex,ey-r*.4f,
                  ex+(float)Math.cos(a)*r*hit*1.3f,
                  ey-r*.4f+(float)Math.sin(a)*r*hit*1.3f,
                  0xffffe09a,3);
              }
            }
          }else{
            float victory=(float)Math.sin((t-.67f)*Math.PI/.33f);
            drawWarrior(moving,ex,ey-r*.12f*victory,r,t,false);
          }
        }else{
          drawWarrior(moving,cx,cy,r,t,false);
        }

        if(t>=1f){
          animating=false;
          board[ty][tx]=moving;
          board[fy][fx]=0;
          afterMove();
        }else postInvalidateDelayed(16);
      }
      text(status,w*.055f,boardY+boardH+38,Math.min(23,w*.044f),rgb("#f4e8d2"),true);
      text(message,w*.055f,boardY+boardH+61,Math.min(18,w*.031f),rgb("#f0c67a"),false);
      rect(w*.055f,buttonY,w*.89f,48,rgb("#b98c52"));
      text("YENİ OYUN   ↻",w*.33f,buttonY+32,Math.min(22,w*.04f),rgb("#16191c"),true);
    }
    long animDurationHit(){return 1450L;}
    void drawPiece(int v,int x,int y){drawWarrior(v,centerX(x,y),centerY(y),getWidth()*(.050f+.028f*y/8f),0,false);}
    void drawWarrior(int v,float cx,float cy,float r,float t,boolean attack){
      int k=Math.abs(v),armor=v>0?rgb("#d9d4c2"):rgb("#343c4a"),trim=v>0?rgb("#a57c3d"):rgb("#a84137"),face=v>0?rgb("#ead2b0"):rgb("#9da5a2");
      // Character shadow and ground plinth.
      circle(cx,cy+r*.20f,r*.77f,0x77000000);
      poly(new float[]{cx-r*.66f,cy+r*.17f,cx+r*.66f,cy+r*.17f,cx+r*.50f,cy+r*.37f,cx-r*.50f,cy+r*.37f},trim);
      float bob=attack?-(float)Math.sin(t*35)*r*.15f:0;
      cy+=bob;
      if(k==2){ // Knight: horse head, neck, ears, muzzle.
        poly(new float[]{cx-r*.35f,cy+r*.16f,cx+r*.35f,cy+r*.16f,cx+r*.18f,cy-r*.50f,cx-r*.20f,cy-r*.72f,cx-r*.42f,cy-r*.22f},armor);
        poly(new float[]{cx-r*.20f,cy-r*.68f,cx-r*.05f,cy-r*1.00f,cx+r*.10f,cy-r*.60f},trim);
        circle(cx-r*.16f,cy-r*.39f,r*.055f,trim);
      }else{
        // Arms and torso: animated humanoid warriors.
        poly(new float[]{cx-r*.43f,cy+r*.10f,cx-r*.32f,cy-r*.46f,cx+r*.31f,cy-r*.46f,cx+r*.43f,cy+r*.10f},armor);
        line(cx-r*.31f,cy-r*.30f,cx-r*.62f,cy-r*.05f,trim,r*.19f);
        line(cx+r*.30f,cy-r*.30f,cx+r*.62f,cy-r*.08f,trim,r*.19f);
        circle(cx,cy-r*.61f,r*.26f,face);
        if(k==1){poly(new float[]{cx-r*.30f,cy-r*.77f,cx+r*.30f,cy-r*.77f,cx+r*.20f,cy-r*.98f,cx-r*.20f,cy-r*.98f},trim);}
        if(k==3){poly(new float[]{cx-r*.27f,cy-r*.70f,cx,cy-r*1.18f,cx+r*.27f,cy-r*.70f},trim);}
        if(k==4){rect(cx-r*.27f,cy-r*.94f,r*.54f,r*.25f,trim);for(int i=-1;i<=1;i++)rect(cx+i*r*.23f-r*.07f,cy-r*1.09f,r*.14f,r*.23f,trim);}
        if(k==5||k==6){poly(new float[]{cx-r*.34f,cy-r*.77f,cx-r*.23f,cy-r*1.10f,cx,cy-r*.91f,cx+r*.22f,cy-r*1.17f,cx+r*.34f,cy-r*.77f},trim);}
        if(k==6){line(cx,cy-r*1.16f,cx,cy-r*1.38f,trim,r*.09f);line(cx-r*.12f,cy-r*1.27f,cx+r*.12f,cy-r*1.27f,trim,r*.09f);}
        // Weapon silhouette, tilts during combat.
        float swing=attack?(float)Math.sin(t*30)*r*.55f:0;
        line(cx+r*.60f,cy-r*.08f,cx+r*.90f+swing,cy-r*.85f,0xffc9d0d0,r*.10f);
        line(cx+r*.68f,cy-r*.39f,cx+r*1.03f,cy-r*.32f,trim,r*.11f);
      }
      circle(cx-r*.08f,cy-r*.62f,r*.035f,0xff242322);
      circle(cx+r*.08f,cy-r*.62f,r*.035f,0xff242322);
    }
    boolean inside(int x,int y){return x>=0&&x<8&&y>=0&&y<8;}
    boolean raw(int[][] b,int x,int y,int xx,int yy){if(!inside(xx,yy)||(x==xx&&y==yy))return false;int a=b[y][x],t=b[yy][xx];if(a==0||a*t>0)return false;int dx=xx-x,dy=yy-y,ax=Math.abs(dx),ay=Math.abs(dy),kind=Math.abs(a);
      if(kind==1)return a>0?(dy==-1&&dx==0&&t==0)||(y==6&&dy==-2&&dx==0&&t==0&&b[5][x]==0)||(dy==-1&&ax==1&&t<0):(dy==1&&dx==0&&t==0)||(y==1&&dy==2&&dx==0&&t==0&&b[2][x]==0)||(dy==1&&ax==1&&t>0);
      if(kind==2)return ax*ay==2;
      if(kind==6)return ax<=1&&ay<=1;
      if(kind==3&&ax!=ay)return false;if(kind==4&&dx!=0&&dy!=0)return false;if(kind==5&&!(ax==ay||dx==0||dy==0))return false;
      int sx=Integer.signum(dx),sy=Integer.signum(dy);int cx=x+sx,cy=y+sy;while(cx!=xx||cy!=yy){if(b[cy][cx]!=0)return false;cx+=sx;cy+=sy;}return true;
    }
    boolean attacked(int[][] b,int x,int y,boolean byWhite){for(int yy=0;yy<8;yy++)for(int xx=0;xx<8;xx++){int v=b[yy][xx];if(v==0||(v>0)!=byWhite)continue;if(Math.abs(v)==1){if(y-yy==(byWhite?-1:1)&&Math.abs(x-xx)==1)return true;}else if(raw(b,xx,yy,x,y))return true;}return false;}
    boolean check(int[][] b,boolean white){for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(b[y][x]==(white?6:-6))return attacked(b,x,y,!white);return true;}
    boolean legal(int[][] b,int x,int y,int xx,int yy,boolean white){if(!inside(x,y)||!inside(xx,yy)||(b[y][x]>0)!=white||b[y][x]==0||!raw(b,x,y,xx,yy)||Math.abs(b[yy][xx])==6)return false;int[][] z=copy(b);z[yy][xx]=z[y][x];z[y][x]=0;return !check(z,white);}
    int[][] copy(int[][] b){int[][] z=new int[8][8];for(int y=0;y<8;y++)System.arraycopy(b[y],0,z[y],0,8);return z;}
    ArrayList<int[]> moves(int[][] b,boolean white){ArrayList<int[]> out=new ArrayList<>();for(int y=0;y<8;y++)for(int x=0;x<8;x++)if(b[y][x]!=0&&(b[y][x]>0)==white)for(int yy=0;yy<8;yy++)for(int xx=0;xx<8;xx++)if(legal(b,x,y,xx,yy,white))out.add(new int[]{x,y,xx,yy});return out;}
    int value(int v){switch(Math.abs(v)){case 1:return 100;case 2:return 320;case 3:return 330;case 4:return 500;case 5:return 900;case 6:return 20000;default:return 0;}}
    int eval(int[][] b){int score=0;for(int y=0;y<8;y++)for(int x=0;x<8;x++){int v=b[y][x];if(v!=0)score+=(v<0?1:-1)*(value(v)+(Math.abs(v)==1?(v<0?y:7-y)*6:0));}return score;}
    int minimax(int[][] b,int depth,int alpha,int beta,boolean black){if(depth==0)return eval(b);ArrayList<int[]> ms=moves(b,black);if(ms.isEmpty())return check(b,black)?(black?-100000:100000):0;Collections.shuffle(ms,random);int best=black?Integer.MIN_VALUE:Integer.MAX_VALUE;for(int[] m:ms){int[][] z=copy(b);z[m[3]][m[2]]=z[m[1]][m[0]];z[m[1]][m[0]]=0;int score=minimax(z,depth-1,alpha,beta,!black);if(black){best=Math.max(best,score);alpha=Math.max(alpha,best);}else{best=Math.min(best,score);beta=Math.min(beta,best);}if(beta<=alpha)break;}return best;}
    void ai(){if(ended)return;ArrayList<int[]> ms=moves(board,false);if(ms.isEmpty()){finish();return;}Collections.shuffle(ms,random);int best=-999999;int[] chosen=ms.get(0);for(int[] m:ms){int[][] z=copy(board);z[m[3]][m[2]]=z[m[1]][m[0]];z[m[1]][m[0]]=0;int score=minimax(z,1,-999999,999999,false)+random.nextInt(12);if(score>best){best=score;chosen=m;}}begin(chosen[0],chosen[1],chosen[2],chosen[3]);}
    void begin(int x,int y,int xx,int yy){fx=x;fy=y;tx=xx;ty=yy;moving=board[y][x];captured=board[yy][xx];animating=true;animStart=System.currentTimeMillis();selected=-1;invalidate();}
    void afterMove(){if(Math.abs(moving)==1&&(ty==0||ty==7))board[ty][tx]=moving>0?5:-5;whiteTurn=!whiteTurn;finish();if(!ended&&!whiteTurn){status="Bilgisayar düşünüyor...";computerNext=true;postDelayed(()->{if(computerNext&&!animating&&!ended){computerNext=false;ai();}},350);}else if(!ended)status="Sıra sende • Beyaz";invalidate();}
    void finish(){ArrayList<int[]> m=moves(board,whiteTurn);if(m.isEmpty()){ended=true;status=check(board,whiteTurn)?(whiteTurn?"ŞAH MAT • Bilgisayar kazandı":"ŞAH MAT • Sen kazandın!"):"PAT • Berabere";message="Yeni oyun ile tekrar başla";}else message=check(board,whiteTurn)?"ŞAH!":"";}
    @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();float btnY=buttonY;if(y>=btnY&&y<=btnY+60){reset();return true;}if(animating||ended||!whiteTurn)return true;int yy=-1;for(int j=0;j<8;j++)if(y>=rowTop(j)&&y<rowTop(j+1)){yy=j;break;}if(yy<0)return true;float hw=halfWidth(yy+.5f);int xx=(int)((x-(getWidth()/2f-hw))/(2*hw/8f));if(!inside(xx,yy))return true;if(selected>=0&&legal(board,selected%8,selected/8,xx,yy,true)){begin(selected%8,selected/8,xx,yy);}else{selected=board[yy][xx]>0?yy*8+xx:-1;invalidate();}return true;}
  }
}
