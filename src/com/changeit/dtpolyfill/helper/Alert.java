package com.changeit.dtpolyfill.helper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class Alert {
	View view;

	public Alert(View view) {
		this.view = view;
	}

	public void show(String alertText) {
        // Obvious next step is to send javascript command

		AlertDialog alertDialog = new AlertDialog.Builder(this.view.getContext()).create();
		alertDialog.setTitle("Reset...");
		alertDialog.setMessage( alertText );
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			// here you can add functions
		}
		});
		alertDialog.show();
	}
}


