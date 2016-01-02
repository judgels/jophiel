package org.iatoki.judgels.jophiel.user.profile.info;

final class InstitutionServiceUtils {

    private InstitutionServiceUtils() {
        // prevent instantiation
    }

    static Institution createInstitutionFromModel(InstitutionModel institutionModel) {
        return new Institution(institutionModel.id, institutionModel.institution, institutionModel.referenceCount);
    }
}
