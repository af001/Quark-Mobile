package technology.xor.chirp.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import technology.xor.chirp.R;

public class ForgotDialog {

    /**
     * AlertUser: Used to reset the user's password in the event they forgot it. Build a message and
     * allow users to enter their email. If the email exists, Parse will send them an email with
     * instructions.
     * @param context
     */
    public void AlertUser(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter the email address that is associated with this account.");

        final int maxLength = 25;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(75, 75, 75, 75);

        TextView tv = new TextView(context);
        tv.setTextSize(16);
        tv.setTextColor(context.getResources().getColor(R.color.text));
        tv.setText("Email Address");
        layout.addView(tv);

        final EditText label = new EditText(context);
        label.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        label.setFilters(fArray);
        label.setTextColor(context.getResources().getColor(R.color.text));
        label.setHintTextColor(context.getResources().getColor(R.color.text));
        label.setHint("Enter email...");
        layout.addView(label);

        builder.setView(layout);
        builder.setPositiveButton("Send",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        if (label.getText().toString().equals("")) {
                            AlertUser(context);
                            label.setError("Empty emails are not allowed!");
                        } else {
                            if (IsValidEmail(label.getText().toString())) {
                                // TODO: Send user an email with password reset instructions
                            } else {
                                AlertUser(context);
                                label.setError("Invalid email!");
                            }
                        }
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                label.setError(null);
                label.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }

    /**
     * IsValidEmail - Validate a user's email address that they enter in the edit text box.
     * @param email
     * @return
     */
    private boolean IsValidEmail(String email) {
        CharSequence target = email;
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}