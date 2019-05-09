package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.nminhanh.spacesharing.DescriptionOldDataReceiver;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.StepContinueListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddDescriptionFragment extends Fragment implements StepContinueListener, DescriptionOldDataReceiver {


    private static final String TAG = "MA:DescriptionFragment";
    private static final String ACTIVITY_TAG = "MA:AddSpaceFragments";
    DescriptionReceiver receiver;
    View view;

    EditText mEditTextSize;
    EditText mEditTextPrice;
    EditText mEditTextDescription;
    CheckBox mCheckBoxPrePaid;
    EditText mEditTextPrepaid;
    RelativeLayout mLayoutPrepaid;
    ImageButton mBtnPrepaidIncrease;
    ImageButton mBtnPrepaidDecrease;


    String price = "0";
    String size = "0";
    String description = "";
    int prepaidMonth = 0;

    Bundle oldData;

    public AddDescriptionFragment() {
        // Required empty public constructor
    }


    public interface DescriptionReceiver {
        void onDescriptionReceived(double size, double price, int prepaidMonth, String description);
    }

    public interface DescriptionInflatedListener {
        void onDescriptionInflated();
    }

    DescriptionInflatedListener inflatedListener;


    @Override
    public void onReceive(Bundle b) {
        oldData = b;
    }

    @Override
    public void onAttach(Context context) {
        receiver = (DescriptionReceiver) context;
        inflatedListener = (DescriptionInflatedListener) context;

        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_description, container, false);
        initialize();
        inflatedListener.onDescriptionInflated();
        return view;
    }

    private void initialize() {
        mEditTextSize = view.findViewById(R.id.add_description_edit_text_size);
        mEditTextPrice = view.findViewById(R.id.add_description_price);
        mEditTextDescription = view.findViewById(R.id.add_description_des);
        mCheckBoxPrePaid = view.findViewById(R.id.checkbox_prepaid);
        mEditTextPrepaid = view.findViewById(R.id.add_description_prepaid);
        mLayoutPrepaid = view.findViewById(R.id.layout_prepaid);
        mBtnPrepaidIncrease = view.findViewById(R.id.description_prepaid_button_increase);
        mBtnPrepaidDecrease = view.findViewById(R.id.description_prepaid_button_decrease);

        mEditTextSize.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                size = s.toString();
                if (s.length() != 0) {
                    mEditTextSize.setError(null);
                } else {
                    size = "0";
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEditTextPrice.addTextChangedListener(new TextWatcher() {
            boolean isManualChange;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    mEditTextPrice.setError(null);
                }
                if (isManualChange) {
                    isManualChange = false;
                    return;
                }

                try {
                    String value = s.toString().replace(",", "");
                    String reverseValue = new StringBuilder(value).reverse()
                            .toString();
                    StringBuilder finalValue = new StringBuilder();
                    for (int i = 1; i <= reverseValue.length(); i++) {
                        char val = reverseValue.charAt(i - 1);
                        finalValue.append(val);
                        if (i % 3 == 0 && i != reverseValue.length() && i > 0) {
                            finalValue.append(",");
                        }
                    }
                    isManualChange = true;
                    mEditTextPrice.setText(finalValue.reverse());
                    mEditTextPrice.setSelection(finalValue.length());
                } catch (Exception e) {
                    // Do nothing since not a number
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEditTextPrice.getText().toString().isEmpty()) {
                    price = mEditTextPrice.getText().toString();
                    price = price.replaceAll(",", "");
                } else {
                    price = "0";
                }
            }
        });
        mEditTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                description = s.toString();
                if (s.length() != 0) {
                    mEditTextDescription.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCheckBoxPrePaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mLayoutPrepaid.setVisibility(View.VISIBLE);
                } else {
                    mLayoutPrepaid.setVisibility(View.GONE);
                    mEditTextPrepaid.setText("0");
                    prepaidMonth = 0;
                }
            }
        });
        mBtnPrepaidIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepaidMonth++;
                mEditTextPrepaid.setText(prepaidMonth + "");

            }
        });
        mBtnPrepaidDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prepaidMonth > 0) {
                    prepaidMonth--;
                    mEditTextPrepaid.setText(prepaidMonth + "");
                }
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(ACTIVITY_TAG, "onDescriptionResume");
        super.onResume();
        if (oldData != null) {
            int size = (int) oldData.getDouble("size");
            int price = (int) oldData.getDouble("price");
            int prepaid = oldData.getInt("prepaid");
            String description = oldData.getString("description");

            mEditTextPrice.setText(String.valueOf(price));
            mEditTextSize.setText(String.valueOf(size));

            if (prepaid > 0) {
                mCheckBoxPrePaid.setChecked(true);
                mEditTextPrepaid.setText(String.valueOf(prepaid));
                prepaidMonth = prepaid;
            }
            mEditTextDescription.setText(description);
            oldData = null;
        }
    }

    @Override
    public void onContinue() {
        if (mEditTextSize.getText().toString().isEmpty()) {
            mEditTextSize.setError("Bạn chưa nhập diện tích");
        }
        if (mEditTextPrice.getText().toString().isEmpty()) {
            mEditTextPrice.setError("Bạn chưa nhập giá dự kiến");
        }
        if (mCheckBoxPrePaid.isChecked() && mEditTextPrepaid.getText().toString().isEmpty()) {
            mEditTextPrepaid.setError("Bạn chưa nhập số tháng đặt cọc dự kiến");
        }
        if (mEditTextDescription.getText().toString().isEmpty()) {
            mEditTextDescription.setError("Bạn chưa nhập mô tả");
        }
        receiver.onDescriptionReceived(Double.valueOf(size), Double.valueOf(price), prepaidMonth, description);
    }

}
