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

import com.example.nminhanh.spacesharing.OtherOldDataReceiver;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.StepContinueListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddOtherFragment extends Fragment implements StepContinueListener, OtherOldDataReceiver {

    View view;
    Spinner mSpinnerType;
    Spinner mSpinnerDoor;
    EditText mEditTextBedroom;
    ImageButton mImgBtnBedroomIncrease;
    ImageButton mImgBtnBedroomDecrease;
    EditText mEditTextBathroom;
    ImageButton mImgBtnBathroomIncrease;
    ImageButton mImgBtnBathroomDecrease;

    ImageView mImageSpinnerTypeError;
    ImageView mImageSpinnerDoorError;

    EditText mEditTextElectric;
    EditText mEditTextWater;
    double electricPrice = 0;
    double waterPrice = 0;

    String currentType;
    String currentDoor;
    int bedRoom = 0;
    int bathRoom = 0;

    Bundle oldData;

    RelativeLayout customOptionLayout;

    @Override
    public void onReceive(Bundle b) {
        oldData = b;
    }

    public interface OtherReceiver {
        void onOtherReceived(String type, String door, int bedRoom, int bathRoom, double electricPrice, double waterPrice);
    }

    public interface OtherInflatedListener {
        void onOtherInflated();
    }

    OtherReceiver receiver;
    OtherInflatedListener inflatedListener;


    public AddOtherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        receiver = (OtherReceiver) context;
        inflatedListener = (OtherInflatedListener) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_other, container, false);
        initialize();
        inflatedListener.onOtherInflated();
        return view;
    }

    private void initialize() {
        mSpinnerType = view.findViewById(R.id.add_other_spinner_type);
        mImageSpinnerTypeError = view.findViewById(R.id.add_other_spinner_type_image_error);

        customOptionLayout = view.findViewById(R.id.add_other_layout_custom_option);
        mSpinnerDoor = view.findViewById(R.id.add_other_spinner_door);
        mImageSpinnerDoorError = view.findViewById(R.id.add_other_spinner_door_image_error);

        mEditTextElectric = view.findViewById(R.id.add_other_edit_text_electric_price);
        mEditTextWater = view.findViewById(R.id.add_other_edit_text_water_price);

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

        mEditTextElectric.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    electricPrice = Double.valueOf(s.toString());
                    mEditTextElectric.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    electricPrice = 0;
                }
            }
        });
        mEditTextWater.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    waterPrice = Double.valueOf(s.toString());
                    mEditTextWater.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    waterPrice = 0;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (oldData != null) {
            for (int i = 1; i < 4; i++) {
                String type = getResources().getStringArray(R.array.type_array)[i];
                if (oldData.getString("type").equalsIgnoreCase(type)) {
                    mSpinnerType.setSelection(i, true);
                    currentType = type;
                    break;
                }
            }
            if (currentType.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[1]) ||
                    currentType.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[2])) {
                for (int i = 1; i < 8; i++) {
                    String door = getResources().getStringArray(R.array.door_direction_array)[i];
                    if (oldData.getString("door").equalsIgnoreCase(door)) {
                        mSpinnerDoor.setSelection(i, true);
                        currentDoor = door;
                        break;
                    }
                }
                customOptionLayout.setVisibility(View.VISIBLE);
                electricPrice = oldData.getDouble("electric");
                mEditTextElectric.setText(electricPrice + "");
                waterPrice = oldData.getDouble("water");
                mEditTextWater.setText(waterPrice + "");
                bedRoom = oldData.getInt("bed");
                mEditTextBedroom.setText(bedRoom + "");
                bathRoom = oldData.getInt("bath");
                mEditTextBathroom.setText(bathRoom + "");
            }
        }
    }

    @Override
    public void onContinue() {
        if (currentType.equals(getResources().getStringArray(R.array.type_array)[0])) {
            Toast.makeText(getContext(), "Bạn chưa chọn loại không gian", Toast.LENGTH_SHORT).show();
            mImageSpinnerTypeError.setVisibility(View.VISIBLE);
        }
        if (currentType.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[1]) ||
                currentType.equalsIgnoreCase(getResources().getStringArray(R.array.type_array)[2])) {
            if (currentDoor.equalsIgnoreCase(getResources().getStringArray(R.array.door_direction_array)[0])) {
                Toast.makeText(getContext(), "Bạn chưa chọn hướng cửa", Toast.LENGTH_SHORT).show();
                mImageSpinnerDoorError.setVisibility(View.VISIBLE);
            }
            if (mEditTextWater.getText().toString().isEmpty() || mEditTextWater.getError() != null) {
                mEditTextWater.setError("Bạn chưa nhập giá tiền nước");
                mEditTextElectric.setError("Bạn chưa nhập giá tiền điện");
            }
        }
        receiver.onOtherReceived(currentType, currentDoor, bedRoom, bathRoom, electricPrice, waterPrice);
    }

}
