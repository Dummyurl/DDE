package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleCondition;
import pratham.dde.domain.DDE_RuleMaster;

@Dao
public interface DDE_RuleMasterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertRuleMaster(DDE_RuleMaster dde_ruleMaster);

    @Query("SELECT * FROM DDE_RuleMaster")
    public List<DDE_RuleMaster> getAllRuleMaster();
}
