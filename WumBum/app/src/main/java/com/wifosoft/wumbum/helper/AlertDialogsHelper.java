package com.wifosoft.wumbum.helper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drew.lang.GeoLocation;

import com.wifosoft.wumbum.R;

import com.wifosoft.wumbum.helper.metadata.MediaDetailsMap;
import com.wifosoft.wumbum.helper.metadata.MetadataHelper;
import com.wifosoft.wumbum.model.Media;
import com.wifosoft.wumbum.util.Measure;

import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;



public class AlertDialogsHelper {

    public static  AlertDialog getInsertTextDialog(ThemedActivity activity, EditText editText, @StringRes int title) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(com.wifosoft.wumbum.R.layout.dialog_insert_text, null);
        TextView textViewTitle = dialogLayout.findViewById(R.id.rename_title);

        ((CardView) dialogLayout.findViewById(com.wifosoft.wumbum.R.id.dialog_chose_provider_title)).setCardBackgroundColor(activity.getCardBackgroundColor());
        textViewTitle.setBackgroundColor(activity.getPrimaryColor());
        textViewTitle.setText(title);
        ThemeHelper.setCursorColor(editText, activity.getTextColor());

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        editText.setSingleLine(true);
        editText.getBackground().mutate().setColorFilter(activity.getTextColor(), PorterDuff.Mode.SRC_IN);
        editText.setTextColor(activity.getTextColor());

        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editText, null);
        } catch (Exception ignored) { }

        ((RelativeLayout) dialogLayout.findViewById(com.wifosoft.wumbum.R.id.container_edit_text)).addView(editText);

        dialogBuilder.setView(dialogLayout);
        return dialogBuilder.create();
    }

    public static AlertDialog getTextDialog(ThemedActivity activity, @StringRes int title, @StringRes int Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity,activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView dialogTitle = dialogLayout.findViewById(R.id.text_dialog_title);
        TextView dialogMessage = dialogLayout.findViewById(R.id.text_dialog_message);

        ((CardView) dialogLayout.findViewById(com.wifosoft.wumbum.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogMessage.setTextColor(activity.getTextColor());
        builder.setView(dialogLayout);
        return builder.create();
    }

    public static AlertDialog getProgressDialog(final ThemedActivity activity,  String title, String message){
        AlertDialog.Builder progressDialog = new AlertDialog.Builder(activity, activity.getDialogStyle());
        View dialogLayout = activity.getLayoutInflater().inflate(com.wifosoft.wumbum.R.layout.dialog_progress, null);
        TextView dialogTitle = dialogLayout.findViewById(R.id.progress_dialog_title);
        TextView dialogMessage = dialogLayout.findViewById(R.id.progress_dialog_text);

        dialogTitle.setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(com.wifosoft.wumbum.R.id.progress_dialog_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
        ((ProgressBar) dialogLayout.findViewById(com.wifosoft.wumbum.R.id.progress_dialog_loading)).getIndeterminateDrawable().setColorFilter(activity.getPrimaryColor(), android.graphics
                .PorterDuff.Mode.SRC_ATOP);

        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogMessage.setTextColor(activity.getTextColor());

        progressDialog.setCancelable(false);
        progressDialog.setView(dialogLayout);
        return progressDialog.create();
    }

    public static AlertDialog getDetailsDialog(final ThemedActivity activity, final Media f) {
        AlertDialog.Builder detailsDialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());
        MetadataHelper mdhelper = new MetadataHelper();
        MediaDetailsMap<String, String> mainDetails = mdhelper.getMainDetails(activity, f);
        final View dialogLayout = activity.getLayoutInflater().inflate(com.wifosoft.wumbum.R.layout.dialog_media_detail, null);
        ImageView imgMap = dialogLayout.findViewById(R.id.photo_map);
        dialogLayout.findViewById(com.wifosoft.wumbum.R.id.details_title).setBackgroundColor(activity.getPrimaryColor());
        ((CardView) dialogLayout.findViewById(com.wifosoft.wumbum.R.id.photo_details_card)).setCardBackgroundColor(activity.getCardBackgroundColor());

        final GeoLocation location;
        imgMap.setVisibility(View.GONE);

        final TextView showMoreText = dialogLayout.findViewById(R.id.details_showmore);
        showMoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDetails(dialogLayout, activity, f);
                showMoreText.setVisibility(View.GONE);
            }
        });

        detailsDialogBuilder.setView(dialogLayout);
        loadDetails(dialogLayout,activity, mainDetails);
        return detailsDialogBuilder.create();
    }

    private static void loadDetails(View dialogLayout, ThemedActivity activity, MediaDetailsMap<String, String> metadata) {
        LinearLayout detailsTable = dialogLayout.findViewById(R.id.ll_list_details);

        int tenPxInDp = Measure.pxToDp(10, activity);
        int hundredPxInDp = Measure.pxToDp (125, activity);//more or less an hundred. Did not used weight for a strange bug

        for (int index : metadata.getKeySet()) {
            LinearLayout row = new LinearLayout(activity.getApplicationContext());
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(activity.getApplicationContext());
            TextView value = new TextView(activity.getApplicationContext());
            label.setText(metadata.getLabel(index));
            label.setLayoutParams((new LinearLayout.LayoutParams(hundredPxInDp, LinearLayout.LayoutParams.WRAP_CONTENT)));
            value.setText(metadata.getValue(index));
            value.setLayoutParams((new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)));
            label.setTextColor(activity.getTextColor());
            label.setTypeface(null, Typeface.BOLD);
            label.setGravity(Gravity.END);
            label.setTextSize(16);
            value.setTextColor(activity.getTextColor());
            value.setTextSize(16);
            value.setPaddingRelative(tenPxInDp, 0, tenPxInDp, 0);
            row.addView(label);
            row.addView(value);
            detailsTable.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private static void showMoreDetails(View dialogLayout, ThemedActivity activity, Media media) {
        MediaDetailsMap<String, String> metadata = new MediaDetailsMap<>();//media.getAllDetails();
        loadDetails(dialogLayout ,activity , metadata);
    }


    private static String getChangeLogFromAssets(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("latest_changelog.md");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int i;
        while ((i = inputStream.read()) != -1)
            outputStream.write(i);

        inputStream.close();
        return outputStream.toString();
    }
}
