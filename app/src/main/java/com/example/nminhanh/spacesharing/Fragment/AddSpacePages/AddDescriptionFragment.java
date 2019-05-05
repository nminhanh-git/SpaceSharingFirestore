package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.StepContinueListener;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddDescriptionFragment extends Fragment implements StepContinueListener {
    public AddDescriptionFragment() {
        // Required empty public constructor
    }

    public interface DescriptionReceiver {
        public void onDescriptionReceived(double size, double price, String des);
    }

    DescriptionReceiver receiver;
    View view;

    EditText mEditTextSize;
    EditText mEditTextPrice;
    EditText mEditTextDescription;
    String price = "0";
    String size = "0";
    String description = "";

    @Override
    public void onAttach(Context context) {
        receiver = (DescriptionReceiver) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_description, container, false);
        initialize();
        return view;
    }

    private void initialize() {
        mEditTextSize = view.findViewById(R.id.add_description_edit_text_size);
        mEditTextPrice = view.findViewById(R.id.add_description_price);
        mEditTextDescription = view.findViewById(R.id.add_description_des);

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
                if (!mEditTextSize.getText().toString().isEmpty()) {
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
    }


    @Override
    public void onContinue() {
        if (mEditTextSize.getText().toString().isEmpty()) {
            mEditTextSize.setError("Bạn chưa nhập diện tích");
        }
        if (mEditTextPrice.getText().toString().isEmpty()) {
            mEditTextPrice.setError("Bạn chưa nhập giá dự kiến");
        }
        if (mEditTextDescription.getText().toString().isEmpty()) {
            mEditTextDescription.setError("Bạn chưa nhập mô tả");
        }
        receiver.onDescriptionReceived(Double.valueOf(size), Double.valueOf(price), description);
    }
}
