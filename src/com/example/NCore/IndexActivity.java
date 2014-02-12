package com.example.NCore;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class IndexActivity extends Activity {

    private char[] mOutputBuffer = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.index);

//        mOutputBuffer = getIntent().getCharArrayExtra(LoginActivity.EXTRA_OUTPUT_BUFFER);
    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView outputTextView = (TextView) findViewById(R.id.OutputTextView);
        //CharSequence outputCS = Html.fromHtml(new String(mOutputBuffer));
        //outputTextView.setText(outputCS);
        outputTextView.setText("Hello, mizu?");
    }

}