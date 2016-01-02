package org.iatoki.judgels.jophiel.activity;

import org.iatoki.judgels.jophiel.models.entities.AbstractActivityLogModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jophiel_activity_log")
public final class ActivityLogModel extends AbstractActivityLogModel {

}
