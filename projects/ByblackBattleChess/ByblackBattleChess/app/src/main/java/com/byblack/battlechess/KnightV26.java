package com.byblack.battlechess;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import java.io.*;
import java.nio.*;
final class KnightV26 {
 static final String[] NAMES={"idle","walk","attack","death"};
 static final class Clip {
  FloatBuffer[][] frames;
  int[] counts;
  float seconds;
 }
 final Clip[] clips=new Clip[4];
 KnightV26(Context ctx){
  for(int k=0;k<4;k++){
   try(InputStream raw=ctx.getAssets().open("v26_"+NAMES[k]+".bin");DataInputStream in=new DataInputStream(new BufferedInputStream(raw,65536))){
    if(readIntLE(in)!=0x26364342)throw new IOException("Invalid V26 magic");
    int frameCount=readIntLE(in),meshCount=readIntLE(in);
    if(frameCount<2||frameCount>200||meshCount!=3)throw new IOException("Invalid V26 dimensions");
    Clip c=new Clip();c.counts=new int[meshCount];
    for(int i=0;i<meshCount;i++){
     c.counts[i]=readIntLE(in);
     if(c.counts[i]<=0||c.counts[i]>100000)throw new IOException("Invalid triangle count");
    }
    c.seconds=Float.intBitsToFloat(readIntLE(in));
    c.frames=new FloatBuffer[frameCount][meshCount];
    for(int f=0;f<frameCount;f++)for(int m=0;m<meshCount;m++){
     int bytes=c.counts[m]*12;
     byte[] arr=new byte[bytes];in.readFully(arr);
     ByteBuffer bb=ByteBuffer.allocateDirect(bytes).order(ByteOrder.LITTLE_ENDIAN);
     bb.put(arr);bb.position(0);c.frames[f][m]=bb.asFloatBuffer();
    }
    clips[k]=c;
   }catch(Exception e){android.util.Log.e("ByblackV26","Animation load "+NAMES[k],e);}
  }
 }
 static int readIntLE(DataInputStream in)throws IOException{return Integer.reverseBytes(in.readInt());}
 boolean draw(int clipId,float seconds,float[] view,float[] proj,float x,float z,int side,int posLoc,int colLoc,int mvpLoc){
  if(clipId<0||clipId>=clips.length||clips[clipId]==null)return false;
  Clip c=clips[clipId];
  float time=clipId==3?Math.min(seconds,c.seconds):seconds%c.seconds;
  int frame=Math.min(c.frames.length-1,Math.max(0,Math.round(time/c.seconds*(c.frames.length-1))));
  float[] model=new float[16],mv=new float[16],mvp=new float[16];
  Matrix.setIdentityM(model,0);
  Matrix.translateM(model,0,x,.095f,z);
   Matrix.rotateM(model,0,side>0?180f:0f,0,1,0);
   // FBX baked vertices: long axis is negative Z; stand it upright.
   Matrix.rotateM(model,0,90f,1f,0f,0f);
   Matrix.scaleM(model,0,.0024f,.0024f,.0024f);
   // Center the character horizontally; ground the lowest Z vertex.
   Matrix.translateM(model,0,7.7f,9.6f,-.18f);
  Matrix.multiplyMM(mv,0,view,0,model,0);
  Matrix.multiplyMM(mvp,0,proj,0,mv,0);
  GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
  float[][] colors={{.12186f,.12186f,.12186f},{.59872f,.44615f,.25851f},{.02485f,.01008f,.00632f}};
  for(int i=0;i<c.counts.length;i++){
   float[] rgb=colors[i];float shade=side>0?1f:.82f;
   GLES20.glUniform4f(colLoc,Math.min(1f,(rgb[0]*.70f+(side>0?.34f:.22f))*shade),Math.min(1f,(rgb[1]*.70f+(side>0?.30f:.24f))*shade),Math.min(1f,(rgb[2]*.70f+(side>0?.24f:.37f))*(side>0?1f:1.1f)),1);
   FloatBuffer mesh=c.frames[frame][i];mesh.position(0);
   GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,mesh);
   GLES20.glEnableVertexAttribArray(posLoc);
   GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,c.counts[i]);
  }
  return true;
 }
}
