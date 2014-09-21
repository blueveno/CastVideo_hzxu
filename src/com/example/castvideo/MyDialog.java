package com.example.castvideo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;


public class MyDialog {
	Context mContext;
	public MyDialog(Context context){
		mContext = context;
	}
	public void ConfirmInputDlg(){
		
	}
	public void ConfirmAlertDlg(String title, String msg){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		// set title
		alertDialogBuilder.setTitle(title);
		// set dialog message
		alertDialogBuilder
			.setMessage(msg)
			.setCancelable(true);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();	
	}
	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {  
		  
        private int which = 0;  
        @Override  
        public void onClick(DialogInterface dialogInterface, int which) {  
            this.which = which;  
        }  
          
        public int getWhich() {  
            return which;  
        }  
    }  
	public void ConfirmAlertDlg(
			String title, 
			final boolean[] select_list, 
			String[] item_list, 
			String Des1, OnClickListener listener_ok,
			String Des2, OnClickListener Listener_cancel){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		// set title
		alertDialogBuilder.setTitle(title);
		final ChoiceOnClickListener choiceListener =   
                new ChoiceOnClickListener(){
			@Override  
	        public void onClick(DialogInterface dialogInterface, int which) {  
	            for (int idx=0; idx<select_list.length; idx++){
	            	if (which == idx){
	            		select_list[idx] = true;
	            	}else{
	            		select_list[idx] = false;
	            	}
	            }
	        }  
		};  
		// set dialog message
		alertDialogBuilder
		//.setMessage(msg)
			.setSingleChoiceItems(item_list, 0, choiceListener)
			.setPositiveButton(Des1, listener_ok)
			.setNegativeButton(Des2, Listener_cancel)
			.setCancelable(true);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();	
	}
	
	public void ConfirmAlertDlg(String title, 
			String msg, 
			String Des1, OnClickListener Listener1){
		Listener1 = chkNullListener(Listener1);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		// set title
		alertDialogBuilder.setTitle(title);
		// set dialog message
		alertDialogBuilder
		.setMessage(msg)
		.setCancelable(false)
		.setPositiveButton(Des1, Listener1);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();	
	}
	public void ConfirmAlertDlg(String title, 
			String msg, 
			String Des1, OnClickListener Listener1,
			String Des2, OnClickListener Listener2){
		Listener1 = chkNullListener(Listener1);
		Listener2 = chkNullListener(Listener2);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		// set title
		alertDialogBuilder.setTitle(title);
		// set dialog message
		alertDialogBuilder
		.setMessage(msg)
		.setCancelable(false)
		.setPositiveButton(Des1, Listener1)
		.setNegativeButton(Des2, Listener2);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();	
	}
	
	public OnClickListener chkNullListener(OnClickListener Listener){
		if (Listener == null){
			return new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
					dialog.cancel();
				}
			};
		}
		return Listener;
	}
	
	
}
