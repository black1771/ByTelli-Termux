package com.byblack.devicelab;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity implements SensorEventListener {

    boolean english;
    String trEn(String value) {
        if(!english || value==null) return value;
        if(value.equals("PANEL")) return "DASHBOARD";
        if(value.equals("SENSÖR")) return "SENSORS";
        if(value.equals("RAPOR")) return "REPORT";
        if(value.equals("CANLI SİSTEM PANELİ")) return "LIVE SYSTEM DASHBOARD";
        if(value.equals("Cihaz")) return "Device";
        if(value.equals("İşlemci mimarisi")) return "CPU architecture";
        if(value.equals("Mantıksal çekirdek")) return "Logical processors";
        if(value.equals("CANLI ÖLÇÜMLER")) return "LIVE READINGS";
        if(value.equals("RAM kullanımı")) return "RAM usage";
        if(value.equals("Ölçülüyor...")) return "Measuring...";
        if(value.equals("Batarya")) return "Battery";
        if(value.equals("Batarya sıcaklığı")) return "Battery temperature";
        if(value.equals("RAM KULLANIM GRAFİĞİ")) return "RAM USAGE CHART";
        if(value.equals("BELLEK VE DEPOLAMA")) return "MEMORY AND STORAGE";
        if(value.equals("Toplam RAM")) return "Total RAM";
        if(value.equals("Kullanılabilir RAM")) return "Available RAM";
        if(value.equals("Uygulama veri bölümü toplam")) return "App data partition total";
        if(value.equals("Uygulama veri bölümü boş")) return "App data partition free";
        if(value.equals("BATARYA LABORATUVARI")) return "BATTERY LAB";
        if(value.equals("Şarj durumu")) return "Charging status";
        if(value.equals("Batarya voltajı")) return "Battery voltage";
        if(value.equals("Batarya sağlığı")) return "Battery health";
        if(value.equals("Erişilemiyor")) return "Unavailable";
        if(value.equals("VERİLERİ YENİLE")) return "REFRESH DATA";
        if(value.equals("Normal (Android durumu)")) return "Normal (Android status)";
        if(value.equals("Aşırı sıcak")) return "Overheated";
        if(value.equals("Sorun bildirildi")) return "Problem reported";
        if(value.equals("Yüksek voltaj")) return "Overvoltage";
        if(value.equals("Çok soğuk")) return "Too cold";
        if(value.equals("Bilinmiyor")) return "Unknown";
        if(value.equals("Şarj oluyor")) return "Charging";
        if(value.equals("Dolu")) return "Full";
        if(value.equals("Bataryadan çalışıyor")) return "Discharging";
        if(value.equals("Şarj olmuyor")) return "Not charging";
        if(value.equals("SENSÖR ENVANTERİ")) return "SENSOR INVENTORY";
        if(value.equals("Durum")) return "Status";
        if(value.equals("Sensör servisi yok")) return "Sensor service unavailable";
        if(value.equals("Bulunan sensör sayısı")) return "Sensors found";
        if(value.equals("Canlı okuma")) return "Live readings";
        if(value.equals("Bir sensöre dokunarak ölçümü başlat.")) return "Tap a sensor to start reading.";
        if(value.equals("CANLI SENSÖR VERİSİ")) return "LIVE SENSOR DATA";
        if(value.equals("Henüz sensör seçilmedi")) return "No sensor selected";
        if(value.equals("ÖLÇÜMÜ DURDUR")) return "STOP READING";
        if(value.equals("Ölçüm durduruldu")) return "Reading stopped";
        if(value.equals("Üretici")) return "Manufacturer";
        if(value.equals("Tür")) return "Type";
        if(value.equals("Maksimum aralık")) return "Maximum range";
        if(value.equals("CANLI OKU")) return "READ LIVE";
        if(value.equals("Ölçüm bekleniyor...")) return "Waiting for readings...";
        if(value.equals("Bu sensör okunamadı")) return "Unable to read this sensor";
        if(value.equals("Değer ")) return "Value ";
        if(value.equals("EKRAN RENK TESTİ")) return "DISPLAY COLOR TEST";
        if(value.equals("Amaç")) return "Purpose";
        if(value.equals("Ölü piksel veya renk sorunu için tam ekran renk testi.")) return "Full-screen color test for dead pixels or color issues.";
        if(value.equals("RENK TESTİNİ BAŞLAT")) return "START COLOR TEST";
        if(value.equals("DOKUNMATİK TESTİ")) return "TOUCHSCREEN TEST";
        if(value.equals("Parmağının izini ekranda çizerek dokunmayı kontrol et.")) return "Draw on the screen with your finger to check touch input.";
        if(value.equals("DOKUNMATİK TESTİNİ BAŞLAT")) return "START TOUCH TEST";
        if(value.equals("TEST BİLGİSİ")) return "TEST INFORMATION";
        if(value.equals("Not")) return "Note";
        if(value.equals("Bu testler donanımı onarmaz; yalnızca görsel kontrol sağlar.")) return "These tests do not repair hardware; they only provide visual checks.";
        if(value.equals("GERİ DÖN")) return "GO BACK";
        if(value.equals("CİHAZ RAPORU")) return "DEVICE REPORT";
        if(value.equals("RAPORU PAYLAŞ")) return "SHARE REPORT";
        if(value.equals("Raporu paylaş")) return "Share report";
        if(value.equals("RAPORU KOPYALA")) return "COPY REPORT";
        if(value.equals("Rapor kopyalandı")) return "Report copied";
        if(value.equals("RAPORU YENİLE")) return "REFRESH REPORT";
        return value;
    }
    void changeLanguage() {
        final String[] names={"Türkçe","English"};
        new AlertDialog.Builder(this)
            .setTitle("Dil / Language")
            .setSingleChoiceItems(names,english?1:0,(dialog,which)->{
                english=which==1;
                getPreferences(MODE_PRIVATE).edit().putBoolean("english",english).apply();
                dialog.dismiss();
                showTab(currentTab);
            }).setNegativeButton(english?"Cancel":"İptal",null).show();
    }

    final int BG=Color.rgb(7,12,22);
    final int CARD=Color.rgb(20,31,48);
    final int CYAN=Color.rgb(43,218,220);
    final int WHITE=Color.WHITE;
    final int MUTED=Color.rgb(153,175,195);

    LinearLayout body,nav;
    TextView liveRam,liveBattery,liveTemp;
    SensorManager sensorManager;
    Sensor activeSensor;
    TextView sensorReading;
    Handler handler=new Handler(Looper.getMainLooper());
    boolean running=true;
    int currentTab=0;
    ArrayList<Float> batteryHistory=new ArrayList<>();
    ArrayList<Float> ramHistory=new ArrayList<>();
    GraphView graph;
    int colorIndex=0;

    int dp(float x) {
        return (int)(x*getResources().getDisplayMetrics().density+0.5f);
    }

    GradientDrawable shape(int color,int radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    TextView text(String value,int size,int color,boolean bold) {
        TextView t=new TextView(this);
        t.setText(trEn(value));
        t.setTextSize(size);
        t.setTextColor(color);
        if(bold)t.setTypeface(null,1);
        return t;
    }

    LinearLayout column() {
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(1);
        return l;
    }

    void padding(View v,int a,int b,int c,int d) {
        v.setPadding(dp(a),dp(b),dp(c),dp(d));
    }

    LinearLayout card(String title) {
        LinearLayout c=column();
        padding(c,18,18,18,18);
        c.setBackground(shape(CARD,18));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,0,0,dp(13));
        body.addView(c,lp);
        TextView h=text(title,17,CYAN,true);
        padding(h,0,0,0,12);
        c.addView(h);
        return c;
    }

    void field(LinearLayout parent,String name,String value) {
        TextView label=text(name.toUpperCase(Locale.ROOT),11,MUTED,true);
        padding(label,0,10,0,3);
        parent.addView(label);
        TextView v=text(trEn(value),16,WHITE,false);
        v.setTextIsSelectable(true);
        parent.addView(v);
    }

    TextView action(String name,LinearLayout parent,Runnable run) {
        TextView b=text(name,14,BG,true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(shape(CYAN,12));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));
        p.setMargins(0,dp(10),0,dp(8));
        parent.addView(b,p);
        b.setOnClickListener(v->run.run());
        return b;
    }

    String gb(long bytes) {
        return String.format(Locale.US,"%.2f GB",bytes/1073741824.0);
    }

    ActivityManager.MemoryInfo memory() {
        ActivityManager a=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();
        a.getMemoryInfo(m);
        return m;
    }

    Intent battery() {
        return registerReceiver(null,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    int batValue(String key,int fallback) {
        Intent i=battery();
        return i==null?fallback:i.getIntExtra(key,fallback);
    }

    float percent() {
        int a=batValue(BatteryManager.EXTRA_LEVEL,-1);
        int b=batValue(BatteryManager.EXTRA_SCALE,-1);
        return a>=0&&b>0?100f*a/b:-1f;
    }

    float temperature() {
        int t=batValue(BatteryManager.EXTRA_TEMPERATURE,-1);
        return t>=0?t/10f:-1f;
    }

    String chargeStatus() {
        int s=batValue(BatteryManager.EXTRA_STATUS,-1);
        if(s==BatteryManager.BATTERY_STATUS_CHARGING)return "Şarj oluyor";
        if(s==BatteryManager.BATTERY_STATUS_FULL)return "Dolu";
        if(s==BatteryManager.BATTERY_STATUS_DISCHARGING)return "Bataryadan çalışıyor";
        if(s==BatteryManager.BATTERY_STATUS_NOT_CHARGING)return "Şarj olmuyor";
        return "Bilinmiyor";
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        english=getPreferences(MODE_PRIVATE).getBoolean("english",!Locale.getDefault().getLanguage().equals("tr"));
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        showTab(0);
        handler.postDelayed(ticker,2000);
    }

    Runnable ticker=new Runnable() {
        @Override public void run() {
            if(!running)return;
            ActivityManager.MemoryInfo m=memory();
            float used=100f*(m.totalMem-m.availMem)/m.totalMem;
            float bat=percent();

            addHistory(ramHistory,used);
            if(bat>=0)addHistory(batteryHistory,bat);

            if(currentTab==0) {
                if(liveRam!=null)liveRam.setText(
                    String.format(Locale.US,"%.1f%%",used));
                if(liveBattery!=null)liveBattery.setText(
                    bat<0?"N/A":String.format(Locale.US,"%.0f%%",bat));
                if(liveTemp!=null)liveTemp.setText(
                    temperature()<0?"N/A":
                    String.format(Locale.US,"%.1f °C",temperature()));
                if(graph!=null)graph.invalidate();
            }
            handler.postDelayed(this,2000);
        }
    };

    void addHistory(ArrayList<Float> a,float v) {
        a.add(v);
        if(a.size()>90)a.remove(0);
    }

    void stopSensor() {
        if(sensorManager!=null)sensorManager.unregisterListener(this);
        activeSensor=null;
    }

    void showTab(int tab) {
        stopSensor();
        currentTab=tab;
        liveRam=null;
        liveBattery=null;
        liveTemp=null;
        graph=null;
        sensorReading=null;

        LinearLayout root=column();
        root.setBackgroundColor(BG);

        LinearLayout header=column();
        padding(header,18,18,18,13);
        header.addView(text("BYBLACK / DEVICELAB",22,WHITE,true));
        TextView subtitle=text("HARDWARE INTELLIGENCE  •  V2 PRO TEST",11,CYAN,true);
        padding(subtitle,0,5,0,0);
        header.addView(subtitle);
        TextView language=text(english?"🌐 English  ▾":"🌐 Türkçe  ▾",14,CYAN,true);
        padding(language,0,12,0,2);
        header.addView(language);
        language.setOnClickListener(v->changeLanguage());
        root.addView(header);

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);

        body=column();
        padding(body,16,10,16,15);
        scroll.addView(body);

        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        nav=new LinearLayout(this);
        nav.setBackgroundColor(CARD);
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(65)));

        String[] tabs={"PANEL","SENSÖR","TEST","RAPOR"};
        for(int i=0;i<tabs.length;i++) {
            final int index=i;
            TextView t=text(tabs[i],11,i==tab?CYAN:MUTED,i==tab);
            t.setGravity(Gravity.CENTER);
            nav.addView(t,new LinearLayout.LayoutParams(0,-1,1));
            t.setOnClickListener(v->showTab(index));
        }

        setContentView(root);

        if(tab==0)dashboard();
        if(tab==1)sensors();
        if(tab==2)tests();
        if(tab==3)report();
    }

    void dashboard() {
        LinearLayout overview=card("CANLI SİSTEM PANELİ");
        field(overview,"Cihaz",Build.MANUFACTURER+" "+Build.MODEL);
        field(overview,"Android",Build.VERSION.RELEASE+
            " / API "+Build.VERSION.SDK_INT);
        field(overview,"İşlemci mimarisi",
            Build.SUPPORTED_ABIS.length>0?Build.SUPPORTED_ABIS[0]:"N/A");
        field(overview,"Mantıksal çekirdek",
            ""+Runtime.getRuntime().availableProcessors());

        LinearLayout live=card("CANLI ÖLÇÜMLER");
        field(live,"RAM kullanımı","");
        liveRam=text("Ölçülüyor...",26,WHITE,true);
        live.addView(liveRam);
        field(live,"Batarya","");
        liveBattery=text("Ölçülüyor...",26,WHITE,true);
        live.addView(liveBattery);
        field(live,"Batarya sıcaklığı","");
        liveTemp=text("Ölçülüyor...",26,WHITE,true);
        live.addView(liveTemp);

        LinearLayout chart=card("RAM KULLANIM GRAFİĞİ");
        graph=new GraphView();
        chart.addView(graph,new LinearLayout.LayoutParams(-1,dp(150)));

        ActivityManager.MemoryInfo m=memory();
        LinearLayout mem=card("BELLEK VE DEPOLAMA");
        field(mem,"Toplam RAM",gb(m.totalMem));
        field(mem,"Kullanılabilir RAM",gb(m.availMem));

        StatFs fs=new StatFs(Environment.getDataDirectory().getPath());
        field(mem,"Uygulama veri bölümü toplam",gb(fs.getTotalBytes()));
        field(mem,"Uygulama veri bölümü boş",gb(fs.getAvailableBytes()));

        LinearLayout bat=card("BATARYA LABORATUVARI");
        field(bat,"Şarj durumu",chargeStatus());
        int voltage=batValue(BatteryManager.EXTRA_VOLTAGE,-1);
        field(bat,"Batarya voltajı",
            voltage<0?"Erişilemiyor":voltage+" mV");
        field(bat,"Batarya sağlığı",
            healthText(batValue(BatteryManager.EXTRA_HEALTH,-1)));

        action("VERİLERİ YENİLE",bat,()->showTab(0));
    }

    String healthText(int h) {
        switch(h) {
            case BatteryManager.BATTERY_HEALTH_GOOD:return "Normal (Android durumu)";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:return "Aşırı sıcak";
            case BatteryManager.BATTERY_HEALTH_DEAD:return "Sorun bildirildi";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:return "Yüksek voltaj";
            case BatteryManager.BATTERY_HEALTH_COLD:return "Çok soğuk";
            default:return "Bilinmiyor";
        }
    }

    class GraphView extends View {
        Paint p=new Paint(3);
        GraphView(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w=getWidth(),h=getHeight();
            p.setColor(Color.rgb(44,60,79));
            p.setStrokeWidth(dp(1));
            for(int i=0;i<=4;i++) {
                float y=h*i/4f;
                c.drawLine(0,y,w,y,p);
            }
            if(ramHistory.size()<2)return;
            p.setColor(CYAN);
            p.setStrokeWidth(dp(3));
            p.setStyle(Paint.Style.STROKE);
            Path path=new Path();
            for(int i=0;i<ramHistory.size();i++) {
                float x=w*i/89f;
                float y=h*(1-ramHistory.get(i)/100f);
                if(i==0)path.moveTo(x,y);
                else path.lineTo(x,y);
            }
            c.drawPath(path,p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    void sensors() {
        LinearLayout summary=card("SENSÖR ENVANTERİ");
        if(sensorManager==null) {
            field(summary,"Durum","Sensör servisi yok");
            return;
        }
        List<Sensor> list=sensorManager.getSensorList(Sensor.TYPE_ALL);
        field(summary,"Bulunan sensör sayısı",""+list.size());
        field(summary,"Canlı okuma",
            "Bir sensöre dokunarak ölçümü başlat.");

        LinearLayout reading=card("CANLI SENSÖR VERİSİ");
        sensorReading=text("Henüz sensör seçilmedi",15,WHITE,false);
        reading.addView(sensorReading);
        action("ÖLÇÜMÜ DURDUR",reading,()->{
            stopSensor();
            if(sensorReading!=null)sensorReading.setText(trEn("Ölçüm durduruldu"));
        });

        for(Sensor s:list) {
            LinearLayout item=card(s.getName());
            field(item,"Üretici",s.getVendor());
            field(item,"Tür",s.getStringType());
            field(item,"Maksimum aralık",""+s.getMaximumRange());
            action("CANLI OKU",item,()->{
                stopSensor();
                activeSensor=s;
                if(sensorReading!=null)
                    sensorReading.setText(s.getName()+"\n"+trEn("Ölçüm bekleniyor..."));
                boolean ok=sensorManager.registerListener(
                    this,s,SensorManager.SENSOR_DELAY_NORMAL);
                if(!ok && sensorReading!=null)
                    sensorReading.setText(trEn("Bu sensör okunamadı"));
            });
        }
    }

    @Override public void onSensorChanged(SensorEvent e) {
        if(activeSensor==null || e.sensor!=activeSensor ||
           sensorReading==null)return;
        StringBuilder sb=new StringBuilder(e.sensor.getName());
        for(int i=0;i<e.values.length;i++) {
            sb.append("\n"+trEn("Değer ")).append(i+1).append(": ")
              .append(String.format(Locale.US,"%.4f",e.values[i]));
        }
        sensorReading.setText(sb.toString());
    }

    @Override public void onAccuracyChanged(Sensor s,int accuracy){}

    void tests() {
        LinearLayout screen=card("EKRAN RENK TESTİ");
        field(screen,"Amaç",
            "Ölü piksel veya renk sorunu için tam ekran renk testi.");
        action("RENK TESTİNİ BAŞLAT",screen,()->colorTest());

        LinearLayout touch=card("DOKUNMATİK TESTİ");
        field(touch,"Amaç",
            "Parmağının izini ekranda çizerek dokunmayı kontrol et.");
        action("DOKUNMATİK TESTİNİ BAŞLAT",touch,()->touchTest());

        LinearLayout info=card("TEST BİLGİSİ");
        field(info,"Not",
            "Bu testler donanımı onarmaz; yalnızca görsel kontrol sağlar.");
    }

    void colorTest() {
        final int[] colors={
            Color.RED,Color.GREEN,Color.BLUE,
            Color.WHITE,Color.BLACK,Color.GRAY
        };
        colorIndex=0;
        FrameLayout frame=new FrameLayout(this);
        frame.setBackgroundColor(colors[0]);
        TextView exit=text(english?"TAP: NEXT COLOR\nLONG PRESS: EXIT":"DOKUN: SONRAKİ RENK\nUZUN BAS: ÇIKIŞ",17,Color.WHITE,true);
        exit.setGravity(Gravity.CENTER);
        exit.setShadowLayer(3,1,1,Color.BLACK);
        frame.addView(exit,new FrameLayout.LayoutParams(-1,-1));
        frame.setOnClickListener(v->{
            colorIndex++;
            if(colorIndex>=colors.length)showTab(2);
            else frame.setBackgroundColor(colors[colorIndex]);
        });
        frame.setOnLongClickListener(v->{
            showTab(2);
            return true;
        });
        setContentView(frame);
    }

    void touchTest() {
        FrameLayout f=new FrameLayout(this);
        f.setBackgroundColor(BG);
        TouchView canvas=new TouchView();
        f.addView(canvas,new FrameLayout.LayoutParams(-1,-1));
        TextView back=text("GERİ DÖN",15,BG,true);
        back.setGravity(Gravity.CENTER);
        back.setBackground(shape(CYAN,12));
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(
            dp(125),dp(48),Gravity.TOP|Gravity.RIGHT);
        p.setMargins(0,dp(18),dp(18),0);
        f.addView(back,p);
        back.setOnClickListener(v->showTab(2));
        setContentView(f);
    }

    class TouchView extends View {
        Paint paint=new Paint(3);
        ArrayList<Path> paths=new ArrayList<>();
        Path current;
        TouchView(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas c) {
            c.drawColor(BG);
            paint.setColor(CYAN);
            paint.setStrokeWidth(dp(4));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            for(Path path:paths)c.drawPath(path,paint);
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e) {
            float x=e.getX(),y=e.getY();
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN) {
                current=new Path();
                current.moveTo(x,y);
                paths.add(current);
                invalidate();
                return true;
            }
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE && current!=null) {
                current.lineTo(x,y);
                invalidate();
                return true;
            }
            if(e.getActionMasked()==MotionEvent.ACTION_UP) {
                current=null;
                return true;
            }
            return true;
        }
    }

    String makeReport() {
        ActivityManager.MemoryInfo m=memory();
        StatFs fs=new StatFs(Environment.getDataDirectory().getPath());
        int sensorCount=sensorManager==null?-1:
            sensorManager.getSensorList(Sensor.TYPE_ALL).size();

        return (english?"BYBLACK DEVICELAB V2 REPORT\n":"BYBLACK DEVICELAB V2 CİHAZ RAPORU\n")+
            (english?"Date: ":"Tarih: ")+new Date()+"\n"+
            (english?"Manufacturer: ":"Üretici: ")+Build.MANUFACTURER+"\n"+
            (english?"Model: ":"Model: ")+Build.MODEL+"\n"+
            "Android: "+Build.VERSION.RELEASE+"\n"+
            "API: "+Build.VERSION.SDK_INT+"\n"+
            "ABI: "+Arrays.toString(Build.SUPPORTED_ABIS)+"\n"+
            (english?"Logical processors: ":"Mantıksal işlemciler: ")+
                Runtime.getRuntime().availableProcessors()+"\n"+
            (english?"Total RAM: ":"Toplam RAM: ")+gb(m.totalMem)+"\n"+
            (english?"Available RAM: ":"Kullanılabilir RAM: ")+gb(m.availMem)+"\n"+
            (english?"App data partition total: ":"Uygulama veri alanı toplam: ")+gb(fs.getTotalBytes())+"\n"+
            (english?"App data partition free: ":"Uygulama veri alanı boş: ")+gb(fs.getAvailableBytes())+"\n"+
            (english?"Battery percent: ":"Batarya yüzdesi: ")+percent()+"\n"+
            (english?"Battery temperature C: ":"Batarya sıcaklığı C: ")+temperature()+"\n"+
            (english?"Battery status: ":"Batarya durumu: ")+chargeStatus()+"\n"+
            (english?"Sensor count: ":"Sensör sayısı: ")+sensorCount+"\n"+
            "Generated offline by ByBlack DeviceLab\n";
    }

    void report() {
        LinearLayout c=card("CİHAZ RAPORU");
        TextView t=text(makeReport(),13,WHITE,false);
        t.setTextIsSelectable(true);
        c.addView(t);

        action("RAPORU PAYLAŞ",c,()->{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,makeReport());
            startActivity(Intent.createChooser(i,trEn("Raporu paylaş")));
        });

        action("RAPORU KOPYALA",c,()->{
            android.content.ClipboardManager cm=
                (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(
                ClipData.newPlainText("DeviceLab Report",makeReport()));
            Toast.makeText(this,trEn("Rapor kopyalandı"),Toast.LENGTH_SHORT).show();
        });

        action("RAPORU YENİLE",c,()->showTab(3));
    }

    @Override protected void onPause() {
        super.onPause();
        stopSensor();
    }

    @Override protected void onDestroy() {
        running=false;
        handler.removeCallbacks(ticker);
        stopSensor();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(currentTab!=0)showTab(0);
        else super.onBackPressed();
    }
}
