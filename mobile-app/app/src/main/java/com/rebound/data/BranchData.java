package com.rebound.data;


import com.rebound.connectors.BranchConnector;
import com.rebound.R;


import java.util.Arrays;
import java.util.List;


public class BranchData {


    public static List<BranchConnector> getHanoiBranches() {
        return Arrays.asList(
                new BranchConnector("Rebound Cao Bá Quát", "Số 1 Cao Bá Quát, Ba Đình, Hà Nội", "09:30 - 21:30", R.mipmap.branch1),
                new BranchConnector("Rebound Tam Khương", "22 Tam Khương, Đống Đa, Hà Nội", "09:30 - 21:30", R.mipmap.branch2)
        );
    }


    public static List<BranchConnector> getHCMBranches() {
        return Arrays.asList(
                new BranchConnector("Rebound Điện Biên Phủ", "112 Điện Biên Phủ, Đa Kao, Quận 1", "09:30 - 21:30", R.mipmap.branch3),
                new BranchConnector("Rebound CMT8", "285/44 Cách Mạng Tháng Tám, Quận 3", "09:30 - 21:30", R.mipmap.branch4)
        );
    }
}

