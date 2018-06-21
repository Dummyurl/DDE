package pratham.dde.domain;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

public class DDE_FormWiseDataSource {

    @PrimaryKey
    @NonNull
    private int formwisedsid;
    private int formid;
    private int dsformid;

}