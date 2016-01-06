package com.example.NCore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class QuestionDialogFragment extends DialogFragment {

    // Interface
    public interface QuestionDialogListener {
        public void onQuestionDialogResult(int responseCode, DialogFragment dialogFragment);
    }

    // Responses
    public static final int RESPONSE_YES = 1;
    public static final int RESPONSE_NO = 2;
    public static final int RESPONSE_CANCEL = 0;

    // Members
    private QuestionDialogListener mListener;
    private String mQuestion;

    public QuestionDialogFragment(String aQuestion) {
        super();

        mQuestion = aQuestion;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (QuestionDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement QuestionDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(mQuestion);
        builder.setPositiveButton(
                android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onQuestionDialogResult(RESPONSE_YES, QuestionDialogFragment.this);
                    }
                });
        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onQuestionDialogResult(RESPONSE_NO, QuestionDialogFragment.this);
                    }
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onQuestionDialogResult(RESPONSE_CANCEL, this);

        super.onCancel(dialog);
    }

}
