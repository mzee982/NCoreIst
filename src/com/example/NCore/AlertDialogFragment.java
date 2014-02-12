package com.example.NCore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment {

    // Interface
    public interface AlertDialogListener {
        public void onAlertDialogPositiveClick(DialogFragment dialogFragment);
    }

    // Members
    private AlertDialogListener mListener;
    private String mMessage;

    public AlertDialogFragment(String aMessage) {
        super();

        mMessage = aMessage;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (AlertDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AlertDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(mMessage);
        builder.setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertDialogPositiveClick(AlertDialogFragment.this);
                    }
                });

        return builder.create();
    }

}
