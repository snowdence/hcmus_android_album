package com.wifosoft.wumbum.activities.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

import com.wifosoft.wumbum.R;
import com.wifosoft.wumbum.helper.AlertDialogsHelper;
import com.wifosoft.wumbum.helper.StorageHelper;


public abstract class SharedMediaActivity extends BaseActivity {

    private int REQUEST_CODE_SD_CARD_PERMISSIONS = 42;

    public void requestSdCardPermissions() {
        AlertDialog textDialog = AlertDialogsHelper.getTextDialog(this, R.string.sd_card_write_permission_title, R.string.sd_card_permissions_message);
        textDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_SD_CARD_PERMISSIONS);
            }
        });
        textDialog.show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SD_CARD_PERMISSIONS) {
                Uri treeUri = resultData.getData();
                // Persist URI in shared preference so that you can use it later.
                StorageHelper.saveSdCardInfo(getApplicationContext(), treeUri);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Toast.makeText(this, R.string.got_permission_wr_sdcard, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
