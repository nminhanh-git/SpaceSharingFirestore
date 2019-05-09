package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.nminhanh.spacesharing.AddCanceledListener;
import com.example.nminhanh.spacesharing.AddressOldDataReceiver;
import com.example.nminhanh.spacesharing.GlideApp;
import com.example.nminhanh.spacesharing.Utils.AddressUtils;
import com.example.nminhanh.spacesharing.Model.City;
import com.example.nminhanh.spacesharing.Model.District;
import com.example.nminhanh.spacesharing.Model.Ward;
import com.example.nminhanh.spacesharing.R;
import com.example.nminhanh.spacesharing.StepContinueListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddAddressFragment extends Fragment implements StepContinueListener, AddCanceledListener, AddressOldDataReceiver {

    private static final int IMAGE_PERMISSION_REQUEST_CODE = 0;
    private static final int GET_IMAGE_REQUEST_CODE = 1;
    private static final String TAG = "MinhAnh:AddressFragment";
    private static final String ACTIVITY_TAG = "MA:AddSpaceFragments";
    View view;
    ImageButton mAddImageBtn;
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
    LinearLayout mLayoutIndicator;
    ImageView mImageIndicator1;
    ImageView mImageIndicator2;
    ImageView mImageIndicator3;
    ImageView mImageIndicator4;
    ImageView mImageIndicator5;

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
    AddImageAdapter adapter;

    AddressUtils mAddressUtils;
    boolean isDestroyImmediately = true;
    Bundle oldData;

    public AddAddressFragment() {
        // Required empty public constructor
    }


    public interface AddressReceiver {
        void onAddressReceived(String title, String addressNumber, String cityId, String districtId, String wardId, String fullAddress, ArrayList<Bitmap> imagePath);
    }

    public interface AddressInflatedListener {
        void onAddressInflated();
    }

    @Override
    public void onCanceled() {
        isDestroyImmediately = true;
    }

    @Override
    public void onContinue() {
        if (!currentTitle.isEmpty() && !currentAddressNumber.isEmpty()
                && mEditTextTitle.getError() == null && mEditTextAddress.getError() == null
                && adapter.getImagePathList().size() >= 5
                && !currentCity.getId().equals("-1") && !currentDist.getId().equals("-1") && !currentWard.getId().equals("-1")
                ) {
            isDestroyImmediately = false;
        } else {
            if (currentTitle.isEmpty()) {
                mEditTextTitle.setError("Bạn chưa nhập tiêu đề");
                mEditTextTitle.requestFocus();
            }
            if (currentAddressNumber.isEmpty()) {
                mEditTextAddress.setError("Bạn chưa nhập địa chỉ nhà");
                mEditTextAddress.requestFocus();
            }
            if (adapter.getItemCount() < 5) {
                Toast.makeText(getContext(), "Bạn chưa thêm đủ số lượng ảnh yêu cầu", Toast.LENGTH_SHORT).show();
                mImageRecycleView.requestFocus();
            }
            if (currentCity.getId().equals("-1")) {
                Toast.makeText(getContext(), "Bạn chưa thêm thông tin về thành phố", Toast.LENGTH_SHORT).show();
                mImageCityError.setVisibility(View.VISIBLE);
                mSpinnerCity.requestFocus();
            }
            if (currentDist.getId().equals("-1")) {
                Toast.makeText(getContext(), "Bạn chưa thêm thông tin về quận/huyện", Toast.LENGTH_SHORT).show();
                mImageDistrictError.setVisibility(View.VISIBLE);
                mSpinnerDistrict.requestFocus();
            }
            if (currentWard.getId().equals("-1")) {
                Toast.makeText(getContext(), "Bạn chưa thêm thông tin về phường/xã", Toast.LENGTH_SHORT).show();
                mImageWardError.setVisibility(View.VISIBLE);
                mSpinnerWard.requestFocus();
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
    public void onReceive(Bundle b) {
        oldData = b;
    }

    AddressReceiver receiver;
    AddressInflatedListener addressInflatedListener;

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Address Fragment attach");
        receiver = (AddressReceiver) context;
        addressInflatedListener = (AddressInflatedListener) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView");
        view = inflater.inflate(R.layout.fragment_add_address, container, false);
        mAddressUtils = new AddressUtils(getActivity());
        isDestroyImmediately = true;
        initialize();
        addressInflatedListener.onAddressInflated();
        return view;
    }

    private void initialize() {
        mAddImageBtn = view.findViewById(R.id.add_address_btn_add_image);
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

        mLayoutIndicator = view.findViewById(R.id.add_address_indicator_layout);
        mImageIndicator1 = view.findViewById(R.id.add_address_indicator_1);
        mImageIndicator2 = view.findViewById(R.id.add_address_indicator_2);
        mImageIndicator3 = view.findViewById(R.id.add_address_indicator_3);
        mImageIndicator4 = view.findViewById(R.id.add_address_indicator_4);
        mImageIndicator5 = view.findViewById(R.id.add_address_indicator_5);

        initializeSpinnerData();
        initializeImageRecyclerViewData();
    }

    private void initializeImageRecyclerViewData() {
        String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + 0 + "/5</font> ảnh. Ảnh đầu tiên sẽ được sử dụng làm ảnh bì cho tin đăng";
        mTextViewImageNote.setText(Html.fromHtml(textNote));

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mImageRecycleView.setLayoutManager(layoutManager);
        adapter = new AddImageAdapter(getActivity(), new ArrayList<Bitmap>());
        mImageRecycleView.setAdapter(adapter);
        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mImageRecycleView);

        mAddImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForPermissionAndGetImage();
            }
        });
        mDeleteImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeImage(((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition());
                hideIndicator(adapter.getItemCount());
                mImageRecycleView.scrollToPosition(adapter.getItemCount() - 1);
                String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
                mTextViewImageNote.setText(Html.fromHtml(textNote));
                if (adapter.getItemCount() == 0) {
                    mDeleteImageBtn.setVisibility(View.GONE);
                } else if (adapter.getItemCount() < 5) {
                    mAddImageBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        mImageRecycleView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int currentItemPosition = layoutManager.findFirstVisibleItemPosition();
                switch (currentItemPosition) {
                    case 0:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        break;
                    case 1:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        break;
                    case 2:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        break;
                    case 3:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        break;
                    case 4:
                        mImageIndicator1.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator2.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator3.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator4.setColorFilter(ContextCompat.getColor(getContext(), R.color.light_gray));
                        mImageIndicator5.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        break;
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
                    if (oldData == null) {
                        currentDist = districtList.get(0);
                        mSpinnerDistrict.setSelection(0, true);

                        refreshWardList();
                        currentWard = wardList.get(0);
                        mSpinnerWard.setSelection(0, true);
                    } else {
                        String districtId = oldData.getString("districtId");
                        String districtName = mAddressUtils.getDistrictName(currentCity.getId(), districtId);
                        currentDist = new District(districtId, districtName);
                        int districtPosition = districtAdapter.getPosition(currentDist.getName());
                        mSpinnerDistrict.setSelection(districtPosition);
                    }
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
                    if (oldData == null) {
                        currentDist = districtList.get(position);
                        refreshWardList();
                        mSpinnerWard.setSelection(0);
                    } else {
                        refreshWardList();
                        String wardId = oldData.getString("wardId");
                        String wardName = mAddressUtils.getWardName(currentDist.getId(), wardId);
                        currentWard = new Ward(wardId, wardName);
                        int wardPosition = wardAdapter.getPosition(currentWard.getName());
                        mSpinnerWard.setSelection(wardPosition);
                    }
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
                if (oldData != null) {
                    oldData = null;
                }
                if (getArguments() != null) {
                    setArguments(null);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
    public void onResume() {
        super.onResume();
        Log.d(ACTIVITY_TAG, "onAddressResume");
        if (oldData != null) {
            updateUIWithOldData();
        }
        Bundle b = getArguments();
        if (b != null) {
            for(Bitmap bitmap : b.<Bitmap>getParcelableArrayList("image path")){
                adapter.addImage(bitmap);
                adapter.notifyDataSetChanged();
                showIndicator(adapter.getItemCount());
            }
            mImageRecycleView.scrollToPosition(adapter.getItemCount() - 1);
            String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
            mTextViewImageNote.setText(Html.fromHtml(textNote));
            currentAddressNumber = b.getString("address");
            mEditTextAddress.setText(currentAddressNumber);
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

    private void updateUIWithOldData() {
        mEditTextAddress.setText(oldData.getString("address number"));
        mEditTextTitle.setText(oldData.getString("title"));
        String id = oldData.getString("id");
        String idChu = oldData.getString("idChu");
        for (int i = 1; i <= 5; i++) {
            StorageReference mImageRef = FirebaseStorage.getInstance()
                    .getReference(idChu).child(id).child(i + "");
            GlideApp.with(getContext())
                    .asBitmap()
                    .load(mImageRef)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            adapter.addImage(resource);
                            showIndicator(adapter.getItemCount());
                            String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
                            mTextViewImageNote.setText(Html.fromHtml(textNote));
                            mDeleteImageBtn.setVisibility(View.VISIBLE);
                            if (adapter.getItemCount() == 5) {
                                mAddImageBtn.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
        String cityId = oldData.getString("cityId");
        switch (cityId) {
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


    private void hideIndicator(int itemCount) {
        switch (itemCount) {
            case 0:
                mImageIndicator1.setVisibility(View.GONE);
                break;
            case 1:
                mImageIndicator2.setVisibility(View.GONE);
                break;
            case 2:
                mImageIndicator3.setVisibility(View.GONE);
                break;
            case 3:
                mImageIndicator4.setVisibility(View.GONE);
                break;
            case 4:
                mImageIndicator5.setVisibility(View.GONE);
                break;
        }

    }

    private void showIndicator(int itemCount) {
        switch (itemCount) {
            case 1:
                mImageIndicator1.setVisibility(View.VISIBLE);
                break;
            case 2:
                mImageIndicator2.setVisibility(View.VISIBLE);
                break;
            case 3:
                mImageIndicator3.setVisibility(View.VISIBLE);
                break;
            case 4:
                mImageIndicator4.setVisibility(View.VISIBLE);
                break;
            case 5:
                mImageIndicator5.setVisibility(View.VISIBLE);
                break;
        }

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

                Uri imagePath = data.getData();
                try {
                    Bitmap mCurrentBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagePath);
                    adapter.addImage(mCurrentBitmap);
                    showIndicator(adapter.getItemCount());
                    String textNote = "Bạn cần phải thêm vào 5 ảnh. Bạn đã thêm <font color=#FF9800>" + adapter.getItemCount() + "/5 ảnh</font>. Ảnh đầu tiên sẽ được sử dụng làm ảnh bìa cho tin đăng";
                    mTextViewImageNote.setText(Html.fromHtml(textNote));
                    mImageRecycleView.scrollToPosition(adapter.getItemCount() - 1);
                    if (adapter.getItemCount() == 5) {
                        mAddImageBtn.setVisibility(View.GONE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onDestroyView() {
        if (!isDestroyImmediately) {
            Bundle b = new Bundle();
            b.putString("title", currentTitle);
            b.putString("address", currentAddressNumber);
            b.putSerializable("city", currentCity);
            b.putSerializable("district", currentDist);
            b.putSerializable("ward", currentWard);
            b.putParcelableArrayList("image path", adapter.getImagePathList());
            setArguments(b);
            Log.d(TAG, "onDestroyView");
        }
        super.onDestroyView();
    }
}
