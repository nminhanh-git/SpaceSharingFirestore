package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.City;
import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Ward;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.StepContinueListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddAddressFragment extends Fragment implements StepContinueListener {

    private static final int IMAGE_PERMISSION_REQUEST_CODE = 0;
    private static final int GET_IMAGE_REQUEST_CODE = 1;
    View view;
    Button mImageBtn;
    EditText mEditTextTitle;
    EditText mEditTextAddress;
    Spinner mSpinnerCity;
    Spinner mSpinnerDistrict;
    Spinner mSpinnerWard;

    String currentTitle;
    String currentAddressNumber;
    City currentCity;
    District currentDist;
    Ward currentWard;


    String districtListJSON;
    ArrayList<District> districtList;
    ArrayList<String> districtNameList;
    ArrayAdapter<String> districtAdapter;

    String wardListJSON;
    ArrayList<Ward> wardList;
    ArrayList<String> wardNameList;
    ArrayAdapter<String> wardAdapter;

    ArrayList<String> mImagePathList;
    ArrayList<String> tempImagePathList;
    RecyclerView mImageRecycleView;
    ImageAdapter adapter;

    AddressUtils mAddressUtils;

    public AddAddressFragment() {
        // Required empty public constructor
    }

    public interface AddressReceiver {
        void onAddressReceived(String title, String addressNumber, String cityId, String districtId, String wardId, ArrayList<String> imagePath);
    }

    AddressReceiver receiver;

    @Override
    public void onContinue() {
        Bundle b = new Bundle();
        b.putString("title", currentTitle);
        b.putString("address", currentAddressNumber);
        b.putSerializable("city", currentCity);
        b.putSerializable("district", currentDist);
        b.putSerializable("ward", currentWard);
        b.putStringArrayList("image path", mImagePathList);
        setArguments(b);

        receiver.onAddressReceived(
                currentTitle,
                currentAddressNumber,
                currentCity.getId(),
                currentDist.getId(),
                currentWard.getId(),
                mImagePathList);
    }


    @Override
    public void onAttach(Context context) {
        receiver = (AddressReceiver) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_address, container, false);
        mAddressUtils = new AddressUtils(getActivity());
        initialize();
        Bundle b = getArguments();
        if (b != null) {
            tempImagePathList.clear();
            tempImagePathList.addAll(b.getStringArrayList("image path"));
            mImagePathList.clear();
            mImagePathList.addAll(tempImagePathList);
            adapter.notifyDataSetChanged();
            mImageRecycleView.scrollToPosition(mImagePathList.size() - 1);

            currentAddressNumber = b.getString("address");
            currentCity = (City) b.getSerializable("city");
            switch (currentCity.getId().trim()) {
                case "01":
                    mSpinnerCity.setSelection(0);
                    break;
                case "48":
                    mSpinnerCity.setSelection(1);
                    break;
                case "79":
                    mSpinnerCity.setSelection(2);
                    break;
            }

            currentDist = (District) b.getSerializable("district");
            int indexDist = 0;
            for (String s : districtNameList) {
                if (s.trim().equalsIgnoreCase(currentDist.getName().trim())) {
                    indexDist = districtNameList.indexOf(s);
                    break;
                }
            }
            final int finalIndexDist = indexDist;
            mSpinnerDistrict.post(new Runnable() {
                @Override
                public void run() {
                    mSpinnerDistrict.setAdapter(districtAdapter);
                    mSpinnerDistrict.setSelection(finalIndexDist, true);
                }
            });
            mSpinnerDistrict.setAdapter(districtAdapter);
            mSpinnerDistrict.setSelection(indexDist, true);

            Toast.makeText(getContext(), mSpinnerDistrict.getAdapter().getItem(indexDist) + "", Toast.LENGTH_SHORT).show();
            refreshWardList();
            currentWard = (Ward) b.getSerializable("ward");
            int indexWard = 0;
            for (String s : wardNameList) {
                if (s.trim().equalsIgnoreCase(currentWard.getName().trim())) {
                    indexWard = wardNameList.indexOf(s);
                    break;
                }
            }
            final int finalIndexWard = indexWard;

            mSpinnerWard.post(new Runnable() {
                @Override
                public void run() {
                    mSpinnerWard.setAdapter(wardAdapter);
                    mSpinnerWard.setSelection(finalIndexWard, true);
                }
            });


            Toast.makeText(getContext(), mSpinnerWard.getAdapter().getItem(indexWard) + "", Toast.LENGTH_SHORT).show();
            setArguments(null);
        }
        return view;
    }

    private void initialize() {
        mImageBtn = view.findViewById(R.id.add_address_btn_add_image);
        mImageRecycleView = view.findViewById(R.id.add_address_recycle_view_image);
        mEditTextAddress = view.findViewById(R.id.add_address_text_address_number);
        mEditTextTitle = view.findViewById(R.id.add_address_text_title);
        mSpinnerCity = view.findViewById(R.id.add_address_spinner_city);
        mSpinnerDistrict = view.findViewById(R.id.add_address_spinner_district);
        mSpinnerWard = view.findViewById(R.id.add_address_spinner_ward);

        mEditTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    currentAddressNumber = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    currentTitle = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        districtList = new ArrayList<>();
        districtNameList = new ArrayList<>();
        currentDist = new District();

        wardList = new ArrayList<>();
        wardNameList = new ArrayList<>();
        currentWard = new Ward();

        currentCity = new City("01", "Hà Nội");
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.city_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCity.setAdapter(cityAdapter);
        mSpinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentCity = new City("01", "Hà Nội");
                        break;
                    case 1:
                        currentCity = new City("48", "Đà Nẵng");
                        break;
                    case 2:
                        currentCity = new City("79", "TP.Hồ Chí Minh");
                        break;
                }
                refreshDistrictList();
                currentDist = districtList.get(0);
                refreshWardList();
                currentWard = wardList.get(0);
                mSpinnerDistrict.setSelection(0, true);
                mSpinnerWard.setSelection(0, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCity = new City("01", "Hà Nội");
                refreshDistrictList();
                currentDist = districtList.get(0);
                refreshWardList();
                currentWard = wardList.get(0);
                mSpinnerDistrict.setSelection(0, true);
                mSpinnerWard.setSelection(0, true);
            }
        });

        refreshDistrictList();
        currentDist = districtList.get(0);
        districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, districtNameList);
        mSpinnerDistrict.setAdapter(districtAdapter);
        mSpinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDist = districtList.get(position);
                refreshWardList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentDist = districtList.get(0);
                refreshWardList();
            }
        });

        refreshWardList();
        currentWard = wardList.get(0);
        wardAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, wardNameList);
        mSpinnerWard.setAdapter(wardAdapter);
        mSpinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentWard = wardList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentWard = wardList.get(0);
            }
        });

        mImagePathList = new ArrayList<>();
        tempImagePathList = new ArrayList<>();
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForPermissionAndGetImage();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mImageRecycleView.setLayoutManager(layoutManager);
        adapter = new ImageAdapter(getActivity(), mImagePathList);
        mImageRecycleView.setAdapter(adapter);
        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mImageRecycleView);
        //TODO: tìm hiểu về SnapHelper
        //TODO: viết comment vào trong code để dễ hiểu hơn

    }


    private void checkForPermissionAndGetImage() {
        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), "this app need permission to get the image you want for the item!", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GET_IMAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == IMAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GET_IMAGE_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_IMAGE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                String imagePath = data.getData().toString();
                tempImagePathList.add(imagePath);
                mImagePathList.clear();
                mImagePathList.addAll(tempImagePathList);
                adapter.notifyDataSetChanged();
                mImageRecycleView.scrollToPosition(mImagePathList.size() - 1);
            }
        }
    }

    private void refreshDistrictList() {
        districtListJSON = mAddressUtils.getJSONfromAssets("quan-huyen/" + currentCity.getId() + ".json");
        districtList.clear();
        districtList.addAll(mAddressUtils.getDistrictList(districtListJSON));

        districtNameList.clear();
        districtNameList.addAll(mAddressUtils.getDistrictNameList(districtList));

        if (districtAdapter != null) {
            districtAdapter.notifyDataSetChanged();
        }
    }

    private void refreshWardList() {
        wardListJSON = mAddressUtils.getJSONfromAssets("xa-phuong/" + currentDist.getId() + ".json");
        wardList.clear();
        wardList.addAll(mAddressUtils.getWardList(wardListJSON));
        wardNameList.clear();
        wardNameList.addAll(mAddressUtils.getWardNameList(wardList));

        if (wardAdapter != null) {
            wardAdapter.notifyDataSetChanged();
        }
    }

}
