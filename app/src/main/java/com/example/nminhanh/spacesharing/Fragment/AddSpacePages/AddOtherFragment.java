package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.StepContinueListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddOtherFragment extends Fragment implements StepContinueListener {

    View view;
    Spinner mSpinnerType;
    Spinner mSpinnerDoor;
    EditText mEditTextBedroom;
    ImageButton mImgBtnBedroomIncrease;
    ImageButton mImgBtnBedroomDecrease;
    EditText mEditTextBathroom;
    ImageButton mImgBtnBathroomIncrease;
    ImageButton mImgBtnBathroomDecrease;
    EditText mEditTextDetail;

    ImageView mImageSpinnerTypeError;
    ImageView mImageSpinnerDoorError;

    String currentType;
    String currentDoor;
    int bedRoom = 0;
    int bathRoom = 0;
    String detail;

    RelativeLayout customOptionLayout;

    public interface OtherReceiver {
        public void onOtherReceived(String type, String door, int bedRoom, int bathRoom, String detail);
    }

    OtherReceiver receiver;


    public AddOtherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        receiver = (OtherReceiver) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_other, container, false);
        initialize();
        return view;
    }

    @Override
    public void onContinue() {
        if (currentType.equals(getResources().getStringArray(R.array.type_array)[0])) {
            Toast.makeText(getContext(), "Bạn chưa chọn loại không gian", Toast.LENGTH_SHORT).show();
            mImageSpinnerTypeError.setVisibility(View.VISIBLE);
        }
        if (currentType.equalsIgnoreCase("Nhà ở") || currentType.equalsIgnoreCase("Cửa hàng kinh doanh")) {
            if(currentDoor.equalsIgnoreCase(getResources().getStringArray(R.array.door_direction_array)[0])){
                Toast.makeText(getContext(), "Bạn chưa chọn hướng cửa", Toast.LENGTH_SHORT).show();
                mImageSpinnerDoorError.setVisibility(View.VISIBLE);
            }
        }
        receiver.onOtherReceived(currentType, currentDoor, bedRoom, bathRoom, detail);
    }

    private void initialize() {
        mSpinnerType = view.findViewById(R.id.add_other_spinner_type);
        mImageSpinnerTypeError = view.findViewById(R.id.add_other_spinner_type_image_error);

        customOptionLayout = view.findViewById(R.id.add_other_layout_custom_option);
        mSpinnerDoor = view.findViewById(R.id.add_other_spinner_door);
        mImageSpinnerDoorError = view.findViewById(R.id.add_other_spinner_door_image_error);

        mEditTextBedroom = view.findViewById(R.id.add_other_edit_text_bedroom);
        mEditTextBedroom.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditTextBedroom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    bedRoom = Integer.valueOf(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mImgBtnBedroomIncrease = view.findViewById(R.id.other_bedroom_button_increase);
        mImgBtnBedroomDecrease = view.findViewById(R.id.other_bedroom_button_decrease);

        mEditTextBathroom = view.findViewById(R.id.add_other_edit_text_bathroom);
        mEditTextBathroom.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditTextBathroom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bathRoom = Integer.valueOf(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mImgBtnBathroomIncrease = view.findViewById(R.id.other_bathroom_button_increase);
        mImgBtnBathroomDecrease = view.findViewById(R.id.other_bathroom_button_decrease);

        mEditTextDetail = view.findViewById(R.id.add_other_edit_text_detail);
        mEditTextDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    detail = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.type_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinnerType.setAdapter(typeAdapter);
        mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentType = getActivity().getResources().getStringArray(R.array.type_array)[position];
                if (currentType.equalsIgnoreCase("Nhà ở") || currentType.equalsIgnoreCase("Cửa hàng kinh doanh")) {
                    customOptionLayout.setVisibility(View.VISIBLE);
                } else {
                    customOptionLayout.setVisibility(View.GONE);
                }
                mImageSpinnerTypeError.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                currentType = getActivity().getResources().getStringArray(R.array.type_array)[0];
//                if (currentType.equalsIgnoreCase("Nhà ở") || currentType.equalsIgnoreCase("Cửa hàng kinh doanh")) {
//                    customOptionLayout.setVisibility(View.VISIBLE);
//                } else {
//                    customOptionLayout.setVisibility(View.GONE);
//                }
            }
        });

        ArrayAdapter<CharSequence> doorAdapter = ArrayAdapter.createFromResource(getContext(), R.array.door_direction_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDoor.setAdapter(doorAdapter);
        mSpinnerDoor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDoor = getActivity().getResources().getStringArray(R.array.door_direction_array)[position];
                mImageSpinnerDoorError.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentDoor = getActivity().getResources().getStringArray(R.array.door_direction_array)[0];
            }
        });

        mImgBtnBedroomIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextBedroom.setText(String.valueOf(bedRoom + 1));
            }
        });
        mImgBtnBedroomDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bedRoom > 0) {
                    mEditTextBedroom.setText(String.valueOf(bedRoom - 1));
                }
            }
        });
        mImgBtnBathroomIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextBathroom.setText(String.valueOf(bathRoom + 1));
            }
        });
        mImgBtnBathroomDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bathRoom > 0) {
                    mEditTextBathroom.setText(String.valueOf(bathRoom - 1));
                }
            }
        });
    }
}
