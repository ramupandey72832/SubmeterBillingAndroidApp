package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.android_ui_templete1.templates.nav_activity.bottom_nav_activity.BottomNavActivityConstant;
import com.application.baselibrary.ui.utils.NavigationUtils;
import com.application.baselibrary.ui.utils.ToastMessage;
import com.application.bottomnavigationbarui.BillsFragment;
import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentGenerateBillBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;


import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenerateBillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenerateBillFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener{
    private ToastMessage ui;

    FragmentGenerateBillBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ROOMNUMBER = "param1";
    private static final String ARG_TENANTNAME = "param2";
    private static final String ARG_SUBMETERSERIALNUMBER = "param3";
    private static final String ARG_PREVIOUSMETERREADING = "param4";
    private static final String ARG_METERREADING = "param5";
    private static final String ARG_RATEPERUNIT = "param6";
    private static final String ARG_FIXEDCHARGE = "param7";
    private static final String ARG_EXTRACHARGE = "param8";
    private static final String ARG_NOTE = "param9";

    // TODO: Rename and change types of parameters
    private String getArgRoomnumber;
    private String getArgTenantname;
    private String getArgSubmeterserialnumber;
    private double getArgCurrentReading;
    private double getArgPreviousReading;
    private double getArgRateperunit;
    private double getArgFixedcharge;
    private double getArgExtraCharge;
    private String getArgNote;

    public GenerateBillFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomNumber Parameter 1 .
     * @param tenantName paramter 2 .
     * @return A new instance of fragment GenerateBillFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenerateBillFragment newInstance(String roomNumber, String tenantName, String submeterSerialNumber,
                                                   double current, double prev, double rate ,
                                                   double fixed, double extra, String notes) {
        GenerateBillFragment fragment = new GenerateBillFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOMNUMBER, roomNumber);
        args.putString(ARG_TENANTNAME, tenantName);
        args.putString(ARG_SUBMETERSERIALNUMBER, submeterSerialNumber);
        args.putDouble(ARG_METERREADING, current);
        args.putDouble(ARG_PREVIOUSMETERREADING, prev);
        args.putDouble(ARG_RATEPERUNIT, rate);
        args.putDouble(ARG_FIXEDCHARGE, fixed);
        args.putDouble(ARG_EXTRACHARGE, extra);
        args.putString(ARG_NOTE, notes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            getArgRoomnumber = getArguments().getString(ARG_ROOMNUMBER);
            getArgTenantname = getArguments().getString(ARG_TENANTNAME);
            getArgSubmeterserialnumber = getArguments().getString(ARG_SUBMETERSERIALNUMBER);
            getArgCurrentReading = getArguments().getDouble(ARG_METERREADING);
            getArgPreviousReading = getArguments().getDouble(ARG_PREVIOUSMETERREADING);
            getArgRateperunit = getArguments().getDouble(ARG_RATEPERUNIT);
            getArgFixedcharge = getArguments().getDouble(ARG_FIXEDCHARGE);
            getArgExtraCharge = getArguments().getDouble(ARG_EXTRACHARGE);
            getArgNote = getArguments().getString(ARG_NOTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGenerateBillBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ui = new ToastMessage(requireContext());

        binding.btnBack.setOnClickListener(view1 -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.tvRoomNumberLabel.setText(getArgRoomnumber);
        binding.tvTenantName.setText(getArgTenantname);
        double unitsConsumed = getArgCurrentReading - getArgPreviousReading;
        binding.tvPreviousReading.setText(Double.toString(getArgPreviousReading));
        binding.tvCurrentReading.setText(Double.toString(getArgCurrentReading));
        binding.tvUnitsConsumed.setText(Double.toString(unitsConsumed));
        binding.tvUnitRate.setText(Double.toString(getArgRateperunit));
        binding.tvCalculation.setText(unitsConsumed +" KWh * "+ getArgRateperunit +" + "+ getArgFixedcharge);
        binding.tvFixedCharges.setText(Double.toString(getArgFixedcharge));
        binding.tvExtraCharges.setText(Double.toString(getArgExtraCharge));
        binding.tvNotes.setText(getArgNote);
        double totalAmount = unitsConsumed * getArgRateperunit + getArgFixedcharge +getArgExtraCharge;
        binding.tvTotalAmount.setText(Double.toString(totalAmount));

        // How much Unit Consumed Should be calculated here and also fetch the previous reading from database

        binding.btnGenerateSendBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = "Meter Serial Number "+getArgSubmeterserialnumber+" Current Reading "+getArgCurrentReading +
                        " Rate Per Unit "+getArgRateperunit+" Fixed Charge "+getArgFixedcharge+" Extra Charge "+getArgExtraCharge+" Note "+getArgNote;
                VerifyMpinDialogFragment dialog =  VerifyMpinDialogFragment.newInstance(msg);
                dialog.show(getChildFragmentManager(), "MpinVerifyDialog");
            }
        });

    }

    @Override
    public void onMpinVerified(boolean isSuccess) {
        if(isSuccess){
            addMeterReadingAndGenerateBill();
        }
    }

    private void addMeterReadingAndGenerateBill() {
        MeterBillingService meterBillingService = new MeterBillingServiceImpl();
        boolean isException = false;
        try {
            meterBillingService.addMeterReadingAndGenerateBill(getArgRoomnumber, getArgCurrentReading,
                    getArgRateperunit,getArgFixedcharge,getArgExtraCharge,getArgNote);
        } catch (Exception e) {
            isException = true;
            ErrorUtils.handleDatabaseException("Error : ", e, ui);
        }finally {
            if(!isException) {
                ui.showSuccessAlert("Bill Generated SuccessFully : ", new Exception(""));
                NavigationUtils.replaceFragmentWithBackStack(requireActivity(), new BillsFragment(), BottomNavActivityConstant.MAIN_CONTAINER);
            }
        }
    }
}