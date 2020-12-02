package id.kuduiso.kangsayur.helper;

import android.os.Parcel;
import android.os.Parcelable;

public class HelperPenjual implements Parcelable {
    private String nama;
    private String email;
    private String password;
    private String peran;
    private String noHP;
    private String statusToko;
    private String alamat;
    private String pinAlamat;
    private String imgProfile;

    protected HelperPenjual(Parcel in) {
        nama = in.readString();
        email = in.readString();
        password = in.readString();
        peran = in.readString();
        noHP = in.readString();
        statusToko = in.readString();
        alamat = in.readString();
        pinAlamat = in.readString();
        imgProfile = in.readString();
    }

    public static final Creator<HelperPenjual> CREATOR = new Creator<HelperPenjual>() {
        @Override
        public HelperPenjual createFromParcel(Parcel in) {
            return new HelperPenjual(in);
        }

        @Override
        public HelperPenjual[] newArray(int size) {
            return new HelperPenjual[size];
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
        dest.writeString(statusToko);
        dest.writeString(alamat);
        dest.writeString(pinAlamat);
    }

    public HelperPenjual(String nama, String email, String password, String peran, String noHP, String statusToko, String alamat, String pinAlamat, String imgProfile) {
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.peran = peran;
        this.noHP = noHP;
        this.statusToko = statusToko;
        this.alamat = alamat;
        this.pinAlamat = pinAlamat;
        this.imgProfile = imgProfile;
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

    public String getNoHP() { return noHP; }

    public String getStatusToko() {
        return statusToko;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getPinAlamat() {
        return pinAlamat;
    }

    public String getImgProfile() { return imgProfile; }
}
