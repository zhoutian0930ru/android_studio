package com.rutgers.pocketwallet.ui.expenses;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rutgers.pocketwallet.R;
import com.rutgers.pocketwallet.adapters.CategoriesSpinnerAdapter;
import com.rutgers.pocketwallet.entities.Category;
import com.rutgers.pocketwallet.entities.Expense;
import com.rutgers.pocketwallet.interfaces.IExpensesType;
import com.rutgers.pocketwallet.interfaces.IUserActionsMode;
import com.rutgers.pocketwallet.utils.DateUtils;
import com.rutgers.pocketwallet.utils.DialogManager;
import com.rutgers.pocketwallet.utils.RealmManager;
import com.rutgers.pocketwallet.utils.Util;
import com.rutgers.pocketwallet.widget.ExpensesWidgetProvider;
import com.rutgers.pocketwallet.widget.ExpensesWidgetService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class NewExpenseFragment extends DialogFragment implements View.OnClickListener {

    private TextView tvTitle;
    private Button btnDate;
    private Spinner spCategory;
    private EditText etDescription;
    private EditText etTotal;

    private CategoriesSpinnerAdapter mCategoriesSpinnerAdapter;
    private Date selectedDate;
    private Expense mExpense;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private SQLiteDatabase dbWrite, dbRead;
    private MyDatabaseHelper db;

    public LocationManager locationManager;
    private String provider;

    private @IUserActionsMode
    int mUserActionMode;
    private @IExpensesType
    int mExpenseType;

    static NewExpenseFragment newInstance(@IUserActionsMode int mode, String expenseId) {
        NewExpenseFragment newExpenseFragment = new NewExpenseFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(IUserActionsMode.MODE_TAG, mode);
        if (expenseId != null) bundle.putString(ExpenseDetailFragment.EXPENSE_ID_KEY, expenseId);
        newExpenseFragment.setArguments(bundle);
        return newExpenseFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_new_expense, container, false);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        btnDate = (Button) rootView.findViewById(R.id.btn_date);
        spCategory = (Spinner) rootView.findViewById(R.id.sp_categories);
        etDescription = (EditText) rootView.findViewById(R.id.et_description);
        etTotal = (EditText) rootView.findViewById(R.id.et_total);
        mExpenseType = IExpensesType.MODE_EXPENSES;
        db = new MyDatabaseHelper(this.getActivity(), "contact.db", null, 1);



        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }

        return rootView;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            mUserActionMode = getArguments().getInt(IUserActionsMode.MODE_TAG) == IUserActionsMode.MODE_CREATE ? IUserActionsMode.MODE_CREATE : IUserActionsMode.MODE_UPDATE;
        }
        setModeViews();
        btnDate.setOnClickListener(this);
        (getView().findViewById(R.id.btn_cancel)).setOnClickListener(this);
        (getView().findViewById(R.id.btn_save)).setOnClickListener(this);
    }

    private void setModeViews() {
        List<Category> categoriesList = Category.getCategoriesExpense();
        Category[] categoriesArray = new Category[categoriesList.size()];
        categoriesArray = categoriesList.toArray(categoriesArray);
        mCategoriesSpinnerAdapter = new CategoriesSpinnerAdapter(getActivity(), categoriesArray);
        spCategory.setAdapter(mCategoriesSpinnerAdapter);
        switch (mUserActionMode) {
            case IUserActionsMode.MODE_CREATE:
                selectedDate = new Date();
                break;
            case IUserActionsMode.MODE_UPDATE:
                if (getArguments() != null) {
                    String id = getArguments().getString(ExpenseDetailFragment.EXPENSE_ID_KEY);
                    mExpense = (Expense) RealmManager.getInstance().findById(Expense.class, id);
                    tvTitle.setText("Edit");
                    selectedDate = mExpense.getDate();
                    etDescription.setText(mExpense.getDescription());
                    etTotal.setText(String.valueOf(mExpense.getTotal()));
                    int categoryPosition = 0;
                    for (int i = 0; i < categoriesArray.length; i++) {
                        if (categoriesArray[i].getId().equalsIgnoreCase(mExpense.getCategory().getId())) {
                            categoryPosition = i;
                            break;
                        }
                    }
                    spCategory.setSelection(categoryPosition);
                }
                break;
        }
        updateDate();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_date) {
            showDateDialog();
        } else if (view.getId() == R.id.btn_cancel) {
            dismiss();
        } else if (view.getId() == R.id.btn_save) {
            onSaveExpense();
            onSavePosition();
            //getSite();

        }
    }


    public void getSite() {
        //判断权限
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //有权限
            locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
            //获取所有可用的位置提供器
            List<String> providerList = locationManager.getProviders(true);
            if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
            } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            } else {
                //当没有可用的位置提供器时，弹出Toast提示用户
                Toast.makeText(this.getActivity(), "No Location provider to use", Toast.LENGTH_SHORT).show();
                return;
            }
            //获取坐标
            Location location = locationManager.getLastKnownLocation(provider);
            int k=1000;
            try{
                Thread.sleep(20000);
            }catch(Exception e){
                System.exit(0);//退出程序
            }
            while(location==null && k>0){
                location = locationManager.getLastKnownLocation(provider);
                k--;
            }
            //double x = location.getAltitude();
            Toast.makeText(this.getActivity(), "+"+location+"+", Toast.LENGTH_SHORT).show();
        }
    }


    //add position
    private void onSavePosition() {

        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }



        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        String locationProvider = null;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            //Toast.makeText(this.getActivity(), locationProvider, Toast.LENGTH_SHORT).show();

        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            //Toast.makeText(this.getActivity(), locationProvider, Toast.LENGTH_SHORT).show();

        } else {
            //Toast.makeText(this.getActivity(), "nothing", Toast.LENGTH_SHORT).show();
        }



        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.



            Toast.makeText(this.getActivity(), "Can't access the location.", Toast.LENGTH_SHORT).show();


            return;
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        double myLat = location.getLatitude();
        double myLng = location.getLongitude();

        //Toast.makeText(this.getActivity(), "lat:"+myLat+"   lng:"+myLng, Toast.LENGTH_SHORT).show();

        dbWrite = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("latitude", myLat);
        cv.put("altitude", myLng);
        dbWrite.insert("position", null, cv);
        dbWrite.close();
    }


    private void onSaveExpense() {
        if (mCategoriesSpinnerAdapter.getCount() > 0 ) {
            if (!Util.isEmptyField(etTotal)) {
                Category currentCategory = (Category) spCategory.getSelectedItem();
                String total = etTotal.getText().toString();
                String description = etDescription.getText().toString();
                if (mUserActionMode == IUserActionsMode.MODE_CREATE) {
                    RealmManager.getInstance().save(new Expense(description, selectedDate, mExpenseType, currentCategory, Float.parseFloat(total)), Expense.class);
                } else {
                    Expense editExpense = new Expense();
                    editExpense.setId(mExpense.getId());
                    editExpense.setTotal(Float.parseFloat(total));
                    editExpense.setDescription(description);
                    editExpense.setCategory(currentCategory);
                    editExpense.setDate(selectedDate);
                    RealmManager.getInstance().update(editExpense);
                }
                // update widget if the expense is created today
                if (DateUtils.isToday(selectedDate)) {
                    Intent i = new Intent(getActivity(), ExpensesWidgetProvider.class);
                    i.setAction(ExpensesWidgetService.UPDATE_WIDGET);
                    getActivity().sendBroadcast(i);
                }
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                dismiss();
            } else {
                DialogManager.getInstance().showShortToast(getString(R.string.error_total));
            }
        } else {
            DialogManager.getInstance().showShortToast(getString(R.string.no_categories_error));
        }
    }

    private void showDateDialog() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        DialogManager.getInstance().showDatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(year, month, day);
                selectedDate = calendar.getTime();
                updateDate();
            }
        }, calendar);
    }

    private void updateDate() {
        btnDate.setText(Util.formatDateToString(selectedDate, Util.getCurrentDateFormat()));
    }

}
