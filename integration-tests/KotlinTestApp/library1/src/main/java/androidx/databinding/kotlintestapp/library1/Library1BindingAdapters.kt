package androidx.databinding.kotlintestapp.library1

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setTextViaLibrary1")
fun TextView.setViaLibrary1Adapter(data:String) {
    setText("library1: $data")
}