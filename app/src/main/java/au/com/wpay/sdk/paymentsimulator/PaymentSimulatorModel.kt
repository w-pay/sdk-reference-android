package au.com.wpay.sdk.paymentsimulator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Exception

interface PaymentSimulatorActions {
    fun onError(error: Exception)
}

class PaymentSimulatorModel : ViewModel() {
    val error: MutableLiveData<Exception> = MutableLiveData()

    fun onError(error: Exception) {
        this.error.postValue(error)
    }
}