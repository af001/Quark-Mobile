package technology.xor.chirp.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import technology.xor.chirp.R;

public class IntervalDialog {

    public void AlertUser(final Context context, final Activity activity, final TextView status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setTitle("Beacon Interval Settings");
        builder.setMessage("Enter the amount of time that you would like this device to report its position.");

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int intervalValue = sharedPrefs.getInt("interval", 103);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(75, 75, 75, 75);

        TextView tv = new TextView(context);
        tv.setTextSize(16);
        tv.setTextColor(context.getResources().getColor(R.color.text));
        tv.setText("Beacon Interval");
        layout.addView(tv);

        final RadioButton[] rb = new RadioButton[9];
        final RadioGroup rg = new RadioGroup(context);
        rg.setOrientation(RadioGroup.VERTICAL);

        for (int ix = 0; ix < 9; ix++) {
            rb[ix] = new RadioButton(context);
            rb[ix].setId(100 + ix);
            rg.addView(rb[ix]);
        }

        rb[intervalValue % 10].setChecked(true);

        rb[0].setText("1 minute");
        rb[0].setTextColor(context.getResources().getColor(R.color.text));

        rb[1].setText("2 minutes");
        rb[1].setTextColor(context.getResources().getColor(R.color.text));

        rb[2].setText("5 minutes");
        rb[2].setTextColor(context.getResources().getColor(R.color.text));

        rb[3].setText("10 minutes");
        rb[3].setTextColor(context.getResources().getColor(R.color.text));

        rb[4].setText("15 minutes");
        rb[4].setTextColor(context.getResources().getColor(R.color.text));

        rb[5].setText("30 minutes");
        rb[5].setTextColor(context.getResources().getColor(R.color.text));

        rb[6].setText("1 hour");
        rb[6].setTextColor(context.getResources().getColor(R.color.text));

        rb[7].setText("6 hours");
        rb[7].setTextColor(context.getResources().getColor(R.color.text));

        rb[8].setText("12 hours");
        rb[8].setTextColor(context.getResources().getColor(R.color.text));

        layout.addView(rg);

        builder.setView(layout);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if (rg.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(context, "Please select an interval!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putInt("interval", rg.getCheckedRadioButtonId());
                            editor.apply();
                        }
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (rg.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(context, "Please select an inerval!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }
}
