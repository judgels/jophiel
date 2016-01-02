package org.iatoki.judgels.jophiel.activity;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.services.BaseActivityLogService;

@ImplementedBy(ActivityLogServiceImpl.class)
public interface ActivityLogService extends BaseActivityLogService {

}
