package com.pocketdigi.template.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.pocketdigi.template.BR;


public class Person extends BaseObservable {
    String name;
    int age;
    String phone;

    @Bindable
    public String getName() {
        return name;
    }
    @Bindable
    public int getAge() {
        return age;
    }
    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public void setAge(int age) {
        this.age = age;
        notifyPropertyChanged(BR.age);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
