package com.application.bottomnavigationbarui.validation;

public class TenancyValidator {

    public static ValidationResult validate(int roomId, int tenantId, String rentAmountStr, String securityDepositStr) {
        ValidationResult result = new ValidationResult();

        if (roomId <= 0) {
            result.addError("A targeted structural unit framework must be specified.");
        }
        if (tenantId <= 0) {
            result.addError("A registered matching profile recipient must be designated.");
        }

        try {
            double rent = Double.parseDouble(rentAmountStr);
            if (rent <= 0) {
                result.addError("Contract structural rent values must scale above 0.");
            }
        } catch (NumberFormatException e) {
            result.addError("Assigned contract rent must settle cleanly into base currency formats.");
        }

        try {
            if (securityDepositStr != null && !securityDepositStr.trim().isEmpty()) {
                double deposit = Double.parseDouble(securityDepositStr);
                if (deposit < 0) {
                    result.addError("Collateral validation deposits cannot fall negative.");
                }
            }
        } catch (NumberFormatException e) {
            result.addError("Deposit parameters do not evaluate to valid standard financial scales.");
        }

        return result;
    }
}