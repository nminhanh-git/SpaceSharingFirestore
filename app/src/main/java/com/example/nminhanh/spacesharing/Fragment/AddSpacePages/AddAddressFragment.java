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
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
    private static final String TAG = "MinhAnh:AddressFragment";
    View view;
    ImageButton mImageBtn;
    ImageButton mDeleteImageBtn;
    EditText mEditTextTitle;
    EditText mEditTextAddress;
    Spinner mSpinnerCity;
    Spinner mSpinnerDistrict;
    Spinner mSpinnerWard;
    ImageView mImageCityError;
    ImageView mImageDistrictError;
    ImageView mImageWardError;
    TextView mTextViewImageNote;

    String currentTitle = "";
    String currentAddressNumber = "";
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

    RecyclerView mImageRecycleView;
    ImageAdapter adapter;

    AddressUtils mAddressUtils;


    public AddAddressFragment() {
        // Required empty public constructor
    }

    public interface AddressReceiver {
        void onAddressReceived(String title, String addressNumber, String cityId, String districtId, String wardId, String fullAddress, ArrayList<String> imagePath);
    }

    AddressReceiver receiver;

    @Override
    public void onContinue() {
        if (!currentTitle.isEmpty() && !currentAddressNumber.isEmpty()
                && mEditTextTitle.getError() == null && mEditTextAddress.getError() == null
                && adapter.getImagePathList().size() >= 5
                && !currentCity.getId().equals("-1") && !currentDist.getId().equals("-1") && !currentWard.getId().equals("-1")
                ) {

        } else {
            if (currentTitle.isEmpty()) {
                mEditTextTitle.setError("Bạn chưa nhập tiêu đề");
            }
            if (currentAddressNumber.isEmpty()) {
                mEditTextAddress.setError("Bạn chưa nhập địa chỉ nhà");
            }
            if (adapter.getItemCount() < 5) {
                Toast.makeText(getContext(), "Bạn chưa thêm đủ số lượng ảnh yêu cầu", Toast.LENGTH_SHORT).show();
            }
            if (currentCity.getId().equals("-1")) {
                Toast.makeText(getContext(), "Bạn chưa thêm thông tin về thành phố", Toast.LENGTH_SHORT).show();
                mImageCityError.setVisibility(View.VISIBLE);
            }
            if (currentDist.getId().equals("-1")) {
                Toast.makeText(getContext(), "Bạn chưa thêm thông tin về quận/huyện", Toast.LENGTH_SHORT).show();
                mImageDistrictError.setVisibility(View.VISIBLE);
            }
            if (currentWard.getId().equals("-1")) {
                Toast.makeText(getContext(), "Bạn chưa thêm thông tin về phường/xã", Toast.LENGTH_SHORT).show();
                mImageWardError.setVisibility(View.VISIBLE);
            }
        }
        receiver.onAddressReceived(
                currentTitle,
                currentAddressNumber,
                currentCity.getId(),
                currentDist.getId(),
                currentWard.getId(),
                currentAddressNumber + ", " + currentWard.getName() + ", " + currentDist.getName() + ", " + currentCity.getName(),
                adapter.getImagePathList());
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
        Log.d(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_add_address, container, false);
        mAddressUtils = new AddressUtils(getActivity());
        initialize();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Bundle b = getArguments();
        if (b != null) {
            adapter.setImagePathList(b.getStringArrayList("image path"));
            adapter.notifyDataSetChanged();
            mImageRecycleView.scrollToPosition(adapter.getItemCount() - 1);
            String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
            mTextViewImageNote.setText(Html.fromHtml(textNote));


            currentAddressNumber = b.getString("address");
            currentCity = (City) b.getSerializable("city");
            switch (currentCity.getId().trim()) {
                case "01":
                    mSpinnerCity.setSelection(1);
                    break;
                case "48":
                    mSpinnerCity.setSelection(2);
                    break;
                case "79":
                    mSpinnerCity.setSelection(3);
                    break;
            }
        }
    }

    private void initialize() {
        mImageBtn = view.findViewById(R.id.add_address_btn_add_image);
        mDeleteImageBtn = view.findViewById(R.id.add_address_btn_delete_image);
        mDeleteImageBtn.setVisibility(View.GONE);
        mImageRecycleView = view.findViewById(R.id.add_address_recycle_view_image);
        mEditTextAddress = view.findViewById(R.id.add_address_text_address_number);
        mEditTextTitle = view.findViewById(R.id.add_address_text_title);
        mSpinnerCity = view.findViewById(R.id.add_address_spinner_city);
        mSpinnerDistrict = view.findViewById(R.id.add_address_spinner_district);
        mSpinnerWard = view.findViewById(R.id.add_address_spinner_ward);
        mImageCityError = view.findViewById(R.id.add_address_spinner_city_image_error);
        mImageDistrictError = view.findViewById(R.id.add_address_spinner_district_image_error);
        mImageWardError = view.findViewById(R.id.add_address_spinner_ward_image_error);
        mTextViewImageNote = view.findViewById(R.id.add_address_image_note);

        mEditTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentAddressNumber = s.toString();
                if (s.length() != 0) {
                    mEditTextAddress.setError(null);
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
                currentTitle = s.toString();
                if (s.length() != 0) {
                    mEditTextTitle.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        initializeSpinnerData();
        initializeImageRecyclerViewData();
    }

    private void initializeImageRecyclerViewData() {
        String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + 0 + "/5</font> ảnh. Ảnh đầu tiên sẽ được sử dụng làm ảnh bì cho tin đăng";
        mTextViewImageNote.setText(Html.fromHtml(textNote));

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mImageRecycleView.setLayoutManager(layoutManager);
        adapter = new ImageAdapter(getActivity(), new ArrayList<String>());
        mImageRecycleView.setAdapter(adapter);
        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mImageRecycleView);

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForPermissionAndGetImage();
            }
        });
        mDeleteImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeImage(((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition());
                String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
                mTextViewImageNote.setText(Html.fromHtml(textNote));
                if (adapter.getItemCount() == 0) {
                    mDeleteImageBtn.setVisibility(View.GONE);
                }
            }
        });
        //TODO: tìm hiểu về SnapHelper
        //TODO: viết comment vào trong code để dễ hiểu hơn
    }

    private void initializeSpinnerData() {
        districtList = new ArrayList<>();
        districtNameList = new ArrayList<>();
        currentDist = new District();

        wardList = new ArrayList<>();
        wardNameList = new ArrayList<>();
        currentWard = new Ward();

        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(getContext(), R.array.city_array, android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCity.setAdapter(cityAdapter);
        mSpinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mImageCityError.setVisibility(View.GONE);
                switch (position) {
                    case 1:
                        currentCity = new City("01", "Hà Nội");
                        break;
                    case 2:
                        currentCity = new City("48", "Đà Nẵng");
                        break;
                    case 3:
                        currentCity = new City("79", "TP.Hồ Chí Minh");
                        break;
                    default:
                        currentCity = new City("-1", "Hãy chọn thành phố...");
                        break;
                }
                refreshDistrictList();

                if (getArguments() == null) {
                    currentDist = districtList.get(0);
                    mSpinnerDistrict.setSelection(0, true);

                    refreshWardList();
                    currentWard = wardList.get(0);
                    mSpinnerWard.setSelection(0, true);
                } else {
                    currentDist = (District) getArguments().getSerializable("district");
                    int districtPosition = districtAdapter.getPosition(currentDist.getName());
                    mSpinnerDistrict.setSelection(districtPosition);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, districtNameList);
        mSpinnerDistrict.setAdapter(districtAdapter);
        mSpinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mImageDistrictError.setVisibility(View.GONE);
                if (getArguments() == null) {
                    currentDist = districtList.get(position);
                    refreshWardList();
                    mSpinnerWard.setSelection(0);
                } else {
                    refreshWardList();
                    currentWard = (Ward) getArguments().getSerializable("ward");
                    int wardPosition = wardAdapter.getPosition(currentWard.getName());
                    mSpinnerWard.setSelection(wardPosition);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        wardAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, wardNameList);
        mSpinnerWard.setAdapter(wardAdapter);
        mSpinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mImageWardError.setVisibility(View.GONE);
                currentWard = wardList.get(position);
                if (getArguments() != null) {
                    setArguments(null);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
                mDeleteImageBtn.setVisibility(View.VISIBLE);
                String imagePath = data.getData().toString();
                adapter.addImage(imagePath);
                String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
                mTextViewImageNote.setText(Html.fromHtml(textNote));
                mImageRecycleView.scrollToPosition(adapter.getItemCount() - 1);
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

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        Bundle b = new Bundle();
        b.putString("title", currentTitle);
        b.putString("address", currentAddressNumber);
        b.putSerializable("city", currentCity);
        b.putSerializable("district", currentDist);
        b.putSerializable("ward", currentWard);
        b.putStringArrayList("image path", adapter.getImagePathList());
        setArguments(b);
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
