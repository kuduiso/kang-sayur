package id.kuduiso.kangsayur.helper;

import android.os.Parcel;
import android.os.Parcelable;

public class HelperDaftar implements Parcelable {
    private String nama;
    private String email;
    private String password;
    private String peran;
    private String noHP;

    public HelperDaftar(String nama, String email, String password, String peran, String noHP) {
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.peran = peran;
        this.noHP = noHP;
    }

    protected HelperDaftar(Parcel in) {
        nama = in.readString();
        email = in.readString();
        password = in.readString();
        peran = in.readString();
        noHP = in.readString();
    }

    public static final Creator<HelperDaftar> CREATOR = new Creator<HelperDaftar>() {
        @Override
        public HelperDaftar createFromParcel(Parcel in) {
            return new HelperDaftar(in);
        }

        @Override
        public HelperDaftar[] newArray(int size) {
            return new HelperDaftar[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nama);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(peran);
        dest.writeString(noHP);
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPeran() {
        return peran;
    }

    public String getNoHP() {
        return noHP;
    }
}
