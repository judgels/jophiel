package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.i18n.Messages;

final class AutoSuggestionControllerUtils {

    private AutoSuggestionControllerUtils() {
        // prevent instantiation
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("autoSuggestions"), routes.AutoSuggestionController.index()));

        return breadcrumbsBuilder;
    }

    static void appendTabLayout(LazyHtml content) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("institution.institutions"), routes.AutoSuggestionController.jumpToInstitutions()),
                new InternalLink(Messages.get("city.cities"), routes.AutoSuggestionController.jumpToCities()),
                new InternalLink(Messages.get("province.provinces"), routes.AutoSuggestionController.jumpToProvinces())
        ), c));
    }
}
