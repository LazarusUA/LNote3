package com.note.lazarus.lnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Note extends Activity {

    private final Context This = Note.this;
    private Animation anim = null;
    private ToggleButton isRead = null;
    private AlertDialog settingsAlertDialog = null;
    private SharedPreferences InMem;

    private EditText Text = null;
    private TextView FileName = null;
    private Spinner spin;


    ///////////////////CREATE///////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_note);

            FileName = findViewById(Widget.LABEL_FILE_NAME);
            Text = findViewById(Widget.TEXT);

            InMem = PreferenceManager.getDefaultSharedPreferences(this);

        } catch (Exception e) {
            Log.e("CREATE", e.toString());
            Toast.makeText(Note.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /////////////////START///////////////////////////////////////////////////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (getIntent().getStringExtra("PATH") != null) {
                Open(getIntent().getStringExtra("PATH"));
                FileName.setText(getIntent().getStringExtra("PATH"));
            } else {
                Text.setText(InMem.getString("SaveText", ""));
                FileName.setText(InMem.getString("PATH", ""));
            }

            Intent intent = getIntent();
            if (intent.getData() != null) {
                Uri current = intent.getData();

                Open(current.getPath());

                FileName.setText(current.getPath());
            }

            int defTextSize = Integer.parseInt(getResources()
                    .getStringArray(R.array.SizeTXT)[InMem.getInt("TEXT_SIZE", 4)]);

            Text.setTextSize(defTextSize);
            Text.setEnabled(!InMem.getBoolean("isRead", false));
            Text.setTextColor(0xFF000000);

        } catch (Error e) {
            Toast.makeText(Note.this, e.toString(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("START", e.toString());
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case (Widget.NEW_DOC): {
                    new AlertDialog.Builder(This).
                            setMessage("Створити заготовку?")
                            .setPositiveButton("Так", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    InMem.edit().remove("SaveText").remove("PATH").apply();
                                    Text.setText("");
                                    FileName.setText("");
                                }
                            }).
                            setNeutralButton("Ні", null).
                            show();
                    break;
                }
                case (Widget.OPEN):
                    Intent managerIntent;
                {
                    managerIntent = new Intent(This, Manager.class);
                    managerIntent.putExtra("Type", 1);
                    InMem.edit().putString("SaveText", String.valueOf(Text.getText())).
                            putString("PATH", FileName.getText().toString()).
                            apply();
                    startActivity(managerIntent);
                    this.finish();
                    break;
                }
                case (Widget.SAVE): {
                    managerIntent = new Intent(This, Manager.class);
                    managerIntent.putExtra("Type", 2);
                    InMem.edit().putString("SaveText", String.valueOf(Text.getText())).
                            putString("PATH", FileName.getText().toString()).
                            apply();
                    startActivity(managerIntent);
                    this.finish();
                    break;
                }
                case (Widget.SETTINGS): {
                    SettingsDialog();
                    settingsAlertDialog.show();
                    break;
                }
            }

        } catch (Exception e) {
            Toast.makeText(Note.this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e("ITEM_SELECTED", e.toString());
        }
        return super.onOptionsItemSelected(item);
    }

    private void SettingsDialog() {
        try {
            AlertDialog.Builder alertBuilderSettings = new AlertDialog.Builder(this, R.style.Theme);

            LayoutInflater rootSettings = this.getLayoutInflater();
            View settingsView = rootSettings.inflate(R.layout.settings, null);
            alertBuilderSettings.setView(settingsView);
            alertBuilderSettings.setTitle(R.string.settings);

            isRead = settingsView.findViewById(Widget.IS_READ);

            spin = settingsView.findViewById(R.id.btn_dialog_size_text);

            spin.setSelection(InMem.getInt("TEXT_SIZE", 4));
            isRead.setChecked(InMem.getBoolean("isRead", false));

            alertBuilderSettings.setPositiveButton(R.string.confirmation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int pos = spin.getSelectedItemPosition();
                    String[] ar = getResources().getStringArray(R.array.SizeTXT);

                    Text.setTextSize(Float.parseFloat(ar[pos]));

                    if (isRead.isChecked()) {
                        Text.setEnabled(false);
                        Text.setTextColor(0xff000000);

                    } else {
                        Text.setEnabled(true);
                        Text.setTextColor(0xff000000);
                    }

                    InMem.edit().putInt("TEXT_SIZE", spin.getSelectedItemPosition())
                            .putBoolean("isRead", isRead.isChecked())
                            .apply();

                    settingsAlertDialog.cancel();
                }
            });

            isRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        anim = AnimationUtils.loadAnimation(This, R.anim.anim_is_read);

                        if (isRead.isChecked()) {
                            isRead.setAnimation(anim);
                        } else {
                            isRead.setAnimation(anim);
                        }
                    } catch (Exception e) {
                        Toast.makeText(Note.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            alertBuilderSettings.setNeutralButton(R.string.cancel, null);

            settingsAlertDialog = alertBuilderSettings.create();

        } catch (Exception e) {
            Toast.makeText(Note.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case (KeyEvent.KEYCODE_MENU): {
                SettingsDialog();
                settingsAlertDialog.show();
                break;
            }
            case (KeyEvent.KEYCODE_BACK): {
                AlertDialog.Builder exiteDialog = new AlertDialog.Builder(This, R.style.Theme);
                exiteDialog.setMessage("Завершити роботу програми?");
                exiteDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InMem.edit().remove("SaveText").remove("PATH").apply();
                        finish();
                    }
                });
                exiteDialog.setNeutralButton(R.string.no, null);
                exiteDialog.show();
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    ///////////////////////OPEN///////////////////////////////////////////////////
    private void Open(String path) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(path));
        Text.setText("");
        while (bf.ready()) {
            Text.append(bf.readLine());
            Text.append("\n");
        }

        bf.close();
    }

}