package hcmus.android.crm.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import hcmus.android.crm.R;
import hcmus.android.crm.utilities.PreferenceManager;
import hcmus.android.crm.utilities.Utils;

public class BaseActivity extends AppCompatActivity {
    protected PreferenceManager preferenceManager;

    @Override
    public void setContentView(View view) {
        super.setContentView(view); // Call superclass implementation
        preferenceManager = new PreferenceManager(getApplicationContext());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
    }

    protected void showToast(String message, int length) {
        Utils.showToast(getApplicationContext(), message, length);
    }
}
