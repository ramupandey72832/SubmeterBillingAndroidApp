package com.application.bottomnavigationbarui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.bottomnavigationbarui.R;
import com.application.bottomnavigationbarui.databinding.FragmentDashboardBinding;
import com.application.bottomnavigationbarui.databinding.FragmentGenerateBillBinding;
import com.application.bottomnavigationbarui.utils.ErrorUtils;
import com.application.bottomnavigationbarui.utils.UiHelper;
import com.github.devfrogora.service.MeterBillingService;
import com.github.devfrogora.service.impl.MeterBillingServiceImpl;

import java.sql.SQLException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenerateBillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenerateBillFragment extends Fragment implements VerifyMpinDialogFragment.MpinVerificationListener{
    private UiHelper ui;

    FragmentGenerateBillBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ROOMNUMBER = "param1";
    private static final String ARG_TENANTNAME = "param2";
    private static final String ARG_SUBMETERSERIALNUMBER = "param3";
    private static final String ARG_PREVIOUSMETERREADING = "param4";
    private static final String ARG_METERREADING = "param5";
    private static final String ARG_RATEPERUNIT = "param6";
    private static final String ARG_FIXEDCHARGE = "param67";

    // TODO: Rename and change types of parameters
    private String getArgRoomnumber;
    private String getArgTenantname;
    private String getArgSubmeterserialnumber;
    private double getArgMeterreading;
    private double getArgPreviousMeterreading;
    private double getArgRateperunit;
    private double getArgFixedcharge;

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
                                                   double meterReading, double previousMeterReading, double ratePerUnit ,double fixedCharge) {
        GenerateBillFragment fragment = new GenerateBillFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOMNUMBER, roomNumber);
        args.putString(ARG_TENANTNAME, tenantName);
        args.putString(ARG_SUBMETERSERIALNUMBER, submeterSerialNumber);
        args.putDouble(ARG_METERREADING, meterReading);
        args.putDouble(ARG_PREVIOUSMETERREADING, previousMeterReading);
        args.putDouble(ARG_RATEPERUNIT, ratePerUnit);
        args.putDouble(ARG_FIXEDCHARGE, fixedCharge);
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
            getArgMeterreading = getArguments().getDouble(ARG_METERREADING);
            getArgPreviousMeterreading = getArguments().getDouble(ARG_PREVIOUSMETERREADING);
            getArgRateperunit = getArguments().getDouble(ARG_RATEPERUNIT);
            getArgFixedcharge = getArguments().getDouble(ARG_FIXEDCHARGE);
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
        ui = new UiHelper(requireContext());

        binding.tvRoomNumberLabel.setText(getArgRoomnumber);
        binding.tvTenantName.setText(getArgTenantname);
        double unitsConsumed = getArgMeterreading - getArgPreviousMeterreading;
        binding.tvPreviousReading.setText(Double.toString(getArgPreviousMeterreading));
        binding.tvCurrentReading.setText(Double.toString(getArgMeterreading));
        binding.tvUnitsConsumed.setText(Double.toString(unitsConsumed));
        binding.tvUnitRate.setText(Double.toString(getArgRateperunit));
        binding.tvCalculation.setText(unitsConsumed +" KWh * "+ getArgRateperunit +" + "+ getArgFixedcharge);
        binding.tvFixedCharges.setText(Double.toString(getArgFixedcharge));
        double totalAmount = unitsConsumed * getArgRateperunit + getArgFixedcharge;
        binding.tvTotalAmount.setText(Double.toString(totalAmount));

        // How much Unit Consumed Should be calculated here and also fetch the previous reading from database

        binding.btnGenerateSendBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerifyMpinDialogFragment dialog = new VerifyMpinDialogFragment();
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
        try {
            meterBillingService.addMeterReadingWithGenerateBill(getArgRoomnumber,getArgMeterreading,getArgRateperunit,getArgFixedcharge);
        } catch (Exception e) {
            ErrorUtils.handleDatabaseException("Error : ", e, ui);
        }
    }
}