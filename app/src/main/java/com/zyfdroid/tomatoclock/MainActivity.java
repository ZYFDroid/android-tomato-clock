package com.zyfdroid.tomatoclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zyfdroid.tomatoclock.model.TimeEntry;
import com.zyfdroid.tomatoclock.util.AndroidUtils;
import com.zyfdroid.tomatoclock.util.ICallback;
import com.zyfdroid.tomatoclock.util.SpUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    TimeEntryAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(SpUtils.getStatus()){
            startActivity(new Intent(this,ClockActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);
//        List<TimeEntry> testdata = new ArrayList<TimeEntry>();
//        testdata.add(new TimeEntry("学习",30 * 60, Color.BLUE));
//        testdata.add(new TimeEntry("放松",5 * 60, Color.YELLOW));
//        testdata.add(new TimeEntry("活动",6 * 60, Color.GREEN));
        ((ListView)findViewById(R.id.listMain)).setAdapter(mAdapter = new TimeEntryAdapter(new ArrayList<TimeEntry>()));
        mAdapter.addAll(SpUtils.getCurrent());


    }

    @Override
    protected void onPause() {
        super.onPause();
        SpUtils.saveCurrent(mAdapter.toList());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("预设").setIcon(R.drawable.ic_menu_settings).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                new AlertDialog.Builder(MainActivity.this).setItems(new String[]{"默认模式","调试用"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0) {
                            mAdapter.clear();
                            mAdapter.add(new TimeEntry("学习", 30 * 60, 0xFF43341B));
                            mAdapter.add(new TimeEntry("休息", 5 * 60, 0xFFF05E1C));
                            mAdapter.add(new TimeEntry("活动", 6 * 60, 0xFF90B44B));
                        }
                        if(which==1) {
                            mAdapter.clear();
                            mAdapter.add(new TimeEntry("测试事件1", 1 * 60, 0xFF43341B));
                            mAdapter.add(new TimeEntry("测试事件2", 1 * 60, 0xFFF05E1C));
                            mAdapter.add(new TimeEntry("测试事件3", 1 * 60, 0xFF90B44B));
                        }
                    }
                }).setTitle("选择预设时间表").create().show();

                return true;
            }
        });

        menu.add("添加").setIcon(R.drawable.ic_menu_add).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onAddClick(null);
                return true;
            }
        });

        menu.add("开始").setIcon(R.drawable.ic_menu_start).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(MainActivity.this).setTitle("启动番茄钟").setMessage("是否启动番茄钟?")
                        .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SpUtils.setStatus(true);
                                SpUtils.setStartTime(System.currentTimeMillis());
                                startActivity(new Intent(MainActivity.this,ClockActivity.class));
                                finish();
                            }
                        }).setNegativeButton("不是",null).create().show();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }



    public void onAddClick(View view) {
        new AddItemDialog(this, new ICallback<TimeEntry>() {
            @Override
            public void onCallback(TimeEntry value) {
                mAdapter.add(value);
            }
        }).show();
    }

    class TimeEntryAdapter extends ArrayAdapter<TimeEntry>{

        class TimeEntryViewHolder{
            TextView txtItemText;
            ImageButton btnDelete;
            LinearLayout itemBackground;
        }

        public TimeEntryAdapter(List<TimeEntry> objects) {
            super(MainActivity.this, R.layout.adapter_time_entry,R.id.txtItemText, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TimeEntryViewHolder holder = new TimeEntryViewHolder();
            View v = null;
            if(convertView == null)
            {
                LayoutInflater inflater = getLayoutInflater();
                v = inflater.inflate(R.layout.adapter_time_entry,null);
                holder.itemBackground = v.findViewById(R.id.itemBackground);
                holder.txtItemText = v.findViewById(R.id.txtItemText);
                holder.btnDelete = v.findViewById(R.id.btnDeleteItem);
                v.setTag(holder);
            }
            else
            {
                v = (LinearLayout) convertView;
                holder = (TimeEntryViewHolder) v.getTag();
            }

            TimeEntry item = getItem(position);
            int minute = item.getDuration() / 60;
            holder.itemBackground.setBackgroundColor(item.getBackgroundColor());
            holder.txtItemText.setText(item.getName()+" "+minute+"分钟");
            holder.btnDelete.setOnClickListener(new ItemDeleter(item));

            return v;
        }

        class ItemDeleter implements View.OnClickListener{
            TimeEntry target;

            public ItemDeleter(TimeEntry target) {
                this.target = target;
            }

            @Override
            public void onClick(View v) {
                TimeEntryAdapter.this.remove(target);
                TimeEntryAdapter.this.notifyDataSetChanged();
            }
        }

        public List<TimeEntry> toList(){
            ArrayList<TimeEntry> result = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                result.add(getItem(i));
            }
            return result;
        }
    }

    class AddItemDialog extends Dialog{

        public AddItemDialog(Context context,ICallback<TimeEntry> callback) {
            super(context);
            this.callback = callback;
        }

        ICallback<TimeEntry> callback;

        final int[] availableColors = {
                0xFFE87A90,
                0xFFF05E1C,
                0xFF43341B,
                0xFF90B44B,
                0xFF1B813E,
                0xFF33A6B8,
                0xFF005CAF,
                0xFF6A4C9C,
                0xFFC1328E,
                0xFF91989F,
                0xFF3A3226,
                0xFF434343,
                0xFF080808
        };

        final String[] predefines = {
                "学习","休息","活动","工作"
                //先弄这么多
        };

        private int _selectedColor = 0xFFE87A90;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_add);
            setTitle("添加时间段");
            initUi();

            btnYushe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext()).setItems(predefines, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            txtDescription.setText(predefines[which]);
                        }
                    }).create().show();
                }
            });
            numTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    valueChanged(seekBar.getProgress()+5);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    valueChanged(seekBar.getProgress()+5);

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    valueChanged(seekBar.getProgress()+5);

                }

                void valueChanged(int value){
                    txtDurationText.setText("时长："+(value)+"分钟");
                }
            });
            btnTimeMinus.setTag("-1");
            btnTimePlus.setTag("1");
            View.OnClickListener modifySeekbar = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int delta = Integer.parseInt(v.getTag().toString());
                    numTime.setProgress(numTime.getProgress()+delta);
                }
            };
            btnTimePlus.setOnClickListener(modifySeekbar);
            btnTimeMinus.setOnClickListener(modifySeekbar);

            View.OnClickListener colorPicker = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _selectedColor = Integer.parseInt(v.getTag().toString());
                    for (TextView vi :
                            colorPickerViews) {
                        vi.setText(" ");
                    }
                    ((TextView)v).setText("√");
                }
            };

            int _4dp = AndroidUtils.dip2px(getContext(),4);

            for(int i=0;i<availableColors.length;i++){
                TextView pickerView = new TextView(getContext());
                pickerView.setBackgroundColor(availableColors[i]);
                colorPickerPanel.addView(pickerView);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pickerView.getLayoutParams();
                lp.width = AndroidUtils.dip2px(getContext(),28);
                lp.height = AndroidUtils.dip2px(getContext(),28);
                pickerView.setLayoutParams(lp);
                pickerView.setOnClickListener(colorPicker);
                pickerView.setTag(availableColors[i]);
                lp.setMargins(_4dp,_4dp,_4dp,_4dp);
                pickerView.setTextColor(Color.WHITE);
                pickerView.setGravity(Gravity.CENTER);
                pickerView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                colorPickerViews.add(pickerView);
            }

            colorPickerViews.get(0).performClick();

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddItemDialog.this.dismiss();
                }
            });
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(txtDescription.getText())){
                        Toast.makeText(MainActivity.this, "请输入时间段说明", Toast.LENGTH_SHORT).show();
                        txtDescription.requestFocus();
                        return;
                    }
                    callback.onCallback(new TimeEntry(txtDescription.getText().toString(),(numTime.getProgress()+5) * 60, _selectedColor));
                    AddItemDialog.this.dismiss();
                }
            });
        }

        ArrayList<TextView> colorPickerViews = new ArrayList<>();

        Button btnConfirm = null;
        Button btnCancel = null;
        LinearLayout colorPickerPanel = null;
        Button btnTimePlus = null;
        SeekBar numTime = null;
        Button btnTimeMinus = null;
        TextView txtDurationText = null;
        Button btnYushe = null;
        EditText txtDescription = null;

        //ohhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

        void initUi(){
            btnConfirm = (Button)findViewById(R.id.btnConfirm);
            btnCancel = (Button)findViewById(R.id.btnCancel);
            colorPickerPanel = (LinearLayout)findViewById(R.id.colorPickerPanel);
            btnTimePlus = (Button)findViewById(R.id.btnTimePlus);
            numTime = (SeekBar)findViewById(R.id.numTime);
            btnTimeMinus = (Button)findViewById(R.id.btnTimeMinus);
            txtDurationText = (TextView)findViewById(R.id.txtDurationText);
            btnYushe = (Button)findViewById(R.id.btnYushe);
            txtDescription = (EditText)findViewById(R.id.txtDescription);
        }




    }

}
