package com.example.eyesyhopefyp.Contacts;


import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eyesyhopefyp.Common.model.Contact;
import com.example.eyesyhopefyp.R;

import java.util.List;
import java.util.Locale;

public class contactAdapter extends RecyclerView.Adapter<contactAdapter.ViewHolder> {
    //Initialize Variables
    public Activity activity;
    public List<Contact> arrayList;
    private TextToSpeech textToSpeech;
    SharedPreferences pref;

    public contactAdapter(Activity activity, List<Contact> arrayList) {
        this.activity = activity;
        this.arrayList = arrayList;

        notifyDataSetChanged();
        //Checking For saved language in shared preference
        pref = activity.getSharedPreferences("Settings", MODE_PRIVATE);
        //Initializing Text to Speech
        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result;
                    switch (pref.getString("lang", "en")) {
                        case "hi":
                            result = textToSpeech.setLanguage(new Locale("hi"));
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                                textToSpeech.setLanguage(new Locale("en"));
                            } else {
                                textToSpeech.setLanguage(new Locale("hi", "in"));
                            }
                            break;
                        case "ur":
                            result = textToSpeech.setLanguage(new Locale("ur"));
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                                textToSpeech.setLanguage(new Locale("en"));
                                // else you ask the system to install it
                            } else {
                                textToSpeech.setLanguage(new Locale("ur", "pk"));
                            }
                            break;
                        default:
                            result = textToSpeech.setLanguage(new Locale("en"));
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                            }
                            break;
                    }
                }

            }
        });
    }

    @NonNull
    @Override
    public contactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull contactAdapter.ViewHolder holder, int position) {
        holder.itemView.setLongClickable(true);//Required to enable long clicks
        Contact model = arrayList.get(position);
        //Set name
        holder.tvName.setText(model.getName());
        //Set Number
        holder.tvNumber.setText(model.getPhoneNumber());
        //Save the data in the Id of itemView so that it can be acquired when clicking
        holder.itemView.setId(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak("Name: "+model.getName()+"and Number is: "+model.getPhoneNumber(), TextToSpeech.QUEUE_ADD, null, null);
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+model.getPhoneNumber().trim()));
                activity.startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void filterList(List<Contact> filteredList) {
        this.arrayList = filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Initialize variables
        TextView tvName, tvNumber;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvNumber = itemView.findViewById(R.id.tv_contact_number);

            mView = itemView;
        }
    }


}
