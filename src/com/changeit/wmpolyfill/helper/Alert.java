package com.changeit.wmpolyfill.helper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class Alert {
	View view;

	public Alert(View view) {
		this.view = view;
	}

	public void show(String alertText, String title) {
        // Obvious next step is to send javascript command

		AlertDialog alertDialog = new AlertDialog.Builder(this.view.getContext()).create();
		alertDialog.setMessage( alertText );
		alertDialog.setTitle( title );
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// here you can add functions
			}
		});
		alertDialog.show();
	}

	public void show(String alertText) {
		show(alertText, "Info");
	}

}


