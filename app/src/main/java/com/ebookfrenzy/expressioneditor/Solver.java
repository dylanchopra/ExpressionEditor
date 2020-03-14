package com.ebookfrenzy.expressioneditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class Solver extends AppCompatActivity {
    String expression;
    String answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solver);

        Intent intent = getIntent();
        expression = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        answer = ExpressionSolver(expression);
        TextView view = findViewById(R.id.textView);
        answer = answer.substring(1, answer.length()-1);
        view.setText(answer);

        Button closeButton = (Button) findViewById(R.id.button3);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    private static Vector<String> ESolver(Vector<String> expr) {
        Vector<String> simpler = new Vector<>();

        Vector<String> subvec = new Vector<>();

        //simpler = expr; //FIXME Remove later!

        //These three things are for keeping track of parentheses scope and depth.
        int parthes_str = 0;
        int parthes_end = 0;
        Stack<Integer> stack = new Stack<Integer>();

        //First order of business is to make a method that goes through and solves each of the things in parentheses, as well as
        //remove the parentheses from the vector. DO NOT FORGET ABOUT ELEMENTS AT END!
        //WE ALSO NEED A TRY AND CATCH METHOD FOR POSSIBLE BAD INPUT!!!

        int i;
        String tempor;

        for (i = 0; i < expr.size(); i++) {
            tempor = expr.elementAt(i);
            //If we encounter a start parenthesis
            if (tempor.equals("(")) {
                //If we encounter a parenthesis for the first time, save the starting index.
                if (stack.empty()) {
                    parthes_str = i;
                }
                stack.push(1);
            }
            //If we encounter an end parenthesis
            else if (tempor.equals(")")) {
                stack.pop();
                //If we've reached the end of our scope, save the ending index.
                if (stack.empty()) {
                    //System.out.println("Encountered ending parenthesis at " + i);
                    parthes_end = i;
                    List<String> sub_List = expr.subList(parthes_str+1, parthes_end);
                    //System.out.println(sub_List);
                    subvec.addAll(sub_List);
                    subvec = ESolver((subvec));
                    simpler.addAll(subvec);
                    subvec.clear();
                }
            }
            else { //If we are not dealing with parenthesis
                if (stack.empty()) { //If we are not dealing with a substring...
                    simpler.add(tempor);
                }
            }
            //System.out.println(i);
        }

        //System.out.println(simpler);


        //Once we go through parentheses, we have to do exponents.

        Vector<String> expo = new Vector<>();
        for (int j = 0; j < simpler.size(); j++) {
            if (simpler.elementAt(j).equals("^")) {
                double a =  Double.parseDouble(simpler.elementAt(j-1));
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                expo.remove(expo.lastElement());
                double result = Math.pow(a, b);
                expo.add(Double.toString(result));
                j++;
            }
            else {
                expo.add(simpler.elementAt(j));
            }

        }
        //System.out.println("expo is " + expo);

        simpler = expo;

        //System.out.println(simpler);




        //Next up, we have to do multiplication and division.
        Vector<String> multdiv = new Vector<>();
        for (int j = 0; j < simpler.size(); j++) {
            if (simpler.elementAt(j).equals("*")) {
                double a =  Double.parseDouble(simpler.elementAt(j-1));
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                multdiv.remove(multdiv.lastElement());
                double result = a * b;
                multdiv.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt((j)).equals("/")) {
                double a =  Double.parseDouble(simpler.elementAt(j-1));
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                multdiv.remove(multdiv.lastElement());
                double result = a / b;
                multdiv.add(Double.toString(result));
                j++;
            }
            else {
                multdiv.add(simpler.elementAt(j));
            }

        }
        //System.out.println("multdiv is " + multdiv);

        simpler = multdiv;

        //System.out.println(simpler);



        //Lastly, we have to do addition and subtraction.
        Vector<String> addsub = new Vector<>();
        for (int j = 0; j < simpler.size(); j++) {
            if (simpler.elementAt(j).equals("+")) {
                double a =  Double.parseDouble(simpler.elementAt(j-1));
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                addsub.remove(addsub.lastElement());
                double result = a + b;
                addsub.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt((j)).equals("-")) {
                double a =  Double.parseDouble(simpler.elementAt(j-1));
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                addsub.remove(addsub.lastElement());
                double result = a - b;
                addsub.add(Double.toString(result));
                j++;
            }
            else {
                addsub.add(simpler.elementAt(j));
            }

        }
        //System.out.println("addsub is " + addsub);

        simpler = addsub;

        //System.out.println("Mini-result: " + simpler);


        return simpler;
    }




    private static String ExpressionSolver(String expr)
    {
        String answer;
        String temp = "";
        Vector<String> myVec = new Vector<>(expr.length());

        //char[] exprCharacters = expr.toCharArray();
        String t;

        for(int i = 0; i < expr.length(); ++i) { //PUT NUMBERS AS ONE ELEMENT.
            //If character is a digit.
            t = Character.toString(expr.charAt(i));
            if (expr.charAt(i) >= '0' && expr.charAt(i) <= '9') {
                temp += expr.charAt(i);
                //If we reach the end of the list.
                if (i == expr.length()-1) {
                    myVec.add(temp);
                    temp = "";
                }
            }
            //If character is not a digit.
            else {
                //If we had a list of digits going in, add it in first.
                if (!temp.equals("")) {
                    myVec.add(temp);
                    temp = "";
                }
                //Add in character.
                myVec.add(t);
                temp = "";
            }
        }

        //Now, everything is an element in a vector. We must pass this through to a recursive method that first deals with parentheses.

        try {
            Vector<String> terror = ESolver(myVec);
            answer = terror.toString();
        }
        catch (Exception e) {
            answer = "ERROR!";
        }

        //This turns the simplified vector into a string to return. However, it keeps the beginning and end brackets.


        //System.out.println(answer);

        return answer;
    }
}
