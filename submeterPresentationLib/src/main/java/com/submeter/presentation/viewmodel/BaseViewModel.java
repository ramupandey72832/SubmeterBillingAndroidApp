package com.submeter.presentation.viewmodel;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewModel {

    private boolean isLoading;
    private String errorMessage;
    private boolean isOperationSuccess;

    private final List<StateListener> listeners = new ArrayList<>();

    public interface StateListener {
        void onStateChanged();
    }

    public void addStateListener(StateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }

    protected void notifyStateChanged() {
        for (StateListener listener : new ArrayList<>(listeners)) {
            listener.onStateChanged();
        }
    }

    protected void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyStateChanged();
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        notifyStateChanged();
    }

    protected void setOperationSuccess(boolean success) {
        this.isOperationSuccess = success;
        notifyStateChanged();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isOperationSuccess() {
        return isOperationSuccess;
    }
}