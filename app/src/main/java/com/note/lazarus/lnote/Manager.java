package com.note.lazarus.lnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Manager extends Activity {

    private final Context This = this;
    private SharedPreferences InMem;
    private String stringPath = Environment.getExternalStorageDirectory().toString();

    private File[] arrayFile = null;
    private File renamePath;
    private ManagerCode mgrC;
    private ManagerList mgrL;

    private TextView labelPath = null;
    private EditText fileName = null;
    private LinearLayout lBar = null;
    private Button btnSave = null;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.manager);
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
            lBar = (LinearLayout) findViewById(R.id.lay_save_bar);
            labelPath = (TextView) findViewById(Widget.LabelPath);
            listView = (ListView) findViewById(Widget.LIST);
            btnSave = (Button) findViewById(Widget.BTN_SAVE);
            fileName = (EditText) findViewById(Widget.FILE_NAME);

            mgrC = new ManagerCode();
            mgrL = new ManagerList();

            InMem = PreferenceManager.getDefaultSharedPreferences(this);

            event();

            registerForContextMenu(listView);

        } catch (Exception e) {
            Toast.makeText(Manager.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    ///////////////////START////////////////////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        ListFiles(stringPath);

        if ((getIntent().getIntExtra("Type", 0)) == 2) {
            lBar.setVisibility(View.VISIBLE);
        }
    }

    ////////////////////BACK_PRESSED////////////////////////////////
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Note.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.storage_home): {
                stringPath = Environment.getExternalStorageDirectory().toString();
                ListFiles(stringPath);
                break;
            }
            case (R.id.storage_external): {
                String SDPath = new ManagerCode().getSdCardPath();

                if (new File(SDPath).listFiles() != null) {
                    stringPath = SDPath;
                    ListFiles(stringPath);
                } else
                    Toast.makeText(Manager.this, "Карта пам'яті не встановлена!", Toast.LENGTH_SHORT).show();

                break;
            }
            case (android.R.id.home): {
                ListFiles(mgrC.getPathBack(stringPath));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("PATH", stringPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        stringPath = savedInstanceState.getString("PATH");
        ListFiles(stringPath);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case (KeyEvent.KEYCODE_MENU): {
                makeDir();
                break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void ListFiles(String path) {
        labelPath.setText(path);
        getFileList(path);
    }

    ///////////////////////MAKE_DIR////////////////////////////////
    private void makeDir() {
        AlertDialog.Builder makeDirDialog = new AlertDialog.Builder(this, R.style.Theme);
        makeDirDialog.setTitle("Нова папка в \"" + stringPath + "\"");
        View view = getLayoutInflater().inflate(R.layout.view_make_dir, null);
        makeDirDialog.setView(view);
        final EditText dirName = (EditText) view.findViewById(R.id.dir_name);

        makeDirDialog.setPositiveButton(getResources().getString(R.string.make),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new File(stringPath + "/" + dirName.getText().toString()).mkdirs();
                        getFileList(stringPath);
                        listView.setFocusable(true);

                    }
                });
        makeDirDialog.setNeutralButton(getResources().getString(R.string.cancel), null);
        makeDirDialog.show();
    }

    ////////////FILE_LIST/////////////////////////////////////
    private void getFileList(String path) {
        try {
            File file = new File(path);
            stringPath = path;
            arrayFile = mgrL.Filter(file.listFiles());
            ArrayList<Map<String, Object>> list = new ArrayList<>(arrayFile.length);
            for (File finish : arrayFile) {
                Map map = new HashMap<>();
                if (finish.isFile()) {
                    map.put("Name", mgrC.getName(finish.getName()));
                    map.put("Type",mgrC.getFileType(finish.getName()).equals("0") ? "" :
                            mgrC.getFileType(finish.getName()));
                } else
                    map.put("Name", finish.getName());


                if (finish.isDirectory()) {
                    if (mgrC.getSpace(finish) > 0) {
                        map.put("Icon", R.mipmap.ic_folder);
                    } else if (mgrC.getSpace(finish) == 0) {
                        map.put("Icon", R.mipmap.ic_folder_null);
                    }
                } else if (finish.isFile()) {
                    map.put("Size", mgrC.getSize(finish));

                    if (mgrC.getFileType(finish.getName()).equals("txt")) {
                        map.put("Icon", R.mipmap.ic_txt_file);
                    } else if (mgrC.getFileType(finish.getName()).equals("apk")) {
                        map.put("Icon", R.mipmap.ic_file_apk);
                    } else
                        map.put("Icon", R.mipmap.ic_file);
                }

                list.add(map);
            }
            String[] name = {"Name", "Icon", "Size","Type"};
            int[] id = {R.id.name, R.id.icon, R.id.size, R.id.type};
            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.list, name, id);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, e.toString() + " getFileList", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteDialog(final int position, final AlertDialog al) {
        al.cancel();
        new AlertDialog.Builder(This, R.style.Theme)
                .setTitle(getResources().getString(R.string.delete))
                .setMessage("Видалити цей файл?")
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Manager.this, arrayFile[position].getName()
                                + " видалено", Toast.LENGTH_LONG).show();
                        mgrC.AllDelete(arrayFile[position]);
                        getFileList(stringPath);
                        al.cancel();
                    }
                })
                .setNeutralButton(getResources().getString(R.string.no), null)
                .show();
    }

    /////////////////RENAME/////////////////////////////////////
    private void rename(final AlertDialog al) {
        al.cancel();
        AlertDialog.Builder renameDialog = new AlertDialog.Builder(This, R.style.Theme)
                .setTitle(getResources().getString(R.string.rename))
                .setMessage("Нова назва:")
                .setNeutralButton(getResources().getString(R.string.cancel), null);

        View renameV = getLayoutInflater().inflate(R.layout.rename_layout, null);
        final EditText name = (EditText) renameV.findViewById(R.id.name);

        name.setText(renamePath.getName());
        renameDialog.setView(renameV);
        renameDialog.setPositiveButton(getResources().getString(R.string.rename),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        renamePath.renameTo(new File(stringPath + "/" + name.getText().toString()));
                        getFileList(stringPath);

                        al.cancel();
                        listView.setFocusable(true);
                    }
                });

        renameDialog.show();
    }

    ///////////////////////////////////EVENT//////////////////////////////////////////////////////////////
    private void event() throws Exception {
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BufferedWriter sWriter = new BufferedWriter(
                            new FileWriter(stringPath + "/" + fileName.getText().toString()));

                    sWriter.write(String.valueOf(InMem.getString("SaveText", "")));

                    sWriter.close();

                    ListFiles(stringPath);
                    fileName.setText("");
                    Toast.makeText(Manager.this, "Файл збережено", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } catch (IOException e) {
                    Toast.makeText(Manager.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayFile[position].isDirectory()) {
                    ListFiles(arrayFile[position].getAbsolutePath());
                } else if (arrayFile[position].isFile()) {

                    final int type_len = getResources().getStringArray(R.array.type).length;
                    fileName.setText(arrayFile[position].getName());


                    if ((getIntent().getIntExtra("Type", 0)) == 1) {


                        for (int i = 0; i < type_len; i++)
                            if (getResources().getStringArray(R.array.type)[i]
                                    .equals(mgrC.getFileType(arrayFile[position].getName()))) {
                                Intent intent = new Intent(This, Note.class);
                                intent.putExtra("PATH", arrayFile[position].getAbsolutePath());
                                startActivity(intent);
                                finish();
                                break;

                            }
                    }
                }

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder itemClick = new AlertDialog.Builder(This, R.style.Theme);
                final AlertDialog al = itemClick.create();

                renamePath = arrayFile[position];

                al.setTitle(arrayFile[position].getName());
                View v = getLayoutInflater().inflate(R.layout.item_click, null);

                Button delete = (Button) v.findViewById(R.id.btn_delete);
                final Button rename = (Button) v.findViewById(R.id.btn_rename);
                Button newFolder = (Button) v.findViewById(R.id.newFolder);

                delete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog(position, al);
                    }
                });

                rename.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rename(al);
                    }
                });

                newFolder.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeDir();
                        al.cancel();
                    }
                });

                al.setView(v);
                al.show();

                return true;
            }
        });
    }
}
