package com.application.bottomnavigationbarui.utils;

import com.application.baselibrary.ui.utils.ToastMessage;
import com.github.devfrogora.service.exception.BusinessRuleException;
import com.github.devfrogora.service.exception.ResourceNotFoundException;
import com.github.devfrogora.service.exception.RoomOccupiedException;

import java.sql.SQLException;

public class ErrorUtils {

    public static void handleDatabaseException(String message ,Exception e, ToastMessage ui) {
        message = message + " : ";
        if (e instanceof ResourceNotFoundException) {
            ui.showWarningAlert(message, e);
        } else if (e instanceof RoomOccupiedException) {
            ui.showErrorAlert(message, e);
        } else if (e instanceof BusinessRuleException) {
            ui.showErrorAlert(message, e);
        } else if (e instanceof SQLException) {
            ui.showErrorAlert(message, e);
        } else {
            ui.showErrorAlert(message, e);
        }
    }
}