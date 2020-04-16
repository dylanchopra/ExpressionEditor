package com.ebookfrenzy.expressioneditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class Checker extends AppCompatActivity {

    String expression;
    String answer;
    String givenAnswer;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checker);

        String appended = "";
        String correct = "CORRECT!";
        String wrong = "           INCORRECT! \n The correct answer is: ";

        Intent intent = getIntent();
        expression = intent.getStringExtra(Practice.EXTRA_MESSAGE);
        answer = ExpressionSolver(expression);

        givenAnswer = intent.getStringExtra(Practice.EXTRA_MESSAGE2);
        appended = "[" + givenAnswer + ".0]";

        tv = findViewById(R.id.textView10);

        if(appended.equals(answer)){
            tv.setText(correct);
        }
        else{
            tv.setText(wrong);
        }

        TextView view = findViewById(R.id.textView11);
        view.setText(answer);

        Button closeButton = (Button) findViewById(R.id.button9);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = new Intent(Checker.this, Practice.class);
                startActivity(intent2);
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

        //System.out.println(simpler); //To check what is going into our calculator.

        //Once we go through parentheses, we have to do word-related items first.
        Vector<String> wordBank = new Vector<>();

        for (int j = 0; j < simpler.size(); j++) {
            if (simpler.elementAt(j).equals("sin")) {//Sin
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                double result = Math.sin(b);
                wordBank.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt(j).equals("cos")) {//Cos
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                double result = Math.cos(b);
                wordBank.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt(j).equals("tan")) {//Tan
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                double result = Math.tan(b);
                wordBank.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt(j).equals("log")) { //FIXME Does it only in base 10.
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                double result = Math.log(b)/Math.log(10);
                wordBank.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt(j).equals("ln")) { //Natural log
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                double result = Math.log(b);
                wordBank.add(Double.toString(result));
                j++;
            }
            else {
                wordBank.add(simpler.elementAt(j));
            }

        }
        //System.out.println("wordbank is " + wordBank);

        simpler = wordBank;

        //Next, we have to do exponents.

        Vector<String> expo = new Vector<>();
        for (int j = 0; j < simpler.size(); j++) {
            if (simpler.elementAt(j).equals("^")) {
                double a =  Double.parseDouble(expo.lastElement());
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
                double a =  Double.parseDouble(multdiv.lastElement());
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                multdiv.remove(multdiv.lastElement());
                double result = a * b;
                multdiv.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt((j)).equals("/")) {
                double a =  Double.parseDouble(multdiv.lastElement());
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

        // System.out.println(simpler);



        //Lastly, we have to do addition and subtraction.
        Vector<String> addsub = new Vector<>();
        for (int j = 0; j < simpler.size(); j++) {
            if (simpler.elementAt(j).equals("+")) {
                double a =  Double.parseDouble(addsub.lastElement());
                double b =  Double.parseDouble(simpler.elementAt(j+1));
                addsub.remove(addsub.lastElement());
                double result = a + b;
                //System.out.println(result);
                addsub.add(Double.toString(result));
                j++;
            }
            else if (simpler.elementAt((j)).equals("-")) {
                double a;
                double b;
                a = Double.parseDouble(addsub.lastElement());
                b =  Double.parseDouble(simpler.elementAt(j+1));
                addsub.remove(addsub.lastElement());
                double result = a - b;
                //System.out.println(result);
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

                //FIXME ADD IN THE REMAINING WORDS HERE AND SOLVE THEM UP ABOVE. NEED TO FIX FOR VARIABLES AS WELL.
                if (i+1 < expr.length()) { //For 2-letter words
                    if (expr.charAt(i) == 'l' && expr.charAt(i+1) == 'n') { //SIN
                        myVec.add("ln");
                        i = i + 1;
                        temp = "";
                        continue;
                    }
                }
                if (i+2 < expr.length()) { //For 3-letter words
                    if (expr.charAt(i) == 's' && expr.charAt(i+1) == 'i' && expr.charAt(i+2) == 'n') { //SIN
                        myVec.add("sin");
                        i = i + 2;
                        temp = "";
                        continue;
                    }
                    else if (expr.charAt(i) == 'c' && expr.charAt(i+1) == 'o' && expr.charAt(i+2) == 's') { //COS
                        myVec.add("cos");
                        i = i + 2;
                        temp = "";
                        continue;
                    }
                    else if (expr.charAt(i) == 't' && expr.charAt(i+1) == 'a' && expr.charAt(i+2) == 'n') { //TAN
                        myVec.add("tan");
                        i = i + 2;
                        temp = "";
                        continue;
                    }
                    else if (expr.charAt(i) == 'l' && expr.charAt(i+1) == 'o' && expr.charAt(i+2) == 'g') { //LOG
                        myVec.add("log");
                        i = i + 2;
                        temp = "";
                        continue;
                    }
                }

                //Add in character if all else fails.
                myVec.add(t);
                temp = "";
            }
        }

        //This is good.

        //Now, everything is an element in a vector. We must pass this through to a recursive method that first deals with parentheses.

        try {
            Vector<String> terror = ESolver(myVec);
            answer = terror.toString();
        }
        catch (Exception e) {
            answer = "ERROR!";
        }

        //This turns the simplified vector into a string to return. However, it keeps the beginning and end brackets.


        System.out.println(answer);



        return answer;
    }
}
