package com.kosbrother.tool;


import com.novel.reader.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class Report {

    static Activity mActivity;
    private static AlertDialog dialog;

    public static void createReportDialog(Activity act, String novelName, String article) {
        mActivity = act;
        showDialog(novelName, article);
    }

    public static void createReportDialog(Activity act, String novelName, String article, String problem) {
        mActivity = act;
        showDialog(novelName, article, problem);
    }

    private static void showDialog(String novelName, String article, String problem) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        LinearLayout recomendLayout = (LinearLayout) inflater.inflate(R.layout.dialog_report, null);

        final EditText novelNameEditText = (EditText) recomendLayout.findViewById(R.id.novel_name);
        final EditText articleEditText = (EditText) recomendLayout.findViewById(R.id.article_name);
        final EditText problemEditText = (EditText) recomendLayout.findViewById(R.id.problem);

        articleEditText.setText(article);
        novelNameEditText.setText(novelName);
        problemEditText.setText(problem);
        problemEditText.requestFocus();

        Builder a = new Builder(mActivity).setTitle(mActivity.getResources().getString(R.string.report_title)).setIcon(R.drawable.icon_report)
                .setPositiveButton(mActivity.getResources().getString(R.string.report_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (problemEditText.getText().toString().equals("")) {
                            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.report_problem_hint), Toast.LENGTH_LONG).show();
                            createReportDialog(mActivity, novelNameEditText.getText().toString(), articleEditText.getText().toString());
                        } else {
                            final Intent emailIntent2 = new Intent(Intent.ACTION_SEND);
                            emailIntent2.setType("plain/text");
                            emailIntent2.putExtra(Intent.EXTRA_EMAIL, new String[]{mActivity.getResources().getString(R.string.respond_mail_address)});
                            emailIntent2.putExtra(Intent.EXTRA_SUBJECT, mActivity.getResources().getString(R.string.report_title));
                            emailIntent2.putExtra(Intent.EXTRA_TEXT,
                                    mActivity.getResources().getString(R.string.report_novel) + novelNameEditText.getText().toString()
                                            + "\n" + mActivity.getResources().getString(R.string.report_chapter) + articleEditText.getText().toString()
                                            + "\n" + mActivity.getResources().getString(R.string.report_content) + "\n" + problemEditText.getText().toString()
                            );
                            mActivity.startActivity(Intent.createChooser(emailIntent2, "Send mail..."));

                        }
                    }
                }).setNegativeButton(mActivity.getResources().getString(R.string.report_cancel), null);
        a.setView(recomendLayout);
        dialog = a.create();
        dialog.show();
    }

    private static void showDialog(String novelName, String article) {
        showDialog(novelName, article, "");
    }
}
