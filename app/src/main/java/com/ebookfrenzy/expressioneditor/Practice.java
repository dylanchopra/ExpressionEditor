package com.ebookfrenzy.expressioneditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ebookfrenzy.expressioneditor.recognizer.Point;
import com.ebookfrenzy.expressioneditor.recognizer.PointCloudRecognizer;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class Practice extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    Button recognize;
    Button recognize2;
    TextView tv;
    GestureOverlayView mView;
    ArrayList<GestureStroke> gstroke;
    com.ebookfrenzy.expressioneditor.recognizer.Gesture[] globalgest;
    public static final String EXTRA_MESSAGE = "com.ebookfrenzy.expressioneditor";
    public static final String EXTRA_MESSAGE2 = "com.ebookfrenzy.expressioneditor2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        tv = findViewById(R.id.textView9);
        tv.setText(generator());


        recognize = findViewById(R.id.button5);
        recognize.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView view = findViewById(R.id.textView7);
                view.setText("");
            }
        });
        recognize2 = findViewById(R.id.button7);
        recognize2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView view = findViewById(R.id.textView7);
                String s = view.getText().toString();
                String lastchar = "";
                String secondlast = "";
                String thirdlast = "";
                if(s.length() > 0){
                    lastchar = s.substring(s.length()-1);
                }
                if(s.length() > 1){
                    secondlast = s.substring(s.length()-2, s.length()-1);
                }
                if(s.length() > 2){
                    thirdlast = s.substring(s.length()-3, s.length()-2);
                }


                if(lastchar.equals("(") && (secondlast.equals("n") || secondlast.equals("g") || secondlast.equals("s") || secondlast.equals("t"))){
                    if(thirdlast.equals("i") || thirdlast.equals("o") || thirdlast.equals("a")) {
                        s = s.substring(0, s.length() - 4);
                        view.setText(s);
                    }
                    if(thirdlast.equals("l")){
                        s = s.substring(0, s.length()-3);
                        view.setText(s);
                    }
                    if(thirdlast.equals("r")){
                        s = s.substring(0, s.length()-5);
                        view.setText(s);
                    }
                }
                else {
                    if (s.length() > 0) {
                        s = s.substring(0, s.length() - 1);
                        view.setText(s);
                    }
                    view.setText(s);
                }
            }
        }
        );

        mView = findViewById(R.id.gestures2);
        mView.addOnGesturePerformedListener(this);

        try{
            globalgest = loadTemplates() ;
        }
        catch(IOException e){
            String s = e.getMessage();
            Log.d("tag",s);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.item3:
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

        Vector ptvec = new Vector();
        gstroke = gesture.getStrokes();


        for(int stk = 0; stk < gstroke.size(); stk++){
            float[] pts = gstroke.get(stk).points;
            for(int p = 0; p < pts.length; p+=2 ){
                Point q = new Point(pts[p], pts[p+1], stk);
                ptvec.add(q);
            }
        }

        Point[] all = new Point[ptvec.size()];

        for(int j= 0; j < ptvec.size(); j++){
            all[j] = (Point) ptvec.get(j);

            /*
            String s = String.valueOf(all[j].X);
            s+= "," + all[j].Y;
            s+= "+" + all[j].StrokeID;
            Log.d("tag", s);

             */

        }

        com.ebookfrenzy.expressioneditor.recognizer.Gesture gest = new com.ebookfrenzy.expressioneditor.recognizer.Gesture(all, "input");

        PointCloudRecognizer pcr = new PointCloudRecognizer();

        String expression = pcr.Classify(gest, globalgest);


        TextView v = findViewById(R.id.textView7);
        String get = (String) v.getText();

        String put = get + expression;
        v.setText(put);



    }

    public com.ebookfrenzy.expressioneditor.recognizer.Gesture[] loadTemplates() throws IOException {
        com.ebookfrenzy.expressioneditor.recognizer.Gesture[] gestarr = new com.ebookfrenzy.expressioneditor.recognizer.Gesture[10];

        for(int i = 0; i < 10; i++){
            String id = String.valueOf(i);

            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(id)));
            String line = reader.readLine();
            String name = line;
            int size = Integer.parseInt(reader.readLine());
            Point[] pts = new Point[size];

            for(int j =0; j < size; j++){
                line = reader.readLine();
                int comma = line.indexOf(',');
                int plus = line.indexOf('+');
                float x, y;
                int strokeID;
                x = Float.parseFloat(line.substring(0,comma));
                y = Float.parseFloat(line.substring(comma+1, plus));
                strokeID = Integer.parseInt(line.substring(plus));

                Point p = new Point(x, y, strokeID);
                pts[j] = p;
            }

            com.ebookfrenzy.expressioneditor.recognizer.Gesture gest = new com.ebookfrenzy.expressioneditor.recognizer.Gesture(pts, name);
            gestarr[i] = gest;
        }

        return gestarr;

    }

    public String generator(){
        String output = "";
        String[] arr = {"9+17", "25/5+6", "12-5*2", "16/4+12-7",
                "12-144/12", "cos(0)", "sin(0)", "37-23", "14*6", "39/3", "sqrt(225)"};

        Random rand = new Random();
        int rand1 = rand.nextInt(arr.length);
        output = arr[rand1];

        return output;
    }


    public void sendMessage2(View view){
        Intent intent = new Intent(this, Checker.class);
        TextView string = findViewById(R.id.textView9);
        TextView string2 = findViewById(R.id.textView7);
        String passer = string.getText().toString();
        String passer2 = string2.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, passer);
        intent.putExtra(EXTRA_MESSAGE2, passer2);
        startActivity(intent);
    }
}
