package com.best.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.best.R;
import com.best.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtStudentId;
    private TextView txtContact;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        txtName         = view.findViewById(R.id.txt_name);
        txtEmail        = view.findViewById(R.id.txt_email);
        txtStudentId    = view.findViewById(R.id.txt_student_id);
        txtContact      = view.findViewById(R.id.txt_contact);

        txtName.setText("Name: " + " Ansasry");
        txtEmail.setText("Email: " + "Ansary@gmail.com" );
        txtStudentId.setText("Student ID: " + "22234103132");
        txtContact.setText("Contact: " + "01611929833");

        return view;
    }
}
