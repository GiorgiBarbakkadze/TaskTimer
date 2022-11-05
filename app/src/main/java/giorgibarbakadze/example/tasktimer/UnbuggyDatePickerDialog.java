package giorgibarbakadze.example.tasktimer;


import android.app.DatePickerDialog;
import android.content.Context;

class UnbuggyDatePickerDialog extends DatePickerDialog {

    UnbuggyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    @Override
    protected void onStop() {
        // Replacing tryNotifyDateSet() with nothing - this is a workaround for Android bug https://android-review.googlesource.com/#/c/61270/A

        // Would also like to clear focus, but we cannot get at the private members, so we do nothing.  It seems to do no harm...
        // mDatePicker.clearFocus();

        // Now we would like to call super on onStop(), but actually what we would mean is super.super, because
        // it is super.onStop() that we are trying NOT to run, because it is buggy.  However, doing such a thing
        // in Java is not allowed, as it goes against the philosophy of encapsulation (the Creators never thought
        // that we might have to patch parent classes from the bottom up :)
        // However, we do not lose much by doing nothing at all, because in Android 2.* onStop() in androd.app.Dialog actually
        // does nothing and in 4.* it does:
        //              if (mActionBar != null) mActionBar.setShowHideAnimationEnabled(false);
        // which is not essential for us here because we use no action bar... QED
        // So we do nothing and we intend to keep this workaround forever because of users with older devices, who might
        // run Android 4.1 - 4.3 for some time to come, even if the bug is fixed in later versions of Android.
    }
}
