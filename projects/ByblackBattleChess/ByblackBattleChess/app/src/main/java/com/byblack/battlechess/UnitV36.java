package com.byblack.battlechess;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import java.io.*;
import java.nio.*;

final class UnitV36 {
  static final String[] NAMES={"idle","walk","attack","death"};
  static final class Clip {
    FloatBuffer[][] frames;
    int[] counts;
    float seconds;
  }
  final Clip[] clips=new Clip[4];
  float centerX,centerZ,minY,scale=1f;
  UnitV36(Context context,String role) {
    try {
      for(int k=0;k<4;k++) {
        try(DataInputStream in=new DataInputStream(new BufferedInputStream(context.getAssets().open(role+"_"+NAMES[k]+".bv35")))) {
          byte[] magic=new byte[4];in.readFully(magic);
          if(magic[0]!='B'||magic[1]!='V'||magic[2]!='3'||magic[3]!='5')throw new IOException("Bad BV35 magic");
          int version=readInt(in), frameCount=readInt(in), meshCount=readInt(in);
          float duration=Float.intBitsToFloat(readInt(in));
          if(version!=1||frameCount<2||frameCount>120||meshCount<1||meshCount>32||duration<=0)throw new IOException("Bad BV35 header");
          Clip clip=new Clip();clip.counts=new int[meshCount];clip.seconds=duration;
          for(int m=0;m<meshCount;m++) {
            clip.counts[m]=readInt(in);
            if(clip.counts[m]<3||clip.counts[m]>1000000||clip.counts[m]%3!=0)throw new IOException("Bad vertex count");
          }
          clip.frames=new FloatBuffer[frameCount][meshCount];
          for(int f=0;f<frameCount;f++)for(int m=0;m<meshCount;m++) {
            int bytes=clip.counts[m]*3*4;
            byte[] data=new byte[bytes];in.readFully(data);
            ByteBuffer buf=ByteBuffer.allocateDirect(bytes).order(ByteOrder.LITTLE_ENDIAN);
            buf.put(data).position(0);clip.frames[f][m]=buf.asFloatBuffer();
          }
          clips[k]=clip;
        }
      }
      // Use idle pose bounds for a consistent character size across all animations.
      float minX=Float.POSITIVE_INFINITY,maxX=Float.NEGATIVE_INFINITY;
      float maxY=Float.NEGATIVE_INFINITY,minZ=Float.POSITIVE_INFINITY,maxZ=Float.NEGATIVE_INFINITY;
      minY=Float.POSITIVE_INFINITY;
      for(FloatBuffer mesh:clips[0].frames[0]) {
        for(int i=0;i<mesh.capacity();i+=3) {
          float x=mesh.get(i),y=mesh.get(i+1),z=mesh.get(i+2);
          minX=Math.min(minX,x);maxX=Math.max(maxX,x);
          minY=Math.min(minY,y);maxY=Math.max(maxY,y);
          minZ=Math.min(minZ,z);maxZ=Math.max(maxZ,z);
        }
      }
      centerX=(minX+maxX)*.5f;centerZ=(minZ+maxZ)*.5f;
      float h=maxY-minY,w=Math.max(maxX-minX,maxZ-minZ);
      scale=1.05f/Math.max(.0001f,Math.max(h,w));
    }catch(Exception e){Log.e("ByblackV35","King asset load failed",e);for(int i=0;i<clips.length;i++)clips[i]=null;}
  }
  static int readInt(DataInputStream in)throws IOException {return Integer.reverseBytes(in.readInt());}
  boolean draw(int clipId,float seconds,float[] view,float[] proj,float x,float z,int side,int posLoc,int colLoc,int mvpLoc) {
    if(clipId<0||clipId>=clips.length||clips[clipId]==null)return false;
    Clip c=clips[clipId];
    float time=clipId==3?Math.min(Math.max(0,seconds),c.seconds):Math.max(0,seconds)%c.seconds;
    int frame=Math.max(0,Math.min(c.frames.length-1,Math.round(time/c.seconds*(c.frames.length-1))));
    float[] model=new float[16],mv=new float[16],mvp=new float[16];
    Matrix.setIdentityM(model,0);
    Matrix.translateM(model,0,x,.095f,z);
    Matrix.rotateM(model,0,side>0?180f:0f,0,1,0);
    Matrix.scaleM(model,0,scale,scale,scale);
    Matrix.translateM(model,0,-centerX,-minY,-centerZ);
    Matrix.multiplyMM(mv,0,view,0,model,0);Matrix.multiplyMM(mvp,0,proj,0,mv,0);
    GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
    for(int m=0;m<c.counts.length;m++) {
      float r=side>0?.85f:.37f,g=side>0?.66f:.48f,b=side>0?.25f:.80f;
      if(m==1){r=Math.min(1,r*1.10f);g=Math.min(1,g*1.10f);b=Math.min(1,b*1.10f);}
      GLES20.glUniform4f(colLoc,r,g,b,1f);
      FloatBuffer mesh=c.frames[frame][m];mesh.position(0);
      GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,0,mesh);
      GLES20.glEnableVertexAttribArray(posLoc);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,c.counts[m]);
    }
    return true;
  }
}
