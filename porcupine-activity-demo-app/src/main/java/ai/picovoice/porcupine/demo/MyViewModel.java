package ai.picovoice.porcupine.demo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {
    private MutableLiveData<String> myVariable = new MutableLiveData<>();

    public LiveData<String> getMyVariable() {
        return myVariable;
    }

    public void setMyVariable(String newValue) {
        myVariable.setValue(newValue);  // 更新变量值，并通知观察者
    }
}
