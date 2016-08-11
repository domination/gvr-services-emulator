package com.google.vr.vrcore.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class PermissionsHelperActivity extends Activity {

    public static Intent createIntent(Context context, String[] permissions, String reason) {
        Intent intent = new Intent(context, PermissionsHelperActivity.class);
        intent.putExtra("permissions", permissions);
        intent.putExtra("rationale", "reason");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requestPermissionsForIntent()) {
            finish();
        }
    }

    private boolean requestPermissionsForIntent() {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }

        String[] permissions = getIntent().getStringArrayExtra("permissions");

        requestPermissions(permissions, 1000);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            Toast.makeText(getApplicationContext(), "aaa", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
