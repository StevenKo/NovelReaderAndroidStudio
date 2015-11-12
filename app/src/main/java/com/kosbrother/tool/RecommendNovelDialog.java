package com.kosbrother.tool;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.novel.reader.R;


public class RecommendNovelDialog {

    static Activity mActivity;
    private static AlertDialog dialog;

    public static void createReportDialog(Activity act) {
        mActivity = act;
        showDialog();
    }

    private static void showDialog() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        LinearLayout recomendLayout = (LinearLayout) inflater.inflate(R.layout.dialog_recommend_novel, null);

        final EditText novelNameEditText = (EditText) recomendLayout.findViewById(R.id.novel_name);
        final EditText novelAuthorEditText = (EditText) recomendLayout.findViewById(R.id.novel_author);


        Builder a = new Builder(mActivity).setTitle(mActivity.getResources().getString(R.string.menu_recommend_novel)).setIcon(R.drawable.icon_report)
                .setPositiveButton(mActivity.getResources().getString(R.string.report_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent emailIntent2 = new Intent(Intent.ACTION_SEND);
                        emailIntent2.setType("plain/text");
                        emailIntent2.putExtra(Intent.EXTRA_EMAIL, new String[]{mActivity.getResources().getString(R.string.respond_mail_address)});
                        emailIntent2.putExtra(Intent.EXTRA_SUBJECT, mActivity.getResources().getString(R.string.menu_recommend_novel));
                        emailIntent2.putExtra(Intent.EXTRA_TEXT,
                                mActivity.getResources().getString(R.string.report_novel) + novelNameEditText.getText().toString()
                                        + "\n" + mActivity.getResources().getString(R.string.novel_author) + novelAuthorEditText.getText().toString()
                        );
                        mActivity.startActivity(Intent.createChooser(emailIntent2, "Send mail..."));
                    }
                }).setNegativeButton(mActivity.getResources().getString(R.string.report_cancel), null);
        a.setView(recomendLayout);
        dialog = a.create();
        dialog.show();
    }

}
