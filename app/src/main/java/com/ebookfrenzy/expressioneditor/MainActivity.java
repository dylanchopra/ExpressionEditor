package com.ebookfrenzy.expressioneditor;

import androidx.appcompat.app.AppCompatActivity;
import com.ebookfrenzy.expressioneditor.recognizer.*;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    Button recognize;
    GestureOverlayView mView;
    ArrayList<GestureStroke> gstroke;
    com.ebookfrenzy.expressioneditor.recognizer.Gesture[] globalgest;

    public static final String EXTRA_MESSAGE = "com.ebookfrenzy.expressioneditor";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognize = findViewById(R.id.button);
        recognize.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView view = findViewById(R.id.textView2);
                view.setText("");
            }
        }
);
        mView = findViewById(R.id.gestures);
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

          /*  String s = String.valueOf(all[j].X);
            s+= "," + all[j].Y;
            s+= "+" + all[j].StrokeID;
            Log.d("tag", s);
            */
        }

        com.ebookfrenzy.expressioneditor.recognizer.Gesture gest = new com.ebookfrenzy.expressioneditor.recognizer.Gesture(all, "input");

        PointCloudRecognizer pcr = new PointCloudRecognizer();

        String expression = pcr.Classify(gest, globalgest);


        TextView v = findViewById(R.id.textView2);
        String get = (String) v.getText();

        String put = get + expression;
        v.setText(put);



    }

    public com.ebookfrenzy.expressioneditor.recognizer.Gesture[] loadTemplates() throws IOException{
        com.ebookfrenzy.expressioneditor.recognizer.Gesture[] gestarr = new com.ebookfrenzy.expressioneditor.recognizer.Gesture[20];

        for(int i = 0; i < 20; i++){
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



    public void sendMessage(View view){
        Intent intent = new Intent(this, Solver.class);
        TextView string = findViewById(R.id.textView2);
        String passer = string.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, passer);
        startActivity(intent);
    }

}
