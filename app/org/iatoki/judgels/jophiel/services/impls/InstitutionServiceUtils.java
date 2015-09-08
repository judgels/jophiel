package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.Institution;
import org.iatoki.judgels.jophiel.models.entities.InstitutionModel;

final class InstitutionServiceUtils {

    private InstitutionServiceUtils() {
        // prevent instantiation
    }

    static Institution createInstitutionFromModel(InstitutionModel institutionModel) {
        return new Institution(institutionModel.id, institutionModel.institution, institutionModel.referenceCount);
    }
}
