package com.byblack.battlechess;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import java.io.*;
import java.nio.*;

final class UnitV37 {
  static final String[] NAMES={"idle","walk","attack","death"};
  static final class Clip {
    FloatBuffer[][] frames;
    int[] counts;
    float seconds;
  }
  final Clip[] clips=new Clip[4];
  static volatile float v109Heading = Float.NaN;
  final String roleName;
  float centerX,centerZ,minY,modelHeight,scale=1f;
  FloatBuffer[] uvBuffers;
  int textureId=0, textureProgram=0;
  int tPos,tUV,tMVP,tSampler,tTint;
  String textureRole;

  UnitV37(Context context,String role) {
    roleName=role;
    textureRole=role;
    try {
      for(int k=0;k<4;k++) {
        try(DataInputStream in=new DataInputStream(new BufferedInputStream(context.getAssets().open(role+"_"+NAMES[k]+".bv35")))) {
          byte[] magic=new byte[4];in.readFully(magic);
          if(magic[0]!='B'||magic[1]!='V'||magic[2]!='3'||magic[3]!='5')throw new IOException("Bad BV35 magic");
          int version=readInt(in), frameCount=readInt(in), meshCount=readInt(in);
          float duration=Float.intBitsToFloat(readInt(in));
          if(version!=1||frameCount<2||frameCount>120||meshCount<1||meshCount>32||!Float.isFinite(duration)||duration<=0)throw new IOException("Bad BV35 header");
          Clip clip=new Clip();clip.counts=new int[meshCount];clip.seconds=duration;
          for(int m=0;m<meshCount;m++) {
            clip.counts[m]=readInt(in);
            if(clip.counts[m]<3||clip.counts[m]>150000||clip.counts[m]%3!=0)throw new IOException("Bad vertex count");
          }
          clip.frames=new FloatBuffer[frameCount][meshCount];
          for(int f=0;f<frameCount;f++)for(int m=0;m<meshCount;m++) {
            int bytes=clip.counts[m]*3*4;
            byte[] data=new byte[bytes];in.readFully(data);
            ByteBuffer buf=ByteBuffer.allocateDirect(bytes).order(ByteOrder.LITTLE_ENDIAN);
            buf.put(data).position(0);clip.frames[f][m]=buf.asFloatBuffer();
          }
          // All animation clips share the same UV and mesh topology.
          if(k>0){
            if(clip.counts.length!=clips[0].counts.length)
              throw new IOException("Mesh count mismatch: "+role+" "+NAMES[k]);
            for(int m=0;m<clip.counts.length;m++)
              if(clip.counts[m]!=clips[0].counts[m])
                throw new IOException("Mesh vertex mismatch: "+role+" "+NAMES[k]+" mesh="+m);
          }
          clips[k]=clip;
        }
      }
      try(DataInputStream uvIn=new DataInputStream(new BufferedInputStream(context.getAssets().open(role+".uv37")))) {
        byte[] magic=new byte[4];uvIn.readFully(magic);
        if(magic[0]!='U'||magic[1]!='V'||magic[2]!='3'||magic[3]!='7')throw new IOException("UV magic");
        int version=readInt(uvIn),mc=readInt(uvIn);
        if(version!=1||mc!=clips[0].counts.length)throw new IOException("UV header");
        uvBuffers=new FloatBuffer[mc];
        for(int m=0;m<mc;m++){
          int count=readInt(uvIn);
          if(count!=clips[0].counts[m])throw new IOException("UV vertex mismatch");
          byte[] raw=new byte[count*2*4];uvIn.readFully(raw);
          ByteBuffer bb=ByteBuffer.allocateDirect(raw.length).order(ByteOrder.LITTLE_ENDIAN);
          bb.put(raw).position(0);uvBuffers[m]=bb.asFloatBuffer();
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
      modelHeight=h;
      scale=1.05f/Math.max(.0001f,Math.max(h,w));
      // V114: yeni ordu taslarini ayirt et
      if (roleName.endsWith("_pawn")) {
        scale *= 0.85f;
      } else if (roleName.endsWith("_rook")) {
        scale *= 1.05f;
      } else if (roleName.endsWith("_bishop")) {
        scale *= 1.15f;
      }

      if ("queen".equals(roleName)) {
        scale *= 1.30f;
      }
    }catch(Exception e){Log.e("ByblackV35",roleName+" asset load failed",e);for(int i=0;i<clips.length;i++)clips[i]=null;}
  }

  // V80: animated GLB saddle_socket positions, expressed in the same source
  // coordinate system as the horse BV35 vertices. No guessed rider height.
  private static final float[][][] SADDLE = new float[][][]{{{-0.000000f,1.673637f,-0.086034f},{-0.000000f,1.678287f,-0.083821f},{-0.000898f,1.673154f,-0.084667f},{-0.002621f,1.660892f,-0.087398f},{-0.004164f,1.653269f,-0.088239f},{-0.005444f,1.653168f,-0.086034f},{-0.006360f,1.654439f,-0.083821f},{-0.006817f,1.650915f,-0.084667f},{-0.006789f,1.645154f,-0.087398f},{-0.006319f,1.645117f,-0.088239f},{-0.005444f,1.653168f,-0.086034f},{-0.004191f,1.662590f,-0.083821f},{-0.002631f,1.666654f,-0.084667f},{-0.000894f,1.667392f,-0.087398f},{-0.000000f,1.668965f,-0.088239f},{-0.000000f,1.673637f,-0.086034f}},{{-0.000000f,1.685779f,-0.086034f},{0.007059f,1.671971f,-0.082131f},{0.012939f,1.670575f,-0.080880f},{0.016505f,1.681281f,-0.083043f},{0.017128f,1.692625f,-0.087332f},{0.014885f,1.704159f,-0.091036f},{0.010060f,1.703680f,-0.091381f},{0.003577f,1.696772f,-0.088276f},{-0.003602f,1.681051f,-0.083860f},{-0.010218f,1.673329f,-0.081091f},{-0.015100f,1.677503f,-0.081530f},{-0.017186f,1.686884f,-0.085080f},{-0.016346f,1.700527f,-0.089438f},{-0.012729f,1.704638f,-0.091691f},{-0.006972f,1.701954f,-0.090168f},{0.000000f,1.685779f,-0.086034f}},{{-0.000000f,1.673637f,-0.086034f},{-0.000000f,1.674018f,-0.086034f},{-0.000000f,1.743821f,-0.202211f},{-0.000000f,1.878823f,-0.596889f},{-0.000000f,1.884061f,-1.096192f},{-0.000000f,1.765207f,-1.472090f},{-0.000000f,1.695311f,-1.563319f},{-0.000000f,1.694005f,-1.563319f},{-0.000000f,1.694005f,-1.563319f},{-0.000000f,1.694005f,-1.563319f},{-0.000000f,1.694005f,-1.563319f},{-0.000000f,1.730774f,-1.545642f},{-0.000000f,1.889469f,-1.048181f},{-0.000000f,1.811183f,-0.334098f},{-0.000000f,1.641188f,-0.086034f},{-0.000000f,1.673637f,-0.086034f}},{{-0.000000f,1.673637f,-0.086034f},{-0.000000f,1.678287f,-0.083821f},{-0.000898f,1.673154f,-0.084667f},{-0.002621f,1.660892f,-0.087398f},{-0.004164f,1.653269f,-0.088239f},{-0.005444f,1.653168f,-0.086034f},{-0.006360f,1.654439f,-0.083821f},{-0.006817f,1.650915f,-0.084667f},{-0.006789f,1.645154f,-0.087398f},{-0.006319f,1.645117f,-0.088239f},{-0.005444f,1.653168f,-0.086034f},{-0.004191f,1.662590f,-0.083821f},{-0.002631f,1.666654f,-0.084667f},{-0.000894f,1.667392f,-0.087398f},{-0.000000f,1.668965f,-0.088239f},{-0.000000f,1.673637f,-0.086034f}}};
  // V81: continuous saddle interpolation. Attack/death are one-shot,
  // idle/walk loop. Avoid the 16-step socket jumping visible in V80.
  // V82: socket sampling follows the exact discrete horse frame selected
  // by drawMounted(). 16 socket samples versus 6 BV35 horse frames otherwise
  // cause the rider to move while the horse mesh remains on the same frame.
  // V84: the horse mesh and saddle MUST select the same BV35 frame.
  private int animationFrame(int clipId,float seconds){
    if(clipId<0||clipId>=clips.length)return -1;
    Clip c=clips[clipId];
    if(c==null||c.frames==null||c.frames.length==0||
       !(c.seconds>0f)||Float.isNaN(seconds)||Float.isInfinite(seconds))return -1;
    boolean oneShot=(clipId==2||clipId==3);
    float t=oneShot?Math.min(Math.max(0f,seconds),c.seconds):
      ((seconds%c.seconds)+c.seconds)%c.seconds;
    // V87: looped clips use a half-open time interval [0,duration).
    // Rounding to n-1 duplicates the first/last pose and creates uneven
    // frame dwell times. One-shot clips still reach and hold their last frame.
    int n=c.frames.length;
    int selected=oneShot
      ? Math.round(t/c.seconds*(n-1))
      : (int)Math.floor(t/c.seconds*n);
    return Math.max(0,Math.min(n-1,selected));
  }

  // V88: mounted rider and horse clips have different source durations.
  // Return a safe clip duration so Arena3D can map rider time to horse phase.
  float clipDuration(int clipId){
    if(clipId<0||clipId>=clips.length)return 0f;
    Clip c=clips[clipId];
    return c!=null && Float.isFinite(c.seconds) && c.seconds>0f ? c.seconds : 0f;
  }

  // V89: quantize rider pose to the exact horse frame being rendered.
  // Continuous duration mapping can advance the rider while the horse mesh
  // remains frozen on one of its six baked frames.
  float mountedRiderTime(int horseClip,float horseSeconds,UnitV37 rider,int riderClip){
    if(rider==null||riderClip<0||riderClip>=rider.clips.length||
       horseClip<0||horseClip>=clips.length)return 0f;
    Clip hc=clips[horseClip],rc=rider.clips[riderClip];
    if(hc==null||rc==null||hc.frames==null||rc.frames==null||
       hc.frames.length<2||rc.frames.length<2||
       !(hc.seconds>0f)||!(rc.seconds>0f))return 0f;
    int hf=animationFrame(horseClip,horseSeconds);
    if(hf<0)return 0f;
    int hn=hc.frames.length,rn=rc.frames.length;
    boolean oneShot=(horseClip==2||horseClip==3);
    // The normalized source pose is derived from the horse frame, not
    // wall-clock time. Center of a loop frame avoids floating-point edges.
    float phase=oneShot?(float)hf/(hn-1):((float)hf+0.5f)/hn;
    int rf=oneShot?Math.round(phase*(rn-1)):
        Math.min(rn-1,(int)Math.floor(phase*rn));
    if(oneShot)return ((float)rf/(rn-1))*rc.seconds;
    return (((float)rf+0.5f)/rn)*rc.seconds;
  }

  private static final float[][][] SADDLE_ROT = new float[][][]{{{0.00000000f,0.00000000f,0.00000000f,1.00000000f},{0.00497951f,0.00000000f,0.00000000f,0.99998760f},{0.00307749f,0.00000525f,0.00170849f,0.99999381f},{-0.00307752f,-0.00001550f,0.00500904f,0.99998272f},{-0.00497942f,-0.00003976f,0.00796853f,0.99995585f},{0.00000000f,-0.00000002f,0.01038471f,0.99994608f},{0.00497913f,0.00006023f,0.01209317f,0.99991448f},{0.00307725f,0.00003998f,0.01297753f,0.99991105f},{-0.00307750f,-0.00004002f,0.01297753f,0.99991105f},{-0.00497938f,-0.00006027f,0.01209317f,0.99991448f},{0.00000000f,-0.00000002f,0.01038471f,0.99994608f},{0.00497942f,0.00003974f,0.00796853f,0.99995585f},{0.00307752f,0.00001549f,0.00500904f,0.99998272f},{-0.00307774f,-0.00000525f,0.00170849f,0.99999380f},{-0.00497976f,0.00000000f,0.00000000f,0.99998760f},{0.00000000f,0.00000000f,0.00000000f,1.00000000f}},{{0.00000000f,0.00000000f,0.00000000f,1.00000000f},{0.00766633f,0.01060790f,-0.00000125f,0.99991435f},{0.01031264f,0.01940718f,-0.00000000f,0.99975848f},{0.00610119f,0.02483412f,0.00000049f,0.99967297f},{-0.00214896f,0.02593829f,-0.00000026f,0.99966124f},{-0.00906911f,0.02266976f,-0.00000001f,0.99970187f},{-0.00981503f,0.01533153f,0.00000049f,0.99983429f},{-0.00421156f,0.00542763f,0.00000124f,0.99997640f},{0.00421156f,-0.00542763f,0.00000101f,0.99997640f},{0.00981528f,-0.01533154f,0.00000050f,0.99983429f},{0.00906886f,-0.02266976f,0.00000001f,0.99970187f},{0.00214896f,-0.02593829f,-0.00000024f,0.99966124f},{-0.00610144f,-0.02483412f,0.00000051f,0.99967297f},{-0.01031289f,-0.01940718f,0.00000001f,0.99975847f},{-0.00766633f,-0.01060790f,-0.00000125f,0.99991435f},{0.00000000f,0.00000000f,0.00000000f,1.00000000f}},{{0.00000000f,0.00000000f,0.00000000f,1.00000000f},{0.00000000f,0.00000000f,0.00000000f,1.00000000f},{-0.03791712f,0.00000000f,0.00000000f,0.99928089f},{-0.15905432f,0.00000000f,0.00000000f,0.98726983f},{-0.30356298f,0.00000000f,0.00000000f,0.95281138f},{-0.41164682f,0.00000000f,0.00000000f,0.91134346f},{-0.43837125f,0.00000000f,0.00000000f,0.89879400f},{-0.43837125f,0.00000000f,0.00000000f,0.89879400f},{-0.43837125f,0.00000000f,0.00000000f,0.89879400f},{-0.43837125f,0.00000000f,0.00000000f,0.89879400f},{-0.43837125f,0.00000000f,0.00000000f,0.89879400f},{-0.43316740f,0.00000000f,0.00000000f,0.90131349f},{-0.28983380f,0.00000000f,0.00000000f,0.95707699f},{-0.07951340f,0.00000000f,0.00000000f,0.99683380f},{0.00000000f,0.00000000f,0.00000000f,1.00000000f},{0.00000000f,0.00000000f,0.00000000f,1.00000000f}},{{0.00000000f,0.00000000f,0.00000000f,1.00000000f},{-0.00631356f,0.00000000f,0.00000000f,0.99998007f},{-0.01154940f,0.00000000f,0.00000000f,0.99993330f},{-0.01493852f,0.00000000f,0.00000000f,0.99988841f},{-0.01545119f,0.00000000f,0.00000000f,0.99988062f},{-0.01345133f,0.00000000f,0.00000000f,0.99990953f},{-0.00923280f,0.00000000f,0.00000000f,0.99995738f},{-0.00323598f,0.00000000f,0.00000000f,0.99999476f},{0.00323598f,0.00000000f,0.00000000f,0.99999476f},{0.00923280f,0.00000000f,0.00000000f,0.99995738f},{0.01345133f,0.00000000f,0.00000000f,0.99990953f},{0.01545094f,0.00000000f,0.00000000f,0.99988063f},{0.01493876f,0.00000000f,0.00000000f,0.99988841f},{0.01154941f,0.00000000f,0.00000000f,0.99993330f},{0.00631381f,0.00000000f,0.00000000f,0.99998007f},{0.00000000f,0.00000000f,0.00000000f,1.00000000f}}};

  // V95: Relative saddle orientation (clip start = identity). The GLB
  // socket samples are indexed by the ACTUAL rendered horse frame.
  float[] saddleRelativeQuaternion(int clipId,float seconds){
    // The fourth source GLB animation is GALLOP, not death. Never apply
    // gallop socket tilt to the BV35 death clip.
    if(clipId==3)return new float[]{0f,0f,0f,1f};
    if(clipId<0||clipId>=SADDLE_ROT.length)return new float[]{0f,0f,0f,1f};
    Clip c=clips[clipId];
    if(c==null||c.frames==null||c.frames.length<2)return new float[]{0f,0f,0f,1f};
    int frame=animationFrame(clipId,seconds);
    if(frame<0)return new float[]{0f,0f,0f,1f};
    int socketFrame=Math.max(0,Math.min(SADDLE_ROT[clipId].length-1,
        Math.round(frame*(SADDLE_ROT[clipId].length-1f)/(c.frames.length-1f))));
    return SADDLE_ROT[clipId][socketFrame].clone();
  }

  float[] saddleWorldOffset(int clipId,float seconds,int side){
    // The GLB has idle/walk/rear/gallop, but BV35 clip 3 is death.
    // Anchor dying rider to the idle saddle instead of gallop samples.
    if(clipId==3){
      if(SADDLE.length==0||SADDLE[0].length==0)return new float[]{0f,0f,0f};
      float[] p=SADDLE[0][0];
      float horseScale=scale*1.30f;
      float sign=side>0?-1f:1f;
      return new float[]{(p[0]-centerX)*horseScale*sign,
          (p[1]-minY)*horseScale,(p[2]-centerZ)*horseScale*sign};
    }
    if(clipId<0||clipId>=clips.length)return new float[]{0f,0f,0f};
    Clip c=clips[clipId];
    if(c==null||c.frames==null||c.frames.length<2||c.seconds<=0f||
        Float.isNaN(seconds)||Float.isInfinite(seconds))
      return new float[]{0f,0f,0f};
    int frame=animationFrame(clipId,seconds);
    if(frame<0)return new float[]{0f,0f,0f};
    int socketFrame=Math.max(0,Math.min(SADDLE[clipId].length-1,
        Math.round(frame*(SADDLE[clipId].length-1f)/(c.frames.length-1f))));
    float[] p=SADDLE[clipId][socketFrame];
    // Match the exact horse draw scale; do not introduce another guessed factor.
    float horseScale=scale*1.30f;
    float dx=(p[0]-centerX)*horseScale;
    float dy=(p[1]-minY)*horseScale;
    float dz=(p[2]-centerZ)*horseScale;
    float sign=side>0?-1f:1f;
    return new float[]{dx*sign,dy,dz*sign};
  }

  // V86: place the rider's estimated pelvis, not its feet, at the saddle.
  // The imported rider is in a standing pose; 0.45 is an explicit
  // approximation pending a true sitting animation and rig socket.
  float mountedPelvisOffset(float sizeFactor){
    if(!Float.isFinite(modelHeight)||modelHeight<=0f)return 0f;
    // Match drawMounted() exactly: knights receive a 1.30 model multiplier.
    // Without it the pelvis offset is too small, leaving the rider too high.
    float roleScale="knight".equals(roleName)?1.30f:
        ("rook".equals(roleName)?0.88f:1f);
    return modelHeight*0.45f*scale*roleScale*sizeFactor;
  }

  static int readInt(DataInputStream in)throws IOException {return Integer.reverseBytes(in.readInt());}
  private static int compile(int type,String source){
    int shader=GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader,source);GLES20.glCompileShader(shader);
    int[] ok=new int[1];GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,ok,0);
    if(ok[0]==0)throw new RuntimeException(GLES20.glGetShaderInfoLog(shader));
    return shader;
  }
  void initGL(Context context){
    try{
      String vs="attribute vec3 aPos;attribute vec2 aUV;uniform mat4 uMVP;varying vec2 vUV;void main(){vUV=aUV;gl_Position=uMVP*vec4(aPos,1.0);}";
      String fs="precision mediump float;varying vec2 vUV;uniform sampler2D uTex;uniform vec4 uTint;void main(){gl_FragColor=texture2D(uTex,vUV)*uTint;}";
      int vert=compile(GLES20.GL_VERTEX_SHADER,vs),frag=compile(GLES20.GL_FRAGMENT_SHADER,fs);
      textureProgram=GLES20.glCreateProgram();
      GLES20.glAttachShader(textureProgram,vert);GLES20.glAttachShader(textureProgram,frag);
      GLES20.glLinkProgram(textureProgram);
      int[] linked=new int[1];GLES20.glGetProgramiv(textureProgram,GLES20.GL_LINK_STATUS,linked,0);
      if(linked[0]==0)throw new RuntimeException(GLES20.glGetProgramInfoLog(textureProgram));
      GLES20.glDeleteShader(vert);GLES20.glDeleteShader(frag);
      tPos=GLES20.glGetAttribLocation(textureProgram,"aPos");
      tUV=GLES20.glGetAttribLocation(textureProgram,"aUV");
      tMVP=GLES20.glGetUniformLocation(textureProgram,"uMVP");
      tSampler=GLES20.glGetUniformLocation(textureProgram,"uTex");
      tTint=GLES20.glGetUniformLocation(textureProgram,"uTint");
      android.graphics.Bitmap bmp;
      try(InputStream input=context.getAssets().open(textureRole+".png")){
        bmp=android.graphics.BitmapFactory.decodeStream(input);
      }
      if(bmp==null)throw new IOException("Texture decode failed");
      int[] ids=new int[1];GLES20.glGenTextures(1,ids,0);textureId=ids[0];
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
      android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bmp,0);
      bmp.recycle();GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }catch(Exception e){Log.e("ByblackV37","Texture init failed: "+textureRole,e);textureProgram=0;}
  }
  boolean draw(int clipId,float seconds,float[] view,float[] proj,float x,float z,int side,int posLoc,int colLoc,int mvpLoc,int originalProgram) {
    return drawMounted(clipId,seconds,view,proj,x,z,side,
        posLoc,colLoc,mvpLoc,originalProgram,0f,1f);
  }

  boolean drawMounted(int clipId,float seconds,float[] view,float[] proj,
      float x,float z,int side,int posLoc,int colLoc,int mvpLoc,
      int originalProgram,float heightOffset,float sizeFactor) {
    return drawMountedRotated(clipId,seconds,view,proj,x,z,side,posLoc,colLoc,
        mvpLoc,originalProgram,heightOffset,sizeFactor,null);
  }

  boolean drawMountedRotated(int clipId,float seconds,float[] view,float[] proj,
      float x,float z,int side,int posLoc,int colLoc,int mvpLoc,
      int originalProgram,float heightOffset,float sizeFactor,float[] saddleQuat) {
    if(textureProgram==0||textureId==0||uvBuffers==null)return false;
    if(clipId<0||clipId>=clips.length||clips[clipId]==null)return false;
    Clip c=clips[clipId];
    if(c.frames==null || c.frames.length==0 || c.seconds<=0f)return false;
    if(c.counts==null||c.frames[0]==null||c.counts.length!=uvBuffers.length)return false;
    for(int m=0;m<c.counts.length;m++){
      if(c.frames[0].length<=m||uvBuffers[m]==null||
         c.counts[m]!=uvBuffers[m].capacity()/2)return false;
    }
    int frame=animationFrame(clipId,seconds);
    if(frame<0 || frame>=c.frames.length || c.frames[frame]==null ||
       c.frames[frame].length!=c.counts.length)return false;
    // V92: Validate the ACTUAL animated frame before touching OpenGL state.
    // V85 checked the reference frame, but a later damaged frame could still
    // throw NullPointerException or draw with mismatched vertex counts.
    for(int m=0;m<c.counts.length;m++){
      FloatBuffer mesh=c.frames[frame][m];
      if(mesh==null || mesh.capacity()!=c.counts[m]*3)return false;
    }
    float[] model=new float[16],mv=new float[16],mvp=new float[16];
    Matrix.setIdentityM(model,0);
    Matrix.translateM(model,0,x,.095f+heightOffset,z);
    Matrix.rotateM(model,0,
        Float.isNaN(v109Heading)
          ? (side>0?180f:0f)
          : v109Heading,
        0,1,0);
    // V95: Apply socket tilt before model scale, relative to its rest pose.
    // This is rigid-body rider rotation; a sitting skeletal pose is separate.
    if(saddleQuat!=null && saddleQuat.length==4){
      float qx=saddleQuat[0],qy=saddleQuat[1],qz=saddleQuat[2],qw=saddleQuat[3];
      float norm=(float)Math.sqrt(qx*qx+qy*qy+qz*qz+qw*qw);
      if(norm>0.00001f && Float.isFinite(norm)){
        qx/=norm;qy/=norm;qz/=norm;qw/=norm;
        float angle=(float)(2.0*Math.acos(Math.max(-1f,Math.min(1f,qw)))*180.0/Math.PI);
        float axis=(float)Math.sqrt(qx*qx+qy*qy+qz*qz);
        if(axis>0.00001f && angle>0.001f)
          Matrix.rotateM(model,0,angle,qx/axis,qy/axis,qz/axis);
      }
    }
    // V79: Biniciye bagimsiz 90 derece donus uygulama.
    // Tam saddle_socket baglantisi ayrica rig-bake gerektirir.
    // V59: Warhorse forward axis: no legacy 90-degree rotation.
    float modelScale = "knight".equals(roleName) ? scale * 1.30f : ("rook".equals(roleName) ? scale * 0.88f : scale);
    modelScale *= sizeFactor;
    if("rook".equals(roleName)){
      Matrix.scaleM(model,0,
          modelScale * 0.70f,
          modelScale,
          modelScale * 0.70f);
    }else{
      Matrix.scaleM(model,0,
          modelScale,modelScale,modelScale);
    }
    // V60: use the SAME idle-pose anchor for every animation frame.
    // Per-frame bounds cause visible jumping and make board alignment unstable.
    float cx=centerX,cz=centerZ,ground=minY;
    // V99: the mounted human is roleName="rider", NOT "knight".
    // Rider socket is the pelvis pivot, not the feet. Rotating around feet
    // makes the rider swing off the saddle when the horse rears.
    float pelvisLocalY=("rider".equals(roleName) && sizeFactor<0.99f)
        ? modelHeight*0.45f : 0f;
    Matrix.translateM(model,0,-cx,-ground-pelvisLocalY,-cz);
    Matrix.multiplyMM(mv,0,view,0,model,0);Matrix.multiplyMM(mvp,0,proj,0,mv,0);
    GLES20.glUseProgram(textureProgram);
    GLES20.glUniformMatrix4fv(tMVP,1,false,mvp,0);
    GLES20.glUniform4f(tTint,side>0?1f:.84f,side>0?.92f:.87f,side>0?.72f:1f,1f);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
    GLES20.glUniform1i(tSampler,0);
    for(int m=0;m<c.counts.length;m++){
      FloatBuffer mesh=c.frames[frame][m];mesh.position(0);
      FloatBuffer uv=uvBuffers[m];uv.position(0);
      GLES20.glVertexAttribPointer(tPos,3,GLES20.GL_FLOAT,false,0,mesh);
      GLES20.glEnableVertexAttribArray(tPos);
      GLES20.glVertexAttribPointer(tUV,2,GLES20.GL_FLOAT,false,0,uv);
      GLES20.glEnableVertexAttribArray(tUV);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,c.counts[m]);
    }
    GLES20.glDisableVertexAttribArray(tUV);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    GLES20.glUseProgram(originalProgram);
    return true;
  }
}
